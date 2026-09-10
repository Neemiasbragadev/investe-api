package br.com.neemiasbraga.investeapi.historico;

import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import java.time.Instant;

/**
 * Unica classe do projeto que nao eh record: o Hibernate exige uma classe
 * mutavel com construtor vazio (para proxies e lazy loading), o que um
 * record nao oferece.
 */
@Entity
@Table(name = "consulta_historico")
public class HistoricoConsulta {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String par;
    private String cep;
    private Integer ano;

    @Enumerated(EnumType.STRING)
    private StatusConsulta cotacaoStatus;

    @Enumerated(EnumType.STRING)
    private StatusConsulta cepStatus;

    @Enumerated(EnumType.STRING)
    private StatusConsulta feriadosStatus;

    private long duracaoMs;
    private Instant criadoEm;

    protected HistoricoConsulta() {
    }

    public HistoricoConsulta(String par, String cep, Integer ano,
                              StatusConsulta cotacaoStatus, StatusConsulta cepStatus,
                              StatusConsulta feriadosStatus, long duracaoMs) {
        this.par = par;
        this.cep = cep;
        this.ano = ano;
        this.cotacaoStatus = cotacaoStatus;
        this.cepStatus = cepStatus;
        this.feriadosStatus = feriadosStatus;
        this.duracaoMs = duracaoMs;
        this.criadoEm = Instant.now();
    }

    public Long getId() {
        return id;
    }

    public String getPar() {
        return par;
    }

    public String getCep() {
        return cep;
    }

    public Integer getAno() {
        return ano;
    }

    public StatusConsulta getCotacaoStatus() {
        return cotacaoStatus;
    }

    public StatusConsulta getCepStatus() {
        return cepStatus;
    }

    public StatusConsulta getFeriadosStatus() {
        return feriadosStatus;
    }

    public long getDuracaoMs() {
        return duracaoMs;
    }

    public Instant getCriadoEm() {
        return criadoEm;
    }
}
