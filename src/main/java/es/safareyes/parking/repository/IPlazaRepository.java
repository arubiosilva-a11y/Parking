package es.safareyes.parking.repository;

import es.safareyes.parking.dto.DisponibilidadPlantaView;
import es.safareyes.parking.enums.Estado;
import es.safareyes.parking.entity.Plaza;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import jakarta.persistence.LockModeType;

@Repository
public interface IPlazaRepository extends JpaRepository<Plaza, Integer> {

    List<Plaza> findAllByPlanta(Integer planta);

    List<Plaza> findAllByEstado(Estado estado);

    List<Plaza> findAllByTipo(String tipo);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    List<Plaza> findAllByTipoAndEstadoOrderByPlantaDescNumeroAsc(String tipo, Estado estado);

    @Query("select p from Plaza p where p.estado = es.safareyes.parking.enums.Estado.LIBRE " +
            "and p.planta = :planta and p.tipo = :tipo")
    List<Plaza> buscarLibresPorPlantaYTipo(
            @Param("planta") Integer planta,
            @Param("tipo") String tipo
    );

    @Query("select p.planta as planta, p.tipo as tipo, count(p) as plazasLibres " +
            "from Plaza p where p.estado = es.safareyes.parking.enums.Estado.LIBRE " +
            "group by p.planta, p.tipo order by p.planta, p.tipo")
    List<DisponibilidadPlantaView> buscarDisponibilidad();
}