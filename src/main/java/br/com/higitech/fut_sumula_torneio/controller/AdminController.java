package br.com.higitech.fut_sumula_torneio.controller;

import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import br.com.higitech.fut_sumula_torneio.model.TokenRecuperacao;
import br.com.higitech.fut_sumula_torneio.model.Usuario;
import br.com.higitech.fut_sumula_torneio.repository.TokenRecuperacaoRepository;
import br.com.higitech.fut_sumula_torneio.repository.UsuarioRepository;
import jakarta.transaction.Transactional;

@RestController
@RequestMapping("/api/admin")
public class AdminController {

    @Autowired
    private UsuarioRepository usuarioRepository;

    @Autowired
    private TokenRecuperacaoRepository tokenRecuperacaoRepository;

    @GetMapping("/users")
    public ResponseEntity<List<Usuario>> listarUsuarios() {
        return ResponseEntity.ok(usuarioRepository.findAll());
    }

    @PutMapping("/users/{id}/status")
    public ResponseEntity<?> alternarStatus(@PathVariable Long id) {
        return usuarioRepository.findById(id).map(user -> {
            user.setStatus("ATIVO".equals(user.getStatus()) ? "INATIVO" : "ATIVO");
            usuarioRepository.save(user);
            return ResponseEntity.ok().build();
        }).orElse(ResponseEntity.badRequest().build());
    }

    @PutMapping("/users/{id}/premium")
    public ResponseEntity<?> alternarPremium(@PathVariable Long id) {
        return usuarioRepository.findById(id).map(user -> {
            if ("PREMIUM".equals(user.getPlano())) {
                user.setPlano("FREE");
            } else {
                user.setPlano("PREMIUM");
                user.setStatus("ATIVO");
            }
            usuarioRepository.save(user);
            return ResponseEntity.ok().build();
        }).orElse(ResponseEntity.badRequest().build());
    }

    @PutMapping("/users/{id}/cortesia")
    public ResponseEntity<?> gerenciarCortesia(@PathVariable Long id, @RequestBody Map<String, String> dados) {
        return usuarioRepository.findById(id).map(user -> {
            String acao = dados.get("acao");
            if ("CONCEDER".equals(acao)) {
                user.setPlano("CORTESIA");
                user.setNotaCortesia(dados.get("nota"));
                user.setStatus("ATIVO");
            } else {
                user.setPlano("FREE");
                user.setNotaCortesia(null);
            }
            usuarioRepository.save(user);
            return ResponseEntity.ok().build();
        }).orElse(ResponseEntity.badRequest().build());
    }

    @PutMapping("/users/{id}/update")
    public ResponseEntity<?> atualizarUsuario(@PathVariable Long id, @RequestBody Map<String, Object> dados) {
        return usuarioRepository.findById(id).map(user -> {
            if (dados.containsKey("nome")) user.setNome((String) dados.get("nome"));
            if (dados.containsKey("login")) user.setLogin((String) dados.get("login"));
            if (dados.containsKey("cidade")) user.setCidade((String) dados.get("cidade"));
            if (dados.containsKey("uf")) user.setUf((String) dados.get("uf"));
            
            if (dados.containsKey("dataNascimento") && dados.get("dataNascimento") != null && !((String) dados.get("dataNascimento")).isEmpty()) {
                try {
                    user.setDataNascimento(java.time.LocalDate.parse((String) dados.get("dataNascimento")));
                } catch (Exception e) {}
            }

            if (dados.containsKey("trialDays") && dados.get("trialDays") != null) {
                try {
                    user.setTrialDays(Integer.parseInt(dados.get("trialDays").toString()));
                } catch (Exception e) {}
            }

            if (dados.containsKey("novaSenha") && dados.get("novaSenha") != null) {
                String novaSenha = (String) dados.get("novaSenha");
                if (!novaSenha.trim().isEmpty()) {
                    user.setSenha(new BCryptPasswordEncoder().encode(novaSenha));
                }
            }

            usuarioRepository.save(user);
            return ResponseEntity.ok().build();
        }).orElse(ResponseEntity.badRequest().build());
    }

    @Transactional
    @DeleteMapping("/users/{id}")
    public ResponseEntity<?> excluirUsuario(@PathVariable Long id) {
        Usuario user = usuarioRepository.findById(id).orElse(null);
        if (user == null) {
            return ResponseEntity.badRequest().body("Usuário não encontrado.");
        }

        // --- SOLUÇÃO DO ERRO 500: LIMPANDO A CHAVE ESTRANGEIRA ---
        // Apaga os tokens amarrados a este usuário antes de excluí-lo
        try {
            Iterable<TokenRecuperacao> tokens = tokenRecuperacaoRepository.findAll();
            for (TokenRecuperacao t : tokens) {
                if (t.getUsuario() != null && t.getUsuario().getId().equals(id)) {
                    tokenRecuperacaoRepository.delete(t);
                }
            }
        } catch (Exception e) {
            System.out.println("Aviso ao limpar tokens: " + e.getMessage());
        }

        // Agora o PostgreSQL permite deletar o usuário tranquilamente!
        usuarioRepository.deleteById(id);
        return ResponseEntity.ok().build();
    }
}