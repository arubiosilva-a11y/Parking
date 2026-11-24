package es.safareyes.parking.controller;

import es.safareyes.parking.dto.request.TarifaRequest;
import es.safareyes.parking.dto.response.TarifaResponse;
import es.safareyes.parking.service.TarifaService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDateTime;

@RestController
@RequestMapping("/api/v1/tarifas")
@RequiredArgsConstructor
public class TarifaController {

    private final TarifaService service;

    @GetMapping("/vigente")
    public TarifaResponse consultarVigente() {
        return service.consultarVigente(LocalDateTime.now());
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public TarifaResponse crear(@Valid @RequestBody TarifaRequest request) {
        return service.crear(request);
    }
}
