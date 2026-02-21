package br.com.higitech.fut_sumula_torneio.dto;

public class EstatisticaJogadorDTO {
    private Long jogadorId;
    private String nomeJogador;
    private String nomeTime;
    private String fotoUrl;
    private Long quantidade;

    public EstatisticaJogadorDTO(Long jogadorId, String nomeJogador, String nomeTime, String fotoUrl, Long quantidade) {
        this.jogadorId = jogadorId;
        this.nomeJogador = nomeJogador;
        this.nomeTime = nomeTime;
        this.fotoUrl = fotoUrl;
        this.quantidade = quantidade;
    }

    // --- GETTERS E SETTERS MANUAIS ---
    
    public Long getJogadorId() { return jogadorId; }
    public void setJogadorId(Long jogadorId) { this.jogadorId = jogadorId; }

    public String getNomeJogador() { return nomeJogador; }
    public void setNomeJogador(String nomeJogador) { this.nomeJogador = nomeJogador; }

    public String getNomeTime() { return nomeTime; }
    public void setNomeTime(String nomeTime) { this.nomeTime = nomeTime; }

    public String getFotoUrl() { return fotoUrl; }
    public void setFotoUrl(String fotoUrl) { this.fotoUrl = fotoUrl; }

    public Long getQuantidade() { return quantidade; }
    public void setQuantidade(Long quantidade) { this.quantidade = quantidade; }
}