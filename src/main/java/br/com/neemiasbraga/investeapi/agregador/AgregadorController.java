package br.com.neemiasbraga.investeapi.agregador;

import br.com.neemiasbraga.investeapi.historico.HistoricoService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.concurrent.TimeUnit;

@RestController
public class AgregadorController {

    private final AgregadorService agregadorService;
    private final HistoricoService historicoService;

    public AgregadorController(AgregadorService agregadorService, HistoricoService historicoService) {
        this.agregadorService = agregadorService;
        this.historicoService = historicoService;
    }

    @GetMapping("/api/agregado")
    public AgregadoResponse buscar(@RequestParam String par, @RequestParam String cep, @RequestParam int ano) {
        long inicio = System.nanoTime();
        AgregadoResponse resposta = agregadorService.buscarParalelo(par, cep, ano);
        long duracaoMs = TimeUnit.NANOSECONDS.toMillis(System.nanoTime() - inicio);

        historicoService.registrar(resposta, par, cep, ano, duracaoMs);

        return resposta;
    }
}
