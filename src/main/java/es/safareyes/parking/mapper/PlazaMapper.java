package es.safareyes.parking.mapper;

import es.safareyes.parking.dto.request.PlazaRequest;
import es.safareyes.parking.dto.response.PlazaResponse;
import es.safareyes.parking.entity.Plaza;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;

@Mapper(componentModel = "spring")
public interface PlazaMapper {

    @Mapping(target = "idPlaza", ignore = true)
    Plaza toEntity(PlazaRequest request);

    PlazaResponse toResponse(Plaza plaza);

    @Mapping(target = "idPlaza", ignore = true)
    void actualizar(PlazaRequest request, @MappingTarget Plaza plaza);
}