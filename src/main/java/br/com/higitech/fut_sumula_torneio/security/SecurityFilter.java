package br.com.higitech.fut_sumula_torneio.security;

import java.io.IOException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import br.com.higitech.fut_sumula_torneio.repository.UsuarioRepository;
import br.com.higitech.fut_sumula_torneio.service.TokenService;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

@Component
public class SecurityFilter extends OncePerRequestFilter {

    private static final Logger logger = LoggerFactory.getLogger(SecurityFilter.class);

    @Autowired
    private TokenService tokenService;

    @Autowired
    private UsuarioRepository usuarioRepository;

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {
        var token = this.recoverToken(request); //
        
        if (token != null) { //[cite: 1]
            try {
                var login = tokenService.getSubject(token); //[cite: 1]
                UserDetails user = usuarioRepository.findByLogin(login); //[cite: 1]

                if (user != null) { //[cite: 1]
                    var authentication = new UsernamePasswordAuthenticationToken(user, null, user.getAuthorities()); //[cite: 1]
                    SecurityContextHolder.getContext().setAuthentication(authentication); //[cite: 1]
                }
            } catch (Exception e) { //[cite: 1]
                // 1. Limpa o contexto explicitamente para evitar vazamentos em caso de falha na validação
                SecurityContextHolder.clearContext(); 
                
                // 2. Registra a falha no log (nível WARN ou DEBUG) em vez de ignorar silenciosamente,
                // auxiliando na auditoria e detecção de tentativas de manipulação de token.
                logger.warn("Falha na validação do token JWT: {}", e.getMessage()); 
            }
        }
        
        filterChain.doFilter(request, response); //[cite: 1]
    }

    private String recoverToken(HttpServletRequest request) { //[cite: 1]
        // 1. TENTA PEGAR DO CABEÇALHO (Padrão Bearer - Usado pelo LocalStorage)[cite: 1]
        var authHeader = request.getHeader("Authorization"); //[cite: 1]
        if (authHeader != null && authHeader.startsWith("Bearer ")) { //[cite: 1]
            return authHeader.replace("Bearer ", ""); //[cite: 1]
        }
        
        // 2. BLINDAGEM MÁXIMA: SE NÃO ACHOU NO CABEÇALHO, TENTA PEGAR DO COOKIE HTTP-ONLY[cite: 1]
        if (request.getCookies() != null) { //[cite: 1]
            for (Cookie cookie : request.getCookies()) { //[cite: 1]
                if ("jwtToken".equals(cookie.getName())) { //[cite: 1]
                    return cookie.getValue(); //[cite: 1]
                }
            }
        }
        
        return null; //[cite: 1]
    }
}