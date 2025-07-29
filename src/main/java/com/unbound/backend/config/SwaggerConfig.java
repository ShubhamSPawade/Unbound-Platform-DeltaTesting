package com.unbound.backend.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class SwaggerConfig {
    
    @Bean
    public OpenAPI customOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("Unbound Platform API")
                        .description("RESTful API for the Unbound Platform - A comprehensive fest and event management system for colleges and students.")
                        .version("1.0.0"));
    }
} 