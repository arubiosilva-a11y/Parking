package DTOS;

import java.math.BigDecimal;
import java.time.LocalDate;

public record RecaudacionDiaDto(
        LocalDate fecha,
        BigDecimal total
) {
}