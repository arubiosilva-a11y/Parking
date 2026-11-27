package es.safareyes.parking.security;

import es.safareyes.parking.entity.Abonado;
import es.safareyes.parking.entity.Tarifa;
import es.safareyes.parking.entity.Ticket;
import es.safareyes.parking.entity.Usuario;
import es.safareyes.parking.enums.Rol;
import es.safareyes.parking.repository.IAbonadoRepository;
import es.safareyes.parking.repository.IActivacionAbonadoRepository;
import es.safareyes.parking.repository.ITarifaRepository;
import es.safareyes.parking.repository.ITicketRepository;
import es.safareyes.parking.repository.IUsuarioRepository;
import es.safareyes.parking.service.TarifaService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.AuthenticationCredentialsNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.jose.jws.MacAlgorithm;
import org.springframework.security.oauth2.jwt.JwsHeader;
import org.springframework.security.oauth2.jwt.JwtClaimsSet;
import org.springframework.security.oauth2.jwt.JwtEncoderParameters;
import org.springframework.security.oauth2.jwt.NimbusJwtEncoder;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.json.JsonMapper;

import javax.crypto.spec.SecretKeySpec;
import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.time.LocalDateTime;
import java.util.Map;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.Executors;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.options;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest(properties = {
        "spring.datasource.url=jdbc:h2:mem:parking_seguridad;MODE=PostgreSQL;DB_CLOSE_DELAY=-1",
        "spring.datasource.username=sa",
        "spring.datasource.password=",
        "spring.datasource.hikari.schema=PUBLIC",
        "spring.jpa.properties.hibernate.default_schema=PUBLIC",
        "spring.jpa.hibernate.ddl-auto=create-drop",
        "spring.jpa.show-sql=false",
        "jwt.secreto=" + SeguridadParkingTest.SECRETO
})
@AutoConfigureMockMvc
class SeguridadParkingTest {

    static final String SECRETO = "clave-exclusiva-de-prueba-32-bytes";
    private static final String PASSWORD = "parking-prueba-25";
    private static final String EMAIL_ADMIN = "admin@example.test";
    private static final String EMAIL_OPERADOR = "operador@example.test";
    private static final String EMAIL_ABONADO = "abonado@example.test";

    @Autowired private MockMvc mvc;
    @Autowired private JsonMapper json;
    @Autowired private JwtService jwt;
    @Autowired private IUsuarioRepository usuarios;
    @Autowired private IAbonadoRepository abonados;
    @Autowired private IActivacionAbonadoRepository activaciones;
    @Autowired private ITicketRepository tickets;
    @Autowired private ITarifaRepository tarifas;
    @Autowired private TarifaService tarifaService;
    @Autowired private PasswordEncoder encoder;

    private Abonado abonado;
    private Usuario admin;

    @BeforeEach
    void prepararDatos() {
        tickets.deleteAll();
        activaciones.deleteAll();
        usuarios.deleteAll();
        abonados.deleteAll();
        tarifas.deleteAll();
        abonado = guardarAbonado(EMAIL_ABONADO, "1234BCD");
        String hash = encoder.encode(PASSWORD);
        admin = usuarios.save(new Usuario(null, "Admin", EMAIL_ADMIN, hash, Rol.ADMIN, null));
        usuarios.save(new Usuario(null, "Operador", EMAIL_OPERADOR, hash, Rol.OPERADOR, null));
    }

    @Test
    void permiteConsultasPublicasYSwagger() throws Exception {
        tarifas.save(new Tarifa(null, BigDecimal.ONE, 5, 24, LocalDateTime.now().minusDays(1)));
        mvc.perform(get("/api/v1/plazas/disponibilidad")).andExpect(status().isOk());
        mvc.perform(get("/api/v1/tarifas/vigente")).andExpect(status().isOk());
        mvc.perform(get("/swagger-ui.html")).andExpect(status().is3xxRedirection());
        mvc.perform(get("/swagger-ui/index.html")).andExpect(status().isOk());
        mvc.perform(get("/v3/api-docs")).andExpect(status().isOk())
                .andExpect(jsonPath("$.components.securitySchemes.bearerAuth.scheme").value("bearer"))
                .andExpect(jsonPath("$.paths['/api/v1/auth/login'].post.security").isEmpty());
    }

