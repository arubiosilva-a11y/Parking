package es.safareyes.parking.service;

import es.safareyes.parking.dto.request.PromocionRequest;
import es.safareyes.parking.dto.response.PromocionResponse;

import java.util.List;

public interface PromocionService {

    List<PromocionResponse> listar();

    PromocionResponse crear(PromocionRequest request);
}
