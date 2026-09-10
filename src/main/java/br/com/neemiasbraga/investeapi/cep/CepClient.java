package br.com.neemiasbraga.investeapi.cep;

import br.com.neemiasbraga.investeapi.core.Resultado;
import org.springframework.http.client.JdkClientHttpRequestFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.client.HttpStatusCodeException;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.RestClient;

import java.net.http.HttpClient;
import java.net.http.HttpTimeoutException;

/**
 * Cliente HTTP para a BrasilAPI de CEP (brasilapi.com.br).
 * Nunca lanca excecao para o chamador: todo desfecho vira um Resultado.
 */
@Component
public class CepClient {

    private final RestClient restClient;

    public CepClient(RestClient.Builder builder, CepApiProperties properties) {
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

    public Resultado<CepBruta> buscar(String cep) {
        try {
            CepBruta resposta = restClient.get()
                    .uri("/api/cep/v1/{cep}", cep)
                    .retrieve()
                    .body(CepBruta.class);

            return new Resultado.Sucesso<>(resposta);

        } catch (HttpStatusCodeException e) {
            return new Resultado.Falha<>(e.getMessage(), e.getStatusCode().value());
        } catch (ResourceAccessException e) {
            if (e.getCause() instanceof HttpTimeoutException) {
                return new Resultado.Timeout<>("Timeout ao consultar CEP " + cep);
            }
            return new Resultado.Falha<>(e.getMessage(), 502);
        }
    }
}
