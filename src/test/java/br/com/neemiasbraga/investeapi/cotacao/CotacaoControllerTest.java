package br.com.neemiasbraga.investeapi.cotacao;

import br.com.neemiasbraga.investeapi.core.Resultado;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(CotacaoController.class)
class CotacaoControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private CotacaoClient cotacaoClient;

    @Test
    void retorna200ComACotacaoQuandoSucesso() throws Exception {
        var cotacao = new CotacaoBruta("USD", "BRL", "Dolar Americano/Real Brasileiro",
                "5.4321", "5.4000", "0.0123", "0.23", "5.4200", "5.4250",
                "1694370000", "2026-09-10 12:00:00");
        when(cotacaoClient.buscar("USD-BRL")).thenReturn(new Resultado.Sucesso<>(cotacao));

        mockMvc.perform(get("/api/cotacoes/USD-BRL"))
                .andExpect(status().isOk())
                .andExpect(content().json("""
                        {"code":"USD","bid":"5.4200"}
                        """));
    }

    @Test
    void retorna502ComProblemDetailQuandoFalha() throws Exception {
        when(cotacaoClient.buscar("XXX-BRL")).thenReturn(new Resultado.Falha<>("erro no provedor", 502));

        mockMvc.perform(get("/api/cotacoes/XXX-BRL"))
                .andExpect(status().isBadGateway())
                .andExpect(content().contentType(MediaType.APPLICATION_PROBLEM_JSON))
                .andExpect(jsonPath("$.status").value(502))
                .andExpect(jsonPath("$.detail").value("erro no provedor"));
    }

    @Test
    void retorna504ComProblemDetailQuandoTimeout() throws Exception {
        when(cotacaoClient.buscar("USD-BRL")).thenReturn(new Resultado.Timeout<>("demorou demais"));

        mockMvc.perform(get("/api/cotacoes/USD-BRL"))
                .andExpect(status().isGatewayTimeout())
                .andExpect(content().contentType(MediaType.APPLICATION_PROBLEM_JSON))
                .andExpect(jsonPath("$.detail").value("demorou demais"));
    }
}
