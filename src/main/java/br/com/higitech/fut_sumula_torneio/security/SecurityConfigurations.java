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
                    // REGRAS DE LIBERAÇÃO TOTAL
                    req.requestMatchers("/api/auth/**").permitAll();       
                    req.requestMatchers("/api/integracao/**").permitAll(); 
                    
                    // REGRAS PÚBLICAS
                    req.requestMatchers(HttpMethod.GET, "/api/torneios/**").permitAll();
                    req.requestMatchers(HttpMethod.GET, "/api/estatisticas/**").permitAll();
                    req.requestMatchers(HttpMethod.GET, "/api/times/**").permitAll();
                    req.requestMatchers(HttpMethod.GET, "/api/jogadores/**").permitAll();
                    req.requestMatchers(HttpMethod.GET, "/api/partidas/**").permitAll(); 
                    
                    // REGRAS DE BLOQUEIO (APIs do sistema)
                    req.requestMatchers("/api/**").authenticated();        
                    
                    // REGRA FINAL
                    req.anyRequest().permitAll();                          
                })
                .authenticationProvider(authenticationProvider())
                .addFilterBefore(securityFilter, UsernamePasswordAuthenticationFilter.class)
                .build();
    }
    
    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();
        
        // BLINDAGEM CORS: APENAS SEUS DOMÍNIOS SÃO AUTORIZADOS
        configuration.setAllowedOrigins(Arrays.asList(
            "https://fut-sumula-torneio.onrender.com", 
            "http://localhost:5500", 
            "http://127.0.0.1:5500", 
            "http://localhost:8080"
        ));
        
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