package es.safareyes.parking.dto.response;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record TicketResponse(
        Integer idTicket,
        String matricula,
        LocalDateTime horaEntrada,
        LocalDateTime horaSalida,
        Integer minutos,
        BigDecimal descuento,
        BigDecimal importeDescuento,
        BigDecimal importe,
        String estado,
        Integer idPlaza,
        Integer idAbonado,
        Integer idTarifa,
        Integer idPromocion
) {}