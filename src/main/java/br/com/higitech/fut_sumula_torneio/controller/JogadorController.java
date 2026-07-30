package br.com.higitech.fut_sumula_torneio.controller;

import java.io.IOException;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
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
import br.com.higitech.fut_sumula_torneio.repository.TimeRepository;

@RestController
@RequestMapping("/api/jogadores")
public class JogadorController {

    @Autowired
    private JogadorRepository repository; 

    @Autowired
    private TimeRepository timeRepository;

    @Autowired
    private Cloudinary cloudinary;

    // LISTAR (PROTEGIDO)
    @GetMapping
    public ResponseEntity<List<Jogador>> listar() {
        Usuario usuarioLogado = (Usuario) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        return ResponseEntity.ok(repository.findAllByOrganizador(usuarioLogado));
    }

    // BUSCAR POR ID (PROTEGIDO)
    @GetMapping("/{id}")
    public ResponseEntity<Jogador> buscarPorId(@PathVariable Long id) {
        Usuario usuarioLogado = (Usuario) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        
        return repository.findById(id)
                .filter(j -> j.getOrganizador().getId().equals(usuarioLogado.getId())) 
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    // CADASTRAR (COM VÍNCULO AO DONO E PROTEÇÃO DE UPLOAD)
    @PostMapping("/cadastrar")
    public ResponseEntity<?> cadastrar(
            @RequestParam("nome") String nome,
            @RequestParam("numero") String numero,
            @RequestParam(value = "posicao", defaultValue = "LINHA") String posicao,
            @RequestParam(value = "nivel", defaultValue = "3") Integer nivel,
            @RequestParam(value = "whatsapp", required = false) String whatsapp,
            @RequestParam(value = "timeId", required = false) Long timeId,
            @RequestParam(value = "foto", required = false) MultipartFile foto) {

        try {
            Usuario usuarioLogado = (Usuario) SecurityContextHolder.getContext().getAuthentication().getPrincipal();

            // Valida duplicidade segura
            if(repository.existsByNomeAndNumeroCamisaAndOrganizador(nome, numero, usuarioLogado)) {
                return ResponseEntity.status(HttpStatus.CONFLICT).body("Jogador já existe no seu plantel.");
            }

            // Limite de segurança 
            long totalJogadores = repository.countByOrganizador(usuarioLogado); 
            if (totalJogadores >= 330) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN).body("Limite de jogadores atingido.");
            }

            Jogador j = new Jogador();
            j.setNome(nome);
            j.setNumeroCamisa(numero);
            j.setPosicao(posicao);
            j.setNivelTecnico(nivel);
            j.setWhatsapp(whatsapp);
            
            // VINCULA AO DONO
            j.setOrganizador(usuarioLogado);

            // Vincula ao Time se informado
            if(timeId != null) {
                Time time = timeRepository.findById(timeId).orElse(null);
                if(time != null && time.getOrganizador().getId().equals(usuarioLogado.getId())) {
                    j.setTime(time);
                }
            }

            // --- TRAVA DE SEGURANÇA 1: INSPEÇÃO DE ARQUIVO ---
            if (foto != null && !foto.isEmpty()) {
                String contentType = foto.getContentType();
                
                if (contentType == null || !contentType.startsWith("image/")) {
                    return ResponseEntity.status(HttpStatus.UNSUPPORTED_MEDIA_TYPE).body("Apenas imagens são permitidas.");
                }
                
                if (foto.getSize() > 2097152) { // 2 MB
                    return ResponseEntity.status(HttpStatus.PAYLOAD_TOO_LARGE).body("A imagem excede o tamanho máximo de 2MB.");
                }

                Map uploadResult = cloudinary.uploader().upload(foto.getBytes(), ObjectUtils.emptyMap());
                j.setFotoUrl(uploadResult.get("url").toString());
            }

            return ResponseEntity.ok(repository.save(j));

        } catch (IOException e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Erro no upload da foto.");
        } catch (Exception e) {
            e.printStackTrace();
            // --- TRAVA DE SEGURANÇA 4: OCULTANDO O MAPA DO BANCO DE DADOS ---
            return ResponseEntity.badRequest().body("Erro interno ao processar a requisição.");
        }
    }

    // ATUALIZAR (REFATORADO PARA SUPORTAR ERROS DE UPLOAD)
    @PutMapping("/{id}")
    public ResponseEntity<?> atualizar(@PathVariable Long id, 
                                     @RequestParam(value="nome", required=false) String nome,
                                     @RequestParam(value="numero", required=false) String numero,
                                     @RequestParam(value="posicao", required=false) String posicao,
                                     @RequestParam(value="nivel", required=false) Integer nivel,
                                     @RequestParam(value="whatsapp", required=false) String whatsapp,
                                     @RequestParam(value="foto", required=false) MultipartFile foto) {
        
        Usuario usuarioLogado = (Usuario) SecurityContextHolder.getContext().getAuthentication().getPrincipal();

        Jogador j = repository.findById(id).orElse(null);
        if (j == null) {
            return ResponseEntity.notFound().build();
        }

        if(!j.getOrganizador().getId().equals(usuarioLogado.getId())) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body("Acesso negado.");
        }

        if(nome != null) j.setNome(nome);
        if(numero != null) j.setNumeroCamisa(numero);
        if(posicao != null) j.setPosicao(posicao);
        if(nivel != null) j.setNivelTecnico(nivel);
        if(whatsapp != null) j.setWhatsapp(whatsapp);

        // --- TRAVA DE SEGURANÇA 1: INSPEÇÃO DE ARQUIVO ---
        if (foto != null && !foto.isEmpty()) {
            String contentType = foto.getContentType();
            
            if (contentType == null || !contentType.startsWith("image/")) {
                return ResponseEntity.status(HttpStatus.UNSUPPORTED_MEDIA_TYPE).body("Apenas imagens são permitidas.");
            }
            
            if (foto.getSize() > 2097152) { // 2 MB
                return ResponseEntity.status(HttpStatus.PAYLOAD_TOO_LARGE).body("A imagem excede o tamanho máximo de 2MB.");
            }

            try {
                Map uploadResult = cloudinary.uploader().upload(foto.getBytes(), ObjectUtils.emptyMap());
                j.setFotoUrl(uploadResult.get("url").toString());
            } catch (IOException e) {
                e.printStackTrace();
                return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Erro no upload da foto.");
            }
        }
        
        repository.save(j);
        return ResponseEntity.ok(j);
    }

    // EXCLUIR
    @DeleteMapping("/{id}")
    public ResponseEntity<?> excluir(@PathVariable Long id) {
        Usuario usuarioLogado = (Usuario) SecurityContextHolder.getContext().getAuthentication().getPrincipal();

        return repository.findById(id).map(j -> {
            if(!j.getOrganizador().getId().equals(usuarioLogado.getId())) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
            }
            repository.delete(j);
            return ResponseEntity.ok().build();
        }).orElse(ResponseEntity.notFound().build());
    }
}