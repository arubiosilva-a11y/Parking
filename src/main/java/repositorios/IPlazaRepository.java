package repositorios;

import es.safareyes.parking.enums.Estado;
import modelos.Plaza;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;


import java.util.List;

@Repository
public interface IPlazaRepository extends JpaRepository<Plaza,Long> {

    List<Plaza> findAllByPlanta(Integer planta);

    List<Plaza> findAllByEstado(Estado estado);

    List<Plaza> findAllByTipo(String tipo);

    @Query("select p from Plaza p where p.estado = es.safareyes.parking.enums.Estado.LIBRE " +
            "and p.planta = :planta and p.tipo = :tipo")
    List<Plaza> buscarLibresPorPlantaYTipo(
            @Param("planta") Integer planta,
            @Param("tipo") String tipo
    );



}
