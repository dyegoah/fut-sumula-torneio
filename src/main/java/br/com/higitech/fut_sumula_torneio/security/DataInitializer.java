package br.com.higitech.fut_sumula_torneio.security;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.password.PasswordEncoder;

import com.warrenstrange.googleauth.GoogleAuthenticator;

import br.com.higitech.fut_sumula_torneio.model.Usuario;
import br.com.higitech.fut_sumula_torneio.repository.UsuarioRepository;

@Configuration
public class DataInitializer implements CommandLineRunner {

    @Autowired
    private UsuarioRepository repository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Value("${api.admin.email:fut_sumula_pro@hotmail.com}")
    private String adminEmail;

    @Value("${api.admin.password:123456}")
    private String adminPassword;

    @Override
    public void run(String... args) throws Exception {
        // CORREÇÃO AQUI: Adicionado o cast (Usuario) para converter o UserDetails
        Usuario admin = (Usuario) repository.findByLogin(adminEmail);
        
        if (admin == null) {
            admin = new Usuario();
            admin.setNome("Administrador");
            admin.setLogin(adminEmail);
            admin.setSenha(passwordEncoder.encode(adminPassword)); 
            repository.save(admin);
        }
        
        // ====================================================================
        // SCRIPT DE EMERGÊNCIA: ZERA E GERA UMA NOVA CHAVE 2FA PARA O ADMIN
        // ====================================================================
        GoogleAuthenticator gAuth = new GoogleAuthenticator();
        String novaChave = gAuth.createCredentials().getKey();
        
        admin.setChave2fa(novaChave);
        admin.setUsar2fa(true);
        repository.save(admin);
        
        String linkQrCode = String.format("https://api.qrserver.com/v1/create-qr-code/?size=300x300&data=otpauth://totp/Fut-Sumula-Pro:Admin?secret=%s&issuer=Fut-Sumula-Pro", novaChave);
        
        System.out.println("\n========================================================");
        System.out.println("🚨 ATENÇÃO: SEU AUTENTICADOR 2FA FOI ZERADO E RECRIADO 🚨");
        System.out.println("========================================================");
        System.out.println("Abra o Google Authenticator no seu celular e adicione a chave abaixo:");
        System.out.println("Chave Manual: " + novaChave);
        System.out.println("\nOU CLIQUE NO LINK ABAIXO PARA VER O QR CODE NO NAVEGADOR E ESCANEAR:");
        System.out.println(linkQrCode);
        System.out.println("========================================================\n");
    }
}