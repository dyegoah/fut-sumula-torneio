package br.com.higitech.fut_sumula_torneio.model;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;

@Entity
@Table(name = "tb_times")
public class Time {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String nome;
    
    // Configurações visuais (Escudo)
    private String corPrimaria;
    private String corSecundaria;
    private String corTerciaria;
    private String customMascot;
    private String customShape;
    private String tipoEscudo; // "CUSTOM" ou "UPLOAD"
    private String escudoUrl;

    // --- A CHAVE PARA APARECER OS JOGADORES ---
    @OneToMany(mappedBy = "time", fetch = FetchType.EAGER)
    @JsonIgnoreProperties("time") // Evita loop infinito
    private List<Jogador> jogadores;
    
 // NOVO CAMPO: DONO DO TIME
    @ManyToOne
    @JoinColumn(name = "organizador_id")
    @JsonIgnore // Não manda os dados do dono no JSON para economizar
    private Usuario organizador;
    

    // --- GETTERS E SETTERS ---
    
 // Getter e Setter
    public Usuario getOrganizador() { return organizador; }
    public void setOrganizador(Usuario organizador) { this.organizador = organizador; }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getNome() { return nome; }
    public void setNome(String nome) { this.nome = nome; }
    public String getCorPrimaria() { return corPrimaria; }
    public void setCorPrimaria(String corPrimaria) { this.corPrimaria = corPrimaria; }
    public String getCorSecundaria() { return corSecundaria; }
    public void setCorSecundaria(String corSecundaria) { this.corSecundaria = corSecundaria; }
    public String getCorTerciaria() { return corTerciaria; }
    public void setCorTerciaria(String corTerciaria) { this.corTerciaria = corTerciaria; }
    public String getCustomMascot() { return customMascot; }
    public void setCustomMascot(String customMascot) { this.customMascot = customMascot; }
    public String getCustomShape() { return customShape; }
    public void setCustomShape(String customShape) { this.customShape = customShape; }
    public String getTipoEscudo() { return tipoEscudo; }
    public void setTipoEscudo(String tipoEscudo) { this.tipoEscudo = tipoEscudo; }
    public String getEscudoUrl() { return escudoUrl; }
    public void setEscudoUrl(String escudoUrl) { this.escudoUrl = escudoUrl; }
    public List<Jogador> getJogadores() { return jogadores; }
    public void setJogadores(List<Jogador> jogadores) { this.jogadores = jogadores; }
}