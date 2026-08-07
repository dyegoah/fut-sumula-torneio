package br.com.higitech.fut_sumula_torneio.controller;

import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import br.com.higitech.fut_sumula_torneio.dto.FinalizarPartidaDTO;
import br.com.higitech.fut_sumula_torneio.model.EventoPartida;
import br.com.higitech.fut_sumula_torneio.model.Partida;
import br.com.higitech.fut_sumula_torneio.model.Time;
import br.com.higitech.fut_sumula_torneio.model.Torneio;
import br.com.higitech.fut_sumula_torneio.model.Usuario;
import br.com.higitech.fut_sumula_torneio.repository.EventoPartidaRepository;
import br.com.higitech.fut_sumula_torneio.repository.JogadorRepository;
import br.com.higitech.fut_sumula_torneio.repository.PartidaRepository;
import br.com.higitech.fut_sumula_torneio.repository.TimeRepository;
import br.com.higitech.fut_sumula_torneio.repository.TorneioRepository;
import br.com.higitech.fut_sumula_torneio.service.TorneioService;


@RestController
@RequestMapping("/api/partidas")
public class PartidaController {

    @Autowired private PartidaRepository partidaRepo;
    @Autowired private EventoPartidaRepository eventoRepo;
    @Autowired private JogadorRepository jogadorRepo;
    @Autowired private TimeRepository timeRepo;
    @Autowired private TorneioRepository torneioRepo;
    @Autowired private TorneioService torneioService;

    // --- GERA A TABELA DO TORNEIO (BLINDADO CONTRA IDOR) ---
    @PostMapping("/torneio/{torneioId}/gerar")
    @Transactional
    public ResponseEntity<?> gerarPartidas(@PathVariable Long torneioId) {
        Usuario usuarioLogado = (Usuario) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        Torneio t = torneioRepo.findById(torneioId).orElse(null);
        
        if (t == null) {
            return ResponseEntity.notFound().build();
        }

        if (!t.getOrganizador().getId().equals(usuarioLogado.getId())) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body("Acesso negado: Você não é o dono deste torneio.");
        }

        List<Partida> partidas = torneioService.gerarTabela(t);
        
        if("MATA_MATA".equalsIgnoreCase(t.getTipo())) {
            boolean changed = false;
            for(Partida p : partidas) {
                if(p.getTempIndexOrigemCasa() != null) {
                    p.setPartidaOrigemCasaId(partidas.get(p.getTempIndexOrigemCasa()).getId());
                    changed = true;
                }
                if(p.getTempIndexOrigemVisitante() != null) {
                    p.setPartidaOrigemVisitanteId(partidas.get(p.getTempIndexOrigemVisitante()).getId());
                    changed = true;
                }
            }
            if(changed) partidaRepo.saveAll(partidas);
        }
        return ResponseEntity.ok(partidas);
    }

    // --- LEITURA PRIVADA (Painel do Organizador com Trava IDOR) ---
    @GetMapping("/{id}")
    public ResponseEntity<?> getPartida(@PathVariable Long id) {
        Usuario usuarioLogado = (Usuario) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        
        return partidaRepo.findById(id).map(partida -> {
            if(!partida.getTorneio().getOrganizador().getId().equals(usuarioLogado.getId())) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN).body("Acesso negado.");
            }
            return ResponseEntity.ok(partida);
        }).orElse(ResponseEntity.notFound().build());
    }

    // --- FINALIZA E AVANÇA O VENCEDOR (BLINDADO CONTRA IDOR E HACKERS) ---
    @PostMapping("/finalizar")
    @Transactional
    public ResponseEntity<?> finalizar(@RequestBody FinalizarPartidaDTO dto) {
        try {
            Usuario usuarioLogado = (Usuario) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
            Partida p = partidaRepo.findById(dto.getPartidaId()).orElse(null);

            if (p == null) {
                return ResponseEntity.notFound().build();
            }

            if (!p.getTorneio().getOrganizador().getId().equals(usuarioLogado.getId())) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN).body("Acesso negado: Essa partida pertence a outro organizador.");
            }

            p.setPlacarCasa(dto.getScoreHome());
            p.setPlacarVisitante(dto.getScoreAway());
            p.setSumulaHtml(dto.getSummaryHTML());
            p.setFinalizada(true);
            partidaRepo.save(p);

            Time vencedor = null;
            if (p.getPlacarCasa() > p.getPlacarVisitante()) vencedor = p.getTimeCasa();
            else if (p.getPlacarVisitante() > p.getPlacarCasa()) vencedor = p.getTimeVisitante();

            if (vencedor != null) {
                for (Partida prox : partidaRepo.findByPartidaOrigemCasaId(p.getId())) {
                    prox.setTimeCasa(vencedor); partidaRepo.save(prox);
                }
                for (Partida prox : partidaRepo.findByPartidaOrigemVisitanteId(p.getId())) {
                    prox.setTimeVisitante(vencedor); partidaRepo.save(prox);
                }
            }
            
            if (dto.getEventos() != null) {
                for (FinalizarPartidaDTO.EventoDTO evDto : dto.getEventos()) {
                    EventoPartida ev = new EventoPartida();
                    ev.setPartida(p);
                    if(evDto.getType() != null) ev.setTipo(EventoPartida.TipoEvento.valueOf(evDto.getType().toUpperCase()));
                    ev.setMinuto(evDto.getTime());
                    if(evDto.getPlayerId() != null) ev.setJogador(jogadorRepo.findById(evDto.getPlayerId()).orElse(null));
                    if(evDto.getTeamId() != null) ev.setTime(timeRepo.findById(evDto.getTeamId()).orElse(null));
                    eventoRepo.save(ev);
                }
            }
            return ResponseEntity.ok("Finalizado!");
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.internalServerError().body("Erro: " + e.getMessage());
        }
    }
    
    @PostMapping("/{id}/definir-confronto")
    public ResponseEntity<?> definirConfronto(@PathVariable Long id, @RequestBody Map<String, Long> payload) {
        return ResponseEntity.ok().build();
    }
    
    // --- LEITURA PÚBLICA BLINDADA (Compartilhamento para os Fãs) ---
    @GetMapping("/publico/{codigo}")
    public ResponseEntity<?> getPartidaPublica(@PathVariable String codigo) {
        return partidaRepo.findByCodigoPublico(codigo).map(partida -> {
            if (partida.getTorneio() != null) {
                partida.getTorneio().setOrganizador(null); 
            }
            return ResponseEntity.ok(partida);
        }).orElse(ResponseEntity.notFound().build());
    }
}