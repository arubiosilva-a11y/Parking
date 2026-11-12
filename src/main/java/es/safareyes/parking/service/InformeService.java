package es.safareyes.parking.service;

import es.safareyes.parking.dto.OcupacionPlantaDTO;
import es.safareyes.parking.dto.RecaudacionDiaDto;

import java.time.LocalDate;
import java.util.List;

public interface InformeService {

    List<RecaudacionDiaDto> consultarRecaudacion(LocalDate desde, LocalDate hasta);

    List<OcupacionPlantaDTO> consultarOcupacion();
}
