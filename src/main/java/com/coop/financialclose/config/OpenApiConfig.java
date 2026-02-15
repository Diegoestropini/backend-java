package com.coop.financialclose.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI openAPI() {
        return new OpenAPI().info(new Info()
            .title("Financial Close Simulator API")
            .version("1.0.0")
            .description("API para ingesta masiva de transacciones y cierre contable diario"));
    }
}
