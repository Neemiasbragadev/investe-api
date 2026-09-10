package br.com.neemiasbraga.investeapi.agregador;

import br.com.neemiasbraga.investeapi.cep.CepClient;
import br.com.neemiasbraga.investeapi.cotacao.CotacaoClient;
import br.com.neemiasbraga.investeapi.feriado.FeriadoClient;
import org.springframework.stereotype.Service;

import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;

@Service
public class AgregadorService {

    private final CotacaoClient cotacaoClient;
    private final CepClient cepClient;
    private final FeriadoClient feriadoClient;
    private final ExecutorService virtualThreadExecutor;

    public AgregadorService(CotacaoClient cotacaoClient, CepClient cepClient,
                             FeriadoClient feriadoClient, ExecutorService virtualThreadExecutor) {
        this.cotacaoClient = cotacaoClient;
        this.cepClient = cepClient;
        this.feriadoClient = feriadoClient;
        this.virtualThreadExecutor = virtualThreadExecutor;
    }

    /**
     * Dispara as 3 chamadas ao mesmo tempo, cada uma numa virtual thread.
     * O tempo total fica perto do MAIOR tempo individual, nao da soma.
     */
    public AgregadoResponse buscarParalelo(String par, String cep, int ano) {
        Future<?> cotacaoFuture = virtualThreadExecutor.submit(() -> cotacaoClient.buscar(par));
        Future<?> cepFuture = virtualThreadExecutor.submit(() -> cepClient.buscar(cep));
        Future<?> feriadosFuture = virtualThreadExecutor.submit(() -> feriadoClient.buscar(ano));

        return new AgregadoResponse(aguardar(cotacaoFuture), aguardar(cepFuture), aguardar(feriadosFuture));
    }

    /**
     * Chama as 3 fontes uma atras da outra. O tempo total eh a SOMA dos
     * tres tempos individuais. Existe so para comparacao com buscarParalelo.
     */
    public AgregadoResponse buscarSequencial(String par, String cep, int ano) {
        var cotacao = cotacaoClient.buscar(par);
        var cepResultado = cepClient.buscar(cep);
        var feriados = feriadoClient.buscar(ano);

        return new AgregadoResponse(cotacao, cepResultado, feriados);
    }

    @SuppressWarnings("unchecked")
    private <T> T aguardar(Future<?> future) {
        try {
            return (T) future.get();
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new IllegalStateException("Interrompido esperando resultado da fonte externa", e);
        } catch (ExecutionException e) {
            throw new IllegalStateException("Erro inesperado ao buscar dado", e.getCause());
        }
    }
}
