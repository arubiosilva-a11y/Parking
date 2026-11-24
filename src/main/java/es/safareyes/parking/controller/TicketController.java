package es.safareyes.parking.controller;

import es.safareyes.parking.dto.request.TicketEntradaRequest;
import es.safareyes.parking.dto.request.TicketPagarRequest;
import es.safareyes.parking.dto.response.TicketResponse;
import es.safareyes.parking.service.TicketService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.math.BigDecimal;

@RestController
@RequestMapping("/api/v1/tickets")
@RequiredArgsConstructor
public class TicketController {

    private final TicketService service;

    @PostMapping("/entrada")
    @ResponseStatus(HttpStatus.CREATED)
    public TicketResponse registrarEntrada(@Valid @RequestBody TicketEntradaRequest request) {
        return service.registrarEntrada(request);
    }

    @GetMapping("/importe")
    public ImporteResponse calcularImporteActual(@RequestParam @NotBlank String matricula) {
        return new ImporteResponse(service.calcularImporteActual(matricula));
    }

    @PostMapping("/pago")
    public TicketResponse pagar(@Valid @RequestBody TicketPagarRequest request) {
        return service.pagar(request);
    }

    public record ImporteResponse(BigDecimal importe) {}
}
