package com.mservice.cinema.promotion.cinema_promotion.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import io.swagger.v3.oas.models.servers.Server;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

@Configuration
public class SwaggerConfig {
    
    @Bean
    public OpenAPI customOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("Cinema Promotion - User Management API")
                        .description("A Spring Boot REST API for managing users with PostgreSQL database and Redis-based distributed locking")
                        .version("1.0.0")
                        .contact(new Contact()
                                .name("Cinema Promotion Team")
                                .email("support@cinemapromotion.com")
                                .url("https://cinemapromotion.com"))
                        .license(new License()
                                .name("MIT License")
                                .url("https://opensource.org/licenses/MIT")))
                .servers(List.of(
                        new Server()
                                .url("http://localhost:8080")
                                .description("Development server"),
                        new Server()
                                .url("https://api.cinemapromotion.com")
                                .description("Production server")
                ));
    }
} 