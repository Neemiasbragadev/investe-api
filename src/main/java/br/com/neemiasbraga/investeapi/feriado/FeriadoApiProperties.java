package br.com.neemiasbraga.investeapi.feriado;

import org.springframework.boot.context.properties.ConfigurationProperties;

import java.time.Duration;

@ConfigurationProperties(prefix = "investe.feriado")
public record FeriadoApiProperties(String baseUrl, Duration connectTimeout, Duration readTimeout) {
}
