package br.com.higitech.fut_sumula_torneio.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import br.com.higitech.fut_sumula_torneio.dto.EstatisticaJogadorDTO;
import br.com.higitech.fut_sumula_torneio.model.EventoPartida;
import br.com.higitech.fut_sumula_torneio.model.EventoPartida.TipoEvento;

@Repository
public interface EventoPartidaRepository extends JpaRepository<EventoPartida, Long> {

    // Esta Query é a mágica que conta os gols/cartões por jogador
    @Query("SELECT new br.com.higitech.fut_sumula_torneio.dto.EstatisticaJogadorDTO(" +
           "e.jogador.id, e.jogador.nome, e.time.nome, e.jogador.fotoUrl, COUNT(e)) " +
           "FROM EventoPartida e " +
           "WHERE e.partida.torneio.id = :torneioId " +
           "AND e.tipo = :tipo " +
           "AND e.jogador IS NOT NULL " + // Evita erros se o jogador foi excluído ou não salvo
           "GROUP BY e.jogador.id, e.jogador.nome, e.time.nome, e.jogador.fotoUrl " +
           "ORDER BY COUNT(e) DESC, e.jogador.nome ASC")
    List<EstatisticaJogadorDTO> findRankingByTorneioAndTipo(@Param("torneioId") Long torneioId, @Param("tipo") TipoEvento tipo);
}