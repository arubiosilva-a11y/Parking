package es.safareyes.parking.repository;

import es.safareyes.parking.entity.Abonado;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;

@Repository
public interface IAbonadoRepository extends JpaRepository<Abonado, Integer> {

    Abonado findFirstByMatriculaAndFechaIniLessThanEqualAndFechaFinGreaterThanEqual(
            String matricula,
            LocalDateTime fechaInicio,
            LocalDateTime fechaFin
    );
}
