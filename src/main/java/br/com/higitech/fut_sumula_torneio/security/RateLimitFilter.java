package br.com.higitech.fut_sumula_torneio.security;

import java.io.IOException;
import java.time.Duration;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import io.github.bucket4j.Bandwidth;
import io.github.bucket4j.Bucket;
import io.github.bucket4j.Refill;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

@Component
public class RateLimitFilter extends OncePerRequestFilter {

    // Guarda um "balde de créditos" na memória para cada IP que acessa o sistema
    private final Map<String, Bucket> cache = new ConcurrentHashMap<>();

    private Bucket createNewBucket() {
        // REGRA DE OURO: 60 requisições permitidas por minuto
        Bandwidth limit = Bandwidth.classic(60, Refill.greedy(60, Duration.ofMinutes(1)));
        return Bucket.builder().addLimit(limit).build();
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {

        // 1. Aplica o escudo APENAS nas rotas da API (ignora o carregamento de HTML/CSS/Imagens públicas)
        if (!request.getRequestURI().startsWith("/api/")) {
            filterChain.doFilter(request, response);
            return;
        }

        // 2. Pega o IP real do atacante/usuário (mesmo se o projeto estiver atrás do Render)
        String ip = request.getHeader("X-Forwarded-For");
        if (ip == null || ip.isEmpty()) {
            ip = request.getRemoteAddr();
        } else {
            ip = ip.split(",")[0].trim(); // Se vierem vários, pega o primeiro
        }

        // 3. Pega o balde do usuário (se ele for novo, cria um)
        Bucket bucket = cache.computeIfAbsent(ip, k -> createNewBucket());

        // 4. Verifica se ele ainda tem créditos para gastar neste minuto
        if (bucket.tryConsume(1)) {
            // Pode passar
            filterChain.doFilter(request, response);
        } else {
            // Esgotou os créditos! É um ataque ou spam. Bate a porta.
            response.setStatus(HttpStatus.TOO_MANY_REQUESTS.value()); // Retorna Erro 429
            response.setContentType("application/json; charset=UTF-8");
            response.getWriter().write("{\"erro\": \"Sinalizador de segurança ativado: Muitas requisições. Aguarde 1 minuto para evitar sobrecarga.\"}");
        }
    }
}