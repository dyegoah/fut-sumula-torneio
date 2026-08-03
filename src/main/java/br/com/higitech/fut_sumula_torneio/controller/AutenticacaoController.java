package br.com.higitech.fut_sumula_torneio.controller;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseCookie;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.warrenstrange.googleauth.GoogleAuthenticator;
import com.warrenstrange.googleauth.GoogleAuthenticatorKey;

import br.com.higitech.fut_sumula_torneio.dto.AuthenticationDTO;
import br.com.higitech.fut_sumula_torneio.dto.ForgotPasswordDTO;
import br.com.higitech.fut_sumula_torneio.dto.LoginResponseDTO;
import br.com.higitech.fut_sumula_torneio.dto.RegisterDTO;
import br.com.higitech.fut_sumula_torneio.dto.ResetPasswordDTO;
import br.com.higitech.fut_sumula_torneio.model.TokenRecuperacao;
import br.com.higitech.fut_sumula_torneio.model.Usuario;
import br.com.higitech.fut_sumula_torneio.repository.TokenRecuperacaoRepository;
import br.com.higitech.fut_sumula_torneio.repository.UsuarioRepository;
import br.com.higitech.fut_sumula_torneio.service.TokenService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/auth")
public class AutenticacaoController {

    @Autowired private AuthenticationManager authenticationManager;
    @Autowired private UsuarioRepository repository;
    @Autowired private TokenService tokenService;
    @Autowired private TokenRecuperacaoRepository tokenRecuperacaoRepository;
    @Autowired private org.springframework.mail.javamail.JavaMailSender mailSender;

    private final Map<String, LoginAttempt> loginAttempts = new ConcurrentHashMap<>();
    private static final int MAX_ATTEMPTS = 5;
    private static final int LOCK_TIME_MINUTES = 1;

    private static class LoginAttempt {
        int attempts;
        LocalDateTime lockTime;
        LoginAttempt() { this.attempts = 1; this.lockTime = null; }
    }

    @PostMapping("/login")
    public ResponseEntity<?> login(@Valid @RequestBody AuthenticationDTO data, HttpServletRequest request) {
        
        String clientIP = request.getRemoteAddr();
        LoginAttempt attempt = loginAttempts.getOrDefault(clientIP, new LoginAttempt());

        if (attempt.lockTime != null) {
            if (attempt.lockTime.plusMinutes(LOCK_TIME_MINUTES).isAfter(LocalDateTime.now())) {
                return ResponseEntity.status(HttpStatus.TOO_MANY_REQUESTS).body("Muitas tentativas. Aguarde 1 minuto.");
            } else {
                attempt.lockTime = null;
                attempt.attempts = 0;
            }
        }

        try {
            var usernamePassword = new UsernamePasswordAuthenticationToken(data.login(), data.senha());
            var auth = authenticationManager.authenticate(usernamePassword);
            loginAttempts.remove(clientIP);

            Usuario user = (Usuario) auth.getPrincipal();

            if (Boolean.TRUE.equals(user.getUsar2fa())) {
                java.util.Map<String, Object> response = new java.util.HashMap<>();
                response.put("requires2FA", true);
                response.put("login", user.getLogin());
                return ResponseEntity.ok(response);
            }

            var token = tokenService.gerarToken(user);
            ResponseCookie jwtCookie = ResponseCookie.from("jwtToken", token)
                    .httpOnly(true)
                    .secure(true)       
                    .path("/")
                    .maxAge(4 * 60 * 60)
                    .sameSite("None")   
                    .build();
            return ResponseEntity.ok().header(HttpHeaders.SET_COOKIE, jwtCookie.toString()).body(new LoginResponseDTO(token, user.getNome()));

        } catch (org.springframework.security.core.AuthenticationException e) {
            attempt.attempts++;
            if (attempt.attempts >= MAX_ATTEMPTS) attempt.lockTime = LocalDateTime.now(); 
            loginAttempts.put(clientIP, attempt);
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Credenciais inválidas");
        }
    }

    @PostMapping("/login/validar-2fa")
    public ResponseEntity<?> validar2FA(@RequestBody Map<String, String> data) {
        String login = data.get("login");
        String codigoStr = data.get("codigo");

        Usuario user = (Usuario) repository.findByLogin(login);
        if (user == null || !Boolean.TRUE.equals(user.getUsar2fa())) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Usuário inválido ou 2FA não ativado.");
        }

