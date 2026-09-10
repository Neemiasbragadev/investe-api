package br.com.neemiasbraga.investeapi.agregador;

import br.com.neemiasbraga.investeapi.cep.CepBruta;
import br.com.neemiasbraga.investeapi.core.Resultado;
import br.com.neemiasbraga.investeapi.cotacao.CotacaoBruta;
import br.com.neemiasbraga.investeapi.feriado.FeriadoBruta;

import java.util.List;

public record AgregadoResponse(
        Resultado<CotacaoBruta> cotacao,
        Resultado<CepBruta> cep,
        Resultado<List<FeriadoBruta>> feriados) {
}
