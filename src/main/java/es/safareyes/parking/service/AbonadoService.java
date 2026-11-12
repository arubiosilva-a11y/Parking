package es.safareyes.parking.service;

import es.safareyes.parking.dto.request.AbonadoRequest;
import es.safareyes.parking.dto.response.AbonadoResponse;

public interface AbonadoService {

    AbonadoResponse crear(AbonadoRequest request);
}
