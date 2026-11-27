package es.safareyes.parking.security;

import es.safareyes.parking.entity.Usuario;
import es.safareyes.parking.enums.Rol;
import lombok.Getter;
import org.springframework.security.core.CredentialsContainer;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;
import java.util.List;

public final class UsuarioDetails implements UserDetails, CredentialsContainer {

    @Getter
    private final Integer idUsuario;
    @Getter
    private final Integer idAbonado;
    @Getter
    private final String nombre;
    @Getter
    private final Rol rol;

    private final String email;
    private String password;

    public UsuarioDetails(Usuario usuario) {
        this.idUsuario = usuario.getIdUsuario();
        this.idAbonado = usuario.getAbonado() == null ? null : usuario.getAbonado().getIdAbonado();
        this.nombre = usuario.getNombre();
        this.rol = usuario.getRol();
        this.email = usuario.getEmail();
        this.password = usuario.getContraseña();
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return List.of(new SimpleGrantedAuthority("ROLE_" + rol.name()));
    }

    @Override
    public String getUsername() {
        return email;
    }

    @Override
    public String getPassword() {
        return password;
    }

    @Override
    public void eraseCredentials() {
        password = null;
    }
}
