package es.safareyes.parking.dto.response;

import com.fasterxml.jackson.annotation.JsonUnwrapped;

import java.util.List;

public record TicketDetalleResponse(@JsonUnwrapped TicketResponse ticket, List<DescuentoResponse> descuentos) {}
