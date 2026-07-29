package br.com.higitech.fut_sumula_torneio.controller;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import br.com.higitech.fut_sumula_torneio.dto.CriarTorneioDTO;
import br.com.higitech.fut_sumula_torneio.model.Partida;
import br.com.higitech.fut_sumula_torneio.model.Time;
import br.com.higitech.fut_sumula_torneio.model.Torneio;
import br.com.higitech.fut_sumula_torneio.model.Usuario;
import br.com.higitech.fut_sumula_torneio.repository.PartidaRepository;
import br.com.higitech.fut_sumula_torneio.repository.TimeRepository;
import br.com.higitech.fut_sumula_torneio.repository.TorneioRepository;

@RestController
@RequestMapping("/api/torneios")
@CrossOrigin("*")
public class TorneioController {

    @Autowired private TorneioRepository repository;
    @Autowired private TimeRepository timeRepository;
    @Autowired private PartidaRepository partidaRepository;

    @GetMapping
    public List<Torneio> listar() {
        Usuario usuarioLogado = (Usuario) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        return repository.findAllByOrganizador(usuarioLogado);
    }

    @PostMapping("/criar")
    @Transactional
    public ResponseEntity<?> criar(@RequestBody CriarTorneioDTO dados) {
        Usuario usuarioLogado = (Usuario) SecurityContextHolder.getContext().getAuthentication().getPrincipal();

        // --- VALIDAÇÕES DE REGRAS DE NEGÓCIO ---
        if (dados.getTipo().equalsIgnoreCase("COPA")) {
            if (dados.getQuantidadeTimes() < 6) {
                return ResponseEntity.badRequest().body("Para o formato COPA, é necessário no mínimo 6 times (2 Grupos de 3).");
            }
        }
        
        // NOVA TRAVA: MATA-MATA
        if (dados.getTipo().equalsIgnoreCase("MATA_MATA")) {
            if (dados.getQuantidadeTimes() < 4) {
                return ResponseEntity.badRequest().body("Para o formato MATA-MATA, é necessário no mínimo 4 times.");
            }
        }
        // ---------------------------------------

        Torneio t = new Torneio();
        t.setNome(dados.getNome());
        t.setTipo(dados.getTipo());
        t.setQuantidadeTimes(dados.getQuantidadeTimes());
        t.setIdaEVolta(dados.getIdaEVolta());
        t.setModeloTrofeu(dados.getModeloTrofeu());
        t.setCorTrofeu(dados.getCorTrofeu());
        t.setOrganizador(usuarioLogado);

        if(dados.getTimesIds() != null && !dados.getTimesIds().isEmpty()) {
            List<Time> times = timeRepository.findAllById(dados.getTimesIds());
            
            // TRAVA DE SEGURANÇA: Filtra a lista removendo qualquer time que seja de outro organizador
            times.removeIf(time -> !time.getOrganizador().getId().equals(usuarioLogado.getId()));
            
            t.setTimes(times);
        }

        repository.save(t);
        return ResponseEntity.ok(t);
    }
    
    @DeleteMapping("/{id}")
    @Transactional
    public ResponseEntity<?> excluir(@PathVariable Long id) {
        Usuario usuarioLogado = (Usuario) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        
        return repository.findById(id).map(torneio -> {
            if(!torneio.getOrganizador().getId().equals(usuarioLogado.getId())) {
                return ResponseEntity.status(403).build();
            }
            List<Partida> partidas = partidaRepository.findByTorneioIdOrderByIdAsc(id);
            if(!partidas.isEmpty()) {
                partidaRepository.deleteAll(partidas);
            }
            repository.delete(torneio);
            return ResponseEntity.noContent().build();
        }).orElse(ResponseEntity.notFound().build());
    }
    
 /// 1. ROTA INTERNA (SEGURA): Verifica se o ID solicitado pertence ao Usuário Logado
    @GetMapping("/{id}")
    public ResponseEntity<?> buscarPorId(@PathVariable Long id) { // <-- MUDOU DE <Torneio> PARA <?>
        Usuario usuarioLogado = (Usuario) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        
        return repository.findById(id).map(torneio -> {
            // TRAVA DE PROPRIEDADE: O Torneio é meu?
            if(!torneio.getOrganizador().getId().equals(usuarioLogado.getId())) {
                return ResponseEntity.status(403).build(); // 403 Forbidden: É de outro usuário!
            }
            return ResponseEntity.ok(torneio);
        }).orElse(ResponseEntity.notFound().build());
    }

    // 2. NOVA ROTA PÚBLICA (ANTI-VAZAMENTO): Usa o UUID (codigoPublico) impossível de adivinhar
    @GetMapping("/publico/{codigo}")
    public ResponseEntity<?> buscarParaTorcedor(@PathVariable String codigo) { // <-- MUDOU DE <Torneio> PARA <?>
        return repository.findByCodigoPublico(codigo).map(torneio -> {
            // Oculta completamente quem é o dono por segurança antes de enviar para o torcedor
            torneio.setOrganizador(null); 
            return ResponseEntity.ok(torneio);
        }).orElse(ResponseEntity.notFound().build());
    }
}