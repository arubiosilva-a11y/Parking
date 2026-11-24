package es.safareyes.parking.service;

import es.safareyes.parking.dto.request.TicketEntradaRequest;
import es.safareyes.parking.dto.request.TicketPagarRequest;
import es.safareyes.parking.dto.response.DescuentoResponse;
import es.safareyes.parking.dto.response.TicketDetalleResponse;
import es.safareyes.parking.dto.response.TicketResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.math.BigDecimal;
import java.time.LocalDate;

public interface TicketService {

    TicketResponse registrarEntrada(TicketEntradaRequest request);

    BigDecimal calcularImporteActual(String matricula);

    TicketResponse pagar(TicketPagarRequest request);

    DescuentoResponse aplicarDescuento(Integer idTicket, Integer idPromocion);

    Page<TicketResponse> listar(String matricula, String estado, LocalDate desde, LocalDate hasta, Pageable pageable);

    TicketDetalleResponse consultarDetalle(Integer idTicket);
}
