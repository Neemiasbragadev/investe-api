package br.com.neemiasbraga.investeapi.agregador;

import br.com.neemiasbraga.investeapi.cep.CepApiProperties;
import br.com.neemiasbraga.investeapi.cep.CepClient;
import br.com.neemiasbraga.investeapi.core.MetricasFontes;
import br.com.neemiasbraga.investeapi.cotacao.CotacaoApiProperties;
import br.com.neemiasbraga.investeapi.cotacao.CotacaoClient;
import br.com.neemiasbraga.investeapi.feriado.FeriadoApiProperties;
import br.com.neemiasbraga.investeapi.feriado.FeriadoClient;
import com.github.tomakehurst.wiremock.junit5.WireMockExtension;
import io.micrometer.core.instrument.simple.SimpleMeterRegistry;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;
import org.springframework.web.client.RestClient;

import java.time.Duration;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import static com.github.tomakehurst.wiremock.client.WireMock.get;
import static com.github.tomakehurst.wiremock.client.WireMock.okJson;
import static com.github.tomakehurst.wiremock.client.WireMock.urlEqualTo;
import static com.github.tomakehurst.wiremock.core.WireMockConfiguration.wireMockConfig;
import static org.assertj.core.api.Assertions.assertThat;

/**
 * Mede e compara buscarSequencial vs buscarParalelo usando um delay
 * artificial identico nas 3 fontes (via WireMock), para o resultado ser
 * deterministico e nao depender da latencia real da internet.
 */
class AgregadorBenchmarkTest {

    private static final int DELAY_MS = 300;

    @RegisterExtension
    static WireMockExtension wireMock = WireMockExtension.newInstance()
            .options(wireMockConfig().dynamicPort())
            .build();

    private final ExecutorService executor = Executors.newVirtualThreadPerTaskExecutor();

    @AfterEach
    void tearDown() {
        executor.shutdown();
    }

    private AgregadorService agregadorService() {
        var timeout = Duration.ofSeconds(5);
        var metricasFontes = new MetricasFontes(new SimpleMeterRegistry());
        var cotacaoClient = new CotacaoClient(RestClient.builder(),
                new CotacaoApiProperties(wireMock.baseUrl(), timeout, timeout), metricasFontes);
        var cepClient = new CepClient(RestClient.builder(),
                new CepApiProperties(wireMock.baseUrl(), timeout, timeout), metricasFontes);
        var feriadoClient = new FeriadoClient(RestClient.builder(),
                new FeriadoApiProperties(wireMock.baseUrl(), timeout, timeout), metricasFontes);

        return new AgregadorService(cotacaoClient, cepClient, feriadoClient, executor);
    }

    @Test
    void buscaParaleloEhMaisRapidoQueSequencial() {
        wireMock.stubFor(get(urlEqualTo("/json/last/USD-BRL")).willReturn(okJson("""
                {"USDBRL":{"code":"USD","codein":"BRL","name":"n","high":"1","low":"1",
                "varBid":"0","pctChange":"0","bid":"5.10","ask":"5.11","timestamp":"1",
                "create_date":"2026-01-01"}}
                """).withFixedDelay(DELAY_MS)));

        wireMock.stubFor(get(urlEqualTo("/api/cep/v1/01310930")).willReturn(okJson("""
                {"cep":"01310930","state":"SP","city":"Sao Paulo",
                "neighborhood":"Bela Vista","street":"Av. Paulista","service":"viacep"}
                """).withFixedDelay(DELAY_MS)));

        wireMock.stubFor(get(urlEqualTo("/api/feriados/v1/2026")).willReturn(okJson("""
                [{"date":"2026-01-01","name":"Confraternizacao Universal","type":"national"}]
                """).withFixedDelay(DELAY_MS)));

        var agregador = agregadorService();

        long inicioSequencial = System.nanoTime();
        agregador.buscarSequencial("USD-BRL", "01310930", 2026);
        long duracaoSequencialMs = TimeUnit.NANOSECONDS.toMillis(System.nanoTime() - inicioSequencial);

        long inicioParalelo = System.nanoTime();
        agregador.buscarParalelo("USD-BRL", "01310930", 2026);
        long duracaoParaleloMs = TimeUnit.NANOSECONDS.toMillis(System.nanoTime() - inicioParalelo);

        System.out.println("Sequencial: " + duracaoSequencialMs + "ms | Paralelo: " + duracaoParaleloMs + "ms");

        assertThat(duracaoSequencialMs).isGreaterThanOrEqualTo(DELAY_MS * 3L);
        assertThat(duracaoParaleloMs).isLessThan(duracaoSequencialMs);
        assertThat(duracaoParaleloMs).isLessThan(DELAY_MS * 2L);
    }
}
