package es.safareyes.parking.dto.response;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record TarifaResponse(
        Integer idTarifa,
        BigDecimal precioMin,
        Integer minCortesia,
        Integer horasMaximas,
        LocalDateTime fechaAplicacion
) {}