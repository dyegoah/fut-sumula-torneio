package br.com.higitech.fut_sumula_torneio.controller;

import java.io.IOException;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.CrossOrigin;
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
import br.com.higitech.fut_sumula_torneio.model.Usuario; // IMPORTANTE: Sua classe de Usuário
import br.com.higitech.fut_sumula_torneio.repository.JogadorRepository;
import br.com.higitech.fut_sumula_torneio.repository.PartidaRepository;
import br.com.higitech.fut_sumula_torneio.repository.TimeRepository;

@RestController
@RequestMapping("/api/times")
@CrossOrigin("*") // O CORS global já cuida disso, mas pode deixar por garantia
public class TimeController {

    @Autowired private TimeRepository repository;
    @Autowired private JogadorRepository jogadorRepository;
    @Autowired private PartidaRepository partidaRepository;
    @Autowired private Cloudinary cloudinary;

    // --- LISTAR (AGORA É MULTI-TENANT) ---
    @GetMapping
    public ResponseEntity<List<Time>> listar() {
        try {
            // 1. Pega o usuário do Token JWT
            Usuario usuarioLogado = (Usuario) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
            
            // 2. Busca APENAS os times desse usuário
            // ATENÇÃO: Você precisa ter o método findAllByOrganizador no TimeRepository
            List<Time> times = repository.findAllByOrganizador(usuarioLogado);
            
            return ResponseEntity.ok(times);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
    }

    // --- CRIAR (MULTI-TENANT + UPLOAD) ---
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
            // 1. Identifica quem está criando
            Usuario usuarioLogado = (Usuario) SecurityContextHolder.getContext().getAuthentication().getPrincipal();

            // 2. Verifica duplicidade SÓ para esse usuário
            if (repository.existsByNomeIgnoreCaseAndOrganizador(nome, usuarioLogado)) {
                return ResponseEntity.status(HttpStatus.CONFLICT).body("Você já tem um clube com este nome.");
            }

            Time time = new Time();
            time.setNome(nome);
            time.setCorPrimaria(corPrimaria);
            time.setCorSecundaria(corSecundaria);
            time.setCorTerciaria(corTerciaria);
            time.setTipoEscudo(tipoEscudo);
            
            // 3. VINCULA O TIME AO DONO (ISOLAMENTO DE DADOS)
            time.setOrganizador(usuarioLogado);

            // 4. Lógica de Imagem
            if ("UPLOAD".equals(tipoEscudo) && escudo != null && !escudo.isEmpty()) {
                Map uploadResult = cloudinary.uploader().upload(escudo.getBytes(), ObjectUtils.emptyMap());
                time.setEscudoUrl(uploadResult.get("url").toString());
            } else {
                time.setCustomShape(customShape);
                time.setCustomMascot(customMascot);
            }

            return ResponseEntity.ok(repository.save(time));
            
        } catch (IOException e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Erro ao processar imagem.");
        } catch (Exception e) {
             e.printStackTrace();
             return ResponseEntity.badRequest().body("Erro ao salvar time: " + e.getMessage());
        }
    }

    @PutMapping("/{id}")
    public ResponseEntity<?> editar(@PathVariable Long id, @RequestBody Time timeAtualizado) {
        // Idealmente, usar findByIdAndOrganizador para evitar editar time alheio via ID na URL
        return repository.findById(id).map(time -> {
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
        // Pega o usuário para garantir que ele só exclua o que é dele
        Usuario usuarioLogado = (Usuario) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        
        // Verifica se o time existe E pertence ao usuário
        Time t = repository.findById(id).orElse(null);
        
        if (t == null) return ResponseEntity.notFound().build();
        
        // Verifica se o time pertence ao usuário logado (Segurança Extra)
        if (!t.getOrganizador().getId().equals(usuarioLogado.getId())) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body("Você não tem permissão para excluir este time.");
        }

        // Verifica se tem jogos
        if (partidaRepository.existsByTimeCasaIdOrTimeVisitanteId(id, id)) {
            return ResponseEntity.status(HttpStatus.CONFLICT).body("Não é possível excluir: Clube em competição.");
        }

        // Limpa jogadores
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
    public ResponseEntity<Time> adicionarJogadores(@PathVariable Long timeId, @RequestBody List<Long> jogadoresIds) {
        return repository.findById(timeId).map(time -> {
            List<Jogador> jogadores = jogadorRepository.findAllById(jogadoresIds);
            for (Jogador jogador : jogadores) {
                jogador.setTime(time);
            }
            jogadorRepository.saveAll(jogadores);
            return ResponseEntity.ok(repository.findById(timeId).get());
        }).orElse(ResponseEntity.notFound().build());
    }

    @DeleteMapping("/{timeId}/remover-jogador/{jogadorId}")
    public ResponseEntity<Void> removerJogador(@PathVariable Long timeId, @PathVariable Long jogadorId) {
        return jogadorRepository.findById(jogadorId).map(jogador -> {
            if (jogador.getTime() != null && jogador.getTime().getId().equals(timeId)) {
                jogador.setTime(null);
                jogadorRepository.save(jogador);
                return ResponseEntity.noContent().<Void>build();
            }
            return ResponseEntity.badRequest().<Void>build();
        }).orElse(ResponseEntity.notFound().build());
    }
}