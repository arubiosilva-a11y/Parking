package es.safareyes.parking.controller;

import es.safareyes.parking.dto.request.AbonadoRequest;
import es.safareyes.parking.dto.response.AbonadoResponse;
import es.safareyes.parking.service.AbonadoService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/abonados")
@RequiredArgsConstructor
public class AbonadoController {

    private final AbonadoService service;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public AbonadoResponse crear(@Valid @RequestBody AbonadoRequest request) {
        return service.crear(request);
    }
}
