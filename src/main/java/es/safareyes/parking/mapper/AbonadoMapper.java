package es.safareyes.parking.mapper;

import es.safareyes.parking.dto.request.AbonadoRequest;
import es.safareyes.parking.dto.response.AbonadoResponse;
import es.safareyes.parking.entity.Abonado;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;

@Mapper(componentModel = "spring")
public interface AbonadoMapper {

    @Mapping(target = "idAbonado", ignore = true)
    Abonado toEntity(AbonadoRequest request);

    AbonadoResponse toResponse(Abonado abonado);

    @Mapping(target = "idAbonado", ignore = true)
    void actualizar(AbonadoRequest request, @MappingTarget Abonado abonado);
}