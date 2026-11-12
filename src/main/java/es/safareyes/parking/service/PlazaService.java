package es.safareyes.parking.service;

import es.safareyes.parking.dto.DisponibilidadPlantaView;
import es.safareyes.parking.dto.request.PlazaRequest;
import es.safareyes.parking.dto.response.PlazaResponse;
import es.safareyes.parking.enums.Estado;

import java.util.List;

public interface PlazaService {

    List<PlazaResponse> listar();

    List<DisponibilidadPlantaView> consultarDisponibilidad();

    PlazaResponse crear(PlazaRequest request);

    PlazaResponse cambiarEstado(Integer idPlaza, Estado estado);
}
