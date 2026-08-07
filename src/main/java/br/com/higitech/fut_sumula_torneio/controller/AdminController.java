package br.com.higitech.fut_sumula_torneio.controller;

import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import br.com.higitech.fut_sumula_torneio.model.Usuario;
import br.com.higitech.fut_sumula_torneio.repository.UsuarioRepository;
import jakarta.persistence.EntityManager;

@RestController
@RequestMapping("/api/admin")
public class AdminController {

    @Autowired
    private UsuarioRepository usuarioRepository;

    @Autowired
    private EntityManager entityManager;

    @GetMapping("/users")
    public ResponseEntity<List<Usuario>> listarUsuarios() {
        return ResponseEntity.ok(usuarioRepository.findAll());
    }

    @PutMapping("/users/{id}/status")
    public ResponseEntity<?> alternarStatus(@PathVariable Long id) {
        return usuarioRepository.findById(id).map(user -> {
            user.setStatus("ATIVO".equals(user.getStatus()) ? "INATIVO" : "ATIVO");
            usuarioRepository.save(user);
            return ResponseEntity.ok(Map.of("mensagem", "Status atualizado com sucesso!"));
        }).orElse(ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of("erro", "Usuário não encontrado")));
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
            return ResponseEntity.ok(Map.of("mensagem", "Plano atualizado com sucesso!"));
        }).orElse(ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of("erro", "Usuário não encontrado")));
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
            return ResponseEntity.ok(Map.of("mensagem", "Cortesia gerenciada com sucesso!"));
        }).orElse(ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of("erro", "Usuário não encontrado")));
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
            return ResponseEntity.ok(Map.of("mensagem", "Usuário atualizado com sucesso!"));
        }).orElse(ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of("erro", "Usuário não encontrado")));
    }

    @Transactional
    @DeleteMapping("/users/{id}")
    public ResponseEntity<?> excluirUsuario(@PathVariable Long id) {
        Optional<Usuario> usuarioOpt = usuarioRepository.findById(id);
        
        if (usuarioOpt.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of("erro", "Usuário não encontrado."));
        }

        Usuario user = usuarioOpt.get();

        // Trava de segurança: O Admin Mestre nunca pode ser deletado
        if ("fut_sumula_pro@hotmail.com".equals(user.getLogin())) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(Map.of("erro", "O Administrador Mestre não pode ser excluído!"));
        }

        try {
            // FAXINA GERAL DIRETAMENTE NO BANCO DE DADOS (Trator SQL)
            
            // 1. Apagar Eventos das Partidas ligados aos torneios, times ou jogadores do usuário
            entityManager.createNativeQuery("DELETE FROM tb_eventos_partida WHERE partida_id IN (SELECT id FROM tb_partidas WHERE torneio_id IN (SELECT id FROM tb_torneios WHERE organizador_id = :userId)) OR jogador_id IN (SELECT id FROM tb_jogadores WHERE organizador_id = :userId) OR time_id IN (SELECT id FROM tb_times WHERE organizador_id = :userId)").setParameter("userId", id).executeUpdate();

            // 2. Apagar Partidas ligadas aos torneios ou times do usuário
            entityManager.createNativeQuery("DELETE FROM tb_partidas WHERE torneio_id IN (SELECT id FROM tb_torneios WHERE organizador_id = :userId) OR time_casa_id IN (SELECT id FROM tb_times WHERE organizador_id = :userId) OR time_visitante_id IN (SELECT id FROM tb_times WHERE organizador_id = :userId)").setParameter("userId", id).executeUpdate();

            // 3. Apagar vínculos de Torneio e Time (Tabela associativa)
            entityManager.createNativeQuery("DELETE FROM tb_torneio_times WHERE torneio_id IN (SELECT id FROM tb_torneios WHERE organizador_id = :userId) OR time_id IN (SELECT id FROM tb_times WHERE organizador_id = :userId)").setParameter("userId", id).executeUpdate();

            // 4. Apagar Jogadores do usuário
            entityManager.createNativeQuery("DELETE FROM tb_jogadores WHERE organizador_id = :userId").setParameter("userId", id).executeUpdate();

            // 5. Apagar Times do usuário
            entityManager.createNativeQuery("DELETE FROM tb_times WHERE organizador_id = :userId").setParameter("userId", id).executeUpdate();

            // 6. Apagar Torneios do usuário
            entityManager.createNativeQuery("DELETE FROM tb_torneios WHERE organizador_id = :userId").setParameter("userId", id).executeUpdate();

            // 7. Apagar Tokens de Recuperação
            entityManager.createNativeQuery("DELETE FROM tokens_recuperacao WHERE usuario_id = :userId").setParameter("userId", id).executeUpdate();

            // 8. Finalmente, apagar o usuário
            usuarioRepository.delete(user);

            return ResponseEntity.ok(Map.of("mensagem", "Conta suspeita e todos os seus dados foram apagados com sucesso!"));

        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Map.of("erro", "Erro interno no banco de dados ao tentar excluir os dados."));
        }
    }
}