package br.com.neemiasbraga.investeapi.core;

import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * Contador de consultas por fonte externa e desfecho, exposto em
 * /actuator/prometheus como fontes_consultas_total{fonte=...,status=...}.
 */
@Component
public class MetricasFontes {

    private final MeterRegistry registry;

    public MetricasFontes(MeterRegistry registry) {
        this.registry = registry;
    }

    public void registrar(String fonte, StatusConsulta status) {
        Counter.builder("fontes.consultas")
                .tag("fonte", fonte)
                .tag("status", status.name())
                .register(registry)
                .increment();
    }

    /**
     * Resumo legivel dos contadores, para o dashboard consumir em JSON
     * (o formato texto do /actuator/prometheus e ruim de parsear em JS).
     */
    public List<MetricaResumo> listarResumo() {
        return registry.find("fontes.consultas").counters().stream()
                .map(counter -> new MetricaResumo(
                        counter.getId().getTag("fonte"),
                        counter.getId().getTag("status"),
                        counter.count()))
                .toList();
    }
}
