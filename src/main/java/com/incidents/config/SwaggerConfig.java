package com.incidents.config;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Swagger / OpenAPI UI configuration.
 * Access at: http://localhost:8080/swagger-ui.html
 *
 * Interview point: Why Swagger?
 * - Self-documenting API: anyone can test endpoints without Postman
 * - Frontend teams can explore APIs without reading code
 * - JWT integration: you can paste your token directly in the "Authorize" button
 */
@Configuration
public class SwaggerConfig {

    @Bean
    public OpenAPI openAPI() {
        final String securitySchemeName = "bearerAuth";

        return new OpenAPI()
                .info(new Info()
                        .title("Smart Incident Monitoring System API")
                        .description("Backend API for monitoring, detecting, and alerting on incidents from microservices. " +
                                     "Authenticate via /api/auth/login to get a JWT token.")
                        .version("1.0.0")
                        .contact(new Contact()
                                .name("Incident Monitoring Team")
                                .email("admin@incidents.com")))
                // Tell Swagger about our JWT auth scheme
                .addSecurityItem(new SecurityRequirement().addList(securitySchemeName))
                .components(new Components()
                        .addSecuritySchemes(securitySchemeName, new SecurityScheme()
                                .name(securitySchemeName)
                                .type(SecurityScheme.Type.HTTP)
                                .scheme("bearer")
                                .bearerFormat("JWT")
                                .description("Paste your JWT token here (without 'Bearer ' prefix)")));
    }
}
