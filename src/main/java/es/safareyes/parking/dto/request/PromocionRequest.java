package es.safareyes.parking.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

import java.math.BigDecimal;

public record PromocionRequest(

        @NotBlank
        String nombre,

        @NotBlank
        String tipo,

        @NotNull
        @Positive
        BigDecimal minDescuento

) {}