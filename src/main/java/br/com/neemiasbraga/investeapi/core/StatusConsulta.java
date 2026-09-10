package br.com.neemiasbraga.investeapi.core;

/**
 * Desfecho resumido de uma consulta a uma fonte externa, usado tanto pelo
 * historico persistido quanto pelas metricas — conceito compartilhado, por
 * isso vive em core junto com Resultado.
 */
public enum StatusConsulta {
    SUCESSO, FALHA, TIMEOUT;

    /**
     * Converte o Resultado de uma fonte externa no status resumido.
     * Mesmo switch exaustivo com pattern matching usado no
     * CotacaoController (Fase 2), reaproveitado aqui.
     */
    public static StatusConsulta de(Resultado<?> resultado) {
        return switch (resultado) {
            case Resultado.Sucesso<?> s -> SUCESSO;
            case Resultado.Falha<?> f -> FALHA;
            case Resultado.Timeout<?> t -> TIMEOUT;
        };
    }
}
