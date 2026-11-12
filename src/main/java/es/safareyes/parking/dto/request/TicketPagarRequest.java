package es.safareyes.parking.dto.request;

import jakarta.validation.constraints.NotBlank;

public record TicketPagarRequest(
        @NotBlank
        String matricula,

        String codigoCine
) {}