package es.safareyes.parking.service.impl;

import es.safareyes.parking.dto.DisponibilidadPlantaView;
import es.safareyes.parking.dto.request.PlazaRequest;
import es.safareyes.parking.dto.response.PlazaResponse;
import es.safareyes.parking.enums.Estado;
import es.safareyes.parking.exception.RecursoNoEncontradoException;
import es.safareyes.parking.exception.ReglaNegocioException;
import es.safareyes.parking.service.PlazaService;
import es.safareyes.parking.mapper.PlazaMapper;
import es.safareyes.parking.entity.Plaza;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import es.safareyes.parking.repository.IPlazaRepository;

import java.util.List;

@Service
@Transactional(readOnly = true)
public class PlazaServiceImpl implements PlazaService {

    private final IPlazaRepository plazaRepository;
    private final PlazaMapper plazaMapper;

    public PlazaServiceImpl(IPlazaRepository plazaRepository, PlazaMapper plazaMapper) {
        this.plazaRepository = plazaRepository;
        this.plazaMapper = plazaMapper;
    }

    @Override
    public List<PlazaResponse> listar() {
        return plazaRepository.findAll().stream().map(plazaMapper::toResponse).toList();
    }

    @Override
    public List<DisponibilidadPlantaView> consultarDisponibilidad() {
        return plazaRepository.buscarDisponibilidad();
    }

    @Override
    @Transactional
    public PlazaResponse crear(PlazaRequest request) {
        Plaza plaza = plazaMapper.toEntity(request);
        return plazaMapper.toResponse(plazaRepository.save(plaza));
    }

    @Override
    @Transactional
    public PlazaResponse cambiarEstado(Integer idPlaza, Estado nuevoEstado) {
        Plaza plaza = plazaRepository.findById(idPlaza)
                .orElseThrow(() -> new RecursoNoEncontradoException("No existe la plaza " + idPlaza));

        if (plaza.getEstado() == Estado.OCUPADO && nuevoEstado == Estado.BLOQUEADA) {
            throw new ReglaNegocioException("No se puede bloquear una plaza ocupada");
        }

        plaza.setEstado(nuevoEstado);
        return plazaMapper.toResponse(plazaRepository.save(plaza));
    }
}
