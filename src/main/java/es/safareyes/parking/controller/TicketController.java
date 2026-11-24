package es.safareyes.parking.controller;

import es.safareyes.parking.dto.request.DescuentoRequest;
import es.safareyes.parking.dto.request.TicketEntradaRequest;
import es.safareyes.parking.dto.request.TicketPagarRequest;
import es.safareyes.parking.dto.response.DescuentoResponse;
import es.safareyes.parking.dto.response.TicketDetalleResponse;
import es.safareyes.parking.dto.response.TicketResponse;
import es.safareyes.parking.service.TicketService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Positive;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PagedModel;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.math.BigDecimal;
import java.time.LocalDate;

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

    @PostMapping("/{id}/descuentos")
    @ResponseStatus(HttpStatus.CREATED)
    public DescuentoResponse aplicarDescuento(@PathVariable @Positive Integer id,
                                               @Valid @RequestBody DescuentoRequest request) {
        return service.aplicarDescuento(id, request.idPromocion());
    }

    @GetMapping
    public PagedModel<TicketResponse> listar(
            @RequestParam(required = false) String matricula,
            @RequestParam(required = false) String estado,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate desde,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate hasta,
            @RequestParam(defaultValue = "0") @Min(0) int page,
            @RequestParam(defaultValue = "20") @Min(1) @Max(100) int size
    ) {
        var pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "horaEntrada", "idTicket"));
        return new PagedModel<>(service.listar(matricula, estado, desde, hasta, pageable));
    }

    @GetMapping("/{id}")
    public TicketDetalleResponse consultarDetalle(@PathVariable @Positive Integer id) {
        return service.consultarDetalle(id);
    }

    public record ImporteResponse(BigDecimal importe) {}
}
