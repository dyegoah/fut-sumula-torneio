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
       
        Usuario admin = (Usuario) repository.findByLogin(adminEmail);
        
        if (admin == null) {
            admin = new Usuario();
            admin.setNome("Administrador");
            admin.setLogin(adminEmail);
        }
        
        // FORÇA A ATUALIZAÇÃO DA SENHA PARA GARANTIR O ACESSO
        admin.setSenha(passwordEncoder.encode(adminPassword)); 
        repository.save(admin);
        
        // ====================================================================
        // GESTÃO SEGURA DO 2FA DO ADMINISTRADOR
        // ====================================================================
        if (admin.getChave2fa() == null || admin.getChave2fa().trim().isEmpty()) {
            // SÓ GERA UMA CHAVE NOVA SE O BANCO ESTIVER VAZIO!
            GoogleAuthenticator gAuth = new GoogleAuthenticator();
            String novaChave = gAuth.createCredentials().getKey();
            admin.setChave2fa(novaChave);
            admin.setUsar2fa(true);
            repository.save(admin);
            System.out.println("\n🚨 NOVA CHAVE 2FA GERADA E SALVA NO BANCO 🚨");
        } else {
            System.out.println("\n✅ CHAVE 2FA JÁ EXISTENTE DETECTADA.");
            // Garante que o uso está ativado
            if (!Boolean.TRUE.equals(admin.getUsar2fa())) {
                admin.setUsar2fa(true);
                repository.save(admin);
            }
        }
        
        // Imprime o link do QR Code da chave atual (seja ela nova ou antiga)
        String linkQrCode = String.format("https://api.qrserver.com/v1/create-qr-code/?size=300x300&data=otpauth://totp/Fut-Sumula-Pro:Admin?secret=%s&issuer=Fut-Sumula-Pro", admin.getChave2fa());
        
        System.out.println("========================================================");
        System.out.println("Abra o Google Authenticator e escaneie o link abaixo:");
        System.out.println(linkQrCode);
        System.out.println("Ou digite a chave manual: " + admin.getChave2fa());
        
        // GERA O CÓDIGO VÁLIDO NO CONSOLE PARA TESTE ISOLADO
        GoogleAuthenticator gAuth = new GoogleAuthenticator();
        int codigoAtual = gAuth.getTotpPassword(admin.getChave2fa());
        System.out.println("👉 CÓDIGO VÁLIDO NESTE EXATO MOMENTO: " + String.format("%06d", codigoAtual));
        
        System.out.println("========================================================\n");
    }
}