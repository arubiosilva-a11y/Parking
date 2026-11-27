package es.safareyes.parking.dto.response;

import es.safareyes.parking.enums.Rol;

public record UsuarioResponse(Integer idUsuario, String nombre, String email, Rol rol) {
}
