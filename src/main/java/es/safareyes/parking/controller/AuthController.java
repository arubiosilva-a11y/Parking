package es.safareyes.parking.controller;

import es.safareyes.parking.dto.request.LoginRequest;
import es.safareyes.parking.dto.request.RegistroRequest;
import es.safareyes.parking.dto.response.TokenResponse;
import es.safareyes.parking.dto.response.UsuarioResponse;
import es.safareyes.parking.security.JwtService;
import es.safareyes.parking.security.UsuarioDetails;
import es.safareyes.parking.service.ActivacionAbonadoService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.nio.charset.StandardCharsets;

@RestController
@RequestMapping("/api/v1/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthenticationManager authenticationManager;
    private final JwtService jwtService;
    private final ActivacionAbonadoService activaciones;

    @PostMapping("/login")
    public TokenResponse login(@Valid @RequestBody LoginRequest peticion) {
        if (peticion.password().getBytes(StandardCharsets.UTF_8).length > 72) {
            throw new BadCredentialsException("Credenciales incorrectas");
        }
        var autenticacion = authenticationManager.authenticate(
                UsernamePasswordAuthenticationToken.unauthenticated(peticion.email(), peticion.password()));
        var usuario = (UsuarioDetails) autenticacion.getPrincipal();
        return new TokenResponse(jwtService.generar(usuario), "Bearer", JwtService.VIGENCIA.toSeconds(),
                usuario.getNombre(), usuario.getRol());
    }

    @PostMapping("/registro")
    @ResponseStatus(HttpStatus.CREATED)
    public UsuarioResponse registro(@Valid @RequestBody RegistroRequest peticion) {
        return activaciones.registrar(peticion);
    }
}
