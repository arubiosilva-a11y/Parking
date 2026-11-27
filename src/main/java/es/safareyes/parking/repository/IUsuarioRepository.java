package es.safareyes.parking.repository;

import es.safareyes.parking.entity.Usuario;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface IUsuarioRepository extends JpaRepository<Usuario, Integer> {
    @Query("select u from Usuario u left join fetch u.abonado where lower(u.email) = lower(:email)")
    Optional<Usuario> buscarPorEmail(String email);

    boolean existsByAbonado_IdAbonado(Integer idAbonado);
}
