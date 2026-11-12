package es.safareyes.parking.service.impl;

import es.safareyes.parking.dto.request.PromocionRequest;
import es.safareyes.parking.dto.response.PromocionResponse;
import es.safareyes.parking.service.PromocionService;
import es.safareyes.parking.mapper.PromocionMapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import es.safareyes.parking.repository.IPromocionRepository;

import java.util.List;

@Service
@Transactional(readOnly = true)
public class PromocionServiceImpl implements PromocionService {

    private final IPromocionRepository promocionRepository;
    private final PromocionMapper promocionMapper;

    public PromocionServiceImpl(IPromocionRepository promocionRepository, PromocionMapper promocionMapper) {
        this.promocionRepository = promocionRepository;
        this.promocionMapper = promocionMapper;
    }

    @Override
    public List<PromocionResponse> listar() {
        return promocionRepository.findAll().stream().map(promocionMapper::toResponse).toList();
    }

    @Override
    @Transactional
    public PromocionResponse crear(PromocionRequest request) {
        return promocionMapper.toResponse(promocionRepository.save(promocionMapper.toEntity(request)));
    }
}
