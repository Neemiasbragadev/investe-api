package br.com.neemiasbraga.investeapi.core;

import io.micrometer.core.instrument.simple.SimpleMeterRegistry;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class MetricasFontesTest {

    @Test
    void incrementaContadorComAsTagsDeFonteEStatus() {
        var registry = new SimpleMeterRegistry();
        var metricas = new MetricasFontes(registry);

        metricas.registrar("cotacao", StatusConsulta.SUCESSO);
        metricas.registrar("cotacao", StatusConsulta.SUCESSO);
        metricas.registrar("cep", StatusConsulta.FALHA);

        assertThat(registry.get("fontes.consultas")
                .tag("fonte", "cotacao").tag("status", "SUCESSO")
                .counter().count()).isEqualTo(2.0);

        assertThat(registry.get("fontes.consultas")
                .tag("fonte", "cep").tag("status", "FALHA")
                .counter().count()).isEqualTo(1.0);
    }

    @Test
    void listarResumoTrazTodosOsContadoresComFonteEStatus() {
        var registry = new SimpleMeterRegistry();
        var metricas = new MetricasFontes(registry);

        metricas.registrar("cotacao", StatusConsulta.SUCESSO);
        metricas.registrar("cep", StatusConsulta.FALHA);
        metricas.registrar("cep", StatusConsulta.FALHA);

        assertThat(metricas.listarResumo())
                .containsExactlyInAnyOrder(
                        new MetricaResumo("cotacao", "SUCESSO", 1.0),
                        new MetricaResumo("cep", "FALHA", 2.0));
    }
}
