package br.com.neemiasbraga.investeapi.agregador;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class AgregadorController {

    private final AgregadorService agregadorService;

    public AgregadorController(AgregadorService agregadorService) {
        this.agregadorService = agregadorService;
    }

    @GetMapping("/api/agregado")
    public AgregadoResponse buscar(@RequestParam String par, @RequestParam String cep, @RequestParam int ano) {
        return agregadorService.buscarParalelo(par, cep, ano);
    }
}
