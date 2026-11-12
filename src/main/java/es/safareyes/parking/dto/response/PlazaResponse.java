package es.safareyes.parking.dto.response;

import es.safareyes.parking.enums.Estado;

public record PlazaResponse(
        Integer idPlaza,
        Integer numero,
        Integer planta,
        String tipo,
        Estado estado
) {}