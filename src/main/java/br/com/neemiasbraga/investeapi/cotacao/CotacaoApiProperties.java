package br.com.neemiasbraga.investeapi.cotacao;

import org.springframework.boot.context.properties.ConfigurationProperties;

import java.time.Duration;

@ConfigurationProperties(prefix = "investe.cotacao")
public record CotacaoApiProperties(String baseUrl, Duration connectTimeout, Duration readTimeout) {
}
