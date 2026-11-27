package es.safareyes.parking.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.oauth2.jwt.JwtException;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

@RequiredArgsConstructor
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private static final String PREFIJO = "Bearer ";

    private final JwtService jwtService;
    private final UsuarioDetailsService usuarios;

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain chain)
            throws ServletException, IOException {
        String cabecera = request.getHeader(HttpHeaders.AUTHORIZATION);
        if (cabecera != null && cabecera.regionMatches(true, 0, PREFIJO, 0, PREFIJO.length())) {
            autenticar(cabecera.substring(PREFIJO.length()));
        }
        chain.doFilter(request, response);
    }

    private void autenticar(String token) {
        if (token.isBlank()) {
            return;
        }

        try {
            var jwt = jwtService.validar(token);
            var usuario = usuarios.loadUserByUsername(jwt.getSubject());
            if (!usuario.getIdUsuario().toString().equals(jwt.getClaims().get("idUsuario"))) {
                return;
            }

            usuario.eraseCredentials();
            var autenticacion = UsernamePasswordAuthenticationToken.authenticated(
                    usuario, null, usuario.getAuthorities());
            var contexto = SecurityContextHolder.createEmptyContext();
            contexto.setAuthentication(autenticacion);
            SecurityContextHolder.setContext(contexto);
        } catch (JwtException | UsernameNotFoundException ex) {
            SecurityContextHolder.clearContext();
        }
    }
}
