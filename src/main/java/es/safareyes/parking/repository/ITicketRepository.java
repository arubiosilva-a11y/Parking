package es.safareyes.parking.repository;

import es.safareyes.parking.dto.RecaudacionDiaDto;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.Lock;
import es.safareyes.parking.entity.Ticket;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public interface ITicketRepository extends JpaRepository<Ticket, Integer>,
        JpaSpecificationExecutor<Ticket> {

    Ticket findFirstByMatriculaAndEstadoIgnoreCase(String matricula, String estado);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select t from Ticket t where t.idTicket = :id")
    Optional<Ticket> buscarPorIdParaActualizar(@Param("id") Integer id);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    Optional<Ticket> findFirstByMatriculaAndEstadoIgnoreCaseOrderByIdTicketAsc(String matricula, String estado);

    @Query("select t from Ticket t where t.idTicket = :id")
    @EntityGraph(attributePaths = {"plaza", "abonado", "tarifa", "promocion"})
    Ticket buscarPorIdConDescuentos(@Param("id") Integer id);

    @Query(value = """
        select o.planta, o.total_plazas, o.plazas_ocupadas,
               round(o.plazas_ocupadas * 100.0 / o.total_plazas, 2) as porcentaje,
               round(coalesce(e.estancia_media, 0)::numeric, 2) as estancia_media
        from (select p.planta, count(*) as total_plazas,
              count(*) filter (where p.estado = 'Ocupada') as plazas_ocupadas
              from plaza p group by p.planta) o
        left join (select p.planta,
                   avg(extract(epoch from (t.hora_salida - t.hora_entrada)) / 60.0) as estancia_media
                   from ticket t join plaza p on p.id_plaza = t.id_plaza
                   where t.estado = 'Pagado' and t.hora_salida is not null
                   group by p.planta) e on o.planta = e.planta
        order by o.planta desc
        """, nativeQuery = true)
    List<Object[]> buscarOcupacionPorPlanta();

    @Query("""
    select new es.safareyes.parking.dto.RecaudacionDiaDto(cast(t.horaSalida as LocalDate), sum(t.importe))
    from Ticket t
    where t.estado = 'Pagado'
      and cast(t.horaSalida as LocalDate) between :desde and :hasta
    group by cast(t.horaSalida as LocalDate)
    order by cast(t.horaSalida as LocalDate)
    """)
    List<RecaudacionDiaDto> buscarRecaudacionPorDia(
            @Param("desde") LocalDate desde,
            @Param("hasta") LocalDate hasta
    );


}