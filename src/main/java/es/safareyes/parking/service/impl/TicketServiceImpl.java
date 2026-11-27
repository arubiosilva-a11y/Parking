package es.safareyes.parking.service.impl;

import es.safareyes.parking.dto.request.TicketEntradaRequest;
import es.safareyes.parking.dto.request.TicketPagarRequest;
import es.safareyes.parking.dto.response.DescuentoResponse;
import es.safareyes.parking.dto.response.TicketDetalleResponse;
import es.safareyes.parking.dto.response.TicketResponse;
import es.safareyes.parking.entity.Descuento;
import es.safareyes.parking.entity.Promocion;
import es.safareyes.parking.exception.ConflictoException;
import es.safareyes.parking.enums.Estado;
import es.safareyes.parking.enums.Rol;
import es.safareyes.parking.exception.RecursoNoEncontradoException;
import es.safareyes.parking.exception.ReglaNegocioException;
import es.safareyes.parking.service.TicketService;
import es.safareyes.parking.mapper.TicketMapper;
import es.safareyes.parking.entity.Abonado;
import es.safareyes.parking.entity.Plaza;
import es.safareyes.parking.entity.Tarifa;
import es.safareyes.parking.entity.Ticket;
import es.safareyes.parking.security.UsuarioDetails;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import es.safareyes.parking.repository.IAbonadoRepository;
import es.safareyes.parking.repository.IDescuentoRepository;
import es.safareyes.parking.repository.IPlazaRepository;
import es.safareyes.parking.repository.IPromocionRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import es.safareyes.parking.repository.ITarifaRepository;
import es.safareyes.parking.repository.ITicketRepository;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Locale;
import java.util.List;
import java.util.regex.Pattern;

@Service
public class TicketServiceImpl implements TicketService {

    private static final BigDecimal MAXIMO_DESCUENTO = BigDecimal.valueOf(180);
    private static final String ESTADO_ACTIVO = "Activo";
    private static final String ESTADO_PAGADO = "Pagado";
    private static final Pattern MATRICULA_ESPANOLA =
            Pattern.compile("^[0-9]{4}[BCDFGHJKLMNPRSTVWXYZ]{3}$");

    private final ITicketRepository ticketRepository;
    private final IPlazaRepository plazaRepository;
    private final ITarifaRepository tarifaRepository;
    private final IAbonadoRepository abonadoRepository;
    private final IPromocionRepository promocionRepository;
    private final IDescuentoRepository descuentoRepository;
    private final TicketMapper ticketMapper;

    public TicketServiceImpl(
            ITicketRepository ticketRepository,
            IPlazaRepository plazaRepository,
            ITarifaRepository tarifaRepository,
            IAbonadoRepository abonadoRepository,
            IPromocionRepository promocionRepository,
            IDescuentoRepository descuentoRepository,
            TicketMapper ticketMapper
    ) {
        this.ticketRepository = ticketRepository;
        this.plazaRepository = plazaRepository;
        this.tarifaRepository = tarifaRepository;
        this.abonadoRepository = abonadoRepository;
        this.promocionRepository = promocionRepository;
        this.descuentoRepository = descuentoRepository;
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
            throw new ConflictoException("La matrícula ya tiene un ticket activo");
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
        long minutos = minutosTranscurridos(ticket.getHoraEntrada(), LocalDateTime.now());
        return calcularImporte(ticket, minutos, minutosDescuento(ticket));
    }

    @Override
    @PreAuthorize("hasAnyRole('ADMIN', 'OPERADOR')")
    @Transactional
    public TicketResponse pagar(TicketPagarRequest request) {
        if (request.codigoCine() != null && !request.codigoCine().isBlank()) {
            throw new ReglaNegocioException("La integración con CineTown todavía no está configurada");
        }

        String matricula = normalizarMatricula(request.matricula());
        Ticket ticket = ticketRepository
                .findFirstByMatriculaAndEstadoIgnoreCaseOrderByIdTicketAsc(matricula, ESTADO_ACTIVO)
                .orElseThrow(() -> sinTicketActivo(matricula));
        LocalDateTime ahora = LocalDateTime.now();
        long minutos = minutosTranscurridos(ticket.getHoraEntrada(), ahora);
        BigDecimal importe = calcularImporte(ticket, minutos, minutosDescuento(ticket));
        BigDecimal importeSinDescuento = calcularImporte(ticket, minutos, BigDecimal.ZERO);

        ticket.setHoraSalida(ahora);
        ticket.setMinutos(Math.toIntExact(minutos));
        ticket.setImporte(importe);
        ticket.setImporteDescuento(importeSinDescuento.subtract(importe));
        ticket.setEstado(ESTADO_PAGADO);
        ticket.getPlaza().setEstado(Estado.LIBRE);
        plazaRepository.save(ticket.getPlaza());

        return ticketMapper.toResponse(ticketRepository.save(ticket));
    }

