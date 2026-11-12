package es.safareyes.parking.dto.response;

import java.time.LocalDateTime;

public record AbonadoResponse(
        Integer idAbonado,
        String nombre,
        String email,
        String matricula,
        LocalDateTime fechaFin,
        LocalDateTime fechaIni
) {}