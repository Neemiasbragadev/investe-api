CREATE TABLE consulta_historico (
    id BIGSERIAL PRIMARY KEY,
    par VARCHAR(20) NOT NULL,
    cep VARCHAR(10) NOT NULL,
    ano INTEGER NOT NULL,
    cotacao_status VARCHAR(20) NOT NULL,
    cep_status VARCHAR(20) NOT NULL,
    feriados_status VARCHAR(20) NOT NULL,
    duracao_ms BIGINT NOT NULL,
    criado_em TIMESTAMP NOT NULL DEFAULT now()
);
