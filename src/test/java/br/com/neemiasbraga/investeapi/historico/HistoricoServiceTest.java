package br.com.neemiasbraga.investeapi.historico;

import br.com.neemiasbraga.investeapi.agregador.AgregadoResponse;
import br.com.neemiasbraga.investeapi.core.Resultado;
import br.com.neemiasbraga.investeapi.core.StatusConsulta;
import br.com.neemiasbraga.investeapi.cotacao.CotacaoBruta;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Pageable;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class HistoricoServiceTest {

    @Mock
    private HistoricoConsultaRepository repository;

    @Test
    void mapeiaCadaResultadoParaOStatusCorrespondente() {
        var historicoService = new HistoricoService(repository);

        var cotacao = new CotacaoBruta("USD", "BRL", "n", "1", "1", "0", "0", "5", "5", "1", "d");
        var resposta = new AgregadoResponse(
                new Resultado.Sucesso<>(cotacao),
                new Resultado.Falha<>("erro no provedor", 500),
                new Resultado.Timeout<>("demorou demais"));

        historicoService.registrar(resposta, "USD-BRL", "01310930", 2026, 321);

        var captor = ArgumentCaptor.forClass(HistoricoConsulta.class);
        verify(repository).save(captor.capture());

        var salvo = captor.getValue();
        assertThat(salvo.getPar()).isEqualTo("USD-BRL");
        assertThat(salvo.getCep()).isEqualTo("01310930");
        assertThat(salvo.getAno()).isEqualTo(2026);
        assertThat(salvo.getCotacaoStatus()).isEqualTo(StatusConsulta.SUCESSO);
        assertThat(salvo.getCepStatus()).isEqualTo(StatusConsulta.FALHA);
        assertThat(salvo.getFeriadosStatus()).isEqualTo(StatusConsulta.TIMEOUT);
        assertThat(salvo.getDuracaoMs()).isEqualTo(321);
    }

    @Test
    void calculaMediaMovelDaDuracaoComJanelaDeslizante() {
        var historicoService = new HistoricoService(repository);

        var c1 = consultaComDuracao(100);
        var c2 = consultaComDuracao(200);
        var c3 = consultaComDuracao(300);
        var c4 = consultaComDuracao(400);

        // repository devolve do mais recente para o mais antigo (c4..c1);
        // calcularTendencia usa .reversed() para virar ordem cronologica.
        when(repository.findAllByOrderByCriadoEmDesc(Pageable.unpaged()))
                .thenReturn(List.of(c4, c3, c2, c1));

        var tendencia = historicoService.calcularTendencia(2);

        assertThat(tendencia).extracting(PontoTendencia::duracaoMediaMs)
                .containsExactly(150.0, 250.0, 350.0);
    }

    private static HistoricoConsulta consultaComDuracao(long duracaoMs) {
        return new HistoricoConsulta("USD-BRL", "01310930", 2026,
                StatusConsulta.SUCESSO, StatusConsulta.SUCESSO, StatusConsulta.SUCESSO, duracaoMs);
    }
}
