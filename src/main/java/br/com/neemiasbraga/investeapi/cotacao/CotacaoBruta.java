package br.com.neemiasbraga.investeapi.cotacao;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Espelha exatamente o JSON devolvido pela AwesomeAPI para um par de moeda,
 * ex.: GET /json/last/USD-BRL -> {"USDBRL": { ...estes campos... }}.
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public record CotacaoBruta(
        String code,
        String codein,
        String name,
        String high,
        String low,
        String varBid,
        String pctChange,
        String bid,
        String ask,
        String timestamp,
        @JsonProperty("create_date") String createDate) {
}
