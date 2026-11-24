package es.safareyes.parking.controller;

import es.safareyes.parking.dto.OcupacionPlantaDTO;
import es.safareyes.parking.dto.RecaudacionDiaDto;
import es.safareyes.parking.service.InformeService;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/v1/informes")
@RequiredArgsConstructor
public class InformeController {

    private final InformeService service;

    @GetMapping("/recaudacion")
    public List<RecaudacionDiaDto> consultarRecaudacion(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate desde,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate hasta
    ) {
        return service.consultarRecaudacion(desde, hasta);
    }

    @GetMapping("/ocupacion")
    public List<OcupacionPlantaDTO> consultarOcupacion() {
        return service.consultarOcupacion();
    }
}
