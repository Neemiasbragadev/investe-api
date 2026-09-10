package br.com.neemiasbraga.investeapi.core;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(MetricasController.class)
class MetricasControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private MetricasFontes metricasFontes;

    @Test
    void listaResumoDasMetricasEmJson() throws Exception {
        when(metricasFontes.listarResumo()).thenReturn(List.of(
                new MetricaResumo("cotacao", "SUCESSO", 3.0),
                new MetricaResumo("cep", "FALHA", 1.0)));

        mockMvc.perform(get("/api/metricas"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].fonte").value("cotacao"))
                .andExpect(jsonPath("$[0].status").value("SUCESSO"))
                .andExpect(jsonPath("$[0].total").value(3.0))
                .andExpect(jsonPath("$[1].fonte").value("cep"));
    }
}
