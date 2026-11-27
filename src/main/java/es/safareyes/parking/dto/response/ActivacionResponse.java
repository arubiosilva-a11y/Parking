package es.safareyes.parking.dto.response;

import java.time.Instant;

public record ActivacionResponse(String codigo, Instant expiraEn) {
    @Override
    public String toString() {
        return "ActivacionResponse[expiraEn=" + expiraEn + "]";
    }
}
