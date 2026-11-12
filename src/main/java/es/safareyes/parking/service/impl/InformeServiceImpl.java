package es.safareyes.parking.service.impl;

import es.safareyes.parking.dto.OcupacionPlantaDTO;
import es.safareyes.parking.dto.RecaudacionDiaDto;
import es.safareyes.parking.exception.ReglaNegocioException;
import es.safareyes.parking.service.InformeService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import es.safareyes.parking.repository.ITicketRepository;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

@Service
@Transactional(readOnly = true)
public class InformeServiceImpl implements InformeService {

    private final ITicketRepository ticketRepository;

    public InformeServiceImpl(ITicketRepository ticketRepository) {
        this.ticketRepository = ticketRepository;
    }

    @Override
    public List<RecaudacionDiaDto> consultarRecaudacion(LocalDate desde, LocalDate hasta) {
        if (desde.isAfter(hasta)) {
            throw new ReglaNegocioException("La fecha inicial no puede ser posterior a la fecha final");
        }
        return ticketRepository.buscarRecaudacionPorDia(desde, hasta);
    }

    @Override
    public List<OcupacionPlantaDTO> consultarOcupacion() {
        return ticketRepository.buscarOcupacionPorPlanta().stream()
                .map(this::convertirOcupacion)
                .toList();
    }

    private OcupacionPlantaDTO convertirOcupacion(Object[] fila) {
        return new OcupacionPlantaDTO(
                ((Number) fila[0]).intValue(),
                ((Number) fila[1]).longValue(),
                ((Number) fila[2]).longValue(),
                decimal(fila[3]),
                decimal(fila[4])
        );
    }

    private BigDecimal decimal(Object valor) {
        return valor instanceof BigDecimal decimal ? decimal : new BigDecimal(valor.toString());
    }
}
