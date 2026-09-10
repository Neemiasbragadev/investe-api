package br.com.neemiasbraga.investeapi.agregador;

import br.com.neemiasbraga.investeapi.cep.CepBruta;
import br.com.neemiasbraga.investeapi.core.Resultado;
import br.com.neemiasbraga.investeapi.cotacao.CotacaoBruta;
import br.com.neemiasbraga.investeapi.feriado.FeriadoBruta;
import br.com.neemiasbraga.investeapi.historico.HistoricoService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(AgregadorController.class)
class AgregadorControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private AgregadorService agregadorService;

    @MockitoBean
    private HistoricoService historicoService;

    @Test
    void buscaEmParaleloEDepoisRegistraNoHistorico() throws Exception {
        var cotacao = new CotacaoBruta("USD", "BRL", "Dolar", "5.4", "5.3", "0", "0", "5.35", "5.36", "1", "2026-01-01");
        var cep = new CepBruta("01310930", "SP", "Sao Paulo", "Bela Vista", "Av. Paulista", "viacep");
        var feriados = List.of(new FeriadoBruta("2026-01-01", "Confraternizacao", "national"));

        var resposta = new AgregadoResponse(
                new Resultado.Sucesso<>(cotacao),
                new Resultado.Sucesso<>(cep),
                new Resultado.Sucesso<>(feriados));

        when(agregadorService.buscarParalelo("USD-BRL", "01310930", 2026)).thenReturn(resposta);

        mockMvc.perform(get("/api/agregado?par=USD-BRL&cep=01310930&ano=2026"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.cotacao.dado.code").value("USD"))
                .andExpect(jsonPath("$.cep.dado.city").value("Sao Paulo"));

        verify(agregadorService).buscarParalelo("USD-BRL", "01310930", 2026);
        verify(historicoService).registrar(eq(resposta), eq("USD-BRL"), eq("01310930"), eq(2026), anyLong());
    }
}
