package repositorios;

import modelos.Tarifa;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;

@Repository
public interface ITarifaRepository extends JpaRepository<Tarifa, Long> {

    Tarifa findFirstByFechaAplicacionLessThanEqualOrderByFechaAplicacionDesc(
            LocalDate fecha
    );
}
