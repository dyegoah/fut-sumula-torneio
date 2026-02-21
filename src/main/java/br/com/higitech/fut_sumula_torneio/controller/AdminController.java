package br.com.higitech.fut_sumula_torneio.controller;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import br.com.higitech.fut_sumula_torneio.model.Usuario;
import br.com.higitech.fut_sumula_torneio.repository.UsuarioRepository;

@RestController
@RequestMapping("/api/admin")
@CrossOrigin("*")
public class AdminController {

    @Autowired private UsuarioRepository usuarioRepository;
    @Autowired private PasswordEncoder passwordEncoder;

    private boolean isAdmin() {
        try {
            Usuario u = (Usuario) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
            return "admin@futsumula.com".equals(u.getLogin());
        } catch (Exception e) { return false; }
    }

    @GetMapping("/users")
    public ResponseEntity<?> listarTodos() {
        if (!isAdmin()) return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        List<Usuario> usuarios = usuarioRepository.findAll();
        usuarios.forEach(u -> u.setSenha(null)); 
        return ResponseEntity.ok(usuarios);
    }

    @PutMapping("/users/{id}/status")
    public ResponseEntity<?> alternarStatus(@PathVariable Long id) {
        if (!isAdmin()) return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        return usuarioRepository.findById(id).map(user -> {
            String novoStatus = "ATIVO".equals(user.getStatus()) ? "INATIVO" : "ATIVO";
            user.setStatus(novoStatus);
            usuarioRepository.save(user);
            return ResponseEntity.ok(user);
        }).orElse(ResponseEntity.notFound().build());
    }

    // --- NOVO: BOTÃO PARA LIBERAR ASSINATURA PAGA ---
    @PutMapping("/users/{id}/premium")
    public ResponseEntity<?> alternarPremium(@PathVariable Long id) {
        if (!isAdmin()) return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        return usuarioRepository.findById(id).map(user -> {
            if ("PREMIUM".equals(user.getPlano())) {
                user.setPlano("FREE");
            } else {
                user.setPlano("PREMIUM");
                user.setStatus("ATIVO"); // Reativa conta automaticamente se estava suspensa
            }
            usuarioRepository.save(user);
            return ResponseEntity.ok(user);
        }).orElse(ResponseEntity.notFound().build());
    }

    @PutMapping("/users/{id}/update")
    public ResponseEntity<?> atualizarDadosCompletos(@PathVariable Long id, @RequestBody Map<String, Object> payload) {
        if (!isAdmin()) return ResponseEntity.status(HttpStatus.FORBIDDEN).build();

        return usuarioRepository.findById(id).map(user -> {
            if(payload.containsKey("nome")) user.setNome((String) payload.get("nome"));
            if(payload.containsKey("login")) user.setLogin((String) payload.get("login"));
            if(payload.containsKey("cidade")) user.setCidade((String) payload.get("cidade"));
            if(payload.containsKey("uf")) user.setUf((String) payload.get("uf"));
            if(payload.containsKey("genero")) user.setGenero((String) payload.get("genero"));
            if(payload.containsKey("idioma")) user.setIdioma((String) payload.get("idioma"));
            
            if(payload.containsKey("trialDays")) {
                Object tdObj = payload.get("trialDays");
                if(tdObj == null) {
                    user.setTrialDays(null); 
                } else if (tdObj instanceof Number) {
                    user.setTrialDays(((Number) tdObj).intValue());
                } else if (tdObj instanceof String && !((String)tdObj).isBlank()) {
                    user.setTrialDays(Integer.parseInt((String) tdObj));
                }
            }

            String novaSenha = (String) payload.get("novaSenha");
            if(novaSenha != null && !novaSenha.isBlank()) {
                user.setSenha(passwordEncoder.encode(novaSenha));
            }
            
            String dataNasc = (String) payload.get("dataNascimento");
            if(dataNasc != null && !dataNasc.isEmpty()) {
                user.setDataNascimento(LocalDate.parse(dataNasc));
            }

            usuarioRepository.save(user);
            return ResponseEntity.ok(user);
        }).orElse(ResponseEntity.notFound().build());
    }

    @PutMapping("/users/{id}/cortesia")
    public ResponseEntity<?> gerenciarCortesia(@PathVariable Long id, @RequestBody Map<String, String> payload) {
        if (!isAdmin()) return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        return usuarioRepository.findById(id).map(user -> {
            String acao = payload.get("acao");
            if ("CONCEDER".equals(acao)) {
                user.setPlano("CORTESIA");
                user.setStatus("ATIVO"); 
                String nota = payload.get("nota");
                if(nota != null && nota.length() > 20) nota = nota.substring(0, 20);
                user.setNotaCortesia(nota);
            } else {
                user.setPlano("FREE");
                user.setNotaCortesia(null);
            }
            usuarioRepository.save(user);
            return ResponseEntity.ok(user);
        }).orElse(ResponseEntity.notFound().build());
    }
    
    @DeleteMapping("/users/{id}")
    public ResponseEntity<?> excluirUsuario(@PathVariable Long id) {
        if (!isAdmin()) return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        if (!usuarioRepository.existsById(id)) return ResponseEntity.notFound().build();
        usuarioRepository.deleteById(id);
        return ResponseEntity.noContent().build();
    }
}