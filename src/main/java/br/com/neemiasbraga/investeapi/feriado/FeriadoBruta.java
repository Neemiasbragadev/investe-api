package br.com.neemiasbraga.investeapi.feriado;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

/**
 * Espelha um item do array devolvido pela BrasilAPI de feriados,
 * ex.: GET /api/feriados/v1/2026.
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public record FeriadoBruta(String date, String name, String type) {
}
