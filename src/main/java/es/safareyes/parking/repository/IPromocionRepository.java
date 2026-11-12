package es.safareyes.parking.repository;

import es.safareyes.parking.entity.Promocion;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface IPromocionRepository extends JpaRepository<Promocion, Integer> {

}