package es.safareyes.parking.controller;

import es.safareyes.parking.dto.request.AbonadoRequest;
import es.safareyes.parking.dto.response.AbonadoResponse;
import es.safareyes.parking.dto.response.ActivacionResponse;
import es.safareyes.parking.service.AbonadoService;
import es.safareyes.parking.service.ActivacionAbonadoService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Positive;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.PathVariable;
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
    private final ActivacionAbonadoService activaciones;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public AbonadoResponse crear(@Valid @RequestBody AbonadoRequest request) {
        return service.crear(request);
    }

    @PostMapping("/{id}/activacion")
    @ResponseStatus(HttpStatus.CREATED)
    public ActivacionResponse emitirActivacion(@PathVariable @Positive Integer id) {
        return activaciones.emitir(id);
    }
}
