package es.safareyes.parking.service.impl;

import es.safareyes.parking.dto.request.TarifaRequest;
import es.safareyes.parking.dto.response.TarifaResponse;
import es.safareyes.parking.exception.RecursoNoEncontradoException;
import es.safareyes.parking.service.TarifaService;
import es.safareyes.parking.mapper.TarifaMapper;
import es.safareyes.parking.entity.Tarifa;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import es.safareyes.parking.repository.ITarifaRepository;

import java.time.LocalDateTime;

@Service
@Transactional(readOnly = true)
public class TarifaServiceImpl implements TarifaService {

    private final ITarifaRepository tarifaRepository;
    private final TarifaMapper tarifaMapper;

    public TarifaServiceImpl(ITarifaRepository tarifaRepository, TarifaMapper tarifaMapper) {
        this.tarifaRepository = tarifaRepository;
        this.tarifaMapper = tarifaMapper;
    }

    @Override
    public TarifaResponse consultarVigente(LocalDateTime fecha) {
        Tarifa tarifa = tarifaRepository
                .findFirstByFechaAplicacionLessThanEqualOrderByFechaAplicacionDesc(fecha);
        if (tarifa == null) {
            throw new RecursoNoEncontradoException("No hay una tarifa vigente para esa fecha");
        }
        return tarifaMapper.toResponse(tarifa);
    }

    @Override
    @Transactional
    public TarifaResponse crear(TarifaRequest request) {
        return tarifaMapper.toResponse(tarifaRepository.save(tarifaMapper.toEntity(request)));
    }
}