    @Override
    @Transactional
    public DescuentoResponse aplicarDescuento(Integer idTicket, Integer idPromocion) {
        Ticket ticket = ticketRepository.buscarPorIdParaActualizar(idTicket)
                .orElseThrow(() -> ticketNoEncontrado(idTicket));
        if (!ESTADO_ACTIVO.equalsIgnoreCase(ticket.getEstado())) {
            throw new ReglaNegocioException("Solo se pueden aplicar promociones a tickets activos");
        }

        Promocion anterior = ticket.getPromocion();
        if ((anterior != null && idPromocion.equals(anterior.getIdPromocion()))
                || descuentoRepository.existsByTicketIdTicketAndPromocionIdPromocion(idTicket, idPromocion)) {
            throw new ConflictoException("La promoción ya está aplicada a este ticket");
        }

        Promocion promocion = promocionRepository.findById(idPromocion).orElseThrow(
                () -> new RecursoNoEncontradoException("No existe la promoción " + idPromocion));
        BigDecimal minutos = promocion.getMinDescuento();
        if (minutos == null || minutos.signum() <= 0) {
            throw new ReglaNegocioException("La promoción debe descontar una cantidad positiva de minutos");
        }
        BigDecimal total = minutosDescuento(ticket).add(minutos);
        if (total.compareTo(MAXIMO_DESCUENTO) > 0) {
            throw new ReglaNegocioException("El descuento total no puede superar los 180 minutos");
        }

        ticket.setDescuento(total);
        Descuento descuento = descuentoRepository.save(new Descuento(ticket, promocion, minutos, LocalDateTime.now()));
        return ticketMapper.toResponse(descuento);
    }

    @Override
    @PreAuthorize("hasAnyRole('ADMIN', 'OPERADOR', 'ABONADO')")
    @Transactional(readOnly = true)
    public Page<TicketResponse> listar(String matricula, String estado, LocalDate desde, LocalDate hasta,
                                       Pageable pageable) {
        if (desde != null && hasta != null && desde.isAfter(hasta)) {
            throw new ReglaNegocioException("La fecha inicial no puede ser posterior a la fecha final");
        }
        Specification<Ticket> filtros = Specification.unrestricted();
        Integer idAbonado = idAbonadoAutenticado();
        if (idAbonado != null) {
            filtros = filtros.and((ticket, query, cb) -> cb.equal(ticket.get("abonado").get("idAbonado"), idAbonado));
        }
        if (matricula != null && !matricula.isBlank()) {
            String matriculaNormalizada = normalizarMatricula(matricula);
            filtros = filtros.and((ticket, query, cb) -> cb.equal(ticket.get("matricula"), matriculaNormalizada));
        }
        if (estado != null && !estado.isBlank()) {
            String estadoNormalizado = estado.trim().toUpperCase(Locale.ROOT);
            filtros = filtros.and((ticket, query, cb) -> cb.equal(cb.upper(ticket.get("estado")), estadoNormalizado));
        }
        if (desde != null) {
            filtros = filtros.and((ticket, query, cb) ->
                    cb.greaterThanOrEqualTo(ticket.get("horaEntrada"), desde.atStartOfDay()));
        }
        if (hasta != null && !hasta.equals(LocalDate.MAX)) {
            filtros = filtros.and((ticket, query, cb) ->
                    cb.lessThan(ticket.get("horaEntrada"), hasta.plusDays(1).atStartOfDay()));
        }
        return ticketRepository.findAll(filtros, pageable).map(ticketMapper::toResponse);
    }

