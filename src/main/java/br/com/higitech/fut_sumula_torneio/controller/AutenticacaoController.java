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
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
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

    // ====================================================================
    // 1. PRIMEIRA ETAPA DO LOGIN (E-mail e Senha)
    // ====================================================================
    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody AuthenticationDTO data, HttpServletRequest request) {
        
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

            // VERIFICA SE O USUÁRIO TEM A SEGURANÇA 2FA ATIVADA
            if (Boolean.TRUE.equals(user.getUsar2fa())) {
                java.util.Map<String, Object> response = new java.util.HashMap<>();
                response.put("requires2FA", true);
                response.put("login", user.getLogin());
                return ResponseEntity.ok(response); // Manda o Front-end pedir a 2ª etapa
            }

            // Se não usar 2FA (Usuário Comum), entra direto
            var token = tokenService.gerarToken(user);
            ResponseCookie jwtCookie = ResponseCookie.from("jwtToken", token)
                    .httpOnly(true)
                    .secure(true)       // <- MUDOU PARA TRUE (EXIGE HTTPS)
                    .path("/")
                    .maxAge(24 * 60 * 60)
                    .sameSite("None")   // <- MUDOU PARA NONE (PERMITE NAVEGAÇÃO CROSS-SITE SEGURA NO RENDER)
                    .build();
            return ResponseEntity.ok().header(HttpHeaders.SET_COOKIE, jwtCookie.toString()).body(new LoginResponseDTO(token, user.getNome()));

        } catch (org.springframework.security.core.AuthenticationException e) {
            attempt.attempts++;
            if (attempt.attempts >= MAX_ATTEMPTS) attempt.lockTime = LocalDateTime.now(); 
            loginAttempts.put(clientIP, attempt);
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Credenciais inválidas");
        }
    }

    // ====================================================================
    // 2. SEGUNDA ETAPA DO LOGIN (Código de 6 Dígitos - Google Auth)
    // ====================================================================
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
                ResponseCookie jwtCookie = ResponseCookie.from("jwtToken", token).httpOnly(true).secure(false).path("/").maxAge(24 * 60 * 60).sameSite("Lax").build();
                return ResponseEntity.ok().header(HttpHeaders.SET_COOKIE, jwtCookie.toString()).body(new LoginResponseDTO(token, user.getNome()));
            } else {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Código de segurança inválido!");
            }
        } catch (NumberFormatException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Formato de código inválido.");
        }
    }

    // ====================================================================
    // ROTA PARA VINCULAR O SEU CELULAR AO BANCO DE DADOS
    // ====================================================================
    @GetMapping("/gerar-2fa-admin")
    public ResponseEntity<?> gerarQrCodeAdmin() {
        Usuario admin = (Usuario) repository.findByLogin("admin@futsumula.com");
        if (admin == null) return ResponseEntity.badRequest().body("Admin mestre não encontrado!");

        String secretKey = admin.getChave2fa();
        if (secretKey == null || secretKey.isEmpty()) {
            GoogleAuthenticator gAuth = new GoogleAuthenticator();
            GoogleAuthenticatorKey key = gAuth.createCredentials();
            secretKey = key.getKey();
            
            admin.setChave2fa(secretKey);
            admin.setUsar2fa(true); // OBRIGA O ADMIN A USAR O CÓDIGO
            repository.save(admin);
        }

        String qrCodeUrl = String.format("otpauth://totp/Fut-Sumula-Pro:%s?secret=%s&issuer=Fut-Sumula-Pro", admin.getLogin(), secretKey);

        java.util.Map<String, String> resposta = new java.util.HashMap<>();
        resposta.put("chaveSecreta", secretKey);
        resposta.put("urlAplicativo", qrCodeUrl);
        return ResponseEntity.ok(resposta);
    }

    // ... Rotas antigas de recuperar senha e /me e /register abaixo (mantidas iguaizinhas)
    @PostMapping("/register")
    public ResponseEntity<?> register(@RequestBody RegisterDTO data) {
        if (this.repository.findByLogin(data.login()) != null) return ResponseEntity.badRequest().body("E-mail já cadastrado.");
        String encryptedPassword = new BCryptPasswordEncoder().encode(data.senha());
        Usuario newUser = new Usuario();
        newUser.setLogin(data.login()); newUser.setSenha(encryptedPassword); newUser.setNome(data.nome()); 
        newUser.setNomeLiga(data.nomeLiga()); newUser.setCidade(data.cidade()); newUser.setUf(data.uf());
        newUser.setWhatsapp(data.whatsapp()); newUser.setGenero(data.genero()); newUser.setIdioma(data.idioma());
        newUser.setDataNascimento(data.dataNascimento()); newUser.setSistemaOrigem(data.sistemaOrigem() != null ? data.sistemaOrigem() : "TORNEIO");
        newUser.setStatus("ATIVO"); newUser.setPlano("FREE");
        this.repository.save(newUser);
        return ResponseEntity.ok().build();
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

            if ("Administrador".equals(user.getNome()) || "admin@futsumula.com".equals(user.getLogin())) {
                perfil.put("nome", "Administrador"); perfil.put("status", "ATIVO"); perfil.put("plano", "PREMIUM");
                perfil.put("diasRestantes", 9999); perfil.put("acessoLiberado", true);
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
            if ("ATIVO".equals(user.getStatus())) {
                if ("PREMIUM".equals(user.getPlano()) || "CORTESIA".equals(user.getPlano()) || diasRestantes >= 0) isLiberado = true;
            }

            perfil.put("status", user.getStatus() != null ? user.getStatus() : "INATIVO");
            perfil.put("plano", user.getPlano() != null ? user.getPlano() : "FREE");
            perfil.put("notaCortesia", user.getNotaCortesia());
            perfil.put("diasRestantes", diasRestantes);
            perfil.put("acessoLiberado", isLiberado);

        } catch (Exception e) { perfil.put("acessoLiberado", false); perfil.put("erro", "Falha interna."); }
        return ResponseEntity.ok(perfil); 
    }

    @PostMapping("/forgot-password")
    public ResponseEntity<?> forgotPassword(@RequestBody ForgotPasswordDTO data) {
        Usuario user = (Usuario) repository.findByLogin(data.email());
        if (user == null) return ResponseEntity.ok("Se o e-mail existir, um link de recuperação foi enviado.");

        String token = java.util.UUID.randomUUID().toString();
        TokenRecuperacao tokenRecuperacao = new TokenRecuperacao();
        tokenRecuperacao.setToken(token); tokenRecuperacao.setUsuario(user); tokenRecuperacao.setDataExpiracao(LocalDateTime.now().plusMinutes(15));
        tokenRecuperacaoRepository.save(tokenRecuperacao);

        org.springframework.mail.SimpleMailMessage message = new org.springframework.mail.SimpleMailMessage();
        message.setFrom("seu.email@gmail.com"); message.setTo(user.getLogin()); message.setSubject("Recuperação de Senha - Fut-Súmula Torneio");
        String link = "http://localhost:8080/reset-password.html?token=" + token;
        message.setText("Olá, " + user.getNome() + "!\n\nVocê solicitou a redefinição de sua senha.\nClique no link abaixo para criar uma nova senha.\n\n" + link);
        mailSender.send(message);

        return ResponseEntity.ok("Se o e-mail existir, um link de recuperação foi enviado.");
    }

    @PostMapping("/reset-password")
    public ResponseEntity<?> resetPassword(@RequestBody ResetPasswordDTO data) {
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
}