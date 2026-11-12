package es.safareyes.parking.repository;

import es.safareyes.parking.entity.Tarifa;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;

@Repository
public interface ITarifaRepository extends JpaRepository<Tarifa, Integer> {

    Tarifa findFirstByFechaAplicacionLessThanEqualOrderByFechaAplicacionDesc(
            LocalDateTime fecha
    );
}
