package es.safareyes.parking.dto.response;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record DescuentoResponse(Integer idDescuento, Integer idPromocion, BigDecimal minutos, LocalDateTime aplicadoEn) {}
