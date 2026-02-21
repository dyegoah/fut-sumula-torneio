package br.com.higitech.fut_sumula_torneio.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class ViewController {

    /**
     * Mapeia a raiz do site (localhost:8080) para o dashboard.
     * O "forward" garante que o navegador carregue o arquivo estático.
     */
    @GetMapping("/")
    public String home() {
        return "/index.html";
    }

    /**
     * Mapeia a URL limpa "/dashboard" também para o index.
     */
    @GetMapping("/dashboard")
    public String dashboard() {
        return "/index.html";
    }

    /**
     * Mapeia a URL "/cadastro-jogador" para o arquivo HTML correspondente.
     * Isso permite que a gente acesse a página sem digitar ".html" no final.
     */
    @GetMapping("/cadastro-jogador")
    public String cadastroJogadorPage() {
        return "/cadastro-jogador.html";
    }
    
    @GetMapping("/jogadores")
    public String jogadoresPage() {
        return "/jogadores.html";
    }
    
    @GetMapping("/cadastro-time")
    public String cadastroTimePage() {
        return "/cadastro-time.html";
    }
    
    @GetMapping("/times")
    public String timesPage() {
        return "/times.html";
    }
    
    @GetMapping("/cadastro-torneio")
    public String cadastroTorneioPage() {
        return "/cadastro-torneio.html";
    }

    @GetMapping("/torneios")
    public String torneiosPage() {
        return "/torneios.html";
    }
}