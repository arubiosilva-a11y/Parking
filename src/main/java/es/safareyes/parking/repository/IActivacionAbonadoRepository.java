package es.safareyes.parking.repository;

import es.safareyes.parking.entity.ActivacionAbonado;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.Optional;

public interface IActivacionAbonadoRepository extends JpaRepository<ActivacionAbonado, Integer> {

    @Query("select a.idAbonado from ActivacionAbonado a where a.codigoHash = :codigoHash")
    Optional<Integer> buscarIdAbonado(String codigoHash);
}
