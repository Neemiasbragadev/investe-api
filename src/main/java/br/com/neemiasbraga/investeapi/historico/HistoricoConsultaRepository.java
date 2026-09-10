package br.com.neemiasbraga.investeapi.historico;

import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface HistoricoConsultaRepository extends JpaRepository<HistoricoConsulta, Long> {

    List<HistoricoConsulta> findAllByOrderByCriadoEmDesc(Pageable pageable);
}
