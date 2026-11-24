package es.safareyes.parking.mapper;

import es.safareyes.parking.dto.response.DescuentoResponse;
import es.safareyes.parking.dto.response.TicketResponse;
import es.safareyes.parking.entity.Descuento;
import es.safareyes.parking.entity.Ticket;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface TicketMapper {

    @Mapping(target = "idPlaza", source = "plaza.idPlaza")
    @Mapping(target = "idAbonado", source = "abonado.idAbonado")
    @Mapping(target = "idTarifa", source = "tarifa.idTarifa")
    @Mapping(target = "idPromocion", source = "promocion.idPromocion")
    TicketResponse toResponse(Ticket ticket);

    @Mapping(target = "idPromocion", source = "promocion.idPromocion")
    DescuentoResponse toResponse(Descuento descuento);
}