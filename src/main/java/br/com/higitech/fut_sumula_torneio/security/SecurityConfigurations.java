package br.com.higitech.fut_sumula_torneio.security;

import java.util.Arrays;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
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
                    // 1. REGRAS DE LIBERAÇÃO TOTAL (Login, Cadastro, Integração)
                    req.requestMatchers("/api/auth/**").permitAll();       
                    req.requestMatchers("/api/integracao/**").permitAll(); 
                    
                    // --- LEITURA PÚBLICA ESTRITA (Apenas as telas de compartilhamento para fãs) ---
                    // Agora usamos /publico/** para garantir que hackers não acessem rotas privadas via GET
                    req.requestMatchers(HttpMethod.GET, "/api/torneios/publico/**").permitAll();
                    req.requestMatchers(HttpMethod.GET, "/api/partidas/publico/**").permitAll();
                    req.requestMatchers(HttpMethod.GET, "/api/estatisticas/**").permitAll();
                    
                    // 2. REGRAS DE BLOQUEIO DE ALTO NÍVEL (A Trava do Admin)
                    req.requestMatchers("/api/admin/**").hasRole("ADMIN"); 
                    
                    // 3. REGRAS DE BLOQUEIO GERAL (Toda a gestão interna exige login)
                    req.requestMatchers("/api/**").authenticated();        
                    
                    // 4. REGRA FINAL (Permite carregar o layout HTML, CSS e imagens do site)
                    req.anyRequest().permitAll();                          
                })
                .authenticationProvider(authenticationProvider())
                .addFilterBefore(securityFilter, UsernamePasswordAuthenticationFilter.class)
                .build();
    }
    
    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();
        
        configuration.setAllowedOrigins(Arrays.asList(
            "http://localhost:5500", 
            "http://127.0.0.1:5500",
            "https://fut-sumula-torneio.onrender.com"
        ));

        configuration.setAllowCredentials(true); 
        configuration.setAllowedMethods(Arrays.asList("GET", "POST", "PUT", "DELETE", "OPTIONS"));
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