package es.safareyes.parking.controller;

import es.safareyes.parking.dto.request.PromocionRequest;
import es.safareyes.parking.dto.response.PromocionResponse;
import es.safareyes.parking.service.PromocionService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/v1/promociones")
@RequiredArgsConstructor
public class PromocionController {

    private final PromocionService service;

    @GetMapping
    public List<PromocionResponse> listar() {
        return service.listar();
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public PromocionResponse crear(@Valid @RequestBody PromocionRequest request) {
        return service.crear(request);
    }
}
