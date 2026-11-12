package es.safareyes.parking.service.impl;

import es.safareyes.parking.dto.request.AbonadoRequest;
import es.safareyes.parking.dto.response.AbonadoResponse;
import es.safareyes.parking.service.AbonadoService;
import es.safareyes.parking.mapper.AbonadoMapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import es.safareyes.parking.repository.IAbonadoRepository;

@Service
public class AbonadoServiceImpl implements AbonadoService {

    private final IAbonadoRepository abonadoRepository;
    private final AbonadoMapper abonadoMapper;

    public AbonadoServiceImpl(IAbonadoRepository abonadoRepository, AbonadoMapper abonadoMapper) {
        this.abonadoRepository = abonadoRepository;
        this.abonadoMapper = abonadoMapper;
    }

    @Override
    @Transactional
    public AbonadoResponse crear(AbonadoRequest request) {
        return abonadoMapper.toResponse(abonadoRepository.save(abonadoMapper.toEntity(request)));
    }
}
