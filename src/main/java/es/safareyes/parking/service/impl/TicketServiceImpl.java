package es.safareyes.parking.service.impl;

import es.safareyes.parking.dto.request.TicketEntradaRequest;
import es.safareyes.parking.dto.request.TicketPagarRequest;
import es.safareyes.parking.dto.response.TicketResponse;
import es.safareyes.parking.enums.Estado;
import es.safareyes.parking.exception.RecursoNoEncontradoException;
import es.safareyes.parking.exception.ReglaNegocioException;
import es.safareyes.parking.service.TicketService;
import es.safareyes.parking.mapper.TicketMapper;
import es.safareyes.parking.entity.Abonado;
import es.safareyes.parking.entity.Plaza;
import es.safareyes.parking.entity.Tarifa;
import es.safareyes.parking.entity.Ticket;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import es.safareyes.parking.repository.IAbonadoRepository;
import es.safareyes.parking.repository.IPlazaRepository;
import es.safareyes.parking.repository.ITarifaRepository;
import es.safareyes.parking.repository.ITicketRepository;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.List;
import java.util.regex.Pattern;

@Service
public class TicketServiceImpl implements TicketService {

    private static final String ESTADO_ACTIVO = "Activo";
    private static final String ESTADO_PAGADO = "Pagado";
    private static final Pattern MATRICULA_ESPANOLA =
            Pattern.compile("^[0-9]{4}[BCDFGHJKLMNPRSTVWXYZ]{3}$");

    private final ITicketRepository ticketRepository;
    private final IPlazaRepository plazaRepository;
    private final ITarifaRepository tarifaRepository;
    private final IAbonadoRepository abonadoRepository;
    private final TicketMapper ticketMapper;

    public TicketServiceImpl(
            ITicketRepository ticketRepository,
            IPlazaRepository plazaRepository,
            ITarifaRepository tarifaRepository,
            IAbonadoRepository abonadoRepository,
            TicketMapper ticketMapper
    ) {
        this.ticketRepository = ticketRepository;
        this.plazaRepository = plazaRepository;
        this.tarifaRepository = tarifaRepository;
        this.abonadoRepository = abonadoRepository;
        this.ticketMapper = ticketMapper;
    }

    @Override
    @Transactional
    public TicketResponse registrarEntrada(TicketEntradaRequest request) {
        String matricula = normalizarMatricula(request.matricula());
        if (!MATRICULA_ESPANOLA.matcher(matricula).matches()) {
            throw new ReglaNegocioException("La matrícula no tiene un formato español válido");
        }
        if (ticketRepository.findFirstByMatriculaAndEstadoIgnoreCase(matricula, ESTADO_ACTIVO) != null) {
            throw new ReglaNegocioException("La matrícula ya tiene un ticket activo");
        }

        LocalDateTime ahora = LocalDateTime.now();
        Tarifa tarifa = tarifaRepository
                .findFirstByFechaAplicacionLessThanEqualOrderByFechaAplicacionDesc(ahora);
        if (tarifa == null) {
            throw new RecursoNoEncontradoException("No hay una tarifa vigente");
        }

        List<Plaza> plazasLibres = plazaRepository
                .findAllByTipoAndEstadoOrderByPlantaDescNumeroAsc(request.tipo(), Estado.LIBRE);
        if (plazasLibres.isEmpty()) {
            throw new ReglaNegocioException("No hay plazas libres del tipo solicitado");
        }

        Plaza plaza = plazasLibres.getFirst();
        plaza.setEstado(Estado.OCUPADO);
        plazaRepository.save(plaza);

        Abonado abonado = abonadoRepository
                .findFirstByMatriculaAndFechaIniLessThanEqualAndFechaFinGreaterThanEqual(
                        matricula, ahora, ahora
                );

        Ticket ticket = new Ticket();
        ticket.setMatricula(matricula);
        ticket.setHoraEntrada(ahora);
        ticket.setEstado(ESTADO_ACTIVO);
        ticket.setDescuento(BigDecimal.ZERO);
        ticket.setImporteDescuento(BigDecimal.ZERO);
        ticket.setImporte(BigDecimal.ZERO);
        ticket.setPlaza(plaza);
        ticket.setTarifa(tarifa);
        ticket.setAbonado(abonado);

        return ticketMapper.toResponse(ticketRepository.save(ticket));
    }

    @Override
    @Transactional(readOnly = true)
    public BigDecimal calcularImporteActual(String matricula) {
        Ticket ticket = buscarTicketActivo(matricula);
        return calcularImporte(ticket, LocalDateTime.now());
    }

    @Override
    @Transactional
    public TicketResponse pagar(TicketPagarRequest request) {
        if (request.codigoCine() != null && !request.codigoCine().isBlank()) {
            throw new ReglaNegocioException("La integración con CineTown todavía no está configurada");
        }

        Ticket ticket = buscarTicketActivo(request.matricula());
        LocalDateTime ahora = LocalDateTime.now();
        long minutos = minutosTranscurridos(ticket.getHoraEntrada(), ahora);
        BigDecimal importe = calcularImporte(ticket, ahora);

        ticket.setHoraSalida(ahora);
        ticket.setMinutos(Math.toIntExact(minutos));
        ticket.setImporte(importe);
        ticket.setEstado(ESTADO_PAGADO);
        ticket.getPlaza().setEstado(Estado.LIBRE);
        plazaRepository.save(ticket.getPlaza());

        return ticketMapper.toResponse(ticketRepository.save(ticket));
    }

    private Ticket buscarTicketActivo(String matricula) {
        String normalizada = normalizarMatricula(matricula);
        Ticket ticket = ticketRepository.findFirstByMatriculaAndEstadoIgnoreCase(normalizada, ESTADO_ACTIVO);
        if (ticket == null) {
            throw new RecursoNoEncontradoException("No hay un ticket activo para la matrícula " + normalizada);
        }
        return ticket;
    }

    private BigDecimal calcularImporte(Ticket ticket, LocalDateTime hasta) {
        if (ticket.getAbonado() != null) {
            return BigDecimal.ZERO.setScale(2, RoundingMode.HALF_UP);
        }

        long minutos = minutosTranscurridos(ticket.getHoraEntrada(), hasta);
        Tarifa tarifa = ticket.getTarifa();
        long minutosFacturables = Math.max(0, minutos - tarifa.getMinCortesia());
        long bloquesDe24Horas = Math.max(1, (minutos + 1439) / 1440);
        long maximoFacturable = (long) tarifa.getHorasMaximas() * 60 * bloquesDe24Horas;
        minutosFacturables = Math.min(minutosFacturables, maximoFacturable);

        return tarifa.getPrecioMin()
                .multiply(BigDecimal.valueOf(minutosFacturables))
                .setScale(2, RoundingMode.HALF_UP);
    }

    private long minutosTranscurridos(LocalDateTime desde, LocalDateTime hasta) {
        long milisegundos = Duration.between(desde, hasta).toMillis();
        return Math.max(0, (milisegundos + 59_999) / 60_000);
    }

    private String normalizarMatricula(String matricula) {
        return matricula.trim().replaceAll("\\s+", "").toUpperCase();
    }
}
