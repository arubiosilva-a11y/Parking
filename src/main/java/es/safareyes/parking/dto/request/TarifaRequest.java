package es.safareyes.parking.dto.request;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.PositiveOrZero;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record TarifaRequest(

        @NotNull
        @Positive
        BigDecimal precioMin,

        @NotNull
        @PositiveOrZero
        Integer minCortesia,

        @NotNull
        @Positive
        Integer horasMaximas,

        @NotNull
        LocalDateTime fechaAplicacion

) {}