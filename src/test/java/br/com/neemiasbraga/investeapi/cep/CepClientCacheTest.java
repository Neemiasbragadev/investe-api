package br.com.neemiasbraga.investeapi.cep;

import br.com.neemiasbraga.investeapi.core.CacheConfig;
import br.com.neemiasbraga.investeapi.core.MetricasFontes;
import com.github.tomakehurst.wiremock.junit5.WireMockExtension;
import io.micrometer.core.instrument.simple.SimpleMeterRegistry;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.web.client.RestClient;

import java.time.Duration;

import static com.github.tomakehurst.wiremock.client.WireMock.*;
import static com.github.tomakehurst.wiremock.core.WireMockConfiguration.wireMockConfig;

/**
 * Valida o @Cacheable de CepClient com o proxy real do Spring (nao so
 * chamando o metodo Java direto): a segunda chamada com o mesmo CEP nao
 * bate na API externa de novo, e uma resposta de erro nunca fica em cache.
 */
class CepClientCacheTest {

    @RegisterExtension
    static WireMockExtension wireMock = WireMockExtension.newInstance()
            .options(wireMockConfig().dynamicPort())
            .build();

    @Configuration
    @EnableCaching
    @Import(CacheConfig.class)
    static class TestConfig {

        @Bean
        RestClient.Builder restClientBuilder() {
            return RestClient.builder();
        }

        @Bean
        CepApiProperties cepApiProperties() {
            return new CepApiProperties(wireMock.baseUrl(), Duration.ofSeconds(2), Duration.ofSeconds(2));
        }

        @Bean
        MetricasFontes metricasFontes() {
            return new MetricasFontes(new SimpleMeterRegistry());
        }

        @Bean
        CepClient cepClient(RestClient.Builder builder, CepApiProperties properties, MetricasFontes metricasFontes) {
            return new CepClient(builder, properties, metricasFontes);
        }
    }

    @Test
    void naoRepeteChamadaExternaQuandoResultadoJaEstaEmCache() {
        wireMock.stubFor(get(urlEqualTo("/api/cep/v1/01310930")).willReturn(okJson("""
                {"cep":"01310930","state":"SP","city":"Sao Paulo",
                "neighborhood":"Bela Vista","street":"Av. Paulista","service":"viacep"}
                """)));

        try (var context = new AnnotationConfigApplicationContext(TestConfig.class)) {
            var cepClient = context.getBean(CepClient.class);

            cepClient.buscar("01310930");
            cepClient.buscar("01310930");

            wireMock.verify(1, getRequestedFor(urlEqualTo("/api/cep/v1/01310930")));
        }
    }

    @Test
    void naoCacheiaQuandoApiFalha() {
        wireMock.stubFor(get(urlEqualTo("/api/cep/v1/00000000")).willReturn(serverError()));

        try (var context = new AnnotationConfigApplicationContext(TestConfig.class)) {
            var cepClient = context.getBean(CepClient.class);

            cepClient.buscar("00000000");
            cepClient.buscar("00000000");

            wireMock.verify(2, getRequestedFor(urlEqualTo("/api/cep/v1/00000000")));
        }
    }
}
