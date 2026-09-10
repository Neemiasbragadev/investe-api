package br.com.neemiasbraga.investeapi.historico;

import br.com.neemiasbraga.investeapi.core.Resultado;

public enum StatusConsulta {
    SUCESSO, FALHA, TIMEOUT;

    /**
     * Converte o Resultado de uma fonte externa no status que vai pro
     * historico. Mesmo switch exaustivo com pattern matching usado no
     * CotacaoController (Fase 2), aqui reaproveitado pra outro proposito.
     */
    public static StatusConsulta de(Resultado<?> resultado) {
        return switch (resultado) {
            case Resultado.Sucesso<?> s -> SUCESSO;
            case Resultado.Falha<?> f -> FALHA;
            case Resultado.Timeout<?> t -> TIMEOUT;
        };
    }
}
