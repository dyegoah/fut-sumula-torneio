package br.com.higitech.fut_sumula_torneio.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import br.com.higitech.fut_sumula_torneio.model.Partida;

@Repository
public interface PartidaRepository extends JpaRepository<Partida, Long> {
    
    // Busca partidas ordenadas para montar a tabela corretamente
    List<Partida> findByTorneioIdOrderByIdAsc(Long torneioId);
    
    // Método auxiliar (legado)
    List<Partida> findByTorneioId(Long torneioId);

    // --- CORREÇÃO DO ERRO ---
    // O Spring agora encontrará estas propriedades porque elas existem na classe Partida
    List<Partida> findByPartidaOrigemCasaId(Long id);
    List<Partida> findByPartidaOrigemVisitanteId(Long id);

    boolean existsByTimeCasaIdOrTimeVisitanteId(Long timeCasaId, Long timeVisitanteId);
}