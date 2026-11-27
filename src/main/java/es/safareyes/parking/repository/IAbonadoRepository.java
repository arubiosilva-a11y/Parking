package es.safareyes.parking.repository;

import es.safareyes.parking.entity.Abonado;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.Optional;

@Repository
public interface IAbonadoRepository extends JpaRepository<Abonado, Integer> {
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select a from Abonado a where a.idAbonado = :id")
    Optional<Abonado> buscarParaActualizar(Integer id);

    Abonado findFirstByMatriculaAndFechaIniLessThanEqualAndFechaFinGreaterThanEqual(
            String matricula,
            LocalDateTime fechaInicio,
            LocalDateTime fechaFin
    );
}
