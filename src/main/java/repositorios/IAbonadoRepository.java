package repositorios;

import modelos.Abonado;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface IAbonadoRepository extends JpaRepository<Abonado, Short> {




}
