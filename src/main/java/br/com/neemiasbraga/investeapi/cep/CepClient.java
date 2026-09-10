package br.com.neemiasbraga.investeapi.cep;

import br.com.neemiasbraga.investeapi.core.MetricasFontes;
import br.com.neemiasbraga.investeapi.core.Resultado;
import br.com.neemiasbraga.investeapi.core.StatusConsulta;
import org.springframework.cache.annotation.Cacheable;
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
    private final MetricasFontes metricasFontes;

    public CepClient(RestClient.Builder builder, CepApiProperties properties, MetricasFontes metricasFontes) {
        var httpClient = HttpClient.newBuilder()
                .connectTimeout(properties.connectTimeout())
                .build();
        var requestFactory = new JdkClientHttpRequestFactory(httpClient);
        requestFactory.setReadTimeout(properties.readTimeout());

        this.restClient = builder
                .baseUrl(properties.baseUrl())
                .requestFactory(requestFactory)
                .build();
        this.metricasFontes = metricasFontes;
    }

    /**
     * CEP nao muda, entao o resultado fica em cache por um tempo (ver
     * CacheConfig). "unless" garante que so Sucesso eh cacheado: uma falha
     * ou timeout nao pode "prender" a API reportando erro depois que o
     * provedor externo ja voltou a funcionar.
     */
    @Cacheable(value = "cep", unless = "#result.getClass().getSimpleName() != 'Sucesso'")
    public Resultado<CepBruta> buscar(String cep) {
        Resultado<CepBruta> resultado = executar(cep);
        metricasFontes.registrar("cep", StatusConsulta.de(resultado));
        return resultado;
    }

    private Resultado<CepBruta> executar(String cep) {
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
