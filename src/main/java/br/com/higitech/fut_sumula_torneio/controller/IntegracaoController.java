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

@RestController
@RequestMapping("/api/integracao")
public class IntegracaoController {

    @Autowired
    private UsuarioRepository repository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    // --- CORREÇÃO DE SEGURANÇA AQUI ---
    // Agora o Java vai ler a chave diretamente do ambiente seguro do Render
    @Value("${api.integracao.racha.key}")
    private String API_KEY_SEGURA;

    @PostMapping("/receber-usuario")
    public ResponseEntity<?> receberUsuarioExterno(
            @RequestHeader("X-Api-Key") String apiKey, 
            @RequestBody IntegracaoDTO dados) {

        // 1. Segurança: Verifica se quem chama tem a chave do ambiente
        if (!API_KEY_SEGURA.equals(apiKey)) {
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