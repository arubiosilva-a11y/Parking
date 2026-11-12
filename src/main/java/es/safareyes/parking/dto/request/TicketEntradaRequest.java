package es.safareyes.parking.dto.request;

import jakarta.validation.constraints.NotBlank;

public record TicketEntradaRequest(
        @NotBlank
        String matricula,

        @NotBlank
        String tipo
) {}