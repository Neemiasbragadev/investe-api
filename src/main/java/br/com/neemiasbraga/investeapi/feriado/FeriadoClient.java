package br.com.neemiasbraga.investeapi.feriado;

import br.com.neemiasbraga.investeapi.core.Resultado;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.client.JdkClientHttpRequestFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.client.HttpStatusCodeException;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.RestClient;

import java.net.http.HttpClient;
import java.net.http.HttpTimeoutException;
import java.util.List;

/**
 * Cliente HTTP para a BrasilAPI de feriados nacionais (brasilapi.com.br).
 * Nunca lanca excecao para o chamador: todo desfecho vira um Resultado.
 */
@Component
public class FeriadoClient {

    private final RestClient restClient;

    public FeriadoClient(RestClient.Builder builder, FeriadoApiProperties properties) {
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

    public Resultado<List<FeriadoBruta>> buscar(int ano) {
        try {
            List<FeriadoBruta> resposta = restClient.get()
                    .uri("/api/feriados/v1/{ano}", ano)
                    .retrieve()
                    .body(new ParameterizedTypeReference<List<FeriadoBruta>>() {
                    });

            return new Resultado.Sucesso<>(resposta);

        } catch (HttpStatusCodeException e) {
            return new Resultado.Falha<>(e.getMessage(), e.getStatusCode().value());
        } catch (ResourceAccessException e) {
            if (e.getCause() instanceof HttpTimeoutException) {
                return new Resultado.Timeout<>("Timeout ao consultar feriados de " + ano);
            }
            return new Resultado.Falha<>(e.getMessage(), 502);
        }
    }
}
