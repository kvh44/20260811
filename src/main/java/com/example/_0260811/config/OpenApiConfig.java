package com.example._0260811.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI applicationOpenApi() {
        return new OpenAPI()
                .info(new Info()
                        .title("Client API")
                        .description("REST API for users and client records stored in MySQL and MongoDB.")
                        .version("1.0.0"));
    }
}
