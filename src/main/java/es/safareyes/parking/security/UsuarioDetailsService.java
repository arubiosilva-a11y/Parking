package es.safareyes.parking.security;

import es.safareyes.parking.repository.IUsuarioRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class UsuarioDetailsService implements UserDetailsService {

    private final IUsuarioRepository usuarios;

    @Override
    public UsuarioDetails loadUserByUsername(String email) {
        return usuarios.buscarPorEmail(email.strip())
                .map(UsuarioDetails::new)
                .orElseThrow(() -> new UsernameNotFoundException("Credenciales incorrectas"));
    }
}
