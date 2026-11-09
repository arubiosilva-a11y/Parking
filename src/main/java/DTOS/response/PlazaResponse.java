package DTOS.response;

import es.safareyes.parking.enums.Estado;

public record PlazaResponse(
        Long idPlaza,
        Integer numero,
        Integer planta,
        String tipo,
        Estado estado
) {}