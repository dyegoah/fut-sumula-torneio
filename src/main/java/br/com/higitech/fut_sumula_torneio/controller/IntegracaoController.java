package br.com.higitech.fut_sumula_torneio.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import br.com.higitech.fut_sumula_torneio.dto.IntegracaoDTO;
import br.com.higitech.fut_sumula_torneio.model.Usuario;
import br.com.higitech.fut_sumula_torneio.repository.UsuarioRepository;
import jakarta.validation.Valid; // IMPORTAÇÃO DA BLINDAGEM

@RestController
@RequestMapping("/api/integracao")
public class IntegracaoController {

    @Autowired
    private UsuarioRepository repository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Value("${api.integracao.racha.key}")
    private String API_KEY_SEGURA;

    @PostMapping("/receber-usuario")
    public ResponseEntity<?> receberUsuarioExterno(
            @RequestHeader("X-Api-Key") String apiKey, 
            @Valid @RequestBody IntegracaoDTO dados) {

        if (!API_KEY_SEGURA.equals(apiKey)) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Chave de API inválida.");
        }

        if (repository.findByLogin(dados.email()) != null) {
            return ResponseEntity.status(HttpStatus.CONFLICT).body("Usuário já existe no sistema Torneio.");
        }

        Usuario u = new Usuario();
        u.setNome(dados.nome());
        u.setLogin(dados.email());
        u.setSenha(passwordEncoder.encode(dados.senha())); 
        u.setCidade(dados.cidade());
        u.setUf(dados.uf());
        u.setWhatsapp(dados.whatsapp());
        u.setGenero(dados.genero());
        u.setIdioma(dados.idioma());
        u.setDataNascimento(dados.dataNascimento());
        
        u.setSistemaOrigem("RACHA"); 
        u.setNomeLiga("Racha Externo"); 
        u.setStatus("ATIVO");
        u.setPlano("FREE"); 

        repository.save(u);

        return ResponseEntity.ok("Usuário do Racha importado com sucesso!");
    }
}