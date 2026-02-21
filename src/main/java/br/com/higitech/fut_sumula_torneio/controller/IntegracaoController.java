package br.com.higitech.fut_sumula_torneio.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import br.com.higitech.fut_sumula_torneio.dto.IntegracaoDTO;
import br.com.higitech.fut_sumula_torneio.model.Usuario;
import br.com.higitech.fut_sumula_torneio.repository.UsuarioRepository;

@RestController
@RequestMapping("/api/integracao")
@CrossOrigin("*") // Permite que o outro projeto chame esta API
public class IntegracaoController {

    @Autowired
    private UsuarioRepository repository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    // Chave de Segurança Simples (Pode ser melhorada depois)
    private static final String API_KEY = "HIGTECH_SECRET_KEY_123";

    @PostMapping("/receber-usuario")
    public ResponseEntity<?> receberUsuarioExterno(
            @RequestHeader("X-Api-Key") String apiKey, 
            @RequestBody IntegracaoDTO dados) {

        // 1. Segurança: Verifica se quem chama tem a chave
        if (!API_KEY.equals(apiKey)) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Chave de API inválida.");
        }

        // 2. Verifica se já existe
        if (repository.findByLogin(dados.email()) != null) {
            return ResponseEntity.status(HttpStatus.CONFLICT).body("Usuário já existe no sistema Torneio.");
        }

        // 3. Cria o Usuário com a etiqueta RACHA
        Usuario u = new Usuario();
        u.setNome(dados.nome());
        u.setLogin(dados.email());
        u.setSenha(passwordEncoder.encode(dados.senha())); // Criptografa a senha para compatibilidade
        u.setCidade(dados.cidade());
        u.setUf(dados.uf());
        u.setWhatsapp(dados.whatsapp());
        u.setGenero(dados.genero());
        u.setIdioma(dados.idioma());
        u.setDataNascimento(dados.dataNascimento());
        
        // --- O PULO DO GATO ---
        u.setSistemaOrigem("RACHA"); // Identifica a origem
        u.setNomeLiga("Racha Externo"); // Placeholder
        u.setStatus("ATIVO");
        u.setPlano("FREE"); // Ou o plano que vier do Racha

        repository.save(u);

        return ResponseEntity.ok("Usuário do Racha importado com sucesso!");
    }
}