package br.com.higitech.fut_sumula_torneio.controller;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import br.com.higitech.fut_sumula_torneio.dto.EstatisticaJogadorDTO;
import br.com.higitech.fut_sumula_torneio.model.EventoPartida.TipoEvento;
import br.com.higitech.fut_sumula_torneio.repository.EventoPartidaRepository;

@RestController
@RequestMapping("/api/estatisticas")
public class EstatisticaController {

    @Autowired
    private EventoPartidaRepository eventoRepo;

    @GetMapping("/torneio/{id}/gols")
    public List<EstatisticaJogadorDTO> getArtilharia(@PathVariable Long id) {
        return eventoRepo.findRankingByTorneioAndTipo(id, TipoEvento.GOAL);
    }

    @GetMapping("/torneio/{id}/amarelos")
    public List<EstatisticaJogadorDTO> getAmarelos(@PathVariable Long id) {
        return eventoRepo.findRankingByTorneioAndTipo(id, TipoEvento.YELLOW);
    }

    @GetMapping("/torneio/{id}/vermelhos")
    public List<EstatisticaJogadorDTO> getVermelhos(@PathVariable Long id) {
        return eventoRepo.findRankingByTorneioAndTipo(id, TipoEvento.RED);
    }
    
    // --- NOVO: ENDPOINT PARA BUSCAR AS FALTAS ---
    @GetMapping("/torneio/{id}/faltas")
    public List<EstatisticaJogadorDTO> getFaltas(@PathVariable Long id) {
        return eventoRepo.findRankingByTorneioAndTipo(id, TipoEvento.FOUL);
    }
}