package es.safareyes.parking.mapper;

import es.safareyes.parking.dto.request.PromocionRequest;
import es.safareyes.parking.dto.response.PromocionResponse;
import es.safareyes.parking.entity.Promocion;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;

@Mapper(componentModel = "spring")
public interface PromocionMapper {

    @Mapping(target = "idPromocion", ignore = true)
    Promocion toEntity(PromocionRequest request);

    PromocionResponse toResponse(Promocion promocion);

    @Mapping(target = "idPromocion", ignore = true)
    void actualizar(PromocionRequest request, @MappingTarget Promocion promocion);
}