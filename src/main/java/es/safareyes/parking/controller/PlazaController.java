package es.safareyes.parking.controller;

import es.safareyes.parking.dto.DisponibilidadPlantaView;
import es.safareyes.parking.dto.request.PlazaRequest;
import es.safareyes.parking.dto.response.PlazaResponse;
import es.safareyes.parking.enums.Estado;
import es.safareyes.parking.service.PlazaService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/v1/plazas")
@RequiredArgsConstructor
public class PlazaController {

    private final PlazaService service;

    @GetMapping
    public List<PlazaResponse> listar() {
        return service.listar();
    }

    @GetMapping("/disponibilidad")
    public List<DisponibilidadPlantaView> consultarDisponibilidad() {
        return service.consultarDisponibilidad();
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public PlazaResponse crear(@Valid @RequestBody PlazaRequest request) {
        return service.crear(request);
    }

    @PatchMapping("/{idPlaza}/estado")
    public PlazaResponse cambiarEstado(@PathVariable Integer idPlaza, @Valid @RequestBody EstadoPlazaRequest request) {
        return service.cambiarEstado(idPlaza, request.estado());
    }

    public record EstadoPlazaRequest(@NotNull Estado estado) {}
}
