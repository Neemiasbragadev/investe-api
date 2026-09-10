package br.com.neemiasbraga.investeapi.core;

/**
 * Modela o desfecho de uma consulta a uma fonte externa: ela deu certo,
 * falhou com um erro do provedor, ou estourou o tempo limite. Usado por
 * todo client de fonte externa (cotacao, CEP, feriados, ...).
 */
public sealed interface Resultado<T> permits Resultado.Sucesso, Resultado.Falha, Resultado.Timeout {

    record Sucesso<T>(T dado) implements Resultado<T> {
    }

    record Falha<T>(String mensagem, int statusCode) implements Resultado<T> {
    }

    record Timeout<T>(String mensagem) implements Resultado<T> {
    }
}
