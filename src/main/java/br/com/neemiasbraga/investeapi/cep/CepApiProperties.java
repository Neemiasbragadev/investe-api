package br.com.neemiasbraga.investeapi.cep;

import org.springframework.boot.context.properties.ConfigurationProperties;

import java.time.Duration;

@ConfigurationProperties(prefix = "investe.cep")
public record CepApiProperties(String baseUrl, Duration connectTimeout, Duration readTimeout) {
}
