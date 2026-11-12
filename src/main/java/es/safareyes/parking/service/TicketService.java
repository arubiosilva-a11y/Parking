package es.safareyes.parking.service;

import es.safareyes.parking.dto.request.TicketEntradaRequest;
import es.safareyes.parking.dto.request.TicketPagarRequest;
import es.safareyes.parking.dto.response.TicketResponse;

import java.math.BigDecimal;

public interface TicketService {

    TicketResponse registrarEntrada(TicketEntradaRequest request);

    BigDecimal calcularImporteActual(String matricula);

    TicketResponse pagar(TicketPagarRequest request);
}
