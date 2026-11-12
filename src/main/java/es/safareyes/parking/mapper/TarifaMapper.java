package es.safareyes.parking.mapper;

import es.safareyes.parking.dto.request.TarifaRequest;
import es.safareyes.parking.dto.response.TarifaResponse;
import es.safareyes.parking.entity.Tarifa;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;

@Mapper(componentModel = "spring")
public interface TarifaMapper {

    @Mapping(target = "idTarifa", ignore = true)
    Tarifa toEntity(TarifaRequest request);

    TarifaResponse toResponse(Tarifa tarifa);

    @Mapping(target = "idTarifa", ignore = true)
    void actualizar(TarifaRequest request, @MappingTarget Tarifa tarifa);
}