package br.com.neemiasbraga.investeapi.cotacao;

import br.com.neemiasbraga.investeapi.core.MetricasFontes;
import br.com.neemiasbraga.investeapi.core.Resultado;
import com.github.tomakehurst.wiremock.junit5.WireMockExtension;
import io.micrometer.core.instrument.simple.SimpleMeterRegistry;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;
import org.springframework.web.client.RestClient;

import java.time.Duration;

import static com.github.tomakehurst.wiremock.client.WireMock.*;
import static com.github.tomakehurst.wiremock.core.WireMockConfiguration.wireMockConfig;
import static org.assertj.core.api.Assertions.assertThat;

class CotacaoClientTest {

    @RegisterExtension
    static WireMockExtension wireMock = WireMockExtension.newInstance()
            .options(wireMockConfig().dynamicPort())
            .build();

    private CotacaoClient client(Duration readTimeout) {
        var properties = new CotacaoApiProperties(wireMock.baseUrl(), Duration.ofSeconds(2), readTimeout);
        return new CotacaoClient(RestClient.builder(), properties, new MetricasFontes(new SimpleMeterRegistry()));
    }

    @Test
    void devolveSucessoQuandoApiResponde200() {
        wireMock.stubFor(get(urlEqualTo("/json/last/USD-BRL"))
                .willReturn(okJson("""
                        {
                          "USDBRL": {
                            "code": "USD",
                            "codein": "BRL",
                            "name": "Dolar Americano/Real Brasileiro",
                            "high": "5.4321",
                            "low": "5.4000",
                            "varBid": "0.0123",
                            "pctChange": "0.23",
                            "bid": "5.4200",
                            "ask": "5.4250",
                            "timestamp": "1694370000",
                            "create_date": "2026-09-10 12:00:00"
                          }
                        }
                        """)));

        Resultado<CotacaoBruta> resultado = client(Duration.ofSeconds(2)).buscar("USD-BRL");

        assertThat(resultado).isInstanceOf(Resultado.Sucesso.class);
        var sucesso = (Resultado.Sucesso<CotacaoBruta>) resultado;
        assertThat(sucesso.dado().code()).isEqualTo("USD");
        assertThat(sucesso.dado().bid()).isEqualTo("5.4200");
    }

    @Test
    void devolveFalhaQuandoApiResponde500() {
        wireMock.stubFor(get(urlEqualTo("/json/last/USD-BRL")).willReturn(serverError()));

        Resultado<CotacaoBruta> resultado = client(Duration.ofSeconds(2)).buscar("USD-BRL");

        assertThat(resultado).isInstanceOf(Resultado.Falha.class);
        var falha = (Resultado.Falha<CotacaoBruta>) resultado;
        assertThat(falha.statusCode()).isEqualTo(500);
    }

    @Test
    void devolveTimeoutQuandoApiDemoraDemais() {
        wireMock.stubFor(get(urlEqualTo("/json/last/USD-BRL"))
                .willReturn(okJson("{}").withFixedDelay(800)));

        Resultado<CotacaoBruta> resultado = client(Duration.ofMillis(200)).buscar("USD-BRL");

        assertThat(resultado).isInstanceOf(Resultado.Timeout.class);
    }
}
