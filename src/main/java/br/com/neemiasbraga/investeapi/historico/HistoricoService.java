package br.com.neemiasbraga.investeapi.historico;

import br.com.neemiasbraga.investeapi.agregador.AgregadoResponse;
import org.springframework.stereotype.Service;

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
}
