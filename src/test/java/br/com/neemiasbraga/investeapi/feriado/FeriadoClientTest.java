package br.com.neemiasbraga.investeapi.feriado;

import br.com.neemiasbraga.investeapi.core.MetricasFontes;
import br.com.neemiasbraga.investeapi.core.Resultado;
import com.github.tomakehurst.wiremock.junit5.WireMockExtension;
import io.micrometer.core.instrument.simple.SimpleMeterRegistry;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;
import org.springframework.web.client.RestClient;

import java.time.Duration;
import java.util.List;

import static com.github.tomakehurst.wiremock.client.WireMock.*;
import static com.github.tomakehurst.wiremock.core.WireMockConfiguration.wireMockConfig;
import static org.assertj.core.api.Assertions.assertThat;

class FeriadoClientTest {

    @RegisterExtension
    static WireMockExtension wireMock = WireMockExtension.newInstance()
            .options(wireMockConfig().dynamicPort())
            .build();

    private FeriadoClient client(Duration readTimeout) {
        var properties = new FeriadoApiProperties(wireMock.baseUrl(), Duration.ofSeconds(2), readTimeout);
        return new FeriadoClient(RestClient.builder(), properties, new MetricasFontes(new SimpleMeterRegistry()));
    }

    @Test
    void devolveSucessoQuandoApiResponde200() {
        wireMock.stubFor(get(urlEqualTo("/api/feriados/v1/2026"))
                .willReturn(okJson("""
                        [
                          {"date": "2026-01-01", "name": "Confraternizacao Universal", "type": "national"},
                          {"date": "2026-04-21", "name": "Tiradentes", "type": "national"}
                        ]
                        """)));

        Resultado<List<FeriadoBruta>> resultado = client(Duration.ofSeconds(2)).buscar(2026);

        assertThat(resultado).isInstanceOf(Resultado.Sucesso.class);
        var sucesso = (Resultado.Sucesso<List<FeriadoBruta>>) resultado;
        assertThat(sucesso.dado()).hasSize(2);
        assertThat(sucesso.dado().getFirst().name()).isEqualTo("Confraternizacao Universal");
    }

    @Test
    void devolveFalhaQuandoApiResponde500() {
        wireMock.stubFor(get(urlEqualTo("/api/feriados/v1/2026")).willReturn(serverError()));

        Resultado<List<FeriadoBruta>> resultado = client(Duration.ofSeconds(2)).buscar(2026);

        assertThat(resultado).isInstanceOf(Resultado.Falha.class);
        var falha = (Resultado.Falha<List<FeriadoBruta>>) resultado;
        assertThat(falha.statusCode()).isEqualTo(500);
    }

    @Test
    void devolveTimeoutQuandoApiDemoraDemais() {
        wireMock.stubFor(get(urlEqualTo("/api/feriados/v1/2026"))
                .willReturn(okJson("[]").withFixedDelay(800)));

        Resultado<List<FeriadoBruta>> resultado = client(Duration.ofMillis(200)).buscar(2026);

        assertThat(resultado).isInstanceOf(Resultado.Timeout.class);
    }
}
