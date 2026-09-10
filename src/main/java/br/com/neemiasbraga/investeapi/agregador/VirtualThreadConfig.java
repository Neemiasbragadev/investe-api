package br.com.neemiasbraga.investeapi.agregador;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

@Configuration
public class VirtualThreadConfig {

    /**
     * Uma virtual thread nova por tarefa submetida. Diferente do pool de
     * threads de plataforma do Tomcat (spring.threads.virtual.enabled trata
     * das requisicoes HTTP recebidas): este executor eh usado explicitamente
     * pelo AgregadorService para paralelizar as chamadas de SAIDA de uma
     * unica requisicao.
     */
    @Bean
    public ExecutorService virtualThreadExecutor() {
        return Executors.newVirtualThreadPerTaskExecutor();
    }
}
