package br.com.higitech.fut_sumula_torneio.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import br.com.higitech.fut_sumula_torneio.model.Usuario;

@RestController
@RequestMapping("/api/users")
public class UserController {

	@GetMapping("/me")
    public ResponseEntity<Usuario> getMyProfile() {
        Usuario user = (Usuario) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        user.setSenha(null);
        
        // O Java calcula a verdade e anexa ao JSON enviado ao Frontend
        user.setAcessoLiberado(user.isAcessoLiberado());
        user.setDiasRestantes(user.calcularDiasRestantes());
        
        return ResponseEntity.ok(user);
    }
}