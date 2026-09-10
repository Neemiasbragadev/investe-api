package br.com.neemiasbraga.investeapi.core;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI investeApiOpenApi() {
        return new OpenAPI().info(new Info()
                .title("investe-api")
                .description("Agregador de dados publicos brasileiros: cotacoes de moedas, CEP e feriados nacionais.")
                .version("v1"));
    }
}
