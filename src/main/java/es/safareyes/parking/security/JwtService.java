package es.safareyes.parking.security;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.oauth2.core.DelegatingOAuth2TokenValidator;
import org.springframework.security.oauth2.core.OAuth2Error;
import org.springframework.security.oauth2.core.OAuth2TokenValidatorResult;
import org.springframework.security.oauth2.jose.jws.MacAlgorithm;
import org.springframework.security.oauth2.jwt.JwsHeader;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtClaimsSet;
import org.springframework.security.oauth2.jwt.JwtEncoderParameters;
import org.springframework.security.oauth2.jwt.JwtIssuerValidator;
import org.springframework.security.oauth2.jwt.JwtTimestampValidator;
import org.springframework.security.oauth2.jwt.NimbusJwtDecoder;
import org.springframework.security.oauth2.jwt.NimbusJwtEncoder;
import org.springframework.stereotype.Service;

import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.time.Instant;

@Service
public class JwtService {

    public static final Duration VIGENCIA = Duration.ofHours(1);

    private final NimbusJwtEncoder codificador;
    private final NimbusJwtDecoder decodificador;
    private final String emisor;

    public JwtService(@Value("${jwt.secreto}") String secreto, @Value("${jwt.emisor}") String emisor) {
        byte[] bytes = secreto.getBytes(StandardCharsets.UTF_8);
        if (secreto.isBlank() || bytes.length < 32) {
            throw new IllegalArgumentException("JWT_SECRETO debe tener al menos 32 bytes");
        }

        var clave = new SecretKeySpec(bytes, "HmacSHA256");
        this.emisor = emisor;
        codificador = NimbusJwtEncoder.withSecretKey(clave).algorithm(MacAlgorithm.HS256).build();
        decodificador = NimbusJwtDecoder.withSecretKey(clave).macAlgorithm(MacAlgorithm.HS256).build();
        decodificador.setJwtValidator(new DelegatingOAuth2TokenValidator<>(
                new JwtIssuerValidator(emisor),
                new JwtTimestampValidator(Duration.ZERO),
                jwt -> jwt.getExpiresAt() != null && jwt.getSubject() != null && !jwt.getSubject().isBlank()
                        ? OAuth2TokenValidatorResult.success()
                        : OAuth2TokenValidatorResult.failure(new OAuth2Error("invalid_token"))
        ));
    }

    public String generar(UsuarioDetails usuario) {
        Instant ahora = Instant.now();
        JwtClaimsSet contenido = JwtClaimsSet.builder()
                .issuer(emisor)
                .issuedAt(ahora)
                .expiresAt(ahora.plus(VIGENCIA))
                .subject(usuario.getUsername())
                .claim("idUsuario", usuario.getIdUsuario().toString())
                .claim("nombre", usuario.getNombre())
                .claim("rol", usuario.getRol().name())
                .build();

        return codificador.encode(JwtEncoderParameters.from(JwsHeader.with(MacAlgorithm.HS256).build(), contenido))
                .getTokenValue();
    }

    public Jwt validar(String token) {
        return decodificador.decode(token);
    }
}
