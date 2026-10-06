package repositorios;

import modelos.Tarifa;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ITarifaRepository extends JpaRepository<Tarifa, Short> {
}
