package es.safareyes.parking.dto.response;

import java.math.BigDecimal;

public record PromocionResponse(
        Integer idPromocion,
        String nombre,
        String tipo,
        BigDecimal minDescuento
) {}