package br.com.neemiasbraga.investeapi.core;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
public class MetricasController {

    private final MetricasFontes metricasFontes;

    public MetricasController(MetricasFontes metricasFontes) {
        this.metricasFontes = metricasFontes;
    }

    @GetMapping("/api/metricas")
    public List<MetricaResumo> listar() {
        return metricasFontes.listarResumo();
    }
}
