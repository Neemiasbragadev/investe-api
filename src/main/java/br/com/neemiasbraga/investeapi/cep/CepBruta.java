package br.com.neemiasbraga.investeapi.cep;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

/**
 * Espelha o JSON devolvido pela BrasilAPI para um CEP,
 * ex.: GET /api/cep/v1/01310930.
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public record CepBruta(
        String cep,
        String state,
        String city,
        String neighborhood,
        String street,
        String service) {
}
