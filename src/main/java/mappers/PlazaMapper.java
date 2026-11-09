package mappers;

import DTOS.request.PlazaRequest;
import DTOS.response.PlazaResponse;
import modelos.Plaza;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;

@Mapper(componentModel = "spring")
public interface PlazaMapper {

    Plaza toEntity(PlazaRequest request);

    PlazaResponse toResponse(Plaza plaza);

    @Mapping(target = "idPlaza", ignore = true)
    void actualizar(PlazaRequest request, @MappingTarget Plaza plaza);
}