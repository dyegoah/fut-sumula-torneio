package br.com.higitech.fut_sumula_torneio.controller;

import java.io.IOException;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import com.cloudinary.Cloudinary;
import com.cloudinary.utils.ObjectUtils;

import br.com.higitech.fut_sumula_torneio.model.Jogador;
import br.com.higitech.fut_sumula_torneio.model.Time;
import br.com.higitech.fut_sumula_torneio.model.Usuario;
import br.com.higitech.fut_sumula_torneio.repository.JogadorRepository;
import br.com.higitech.fut_sumula_torneio.repository.PartidaRepository;
import br.com.higitech.fut_sumula_torneio.repository.TimeRepository;

@RestController
@RequestMapping("/api/times")
public class TimeController {

    @Autowired private TimeRepository repository;
    @Autowired private JogadorRepository jogadorRepository;
    @Autowired private PartidaRepository partidaRepository;
    @Autowired private Cloudinary cloudinary;

    @GetMapping
    public ResponseEntity<List<Time>> listar() {
        try {
            Usuario usuarioLogado = (Usuario) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
            List<Time> times = repository.findAllByOrganizador(usuarioLogado);
            return ResponseEntity.ok(times);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
    }

    @PostMapping("/criar")
    public ResponseEntity<?> criar(
            @RequestParam("nome") String nome,
            @RequestParam("cor1") String corPrimaria,
            @RequestParam("cor2") String corSecundaria,
            @RequestParam("cor3") String corTerciaria,
            @RequestParam("tipoEscudo") String tipoEscudo,
            @RequestParam(value = "customShape", required = false) String customShape,
            @RequestParam(value = "customMascot", required = false) String customMascot,
            @RequestParam(value = "escudoFile", required = false) MultipartFile escudo) {

        try {
            Usuario usuarioLogado = (Usuario) SecurityContextHolder.getContext().getAuthentication().getPrincipal();

            if (repository.existsByNomeIgnoreCaseAndOrganizador(nome, usuarioLogado)) {
                return ResponseEntity.status(HttpStatus.CONFLICT).body("Você já tem um clube com este nome.");
            }

            Time time = new Time();
            time.setNome(nome);
            time.setCorPrimaria(corPrimaria);
            time.setCorSecundaria(corSecundaria);
            time.setCorTerciaria(corTerciaria);
            time.setTipoEscudo(tipoEscudo);
            time.setOrganizador(usuarioLogado);

            // --- TRAVA DE SEGURANÇA 1: INSPEÇÃO DE ARQUIVO ---
            if ("UPLOAD".equals(tipoEscudo) && escudo != null && !escudo.isEmpty()) {
                String contentType = escudo.getContentType();
                
                if (contentType == null || !contentType.startsWith("image/")) {
                    return ResponseEntity.status(HttpStatus.UNSUPPORTED_MEDIA_TYPE).body("Apenas imagens são permitidas.");
                }
                
                if (escudo.getSize() > 2097152) { // 2 MB
                    return ResponseEntity.status(HttpStatus.PAYLOAD_TOO_LARGE).body("A imagem excede o tamanho máximo de 2MB.");
                }

                Map uploadResult = cloudinary.uploader().upload(escudo.getBytes(), ObjectUtils.emptyMap());
                // UTILIZANDO SECURE_URL PARA EVITAR BLOQUEIO MIXED CONTENT NOS CELULARES
                time.setEscudoUrl(uploadResult.get("secure_url").toString());
            } else {
                time.setCustomShape(customShape);
                time.setCustomMascot(customMascot);
            }

            return ResponseEntity.ok(repository.save(time));
            
        } catch (IOException e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Erro ao processar imagem.");
        } catch (Exception e) {
             e.printStackTrace();
             return ResponseEntity.badRequest().body("Erro interno ao processar a requisição.");
        }
    }

    @PutMapping("/{id}")
    public ResponseEntity<?> editar(@PathVariable Long id, @RequestBody Time timeAtualizado) {
        Usuario usuarioLogado = (Usuario) SecurityContextHolder.getContext().getAuthentication().getPrincipal();

        return repository.findById(id).map(time -> {
            if (!time.getOrganizador().getId().equals(usuarioLogado.getId())) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN).body("Acesso negado: Este time não pertence a você.");
            }

            time.setNome(timeAtualizado.getNome());
            time.setCorPrimaria(timeAtualizado.getCorPrimaria());
            time.setCorSecundaria(timeAtualizado.getCorSecundaria());
            if(timeAtualizado.getCustomMascot() != null) time.setCustomMascot(timeAtualizado.getCustomMascot());
            if(timeAtualizado.getCustomShape() != null) time.setCustomShape(timeAtualizado.getCustomShape());
            return ResponseEntity.ok(repository.save(time));
        }).orElse(ResponseEntity.notFound().build());
    }

    @DeleteMapping("/{id}")
    @Transactional
    public ResponseEntity<?> excluir(@PathVariable Long id) {
        Usuario usuarioLogado = (Usuario) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        Time t = repository.findById(id).orElse(null);
        
        if (t == null) return ResponseEntity.notFound().build();
        
        if (!t.getOrganizador().getId().equals(usuarioLogado.getId())) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body("Você não tem permissão para excluir este time.");
        }

        if (partidaRepository.existsByTimeCasaIdOrTimeVisitanteId(id, id)) {
            return ResponseEntity.status(HttpStatus.CONFLICT).body("Não é possível excluir: Clube em competição.");
        }

        if(t.getJogadores() != null) {
            for(Jogador j : t.getJogadores()) {
                j.setTime(null);
                jogadorRepository.save(j);
            }
        }
        
        repository.deleteById(id);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/{timeId}/adicionar-jogadores")
    public ResponseEntity<?> adicionarJogadores(@PathVariable Long timeId, @RequestBody List<Long> jogadoresIds) {
        Usuario usuarioLogado = (Usuario) SecurityContextHolder.getContext().getAuthentication().getPrincipal();

        return repository.findById(timeId).map(time -> {
            if (!time.getOrganizador().getId().equals(usuarioLogado.getId())) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
            }

            List<Jogador> jogadores = jogadorRepository.findAllById(jogadoresIds);
            for (Jogador jogador : jogadores) {
                if (jogador.getOrganizador().getId().equals(usuarioLogado.getId())) {
                    jogador.setTime(time);
                }
            }
            jogadorRepository.saveAll(jogadores);
            return ResponseEntity.ok(repository.findById(timeId).get());
        }).orElse(ResponseEntity.notFound().build());
    }

    @DeleteMapping("/{timeId}/remover-jogador/{jogadorId}")
    public ResponseEntity<?> removerJogador(@PathVariable Long timeId, @PathVariable Long jogadorId) {
        Usuario usuarioLogado = (Usuario) SecurityContextHolder.getContext().getAuthentication().getPrincipal();

        return jogadorRepository.findById(jogadorId).map(jogador -> {
            if (!jogador.getOrganizador().getId().equals(usuarioLogado.getId())) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
            }

            if (jogador.getTime() != null && jogador.getTime().getId().equals(timeId)) {
                jogador.setTime(null);
                jogadorRepository.save(jogador);
                return ResponseEntity.noContent().build();
            }
            return ResponseEntity.badRequest().build();
        }).orElse(ResponseEntity.notFound().build());
    }
}