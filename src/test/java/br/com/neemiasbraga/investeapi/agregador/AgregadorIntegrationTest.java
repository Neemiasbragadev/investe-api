package br.com.neemiasbraga.investeapi.agregador;

import com.github.tomakehurst.wiremock.junit5.WireMockExtension;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.resttestclient.TestRestTemplate;
import org.springframework.boot.resttestclient.autoconfigure.AutoConfigureTestRestTemplate;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.postgresql.PostgreSQLContainer;

import static com.github.tomakehurst.wiremock.client.WireMock.get;
import static com.github.tomakehurst.wiremock.client.WireMock.okJson;
import static com.github.tomakehurst.wiremock.client.WireMock.serverError;
import static com.github.tomakehurst.wiremock.client.WireMock.urlEqualTo;
import static com.github.tomakehurst.wiremock.core.WireMockConfiguration.wireMockConfig;
import static org.assertj.core.api.Assertions.assertThat;

/**
 * Teste de integracao de ponta a ponta: contexto Spring inteiro, Postgres
 * real (Testcontainers) e as 3 fontes externas simuladas ao mesmo tempo
 * com desfechos diferentes (sucesso, falha, timeout), via uma chamada HTTP
 * real ao endpoint. Confere que a resposta e o historico gravado batem.
 */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureTestRestTemplate
@Testcontainers
class AgregadorIntegrationTest {

    @Container
    @ServiceConnection
    static PostgreSQLContainer postgres = new PostgreSQLContainer("postgres:17");

    @RegisterExtension
    static WireMockExtension wireMock = WireMockExtension.newInstance()
            .options(wireMockConfig().dynamicPort())
            .build();

    @DynamicPropertySource
    static void apisExternas(DynamicPropertyRegistry registry) {
        registry.add("investe.cotacao.base-url", wireMock::baseUrl);
        registry.add("investe.cep.base-url", wireMock::baseUrl);
        registry.add("investe.feriado.base-url", wireMock::baseUrl);
        registry.add("investe.feriado.read-timeout", () -> "200ms");
    }

    @Autowired
    private TestRestTemplate restTemplate;

    @Test
    void agregaComSucessoFalhaETimeoutAoMesmoTempoEGravaNoHistorico() {
        wireMock.stubFor(get(urlEqualTo("/json/last/USD-BRL")).willReturn(okJson("""
                {"USDBRL":{"code":"USD","codein":"BRL","name":"n","high":"1","low":"1",
                "varBid":"0","pctChange":"0","bid":"5.10","ask":"5.11","timestamp":"1",
                "create_date":"2026-01-01"}}
                """)));

        wireMock.stubFor(get(urlEqualTo("/api/cep/v1/01310930")).willReturn(serverError()));

        wireMock.stubFor(get(urlEqualTo("/api/feriados/v1/2026"))
                .willReturn(okJson("[]").withFixedDelay(1000)));

        ResponseEntity<String> resposta = restTemplate.getForEntity(
                "/api/agregado?par=USD-BRL&cep=01310930&ano=2026", String.class);

        assertThat(resposta.getStatusCode().value()).isEqualTo(200);
        assertThat(resposta.getBody()).contains("\"code\":\"USD\"");

        ResponseEntity<String> ultimo = restTemplate.getForEntity("/api/historico/ultimo", String.class);

        assertThat(ultimo.getStatusCode().value()).isEqualTo(200);
        assertThat(ultimo.getBody()).contains("\"cotacaoStatus\":\"SUCESSO\"");
        assertThat(ultimo.getBody()).contains("\"cepStatus\":\"FALHA\"");
        assertThat(ultimo.getBody()).contains("\"feriadosStatus\":\"TIMEOUT\"");
    }
}
