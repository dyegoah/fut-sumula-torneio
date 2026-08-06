package br.com.higitech.fut_sumula_torneio.service;

import java.util.Comparator;

import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import br.com.higitech.fut_sumula_torneio.dto.RegisterDTO;
import br.com.higitech.fut_sumula_torneio.model.TokenRecuperacao;
import br.com.higitech.fut_sumula_torneio.model.Usuario;
import br.com.higitech.fut_sumula_torneio.repository.TokenRecuperacaoRepository;
import br.com.higitech.fut_sumula_torneio.repository.UsuarioRepository;

@Aspect
@Component
public class PrimeiroAcessoAspect {

    private final UsuarioRepository usuarioRepository;
    private final TokenRecuperacaoRepository tokenRepository;
    private final JavaMailSender mailSender;

    @Value("${app.mail.from}")
    private String remetente;

    @Value("${app.base-url}")
    private String baseUrl;

    public PrimeiroAcessoAspect(
            UsuarioRepository usuarioRepository,
            TokenRecuperacaoRepository tokenRepository,
            JavaMailSender mailSender) {
        this.usuarioRepository = usuarioRepository;
        this.tokenRepository = tokenRepository;
        this.mailSender = mailSender;
    }

    @Around("execution(* br.com.higitech.fut_sumula_torneio.controller.AutenticacaoController.register(..)) && args(data)")
    @Transactional
    public Object concluirCadastroComAtivacao(ProceedingJoinPoint joinPoint, RegisterDTO data) throws Throwable {
        Object resultado = joinPoint.proceed();

        if (!(resultado instanceof ResponseEntity<?> resposta) || !resposta.getStatusCode().is2xxSuccessful()) {
            return resultado;
        }

        Usuario usuario = (Usuario) usuarioRepository.findByLogin(data.login());
        if (usuario == null) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("A conta foi criada, mas não foi possível preparar o primeiro acesso.");
        }

        usuario.setStatus("PENDENTE");
        usuarioRepository.save(usuario);

        TokenRecuperacao token = tokenRepository.findAll().stream()
                .filter(item -> item.getUsuario() != null
                        && item.getUsuario().getId().equals(usuario.getId()))
                .max(Comparator.comparing(TokenRecuperacao::getId))
                .orElse(null);

        if (token == null) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("A conta foi criada, mas o link de primeiro acesso não pôde ser gerado.");
        }

        String link = normalizarBaseUrl(baseUrl)
                + "/api/auth/confirmar-email?token=" + token.getToken();

        SimpleMailMessage mensagem = new SimpleMailMessage();
        mensagem.setFrom(remetente);
        mensagem.setTo(usuario.getLogin());
        mensagem.setSubject("Ative sua conta - Fut-Súmula Torneio");
        mensagem.setText("Olá, " + usuario.getNome() + "!\n\n"
                + "Sua conta foi criada com sucesso.\n"
                + "Para realizar o primeiro acesso, confirme seu e-mail no link abaixo:\n\n"
                + link + "\n\n"
                + "Este link é válido por 24 horas.\n\n"
                + "Se você não criou esta conta, ignore esta mensagem.");

        try {
            mailSender.send(mensagem);
        } catch (RuntimeException exception) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("A conta foi criada, mas o e-mail não pôde ser enviado. Use 'Reenviar ativação' na tela de login.");
        }

        return ResponseEntity.status(HttpStatus.CREATED)
                .body("Conta criada! Enviamos um link de primeiro acesso para o seu e-mail.");
    }

    private String normalizarBaseUrl(String url) {
        if (url == null || url.isBlank()) {
            throw new IllegalStateException("A propriedade app.base-url não foi configurada.");
        }
        return url.endsWith("/") ? url.substring(0, url.length() - 1) : url;
    }
}
