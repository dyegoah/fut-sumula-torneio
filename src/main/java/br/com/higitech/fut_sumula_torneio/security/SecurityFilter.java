package br.com.higitech.fut_sumula_torneio.security;

import java.io.IOException;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import br.com.higitech.fut_sumula_torneio.model.Usuario;
import br.com.higitech.fut_sumula_torneio.repository.UsuarioRepository;
import br.com.higitech.fut_sumula_torneio.service.TokenService;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

@Component
public class SecurityFilter extends OncePerRequestFilter {

    @Autowired TokenService tokenService;
    @Autowired UsuarioRepository usuarioRepository;

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {
        var token = recuperarToken(request);
        if (token != null) {
            var login = tokenService.getSubject(token);
            // Pegamos o usuário para checar a validade
            Usuario user = (Usuario) usuarioRepository.findByLogin(login);

            if(user != null) {
                // --- TRAVA DE SEGURANÇA BACKEND (Adeus burla de F12) ---
                String uri = request.getRequestURI();
                boolean isPublicAuth = uri.startsWith("/api/auth");
                boolean isMeRoute = uri.equals("/api/users/me"); // Única rota que o expirado pode ver para mostrar a tela de bloqueio
                boolean isAdminRoute = uri.startsWith("/api/admin"); 

                // Se não tiver acesso liberado e tentar usar a API (salvar, deletar, buscar torneios)... BLOQUEIA!
                if (!user.isAcessoLiberado() && !isMeRoute && !isPublicAuth && !isAdminRoute && uri.startsWith("/api/")) {
                    response.sendError(HttpServletResponse.SC_FORBIDDEN, "ACESSO BLOQUEADO: Período de teste expirado ou conta suspensa.");
                    return; // A requisição morre aqui. Não chega no Controller.
                }

                var authentication = new UsernamePasswordAuthenticationToken(user, null, user.getAuthorities());
                SecurityContextHolder.getContext().setAuthentication(authentication);
            }
        }
        filterChain.doFilter(request, response);
    }

    private String recuperarToken(HttpServletRequest request) {
        var authHeader = request.getHeader("Authorization");
        if (authHeader == null) return null;
        return authHeader.replace("Bearer ", "");
    }
}