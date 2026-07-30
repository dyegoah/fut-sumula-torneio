package br.com.higitech.fut_sumula_torneio.repository;

import java.util.List;
import java.util.Optional; // <-- IMPORTAÇÃO NECESSÁRIA

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import br.com.higitech.fut_sumula_torneio.model.Partida;

@Repository
public interface PartidaRepository extends JpaRepository<Partida, Long> {
    
    // Suas buscas que já existiam (se houver alguma aqui, mantenha)
    List<Partida> findByPartidaOrigemCasaId(Long id);
    List<Partida> findByPartidaOrigemVisitanteId(Long id);
    List<Partida> findByTorneioIdOrderByIdAsc(Long id);
    boolean existsByTimeCasaIdOrTimeVisitanteId(Long id1, Long id2);

    // --- A LINHA MÁGICA QUE RESOLVE O SEU ERRO ---
    Optional<Partida> findByCodigoPublico(String codigoPublico);
}