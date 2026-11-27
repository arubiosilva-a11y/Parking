package es.safareyes.parking.service;

import es.safareyes.parking.dto.request.RegistroRequest;
import es.safareyes.parking.dto.response.ActivacionResponse;
import es.safareyes.parking.dto.response.UsuarioResponse;
import es.safareyes.parking.entity.Abonado;
import es.safareyes.parking.entity.ActivacionAbonado;
import es.safareyes.parking.entity.Usuario;
import es.safareyes.parking.enums.Rol;
import es.safareyes.parking.exception.ConflictoException;
import es.safareyes.parking.exception.RecursoNoEncontradoException;
import es.safareyes.parking.repository.IAbonadoRepository;
import es.safareyes.parking.repository.IActivacionAbonadoRepository;
import es.safareyes.parking.repository.IUsuarioRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.time.Duration;
import java.time.Instant;
import java.util.Base64;
import java.util.HexFormat;
import java.util.Locale;

@Service
@RequiredArgsConstructor
public class ActivacionAbonadoService {

    private static final SecureRandom ALEATORIO = new SecureRandom();
    private static final Duration VIGENCIA = Duration.ofHours(24);

    private final IAbonadoRepository abonados;
    private final IActivacionAbonadoRepository activaciones;
    private final IUsuarioRepository usuarios;
    private final PasswordEncoder passwordEncoder;

    @PreAuthorize("hasRole('ADMIN')")
    @Transactional
    public ActivacionResponse emitir(Integer idAbonado) {
        Abonado abonado = abonados.buscarParaActualizar(idAbonado)
                .orElseThrow(() -> new RecursoNoEncontradoException("No existe el abonado " + idAbonado));
        if (usuarios.existsByAbonado_IdAbonado(idAbonado)
                || usuarios.buscarPorEmail(normalizarEmail(abonado.getEmail())).isPresent()) {
            throw cuentaExistente();
        }

        byte[] bytes = new byte[32];
        ALEATORIO.nextBytes(bytes);
        String codigo = Base64.getUrlEncoder().withoutPadding().encodeToString(bytes);
        String hash = calcularHash(codigo);
        Instant expiraEn = Instant.now().plus(VIGENCIA);

        ActivacionAbonado activacion = activaciones.findById(idAbonado).orElse(null);
        if (activacion == null) {
            activaciones.save(new ActivacionAbonado(idAbonado, hash, expiraEn));
        } else {
            activacion.renovar(hash, expiraEn);
        }
        return new ActivacionResponse(codigo, expiraEn);
    }

    @Transactional
    public UsuarioResponse registrar(RegistroRequest peticion) {
        if (!peticion.password().equals(peticion.confirmPassword())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Las contraseñas no coinciden");
        }
        if (peticion.password().getBytes(StandardCharsets.UTF_8).length > 72) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "La contraseña supera 72 bytes");
        }

        String hash = calcularHash(peticion.codigo());
        Integer idAbonado = activaciones.buscarIdAbonado(hash).orElseThrow(this::codigoNoValido);
        // El bloqueo común a emisión y registro impide consumir o renovar el mismo código a la vez.
        Abonado abonado = abonados.buscarParaActualizar(idAbonado).orElseThrow(this::codigoNoValido);
        String email = normalizarEmail(peticion.email());
        ActivacionAbonado activacion = activaciones.findById(idAbonado)
                .filter(a -> a.getCodigoHash().equals(hash) && a.getExpiraEn().isAfter(Instant.now()))
                .filter(a -> email.equals(normalizarEmail(abonado.getEmail())))
                .orElseThrow(this::codigoNoValido);

        if (usuarios.existsByAbonado_IdAbonado(idAbonado) || usuarios.buscarPorEmail(email).isPresent()) {
            throw cuentaExistente();
        }
        Usuario usuario = new Usuario(null, abonado.getNombre(), email,
                passwordEncoder.encode(peticion.password()), Rol.ABONADO, abonado);
        try {
            usuarios.saveAndFlush(usuario);
        } catch (DataIntegrityViolationException ex) {
            throw cuentaExistente();
        }
        activaciones.delete(activacion);
        return new UsuarioResponse(usuario.getIdUsuario(), usuario.getNombre(), usuario.getEmail(), usuario.getRol());
    }

    private String calcularHash(String codigo) {
        try {
            byte[] hash = MessageDigest.getInstance("SHA-256").digest(codigo.getBytes(StandardCharsets.UTF_8));
            return HexFormat.of().formatHex(hash);
        } catch (NoSuchAlgorithmException ex) {
            throw new IllegalStateException("SHA-256 no está disponible", ex);
        }
    }

    private String normalizarEmail(String email) {
        if (email == null || email.isBlank()) {
            throw new ConflictoException("El abonado debe tener un correo electrónico");
        }
        return email.strip().toLowerCase(Locale.ROOT);
    }

    private ResponseStatusException codigoNoValido() {
        return new ResponseStatusException(HttpStatus.BAD_REQUEST, "Código de activación no válido o caducado");
    }

    private ConflictoException cuentaExistente() {
        return new ConflictoException("Ya existe una cuenta para este abonado o correo");
    }
}
