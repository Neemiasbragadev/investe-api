package br.com.neemiasbraga.investeapi.historico;

import br.com.neemiasbraga.investeapi.agregador.AgregadoResponse;
import br.com.neemiasbraga.investeapi.core.StatusConsulta;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Gatherers;

@Service
public class HistoricoService {

    private final HistoricoConsultaRepository repository;

    public HistoricoService(HistoricoConsultaRepository repository) {
        this.repository = repository;
    }

    public void registrar(AgregadoResponse resposta, String par, String cep, int ano, long duracaoMs) {
        var historico = new HistoricoConsulta(
                par, cep, ano,
                StatusConsulta.de(resposta.cotacao()),
                StatusConsulta.de(resposta.cep()),
                StatusConsulta.de(resposta.feriados()),
                duracaoMs);

        repository.save(historico);
    }

    /**
     * Media movel da duracao das consultas, usando Stream Gatherers
     * (JEP 485, estavel desde o JDK 24) para agrupar em janelas
     * deslizantes. O jeito antigo exigiria um laco manual com uma Deque
     * fazendo o buffer da janela na mao.
     */
    public List<PontoTendencia> calcularTendencia(int tamanhoJanela) {
        List<HistoricoConsulta> emOrdemCronologica =
                repository.findAllByOrderByCriadoEmDesc(Pageable.unpaged()).reversed();

        return emOrdemCronologica.stream()
                .map(HistoricoConsulta::getDuracaoMs)
                .gather(Gatherers.windowSliding(tamanhoJanela))
                .map(janela -> new PontoTendencia(janela.stream().mapToLong(Long::longValue).average().orElse(0)))
                .toList();
    }
}
