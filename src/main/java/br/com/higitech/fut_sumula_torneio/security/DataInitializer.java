package br.com.higitech.fut_sumula_torneio.security;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.password.PasswordEncoder;

import br.com.higitech.fut_sumula_torneio.model.Usuario;
import br.com.higitech.fut_sumula_torneio.repository.UsuarioRepository;

@Configuration
public class DataInitializer implements CommandLineRunner {

    @Autowired
    private UsuarioRepository repository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    // --- SEGURANÇA: Lendo do cofre do Render ou properties ---
    @Value("${api.admin.email:fut_sumula_pro@hotmail.com}")
    private String adminEmail;

    @Value("${api.admin.password:123456}")
    private String adminPassword;

    @Override
    public void run(String... args) throws Exception {
        // Verifica se já existe o admin, se não, cria usando as variáveis seguras.
        if (repository.findByLogin(adminEmail) == null) {
            Usuario admin = new Usuario();
            admin.setNome("Administrador");
            admin.setLogin(adminEmail);
            
            // O Spring gera o hash correto para a senha configurada no ambiente
            admin.setSenha(passwordEncoder.encode(adminPassword)); 
            
            repository.save(admin);
            System.out.println("--- USUÁRIO ADMIN CRIADO COM SUCESSO ---");
            System.out.println("Login: " + adminEmail);
            System.out.println("Senha configurada via Variáveis de Ambiente");
            System.out.println("----------------------------------------");
        }
    }
}