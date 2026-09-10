package br.com.neemiasbraga.investeapi.cep;

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

class CepClientTest {

    @RegisterExtension
    static WireMockExtension wireMock = WireMockExtension.newInstance()
            .options(wireMockConfig().dynamicPort())
            .build();

    private CepClient client(Duration readTimeout) {
        var properties = new CepApiProperties(wireMock.baseUrl(), Duration.ofSeconds(2), readTimeout);
        return new CepClient(RestClient.builder(), properties, new MetricasFontes(new SimpleMeterRegistry()));
    }

    @Test
    void devolveSucessoQuandoApiResponde200() {
        wireMock.stubFor(get(urlEqualTo("/api/cep/v1/01310930"))
                .willReturn(okJson("""
                        {
                          "cep": "01310930",
                          "state": "SP",
                          "city": "Sao Paulo",
                          "neighborhood": "Bela Vista",
                          "street": "Avenida Paulista",
                          "service": "viacep"
                        }
                        """)));

        Resultado<CepBruta> resultado = client(Duration.ofSeconds(2)).buscar("01310930");

        assertThat(resultado).isInstanceOf(Resultado.Sucesso.class);
        var sucesso = (Resultado.Sucesso<CepBruta>) resultado;
        assertThat(sucesso.dado().city()).isEqualTo("Sao Paulo");
        assertThat(sucesso.dado().street()).isEqualTo("Avenida Paulista");
    }

    @Test
    void devolveFalhaQuandoCepNaoExiste() {
        wireMock.stubFor(get(urlEqualTo("/api/cep/v1/00000000")).willReturn(notFound()));

        Resultado<CepBruta> resultado = client(Duration.ofSeconds(2)).buscar("00000000");

        assertThat(resultado).isInstanceOf(Resultado.Falha.class);
        var falha = (Resultado.Falha<CepBruta>) resultado;
        assertThat(falha.statusCode()).isEqualTo(404);
    }

    @Test
    void devolveTimeoutQuandoApiDemoraDemais() {
        wireMock.stubFor(get(urlEqualTo("/api/cep/v1/01310930"))
                .willReturn(okJson("{}").withFixedDelay(800)));

        Resultado<CepBruta> resultado = client(Duration.ofMillis(200)).buscar("01310930");

        assertThat(resultado).isInstanceOf(Resultado.Timeout.class);
    }
}
