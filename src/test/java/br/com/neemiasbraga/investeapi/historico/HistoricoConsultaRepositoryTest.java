package br.com.neemiasbraga.investeapi.historico;

import br.com.neemiasbraga.investeapi.core.StatusConsulta;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.boot.jdbc.test.autoconfigure.AutoConfigureTestDatabase;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.data.domain.PageRequest;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.postgresql.PostgreSQLContainer;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Usa Postgres real via Testcontainers (nao H2) para validar que a
 * migration Flyway roda de verdade e o mapeamento JPA bate com o schema.
 */
@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@Testcontainers
class HistoricoConsultaRepositoryTest {

    @Container
    @ServiceConnection
    static PostgreSQLContainer postgres = new PostgreSQLContainer("postgres:17");

    @Autowired
    private HistoricoConsultaRepository repository;

    @Test
    void salvaEListaHistoricoOrdenadoPorMaisRecente() throws InterruptedException {
        repository.save(new HistoricoConsulta("USD-BRL", "01310930", 2026,
                StatusConsulta.SUCESSO, StatusConsulta.SUCESSO, StatusConsulta.SUCESSO, 120));

        Thread.sleep(5);

        repository.save(new HistoricoConsulta("EUR-BRL", "01310930", 2026,
                StatusConsulta.SUCESSO, StatusConsulta.FALHA, StatusConsulta.SUCESSO, 300));

        var resultado = repository.findAllByOrderByCriadoEmDesc(PageRequest.of(0, 10));

        assertThat(resultado).hasSize(2);
        assertThat(resultado.getFirst().getPar()).isEqualTo("EUR-BRL");
        assertThat(resultado.getLast().getPar()).isEqualTo("USD-BRL");
        assertThat(resultado.getFirst().getCepStatus()).isEqualTo(StatusConsulta.FALHA);
    }
}
