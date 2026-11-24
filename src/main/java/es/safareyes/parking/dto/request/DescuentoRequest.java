package es.safareyes.parking.dto.request;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

public record DescuentoRequest(@NotNull @Positive Integer idPromocion) {}
