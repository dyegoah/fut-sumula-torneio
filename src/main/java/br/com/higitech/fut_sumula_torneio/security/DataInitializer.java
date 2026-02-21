package br.com.higitech.fut_sumula_torneio.security;

import org.springframework.beans.factory.annotation.Autowired;
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

    @Override
    public void run(String... args) throws Exception {
        // Verifica se já existe o admin, se não, cria.
        if (repository.findByLogin("admin@futsumula.com") == null) {
            Usuario admin = new Usuario();
            admin.setNome("Administrador");
            admin.setLogin("admin@futsumula.com");
            // AQUI A MÁGICA: O Spring gera o hash correto para '123456'
            admin.setSenha(passwordEncoder.encode("123456")); 
            
            repository.save(admin);
            System.out.println("--- USUÁRIO ADMIN CRIADO COM SUCESSO ---");
            System.out.println("Login: admin@futsumula.com");
            System.out.println("Senha: 123456");
            System.out.println("----------------------------------------");
        }
    }
}