package es.safareyes.parking.config;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import org.springdoc.core.customizers.OpenApiCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

@Configuration
public class OpenApiConfig {

    private static final String BEARER = "bearerAuth";

    @Bean
    OpenAPI parkingOpenApi() {
        var esquema = new SecurityScheme().type(SecurityScheme.Type.HTTP).scheme("bearer").bearerFormat("JWT");
        return new OpenAPI()
                .components(new Components().addSecuritySchemes(BEARER, esquema))
                .addSecurityItem(new SecurityRequirement().addList(BEARER));
    }

    @Bean
    OpenApiCustomizer operacionesPublicas() {
        return api -> {
            api.getPaths().get("/api/v1/auth/login").getPost().setSecurity(List.of());
            api.getPaths().get("/api/v1/auth/registro").getPost().setSecurity(List.of());
            api.getPaths().get("/api/v1/plazas/disponibilidad").getGet().setSecurity(List.of());
            api.getPaths().get("/api/v1/tarifas/vigente").getGet().setSecurity(List.of());
        };
    }
}
