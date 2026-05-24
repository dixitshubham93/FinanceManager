package com.syfe.financemanager.config;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import io.swagger.v3.oas.models.servers.Server;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI customOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("Personal Finance Manager API")
                        .version("1.0.0")
                        .description("""
                                Production-ready REST API for personal finance management.

                                **Features:**
                                - User registration & session-based authentication
                                - Transaction management (CRUD with filtering)
                                - Category management (system defaults + custom)
                                - Savings goals with live progress tracking
                                - Monthly & yearly financial reports

                                **Authentication:**
                                POST /api/auth/login to receive a session cookie (JSESSIONID).
                                All other endpoints require this cookie.
                                """)
                        .contact(new Contact()
                                .name("Syfe Engineering")
                                .email("engineering@syfe.com")))
                .servers(List.of(
                        new Server().url("http://localhost:8080").description("Local Development"),
                        new Server().url("https://your-app.onrender.com").description("Render Production")))
                .addSecurityItem(new SecurityRequirement().addList("cookieAuth"))
                .components(new Components()
                        .addSecuritySchemes("cookieAuth",
                                new SecurityScheme()
                                        .type(SecurityScheme.Type.APIKEY)
                                        .in(SecurityScheme.In.COOKIE)
                                        .name("JSESSIONID")
                                        .description("Session cookie obtained after login")));
    }
}
