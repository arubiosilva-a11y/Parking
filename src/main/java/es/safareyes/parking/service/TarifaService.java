package es.safareyes.parking.service;

import es.safareyes.parking.dto.request.TarifaRequest;
import es.safareyes.parking.dto.response.TarifaResponse;

import java.time.LocalDateTime;

public interface TarifaService {

    TarifaResponse consultarVigente(LocalDateTime fecha);

    TarifaResponse crear(TarifaRequest request);
}