    @Test
    void iniciaSesionConEmailInsensibleAMayusculasYDevuelveErrorGenerico() throws Exception {
        var respuesta = login("ADMIN@example.test", PASSWORD)
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.rol").value("ADMIN"))
                .andExpect(jsonPath("$.nombre").value("Admin"))
                .andExpect(jsonPath("$.tipo").value("Bearer"))
                .andExpect(jsonPath("$.expiraEnSegundos").value(3600))
                .andExpect(jsonPath("$.password").doesNotExist())
                .andReturn();
        assertThat(respuesta.getRequest().getSession(false)).isNull();
        var token = jwt.validar(leer(respuesta.getResponse().getContentAsString()).get("token").asString());
        assertThat(token.getSubject()).isEqualTo(EMAIL_ADMIN);
        assertThat(token.getExpiresAt()).isEqualTo(token.getIssuedAt().plusSeconds(3600));

        for (String email : new String[] {EMAIL_ADMIN, "desconocido@example.test"}) {
            login(email, "incorrecta")
                    .andExpect(status().isUnauthorized())
                    .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_PROBLEM_JSON))
                    .andExpect(jsonPath("$.detail").value("Credenciales incorrectas"));
        }
    }

    @Test
    void protegeRutasYValidaTokenCaducadoFirmaYEmisor() throws Exception {
        mvc.perform(get("/api/v1/tickets"))
                .andExpect(status().isUnauthorized())
                .andExpect(header().string(HttpHeaders.WWW_AUTHENTICATE, "Bearer"))
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_PROBLEM_JSON))
                .andExpect(jsonPath("$.status").value(401));

        Instant ahora = Instant.now();
        String caducado = firmar(SECRETO, "parkcontrol", ahora.minusSeconds(120));
        String otraFirma = firmar(SECRETO + "-distinta", "parkcontrol", ahora.plusSeconds(120));
        String otroEmisor = firmar(SECRETO, "otra-api", ahora.plusSeconds(120));
        for (String token : new String[] {caducado, otraFirma, otroEmisor, "manipulado"}) {
            mvc.perform(get("/api/v1/tickets").header(HttpHeaders.AUTHORIZATION, bearer(token)))
                    .andExpect(status().isUnauthorized());
        }

        String operador = token(EMAIL_OPERADOR);
        mvc.perform(get("/api/v1/plazas").header(HttpHeaders.AUTHORIZATION, operador))
                .andExpect(status().isOk());
        mvc.perform(get("/api/v1/promociones").header(HttpHeaders.AUTHORIZATION, operador))
                .andExpect(status().isOk());
        mvc.perform(patch("/api/v1/plazas/1/estado").header(HttpHeaders.AUTHORIZATION, operador))
                .andExpect(status().isBadRequest());
        mvc.perform(post("/api/v1/tickets/pago").header(HttpHeaders.AUTHORIZATION, operador))
                .andExpect(status().isBadRequest());
        mvc.perform(post("/api/v1/tarifas").header(HttpHeaders.AUTHORIZATION, operador))
                .andExpect(status().isForbidden())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_PROBLEM_JSON))
                .andExpect(jsonPath("$.status").value(403));
        mvc.perform(get("/api/v1/informes/recaudacion").header(HttpHeaders.AUTHORIZATION, token(EMAIL_ADMIN))
                        .param("desde", "2026-01-01").param("hasta", "2026-01-02"))
                .andExpect(status().isOk());

        usuarios.delete(admin);
        mvc.perform(get("/api/v1/tickets").header(HttpHeaders.AUTHORIZATION, bearer(firmar(
                        SECRETO, "parkcontrol", ahora.plusSeconds(120)))))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void aplicaPermisosDeServicioSinPasarPorElControlador() {
        assertThatThrownBy(() -> tarifaService.crear(null))
                .isInstanceOf(AuthenticationCredentialsNotFoundException.class);
    }

    @Test
    @WithMockUser(roles = "OPERADOR")
    void aplicaPermisosDeServicioParaUnRolInsuficiente() {
        assertThatThrownBy(() -> tarifaService.crear(null)).isInstanceOf(AccessDeniedException.class);
    }

    @Test
    void activaSoloAlAbonadoCorrespondienteYConsumeElCodigo() throws Exception {
        mvc.perform(post("/api/v1/abonados/{id}/activacion", abonado.getIdAbonado())
                        .header(HttpHeaders.AUTHORIZATION, token(EMAIL_OPERADOR)))
                .andExpect(status().isForbidden());

        String adminBearer = token(EMAIL_ADMIN);
        String anterior = emitirCodigo(adminBearer);
        String codigo = emitirCodigo(adminBearer);
        assertThat(activaciones.findById(abonado.getIdAbonado()).orElseThrow().getCodigoHash())
                .hasSize(64).isNotEqualTo(codigo);
        registrar(EMAIL_ABONADO, anterior).andExpect(status().isBadRequest());
        registrar("otra@example.test", codigo).andExpect(status().isBadRequest());
        registrar("ABONADO@example.test", codigo)
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.email").value(EMAIL_ABONADO))
                .andExpect(jsonPath("$.rol").value("ABONADO"))
                .andExpect(jsonPath("$.password").doesNotExist());

        Usuario usuario = usuarios.buscarPorEmail(EMAIL_ABONADO).orElseThrow();
        assertThat(usuario.getAbonado().getIdAbonado()).isEqualTo(abonado.getIdAbonado());
        assertThat(usuario.getContraseña()).isNotEqualTo(PASSWORD);
        assertThat(encoder.matches(PASSWORD, usuario.getContraseña())).isTrue();
        assertThat(activaciones.findById(abonado.getIdAbonado())).isEmpty();

        registrar(EMAIL_ABONADO, codigo).andExpect(status().isBadRequest());
        mvc.perform(post("/api/v1/abonados/{id}/activacion", abonado.getIdAbonado())
                        .header(HttpHeaders.AUTHORIZATION, adminBearer))
                .andExpect(status().isConflict());
        login(EMAIL_ABONADO, PASSWORD).andExpect(status().isOk()).andExpect(jsonPath("$.rol").value("ABONADO"));
    }

    @Test
    void rechazaUnCodigoCaducado() throws Exception {
        String codigo = emitirCodigo(token(EMAIL_ADMIN));
        var activacion = activaciones.findById(abonado.getIdAbonado()).orElseThrow();
        activacion.renovar(activacion.getCodigoHash(), Instant.now().minusSeconds(1));
        activaciones.save(activacion);

        registrar(EMAIL_ABONADO, codigo).andExpect(status().isBadRequest());
        assertThat(usuarios.buscarPorEmail(EMAIL_ABONADO)).isEmpty();
    }

    @Test
    void soloUnRegistroConcurrentePuedeConsumirElCodigo() throws Exception {
        String codigo = emitirCodigo(token(EMAIL_ADMIN));
        var inicio = new CountDownLatch(1);
        try (var executor = Executors.newFixedThreadPool(2)) {
            var primera = executor.submit(() -> {
                inicio.await();
                return registrar(EMAIL_ABONADO, codigo).andReturn().getResponse().getStatus();
            });
            var segunda = executor.submit(() -> {
                inicio.await();
                return registrar(EMAIL_ABONADO, codigo).andReturn().getResponse().getStatus();
            });
            inicio.countDown();
            assertThat(new Integer[] {primera.get(), segunda.get()}).containsExactlyInAnyOrder(201, 400);
        }
    }

    @Test
    void limitaLaPaginacionYElDetalleAlAbonadoDelToken() throws Exception {
        usuarios.save(new Usuario(null, "Abonado", EMAIL_ABONADO, encoder.encode(PASSWORD), Rol.ABONADO, abonado));
        var otroAbonado = guardarAbonado("otro@example.test", "9999BCD");
        Ticket propio = guardarTicket(abonado, "1234BCD");
        guardarTicket(abonado, "2234BCD");
        Ticket ajeno = guardarTicket(otroAbonado, "9999BCD");
        guardarTicket(null, "8234BCD");
        String bearerAbonado = token(EMAIL_ABONADO);

        mvc.perform(get("/api/v1/tickets").param("size", "1").header(HttpHeaders.AUTHORIZATION, bearerAbonado))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.page.totalElements").value(2))
                .andExpect(jsonPath("$.content.length()").value(1));
        mvc.perform(get("/api/v1/tickets").param("matricula", "9999BCD")
                        .header(HttpHeaders.AUTHORIZATION, bearerAbonado))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.page.totalElements").value(0));
        mvc.perform(get("/api/v1/tickets/{id}", propio.getIdTicket()).header(HttpHeaders.AUTHORIZATION, bearerAbonado))
                .andExpect(status().isOk());
        mvc.perform(get("/api/v1/tickets/{id}", ajeno.getIdTicket()).header(HttpHeaders.AUTHORIZATION, bearerAbonado))
                .andExpect(status().isForbidden())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_PROBLEM_JSON));
        mvc.perform(get("/api/v1/tickets/{id}", Integer.MAX_VALUE).header(HttpHeaders.AUTHORIZATION, bearerAbonado))
                .andExpect(status().isNotFound());
        mvc.perform(get("/api/v1/tickets/importe").param("matricula", "9999BCD")
                        .header(HttpHeaders.AUTHORIZATION, bearerAbonado))
                .andExpect(status().isForbidden());
        mvc.perform(get("/api/v1/tickets").header(HttpHeaders.AUTHORIZATION, token(EMAIL_OPERADOR)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.page.totalElements").value(4));
    }

    @Test
    void procesaPreflightSoloParaOrigenesPermitidos() throws Exception {
        mvc.perform(options("/api/v1/plazas/1/estado")
                        .header(HttpHeaders.ORIGIN, "http://localhost:4200")
                        .header(HttpHeaders.ACCESS_CONTROL_REQUEST_METHOD, "PATCH")
                        .header(HttpHeaders.ACCESS_CONTROL_REQUEST_HEADERS, "Authorization, Content-Type"))
                .andExpect(status().isOk())
                .andExpect(header().string(HttpHeaders.ACCESS_CONTROL_ALLOW_ORIGIN, "http://localhost:4200"));
        mvc.perform(options("/api/v1/plazas/1/estado")
                        .header(HttpHeaders.ORIGIN, "https://otro.example.test")
                        .header(HttpHeaders.ACCESS_CONTROL_REQUEST_METHOD, "PATCH"))
                .andExpect(status().isForbidden())
                .andExpect(header().doesNotExist(HttpHeaders.ACCESS_CONTROL_ALLOW_ORIGIN));
    }

    private Abonado guardarAbonado(String email, String matricula) {
        return abonados.save(new Abonado(null, "Abonado", email, matricula,
                LocalDateTime.now().plusDays(7), LocalDateTime.now().minusDays(7)));
    }

    private Ticket guardarTicket(Abonado titular, String matricula) {
        var ticket = new Ticket();
        ticket.setAbonado(titular);
        ticket.setMatricula(matricula);
        ticket.setEstado("Activo");
        ticket.setHoraEntrada(LocalDateTime.now());
        return tickets.save(ticket);
    }

    private ResultActions login(String email, String password) throws Exception {
        return mvc.perform(post("/api/v1/auth/login").contentType(MediaType.APPLICATION_JSON)
                .content(json.writeValueAsString(Map.of("email", email, "password", password))));
    }

    private String token(String email) throws Exception {
        var respuesta = login(email, PASSWORD).andExpect(status().isOk()).andReturn().getResponse();
        return bearer(leer(respuesta.getContentAsString()).get("token").asString());
    }

    private String emitirCodigo(String bearerAdmin) throws Exception {
        var respuesta = mvc.perform(post("/api/v1/abonados/{id}/activacion", abonado.getIdAbonado())
                        .header(HttpHeaders.AUTHORIZATION, bearerAdmin))
                .andExpect(status().isCreated())
                .andReturn().getResponse();
        return leer(respuesta.getContentAsString()).get("codigo").asString();
    }

    private ResultActions registrar(String email, String codigo) throws Exception {
        return mvc.perform(post("/api/v1/auth/registro").contentType(MediaType.APPLICATION_JSON)
                .content(json.writeValueAsString(Map.of(
                        "email", email, "password", PASSWORD, "confirmPassword", PASSWORD, "codigo", codigo,
                        "rol", "ADMIN"
                ))));
    }

    private JsonNode leer(String respuesta) {
        return json.readTree(respuesta);
    }

    private String bearer(String token) {
        return "Bearer " + token;
    }

    private String firmar(String secreto, String emisor, Instant caducidad) {
        var clave = new SecretKeySpec(secreto.getBytes(StandardCharsets.UTF_8), "HmacSHA256");
        var codificador = NimbusJwtEncoder.withSecretKey(clave).algorithm(MacAlgorithm.HS256).build();
        var claims = JwtClaimsSet.builder()
                .subject(EMAIL_ADMIN).issuer(emisor).claim("idUsuario", admin.getIdUsuario().toString())
                .issuedAt(caducidad.minusSeconds(3600)).expiresAt(caducidad).build();
        return codificador.encode(JwtEncoderParameters.from(JwsHeader.with(MacAlgorithm.HS256).build(), claims))
                .getTokenValue();
    }
}
