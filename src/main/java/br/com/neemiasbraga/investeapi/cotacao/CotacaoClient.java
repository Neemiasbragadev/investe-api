package br.com.neemiasbraga.investeapi.cotacao;

import br.com.neemiasbraga.investeapi.core.Resultado;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.client.JdkClientHttpRequestFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.client.HttpStatusCodeException;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.RestClient;

import java.net.http.HttpClient;
import java.net.http.HttpTimeoutException;
import java.util.Map;

/**
 * Cliente HTTP para a AwesomeAPI de cotacoes (economia.awesomeapi.com.br).
 * Nunca lanca excecao para o chamador: todo desfecho vira um Resultado.
 */
@Component
public class CotacaoClient {

    private final RestClient restClient;

    public CotacaoClient(RestClient.Builder builder, CotacaoApiProperties properties) {
        var httpClient = HttpClient.newBuilder()
                .connectTimeout(properties.connectTimeout())
                .build();
        var requestFactory = new JdkClientHttpRequestFactory(httpClient);
        requestFactory.setReadTimeout(properties.readTimeout());

        this.restClient = builder
                .baseUrl(properties.baseUrl())
                .requestFactory(requestFactory)
                .build();
    }

    public Resultado<CotacaoBruta> buscar(String par) {
        try {
            Map<String, CotacaoBruta> resposta = restClient.get()
                    .uri("/json/last/{par}", par)
                    .retrieve()
                    .body(new ParameterizedTypeReference<Map<String, CotacaoBruta>>() {
                    });

            return resposta.values().stream()
                    .findFirst()
                    .<Resultado<CotacaoBruta>>map(Resultado.Sucesso::new)
                    .orElseGet(() -> new Resultado.Falha<>("Par de moeda nao encontrado: " + par, 404));

        } catch (HttpStatusCodeException e) {
            return new Resultado.Falha<>(e.getMessage(), e.getStatusCode().value());
        } catch (ResourceAccessException e) {
            if (e.getCause() instanceof HttpTimeoutException) {
                return new Resultado.Timeout<>("Timeout ao consultar cotacao de " + par);
            }
            return new Resultado.Falha<>(e.getMessage(), 502);
        }
    }
}
