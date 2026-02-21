package br.com.higitech.fut_sumula_torneio.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import br.com.higitech.fut_sumula_torneio.dto.AuthenticationDTO;
import br.com.higitech.fut_sumula_torneio.dto.LoginResponseDTO;
import br.com.higitech.fut_sumula_torneio.dto.RegisterDTO;
import br.com.higitech.fut_sumula_torneio.model.Usuario;
import br.com.higitech.fut_sumula_torneio.repository.UsuarioRepository;
import br.com.higitech.fut_sumula_torneio.service.TokenService;

@RestController
@RequestMapping("/api/auth")
@CrossOrigin("*")
public class AutenticacaoController {

    @Autowired
    private AuthenticationManager authenticationManager;

    @Autowired
    private UsuarioRepository repository;

    @Autowired
    private TokenService tokenService;

    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody AuthenticationDTO data) {
        var usernamePassword = new UsernamePasswordAuthenticationToken(data.login(), data.senha());
        var auth = authenticationManager.authenticate(usernamePassword);
        
        var token = tokenService.gerarToken((Usuario) auth.getPrincipal());
        Usuario user = (Usuario) auth.getPrincipal();

        return ResponseEntity.ok(new LoginResponseDTO(token, user.getNome()));
    }

    @PostMapping("/register")
    public ResponseEntity<?> register(@RequestBody RegisterDTO data) {
        if (this.repository.findByLogin(data.login()) != null) return ResponseEntity.badRequest().body("E-mail já cadastrado.");

        String encryptedPassword = new BCryptPasswordEncoder().encode(data.senha());
        
        Usuario newUser = new Usuario();
        newUser.setLogin(data.login());
        newUser.setSenha(encryptedPassword);
        newUser.setNome(data.nome()); // Nome do Responsável
        
        // --- NOVOS CAMPOS MAPEADOS ---
        newUser.setNomeLiga(data.nomeLiga());
        newUser.setCidade(data.cidade());
        newUser.setUf(data.uf());
        newUser.setWhatsapp(data.whatsapp());
        newUser.setGenero(data.genero());
        newUser.setIdioma(data.idioma());
        newUser.setDataNascimento(data.dataNascimento());
        
        // Define padrão se vier nulo
        newUser.setSistemaOrigem(data.sistemaOrigem() != null ? data.sistemaOrigem() : "TORNEIO");
        newUser.setStatus("ATIVO");
        newUser.setPlano("FREE");

        this.repository.save(newUser);

        return ResponseEntity.ok().build();
    }
}