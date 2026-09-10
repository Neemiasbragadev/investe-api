package br.com.neemiasbraga.investeapi.core;

import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import org.springframework.stereotype.Component;

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
}
