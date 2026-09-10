package br.com.neemiasbraga.investeapi.historico;

import org.springframework.data.domain.PageRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
public class HistoricoController {

    private final HistoricoConsultaRepository repository;
    private final HistoricoService historicoService;

    public HistoricoController(HistoricoConsultaRepository repository, HistoricoService historicoService) {
        this.repository = repository;
        this.historicoService = historicoService;
    }

    @GetMapping("/api/historico")
    public List<HistoricoConsulta> listar(@RequestParam(defaultValue = "20") int limite) {
        return repository.findAllByOrderByCriadoEmDesc(PageRequest.of(0, limite));
    }

    /**
     * Usa getFirst() (Sequenced Collections, JDK 21+) em vez do
     * lista.get(0) de sempre: funciona em qualquer List, sem depender da
     * implementacao concreta.
     */
    @GetMapping("/api/historico/ultimo")
    public ResponseEntity<HistoricoConsulta> ultimo() {
        List<HistoricoConsulta> recentes = repository.findAllByOrderByCriadoEmDesc(PageRequest.of(0, 1));

        if (recentes.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        }

        return ResponseEntity.ok(recentes.getFirst());
    }

    @GetMapping("/api/historico/tendencia")
    public List<PontoTendencia> tendencia(@RequestParam(defaultValue = "3") int janela) {
        return historicoService.calcularTendencia(janela);
    }
}
