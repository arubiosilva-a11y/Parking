package es.safareyes.parking.dto;

import java.math.BigDecimal;

public record OcupacionPlantaDTO(
        Integer planta,
        Long totalPlazas,
        Long plazasOcupadas,
        BigDecimal porcentajeOcupacion,
        BigDecimal estanciaMediaMinutos
) {
}