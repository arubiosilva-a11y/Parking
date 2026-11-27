package es.safareyes.parking.dto.response;

import es.safareyes.parking.enums.Rol;

public record TokenResponse(String token, String tipo, long expiraEnSegundos, String nombre, Rol rol) {
    @Override
    public String toString() {
        return "TokenResponse";
    }
}
