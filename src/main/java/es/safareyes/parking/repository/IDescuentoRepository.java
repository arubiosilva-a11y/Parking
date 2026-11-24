package es.safareyes.parking.repository;

import es.safareyes.parking.entity.Descuento;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface IDescuentoRepository extends JpaRepository<Descuento, Integer> {

    boolean existsByTicketIdTicketAndPromocionIdPromocion(Integer idTicket, Integer idPromocion);

    List<Descuento> findByTicketIdTicketOrderByIdDescuentoAsc(Integer idTicket);
}