    @Override
    @PreAuthorize("hasAnyRole('ADMIN', 'OPERADOR', 'ABONADO')")
    @Transactional(readOnly = true)
    public TicketDetalleResponse consultarDetalle(Integer idTicket) {
        Integer idAbonado = idAbonadoAutenticado();
        Ticket ticket = ticketRepository.buscarPorIdConDescuentos(idTicket);
        if (ticket == null) {
            throw ticketNoEncontrado(idTicket);
        }
        if (idAbonado != null && (ticket.getAbonado() == null
                || !idAbonado.equals(ticket.getAbonado().getIdAbonado()))) {
            throw new AccessDeniedException("El ticket pertenece a otro usuario");
        }

        List<DescuentoResponse> descuentos = new ArrayList<>();
        List<Descuento> aplicados = descuentoRepository.findByTicketIdTicketOrderByIdDescuentoAsc(idTicket);
        if (ticket.getPromocion() != null) {
            BigDecimal minutosRegistrados = aplicados.stream().map(Descuento::getMinutos)
                    .reduce(BigDecimal.ZERO, BigDecimal::add);
            BigDecimal minutosAnteriores = minutosDescuento(ticket).subtract(minutosRegistrados).max(BigDecimal.ZERO);
            descuentos.add(new DescuentoResponse(null, ticket.getPromocion().getIdPromocion(), minutosAnteriores, null));
        }
        aplicados.stream().map(ticketMapper::toResponse).forEach(descuentos::add);
        return new TicketDetalleResponse(ticketMapper.toResponse(ticket), descuentos);
    }

    private Integer idAbonadoAutenticado() {
        var autenticacion = SecurityContextHolder.getContext().getAuthentication();
        if (autenticacion == null || !(autenticacion.getPrincipal() instanceof UsuarioDetails usuario)) {
            throw new AccessDeniedException("Usuario no reconocido");
        }
        if (usuario.getRol() != Rol.ABONADO) {
            return null;
        }
        if (usuario.getIdAbonado() == null) {
            throw new AccessDeniedException("La cuenta no está vinculada a un abonado");
        }
        return usuario.getIdAbonado();
    }

    private RecursoNoEncontradoException ticketNoEncontrado(Integer idTicket) {
        return new RecursoNoEncontradoException("No existe el ticket " + idTicket);
    }

    private RecursoNoEncontradoException sinTicketActivo(String matricula) {
        return new RecursoNoEncontradoException("No hay un ticket activo para la matrícula " + matricula);
    }

    private Ticket buscarTicketActivo(String matricula) {
        String normalizada = normalizarMatricula(matricula);
        Ticket ticket = ticketRepository.findFirstByMatriculaAndEstadoIgnoreCase(normalizada, ESTADO_ACTIVO);
        if (ticket == null) {
            throw sinTicketActivo(normalizada);
        }
        return ticket;
    }

    private BigDecimal minutosDescuento(Ticket ticket) {
        return ticket.getDescuento() == null ? BigDecimal.ZERO : ticket.getDescuento();
    }

    private BigDecimal calcularImporte(Ticket ticket, long minutos, BigDecimal descuento) {
        Tarifa tarifa = ticket.getTarifa();
        if (ticket.getAbonado() != null || minutos <= tarifa.getMinCortesia()) {
            return BigDecimal.ZERO.setScale(2, RoundingMode.HALF_UP);
        }

        long bloquesDe24Horas = Math.max(1, (minutos + 1439) / 1440);
        long maximoFacturable = (long) tarifa.getHorasMaximas() * 60 * bloquesDe24Horas;
        BigDecimal minutosFacturables = BigDecimal.valueOf(minutos).subtract(descuento)
                .max(BigDecimal.ZERO).min(BigDecimal.valueOf(maximoFacturable));

        return tarifa.getPrecioMin().multiply(minutosFacturables).setScale(2, RoundingMode.HALF_UP);
    }

    private long minutosTranscurridos(LocalDateTime desde, LocalDateTime hasta) {
        long milisegundos = Duration.between(desde, hasta).toMillis();
        return Math.max(0, (milisegundos + 59_999) / 60_000);
    }

    private String normalizarMatricula(String matricula) {
        return matricula.trim().replaceAll("\\s+", "").toUpperCase(Locale.ROOT);
    }
}
