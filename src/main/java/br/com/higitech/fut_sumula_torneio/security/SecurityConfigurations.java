package br.com.higitech.fut_sumula_torneio.security; // Ajuste o pacote se necessário

import java.util.Arrays;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod; // Importação necessária para o HttpMethod.GET
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import br.com.higitech.fut_sumula_torneio.service.AuthorizationService;

@Configuration
@EnableWebSecurity
public class SecurityConfigurations {

    @Autowired
    SecurityFilter securityFilter;
    
    @Autowired
    AuthorizationService authorizationService;

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        return http
                .csrf(csrf -> csrf.disable())
                .cors(cors -> cors.configurationSource(corsConfigurationSource()))
                .sessionManagement(sm -> sm.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authorizeHttpRequests(req -> {
                    // 1. REGRAS DE LIBERAÇÃO TOTAL (Devem vir PRIMEIRO)
                    req.requestMatchers("/api/auth/**").permitAll();       // Login e Registro
                    req.requestMatchers("/api/integracao/**").permitAll(); // Integração Racha (Webhook)
                    
                    // --- A MÁGICA ACONTECE AQUI: LIBERANDO A LEITURA PÚBLICA ---
                    // Permite que a Página Pública busque as informações sem precisar de Token
                    req.requestMatchers(HttpMethod.GET, "/api/torneios/**").permitAll();
                    req.requestMatchers(HttpMethod.GET, "/api/estatisticas/**").permitAll();
                    req.requestMatchers(HttpMethod.GET, "/api/times/**").permitAll();
                    req.requestMatchers(HttpMethod.GET, "/api/jogadores/**").permitAll();
                    req.requestMatchers(HttpMethod.GET, "/api/partidas/**").permitAll(); // Permite carregar a súmula também
                    
                    // 2. REGRAS DE BLOQUEIO (APIs do sistema)
                    // Todo o resto da API (POST, PUT, DELETE) continuará exigindo Token
                    req.requestMatchers("/api/**").authenticated();        
                    
                    // 3. REGRA FINAL (Site/Frontend) - OBRIGATORIAMENTE A ÚLTIMA LINHA
                    req.anyRequest().permitAll();                          // Libera HTML, CSS, JS, Imagens
                })
                .authenticationProvider(authenticationProvider())
                .addFilterBefore(securityFilter, UsernamePasswordAuthenticationFilter.class)
                .build();
    }
    
    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();
        configuration.setAllowedOrigins(Arrays.asList("*"));
        configuration.setAllowedMethods(Arrays.asList("GET", "POST", "PUT", "DELETE", "OPTIONS"));
        // ADICIONE "X-Api-Key" AQUI PARA A INTEGRAÇÃO FUNCIONAR
        configuration.setAllowedHeaders(Arrays.asList("Authorization", "Content-Type", "X-Api-Key"));
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);
        return source;
    }

    @Bean
    public AuthenticationProvider authenticationProvider() {
        DaoAuthenticationProvider authProvider = new DaoAuthenticationProvider();
        authProvider.setUserDetailsService(authorizationService);
        authProvider.setPasswordEncoder(passwordEncoder());
        return authProvider;
    }

    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration config) throws Exception {
        return config.getAuthenticationManager();
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
    
}