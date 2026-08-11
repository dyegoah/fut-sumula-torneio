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
        
        // 1. Garantia de acesso vitalício para o Administrador Master
        if ("fut_sumula_pro@hotmail.com".equals(user.getLogin()) || "Administrador".equals(user.getNome())) {
            user.setAcessoLiberado(true);
            user.setDiasRestantes(9999);
            return ResponseEntity.ok(user);
        }

        // 2. Lógica de cálculo do Trial (15 dias padrão)
        int diasTrial = (user.getTrialDays() != null) ? user.getTrialDays() : 15;
        long diasUso = 0;
        
        if (user.getDataCadastro() != null) {
            long diasPassados = java.time.temporal.ChronoUnit.DAYS.between(user.getDataCadastro(), java.time.LocalDate.now());
            diasUso = Math.max(0, diasPassados);
        }

        long diasRestantes = Math.max(0, diasTrial - diasUso);
        boolean isLiberado = false;

        // 3. Verificação de status e plano
        if ("ATIVO".equals(user.getStatus())) {
            if ("PREMIUM".equals(user.getPlano()) || "CORTESIA".equals(user.getPlano()) || diasRestantes >= 0) {
                isLiberado = true;
            }
        }

        // 4. Anexa a verdade calculada ao JSON enviado ao Frontend
        user.setAcessoLiberado(isLiberado);
        user.setDiasRestantes(diasRestantes);
        
        return ResponseEntity.ok(user);
    }
}