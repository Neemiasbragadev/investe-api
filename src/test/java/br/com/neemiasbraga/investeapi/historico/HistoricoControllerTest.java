package br.com.neemiasbraga.investeapi.historico;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.data.domain.PageRequest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(HistoricoController.class)
class HistoricoControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private HistoricoConsultaRepository repository;

    @Test
    void listaHistoricoOrdenadoPorMaisRecente() throws Exception {
        var historico = new HistoricoConsulta("USD-BRL", "01310930", 2026,
                StatusConsulta.SUCESSO, StatusConsulta.SUCESSO, StatusConsulta.SUCESSO, 250);

        when(repository.findAllByOrderByCriadoEmDesc(PageRequest.of(0, 20)))
                .thenReturn(List.of(historico));

        mockMvc.perform(get("/api/historico"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].par").value("USD-BRL"));
    }

    @Test
    void devolveUltimoQuandoExisteHistorico() throws Exception {
        var historico = new HistoricoConsulta("EUR-BRL", "01310930", 2026,
                StatusConsulta.SUCESSO, StatusConsulta.FALHA, StatusConsulta.SUCESSO, 400);

        when(repository.findAllByOrderByCriadoEmDesc(PageRequest.of(0, 1)))
                .thenReturn(List.of(historico));

        mockMvc.perform(get("/api/historico/ultimo"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.par").value("EUR-BRL"))
                .andExpect(jsonPath("$.cepStatus").value("FALHA"));
    }

    @Test
    void devolve404QuandoNaoHaHistorico() throws Exception {
        when(repository.findAllByOrderByCriadoEmDesc(PageRequest.of(0, 1)))
                .thenReturn(List.of());

        mockMvc.perform(get("/api/historico/ultimo"))
                .andExpect(status().isNotFound());
    }
}