        try {
            int codigo = Integer.parseInt(codigoStr);
            GoogleAuthenticator gAuth = new GoogleAuthenticator();
            boolean isValid = gAuth.authorize(user.getChave2fa(), codigo);

            if (isValid) {
                var token = tokenService.gerarToken(user);
                ResponseCookie jwtCookie = ResponseCookie.from("jwtToken", token).httpOnly(true).secure(false).path("/").maxAge(4 * 60 * 60).sameSite("Lax").build();
                return ResponseEntity.ok().header(HttpHeaders.SET_COOKIE, jwtCookie.toString()).body(new LoginResponseDTO(token, user.getNome()));
            } else {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Código de segurança inválido!");
            }
        } catch (NumberFormatException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Formato de código inválido.");
        }
    }

    @GetMapping("/gerar-2fa-admin")
    public ResponseEntity<?> gerarQrCodeAdmin() {
        Usuario admin = (Usuario) repository.findByLogin("fut_sumula_pro@hotmail.com");
        if (admin == null) return ResponseEntity.badRequest().body("Admin mestre não encontrado!");

        String secretKey = admin.getChave2fa();
        if (secretKey == null || secretKey.isEmpty()) {
            GoogleAuthenticator gAuth = new GoogleAuthenticator();
            GoogleAuthenticatorKey key = gAuth.createCredentials();
            secretKey = key.getKey();
            
            admin.setChave2fa(secretKey);
            admin.setUsar2fa(true); 
            repository.save(admin);
        }

        String qrCodeUrl = String.format("otpauth://totp/Fut-Sumula-Pro:%s?secret=%s&issuer=Fut-Sumula-Pro", admin.getLogin(), secretKey);

        java.util.Map<String, String> resposta = new java.util.HashMap<>();
        resposta.put("chaveSecreta", secretKey);
        resposta.put("urlAplicativo", qrCodeUrl);
        return ResponseEntity.ok(resposta);
    }

    @PostMapping("/register")
    public ResponseEntity<?> register(@Valid @RequestBody RegisterDTO data) {
        if (this.repository.findByLogin(data.login()) != null) {
            return ResponseEntity.badRequest().body("E-mail já cadastrado.");
        }
        
        String encryptedPassword = new BCryptPasswordEncoder().encode(data.senha());
        Usuario newUser = new Usuario();
        newUser.setLogin(data.login()); 
        newUser.setSenha(encryptedPassword); 
        newUser.setNome(data.nome()); 
        newUser.setNomeLiga(data.nomeLiga()); 
        newUser.setCidade(data.cidade()); 
        newUser.setUf(data.uf());
        newUser.setPais(data.pais()); 
        newUser.setWhatsapp(data.whatsapp()); 
        newUser.setGenero(data.genero()); 
        newUser.setIdioma(data.idioma());
        newUser.setDataNascimento(data.dataNascimento()); 
        newUser.setSistemaOrigem(data.sistemaOrigem() != null ? data.sistemaOrigem() : "TORNEIO");
        
        // --- MODIFICADO: Status alterado para ATIVO ---
        newUser.setStatus("ATIVO"); 
        newUser.setPlano("FREE");
        this.repository.save(newUser);

        String tokenAtivacao = java.util.UUID.randomUUID().toString();
        TokenRecuperacao token = new TokenRecuperacao(); 
        token.setToken(tokenAtivacao); 
        token.setUsuario(newUser); 
        token.setDataExpiracao(LocalDateTime.now().plusHours(24)); 
        tokenRecuperacaoRepository.save(token);

        org.springframework.mail.SimpleMailMessage message = new org.springframework.mail.SimpleMailMessage();
        message.setFrom("seu_email_oficial@hotmail.com"); 
        message.setTo(newUser.getLogin()); 
        message.setSubject("Bem-vindo ao Fut-Súmula Pro - Confirme seu E-mail");
        
        String link = "https://fut-sumula-torneio.onrender.com/api/auth/confirmar-email?token=" + tokenAtivacao;
        message.setText("Olá, " + newUser.getNome() + "!\n\n"
                + "Falta apenas um passo para organizar seus torneios.\n"
                + "Clique no link abaixo para validar seu e-mail e ativar sua conta:\n\n" 
                + link);
        
        // --- MODIFICADO: O envio de e-mail foi comentado para não travar o cadastro ---
        // mailSender.send(message);

        return ResponseEntity.ok("Conta criada com sucesso! Você já pode fazer login.");
    }

    @GetMapping("/me")
    public ResponseEntity<?> verificarSessao(Authentication authentication) {
        if (authentication == null || !authentication.isAuthenticated() || "anonymousUser".equals(authentication.getPrincipal())) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
        Usuario user;
        try { user = (Usuario) authentication.getPrincipal(); } catch (ClassCastException e) { return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build(); }

        java.util.Map<String, Object> perfil = new java.util.HashMap<>();
        try {
            perfil.put("id", user.getId()); perfil.put("nome", user.getNome()); perfil.put("login", user.getLogin());
            perfil.put("nomeLiga", user.getNomeLiga()); perfil.put("genero", user.getGenero()); perfil.put("idioma", user.getIdioma());
            perfil.put("pais", user.getPais());

            String statusReal = user.getStatus();
            String planoReal = user.getPlano();
            boolean precisaSalvar = false;

            if (statusReal == null) {
                statusReal = "ATIVO"; 
                user.setStatus(statusReal);
                precisaSalvar = true;
            }
            if (planoReal == null) {
                planoReal = "FREE";
                user.setPlano(planoReal);
                precisaSalvar = true;
            }
            if (precisaSalvar) {
                repository.save(user); 
            }

            boolean cadastroIncompleto = false;
            if (user.getPais() == null || user.getPais().trim().isEmpty() || 
                user.getCidade() == null || user.getCidade().trim().isEmpty() || 
                user.getWhatsapp() == null || user.getWhatsapp().trim().isEmpty()) {
                cadastroIncompleto = true;
            }
            perfil.put("cadastroIncompleto", cadastroIncompleto);

            if ("Administrador".equals(user.getNome()) || "fut_sumula_pro@hotmail.com".equals(user.getLogin())) {
                perfil.put("nome", "Administrador"); perfil.put("status", "ATIVO"); perfil.put("plano", "PREMIUM");
                perfil.put("diasRestantes", 9999); perfil.put("acessoLiberado", true);
                perfil.put("cadastroIncompleto", false);
                return ResponseEntity.ok(perfil); 
            }

            int diasTrial = (user.getTrialDays() != null) ? user.getTrialDays() : 15;
            long diasUso = 0;
            if (user.getDataCadastro() != null) {
                try {
                    String dataString = user.getDataCadastro().toString();
                    if (dataString.length() >= 10) {
                        java.time.LocalDate dataCad = java.time.LocalDate.parse(dataString.substring(0, 10));
                        diasUso = java.time.temporal.ChronoUnit.DAYS.between(dataCad, java.time.LocalDate.now());
                    }
                } catch (Exception e) { diasUso = 0; }
            }

            long diasRestantes = Math.max(0, diasTrial - diasUso);
            boolean isLiberado = false;
            
            if ("ATIVO".equals(statusReal)) {
                if ("PREMIUM".equals(planoReal) || "CORTESIA".equals(planoReal) || diasRestantes >= 0) isLiberado = true;
            }

            perfil.put("status", statusReal);
            perfil.put("plano", planoReal);
            perfil.put("notaCortesia", user.getNotaCortesia());
            perfil.put("diasRestantes", diasRestantes);
            perfil.put("acessoLiberado", isLiberado);

        } catch (Exception e) { perfil.put("acessoLiberado", false); perfil.put("erro", "Falha interna."); }
        return ResponseEntity.ok(perfil); 
    }

    @PutMapping("/completar-cadastro")
    public ResponseEntity<?> completarCadastro(Authentication authentication, @RequestBody Map<String, String> dados) {
        if (authentication == null || !authentication.isAuthenticated() || "anonymousUser".equals(authentication.getPrincipal())) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
        
        Usuario userLogado = (Usuario) authentication.getPrincipal();
        Usuario dbUser = repository.findById(userLogado.getId()).orElse(null);
        
        if (dbUser == null) return ResponseEntity.badRequest().body("Usuário não encontrado.");

        if (dados.containsKey("cidade")) dbUser.setCidade(dados.get("cidade"));
        if (dados.containsKey("uf")) dbUser.setUf(dados.get("uf"));
        if (dados.containsKey("pais")) dbUser.setPais(dados.get("pais"));
        if (dados.containsKey("whatsapp")) dbUser.setWhatsapp(dados.get("whatsapp"));

        repository.save(dbUser);
        return ResponseEntity.ok("Cadastro atualizado com sucesso!");
    }

    @PostMapping("/forgot-password")
    public ResponseEntity<?> forgotPassword(@Valid @RequestBody ForgotPasswordDTO data) {
        Usuario user = (Usuario) repository.findByLogin(data.email());
        if (user == null) return ResponseEntity.ok("Se o e-mail existir, um link de recuperação foi enviado.");

        String token = java.util.UUID.randomUUID().toString();
        TokenRecuperacao tokenRecuperacao = new TokenRecuperacao();
        tokenRecuperacao.setToken(token); tokenRecuperacao.setUsuario(user); tokenRecuperacao.setDataExpiracao(LocalDateTime.now().plusMinutes(15));
        tokenRecuperacaoRepository.save(tokenRecuperacao);

        org.springframework.mail.SimpleMailMessage message = new org.springframework.mail.SimpleMailMessage();
        message.setFrom("seu_email_oficial@hotmail.com"); message.setTo(user.getLogin()); message.setSubject("Recuperação de Senha - Fut-Súmula Torneio");
        String link = "https://fut-sumula-torneio.onrender.com/reset-password.html?token=" + token;
        message.setText("Olá, " + user.getNome() + "!\n\nVocê solicitou a redefinição de sua senha.\nClique no link abaixo para criar uma nova senha.\n\n" + link);
        
        // Também comentado aqui para não quebrar testes de recuperação de senha futuramente
        // mailSender.send(message);

        return ResponseEntity.ok("Se o e-mail existir, um link de recuperação foi enviado.");
    }

    @PostMapping("/reset-password")
    public ResponseEntity<?> resetPassword(@Valid @RequestBody ResetPasswordDTO data) {
        java.util.Optional<TokenRecuperacao> tokenOpt = tokenRecuperacaoRepository.findByToken(data.token());
        if (tokenOpt.isEmpty()) return ResponseEntity.badRequest().body("Token inválido ou não encontrado.");
        TokenRecuperacao token = tokenOpt.get();
        if (token.getDataExpiracao().isBefore(LocalDateTime.now())) {
            tokenRecuperacaoRepository.delete(token); return ResponseEntity.badRequest().body("Este link expirou.");
        }
        Usuario user = token.getUsuario();
        user.setSenha(new BCryptPasswordEncoder().encode(data.novaSenha()));
        repository.save(user);
        tokenRecuperacaoRepository.delete(token);
        return ResponseEntity.ok("Senha redefinida com sucesso!");
    }
    
    @GetMapping("/confirmar-email")
    public ResponseEntity<?> confirmarEmail(@RequestParam String token) {
        java.util.Optional<TokenRecuperacao> tokenOpt = tokenRecuperacaoRepository.findByToken(token);
        
        if (tokenOpt.isEmpty()) {
            return ResponseEntity.badRequest().body("Link de ativação inválido ou não encontrado.");
        }
        
        TokenRecuperacao tokenAtivacao = tokenOpt.get();
        
        if (tokenAtivacao.getDataExpiracao().isBefore(LocalDateTime.now())) {
            tokenRecuperacaoRepository.delete(tokenAtivacao); 
            return ResponseEntity.badRequest().body("Este link de ativação expirou. Solicite um novo.");
        }
        
        Usuario user = tokenAtivacao.getUsuario();
        user.setStatus("ATIVO");
        repository.save(user);
        
        tokenRecuperacaoRepository.delete(tokenAtivacao);
        
        return ResponseEntity.status(HttpStatus.FOUND)
                .header(HttpHeaders.LOCATION, "/login.html?ativado=true")
                .build();
    }
}