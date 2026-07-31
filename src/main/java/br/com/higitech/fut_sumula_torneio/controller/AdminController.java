package br.com.higitech.fut_sumula_torneio.controller;

import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import br.com.higitech.fut_sumula_torneio.dto.AdminUpdateUserDTO;
import br.com.higitech.fut_sumula_torneio.model.Usuario;
import br.com.higitech.fut_sumula_torneio.repository.UsuarioRepository;
import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/admin")
// @CrossOrigin("*") <-- REMOVIDO PARA FECHAR A BRECHA (Risco 3)
public class AdminController {

    @Autowired private UsuarioRepository usuarioRepository;
    @Autowired private PasswordEncoder passwordEncoder;

    // --- SEGURANÇA: Lendo o email oficial do cofre ---
    @Value("${api.admin.email:admin@futsumula.com}")
    private String adminEmail;

    private boolean isAdmin() {
        try {
            Usuario u = (Usuario) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
            // Compara o usuário logado com a variável protegida
            return adminEmail.equals(u.getLogin());
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

    @PutMapping("/users/{id}/premium")
    public ResponseEntity<?> alternarPremium(@PathVariable Long id) {
        if (!isAdmin()) return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        return usuarioRepository.findById(id).map(user -> {
            if ("PREMIUM".equals(user.getPlano())) {
                user.setPlano("FREE");
            } else {
                user.setPlano("PREMIUM");
                user.setStatus("ATIVO"); 
            }
            usuarioRepository.save(user);
            return ResponseEntity.ok(user);
        }).orElse(ResponseEntity.notFound().build());
    }

    @PutMapping("/users/{id}/update")
    public ResponseEntity<?> atualizarDadosCompletos(@PathVariable Long id, @Valid @RequestBody AdminUpdateUserDTO dto) {
        if (!isAdmin()) return ResponseEntity.status(HttpStatus.FORBIDDEN).build();

        return usuarioRepository.findById(id).map(user -> {
            if(dto.nome() != null && !dto.nome().isBlank()) user.setNome(dto.nome());
            if(dto.login() != null && !dto.login().isBlank()) user.setLogin(dto.login());
            if(dto.cidade() != null) user.setCidade(dto.cidade());
            if(dto.uf() != null) user.setUf(dto.uf());
            if(dto.genero() != null) user.setGenero(dto.genero());
            if(dto.idioma() != null) user.setIdioma(dto.idioma());
            if(dto.trialDays() != null) user.setTrialDays(dto.trialDays());
            if(dto.dataNascimento() != null) user.setDataNascimento(dto.dataNascimento());

            if(dto.novaSenha() != null && !dto.novaSenha().isBlank()) {
                user.setSenha(passwordEncoder.encode(dto.novaSenha()));
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