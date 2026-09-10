package br.com.neemiasbraga.investeapi.cotacao;

import br.com.neemiasbraga.investeapi.core.Resultado;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ProblemDetail;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class CotacaoController {

    private final CotacaoClient cotacaoClient;

    public CotacaoController(CotacaoClient cotacaoClient) {
        this.cotacaoClient = cotacaoClient;
    }

    @GetMapping("/api/cotacoes/{par}")
    public ResponseEntity<?> buscar(@PathVariable String par) {
        Resultado<CotacaoBruta> resultado = cotacaoClient.buscar(par);

        return switch (resultado) {
            case Resultado.Sucesso<CotacaoBruta> sucesso -> ResponseEntity.ok(sucesso.dado());
            case Resultado.Falha<CotacaoBruta> falha -> {
                var status = HttpStatusCode.valueOf(falha.statusCode());
                yield ResponseEntity.status(status).body(ProblemDetail.forStatusAndDetail(status, falha.mensagem()));
            }
            case Resultado.Timeout<CotacaoBruta> timeout -> ResponseEntity.status(HttpStatus.GATEWAY_TIMEOUT)
                    .body(ProblemDetail.forStatusAndDetail(HttpStatus.GATEWAY_TIMEOUT, timeout.mensagem()));
        };
    }
}
