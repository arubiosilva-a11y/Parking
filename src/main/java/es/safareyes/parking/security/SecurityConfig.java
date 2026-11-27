package es.safareyes.parking.security;

import jakarta.servlet.DispatcherType;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.ProviderManager;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
import org.springframework.web.servlet.HandlerExceptionResolver;

import java.util.Arrays;
import java.util.List;

@Configuration
@EnableMethodSecurity
public class SecurityConfig {

    private static final String ADMIN = "ADMIN";
    private static final String OPERADOR = "OPERADOR";
    private static final String ABONADO = "ABONADO";

    @Bean
    PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    AuthenticationManager authenticationManager(UsuarioDetailsService usuarios, PasswordEncoder encoder) {
        var proveedor = new DaoAuthenticationProvider(usuarios);
        proveedor.setPasswordEncoder(encoder);
        return new ProviderManager(proveedor);
    }

    @Bean
    SecurityFilterChain cadenaApi(HttpSecurity http, JwtService jwt, UsuarioDetailsService usuarios,
            @Qualifier("handlerExceptionResolver") HandlerExceptionResolver errores) throws Exception {
        return http
                .csrf(csrf -> csrf.disable())
                .cors(Customizer.withDefaults())
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authorizeHttpRequests(auth -> auth
                        .dispatcherTypeMatchers(DispatcherType.ERROR).permitAll()
                        .requestMatchers("/error", "/swagger-ui.html", "/swagger-ui/**",
                                "/v3/api-docs", "/v3/api-docs/**", "/v3/api-docs.yaml").permitAll()
                        .requestMatchers(HttpMethod.POST, "/api/v1/auth/login", "/api/v1/auth/registro").permitAll()
                        .requestMatchers(HttpMethod.GET, "/api/v1/plazas/disponibilidad",
                                "/api/v1/tarifas/vigente").permitAll()
                        .requestMatchers(HttpMethod.GET, "/api/v1/tickets/importe").hasAnyRole(ADMIN, OPERADOR)
                        .requestMatchers(HttpMethod.GET, "/api/v1/tickets", "/api/v1/tickets/{id}")
                                .hasAnyRole(ADMIN, OPERADOR, ABONADO)
                        .requestMatchers(HttpMethod.GET, "/api/v1/plazas", "/api/v1/promociones")
                                .hasAnyRole(ADMIN, OPERADOR)
                        .requestMatchers(HttpMethod.POST, "/api/v1/tickets/entrada", "/api/v1/tickets/pago",
                                "/api/v1/tickets/{id}/descuentos").hasAnyRole(ADMIN, OPERADOR)
                        .requestMatchers(HttpMethod.PATCH, "/api/v1/plazas/{id}/estado").hasAnyRole(ADMIN, OPERADOR)
                        .requestMatchers(HttpMethod.POST, "/api/v1/plazas", "/api/v1/tarifas",
                                "/api/v1/promociones", "/api/v1/abonados",
                                "/api/v1/abonados/{id}/activacion").hasRole(ADMIN)
                        .requestMatchers(HttpMethod.GET, "/api/v1/informes/recaudacion",
                                "/api/v1/informes/ocupacion").hasRole(ADMIN)
                        .anyRequest().denyAll())
                .addFilterBefore(new JwtAuthenticationFilter(jwt, usuarios), UsernamePasswordAuthenticationFilter.class)
                .exceptionHandling(ex -> ex
                        .authenticationEntryPoint((request, response, error) -> {
                            response.setHeader(HttpHeaders.WWW_AUTHENTICATE, "Bearer");
                            errores.resolveException(request, response, null, error);
                        })
                        .accessDeniedHandler((request, response, error) ->
                                errores.resolveException(request, response, null, error)))
                .build();
    }

    @Bean
    UrlBasedCorsConfigurationSource corsConfigurationSource(@Value("${app.cors.origenes}") String origenes) {
        List<String> permitidos = Arrays.stream(origenes.split(","))
                .map(String::strip)
                .filter(origen -> !origen.isEmpty())
                .toList();
        if (permitidos.stream().anyMatch(origen -> origen.contains("*"))) {
            throw new IllegalArgumentException("CORS_ORIGENES debe contener orígenes concretos");
        }

        var configuracion = new CorsConfiguration();
        configuracion.setAllowedOrigins(permitidos);
        configuracion.setAllowedMethods(List.of("GET", "POST", "PATCH", "OPTIONS"));
        configuracion.setAllowedHeaders(List.of(HttpHeaders.AUTHORIZATION, HttpHeaders.CONTENT_TYPE));
        var origen = new UrlBasedCorsConfigurationSource();
        origen.registerCorsConfiguration("/api/**", configuracion);
        return origen;
    }
}
