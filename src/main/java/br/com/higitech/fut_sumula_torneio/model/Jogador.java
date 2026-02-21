package br.com.higitech.fut_sumula_torneio.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties; // IMPORTANTE

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;

@Entity
@Table(name = "tb_jogadores")
@Getter
@Setter
public class Jogador {

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String nome;
    private String numeroCamisa;
    private String posicao;
    private Integer nivelTecnico;
    private String whatsapp;
    private String fotoUrl;

    // --- CORREÇÃO AQUI ---
    // Removemos @JsonIgnore e usamos @JsonIgnoreProperties para permitir ver o nome do time
    @ManyToOne
    @JoinColumn(name = "time_id")
    @JsonIgnoreProperties({"jogadores", "organizador"}) 
    private Time time;

    @ManyToOne
    @JoinColumn(name = "organizador_id")
    @JsonIgnore
    private Usuario organizador;

    // Getters e Setters (pode manter os seus ou usar Lombok se estiver configurado)
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getNome() { return nome; }
    public void setNome(String nome) { this.nome = nome; }
    public String getNumeroCamisa() { return numeroCamisa; }
    public void setNumeroCamisa(String numeroCamisa) { this.numeroCamisa = numeroCamisa; }
    public String getPosicao() { return posicao; }
    public void setPosicao(String posicao) { this.posicao = posicao; }
    public Integer getNivelTecnico() { return nivelTecnico; }
    public void setNivelTecnico(Integer nivelTecnico) { this.nivelTecnico = nivelTecnico; }
    public String getWhatsapp() { return whatsapp; }
    public void setWhatsapp(String whatsapp) { this.whatsapp = whatsapp; }
    public String getFotoUrl() { return fotoUrl; }
    public void setFotoUrl(String fotoUrl) { this.fotoUrl = fotoUrl; }
    public Time getTime() { return time; }
    public void setTime(Time time) { this.time = time; }
    public Usuario getOrganizador() { return organizador; }
    public void setOrganizador(Usuario organizador) { this.organizador = organizador; }
}