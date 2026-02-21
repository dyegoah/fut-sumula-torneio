package br.com.higitech.fut_sumula_torneio.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import br.com.higitech.fut_sumula_torneio.model.Usuario;

@RestController
@RequestMapping("/api/users")
@CrossOrigin("*")
public class UserController {

    @GetMapping("/me")
    public ResponseEntity<Usuario> getMyProfile() {
        // Pega o usuário que está logado (pelo Token)
        Usuario user = (Usuario) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        
        // Remove a senha antes de enviar para o site por segurança
        user.setSenha(null);
        
        return ResponseEntity.ok(user);
    }
}