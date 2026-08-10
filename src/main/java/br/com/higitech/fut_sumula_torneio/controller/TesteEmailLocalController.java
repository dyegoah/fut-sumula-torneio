package br.com.higitech.fut_sumula_torneio.controller;

import java.util.Map;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Profile;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.mail.MailException;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Profile("local")
@RestController
@RequestMapping("/api/auth/local")
public class TesteEmailLocalController {

    private final JavaMailSender mailSender;

    @Value("${app.mail.from}")
    private String remetente;

    public TesteEmailLocalController(JavaMailSender mailSender) {
        this.mailSender = mailSender;
    }

    @PostMapping("/testar-email")
    public ResponseEntity<?> testarEmail(@RequestBody Map<String, String> dados) {
        String destino = dados.get("destino");

        if (destino == null || destino.isBlank()) {
            return ResponseEntity.badRequest().body("Informe o campo 'destino'.");
        }

        SimpleMailMessage mensagem = new SimpleMailMessage();
        mensagem.setFrom(remetente);
        mensagem.setTo(destino.trim());
        mensagem.setSubject("Teste local de e-mail - Fut-Súmula Torneio");
        mensagem.setText("O envio SMTP do ambiente localhost está funcionando corretamente.\n\n"
                + "Agora você pode testar o cadastro e o link de primeiro acesso.");

        try {
            mailSender.send(mensagem);
            return ResponseEntity.ok("E-mail de teste enviado com sucesso para " + destino.trim() + ".");
        } catch (MailException exception) {
            String causa = exception.getMostSpecificCause() != null
                    ? exception.getMostSpecificCause().getMessage()
                    : exception.getMessage();

            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Falha no envio SMTP: " + causa);
        }
    }
}
