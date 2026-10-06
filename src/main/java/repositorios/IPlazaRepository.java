package repositorios;

import modelos.Abonado;
import modelos.Plaza;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface IPlazaRepository extends JpaRepository<Plaza,Short> {


    List<Plaza> findAll();

    List<Plaza> findByPlantaAndTipoAndEstado(
            Integer planta,
            String tipo,
            String estado
    );


}
