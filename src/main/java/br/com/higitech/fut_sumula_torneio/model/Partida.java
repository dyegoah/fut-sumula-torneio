package br.com.higitech.fut_sumula_torneio.model;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;
import jakarta.persistence.Transient;
@Entity
@Table(name = "tb_partidas")
public class Partida {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "torneio_id")
    @JsonIgnoreProperties({"partidas", "times", "organizador"}) 
    private Torneio torneio;

    @ManyToOne
    @JoinColumn(name = "time_casa_id")
    private Time timeCasa;

    @ManyToOne
    @JoinColumn(name = "time_visitante_id")
    private Time timeVisitante;

    private Integer placarCasa = 0;
    private Integer placarVisitante = 0;
    
    private Integer rodada; 
    private String grupo;   
    private String fase;    

    private LocalDateTime dataHora;
    private Boolean finalizada = false;
    
    @Column(columnDefinition = "TEXT")
    private String sumulaHtml; 

    @OneToMany(mappedBy = "partida", cascade = CascadeType.ALL)
    @JsonIgnoreProperties("partida") 
    private List<EventoPartida> eventos;

    // --- CAMPOS NOVOS (ESSENCIAIS PARA O MATA-MATA) ---
    // Guarda o ID do jogo anterior. Ex: Jogo 3 recebe vencedor do Jogo 1.
    private Long partidaOrigemCasaId;      
    private Long partidaOrigemVisitanteId; 

    // Campos auxiliares para a criação (não salvam no banco)
    @Transient private Integer tempIndexOrigemCasa;
    @Transient private Integer tempIndexOrigemVisitante;

 // --- BLINDAGEM ANTI-SCRAPING ---
    @Column(unique = true, updatable = false)
    private String codigoPublico;

    @PrePersist
    protected void onCreate() {
        if (this.codigoPublico == null) {
            this.codigoPublico = UUID.randomUUID().toString();
        }
    }

    // --- GETTERS E SETTERS ---

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Torneio getTorneio() { return torneio; }
    public void setTorneio(Torneio torneio) { this.torneio = torneio; }

    public Time getTimeCasa() { return timeCasa; }
    public void setTimeCasa(Time timeCasa) { this.timeCasa = timeCasa; }

    public Time getTimeVisitante() { return timeVisitante; }
    public void setTimeVisitante(Time timeVisitante) { this.timeVisitante = timeVisitante; }

    public Integer getPlacarCasa() { return placarCasa; }
    public void setPlacarCasa(Integer placarCasa) { this.placarCasa = placarCasa; }

    public Integer getPlacarVisitante() { return placarVisitante; }
    public void setPlacarVisitante(Integer placarVisitante) { this.placarVisitante = placarVisitante; }

    public Integer getRodada() { return rodada; }
    public void setRodada(Integer rodada) { this.rodada = rodada; }

    public String getGrupo() { return grupo; }
    public void setGrupo(String grupo) { this.grupo = grupo; }

    public String getFase() { return fase; }
    public void setFase(String fase) { this.fase = fase; }

    public LocalDateTime getDataHora() { return dataHora; }
    public void setDataHora(LocalDateTime dataHora) { this.dataHora = dataHora; }

    public Boolean getFinalizada() { return finalizada; }
    public void setFinalizada(Boolean finalizada) { this.finalizada = finalizada; }

    public String getSumulaHtml() { return sumulaHtml; }
    public void setSumulaHtml(String sumulaHtml) { this.sumulaHtml = sumulaHtml; }

    public List<EventoPartida> getEventos() { return eventos; }
    public void setEventos(List<EventoPartida> eventos) { this.eventos = eventos; }

    // GETTERS E SETTERS DOS NOVOS CAMPOS
    public Long getPartidaOrigemCasaId() { return partidaOrigemCasaId; }
    public void setPartidaOrigemCasaId(Long partidaOrigemCasaId) { this.partidaOrigemCasaId = partidaOrigemCasaId; }

    public Long getPartidaOrigemVisitanteId() { return partidaOrigemVisitanteId; }
    public void setPartidaOrigemVisitanteId(Long partidaOrigemVisitanteId) { this.partidaOrigemVisitanteId = partidaOrigemVisitanteId; }

    public Integer getTempIndexOrigemCasa() { return tempIndexOrigemCasa; }
    public void setTempIndexOrigemCasa(Integer tempIndexOrigemCasa) { this.tempIndexOrigemCasa = tempIndexOrigemCasa; }

    public Integer getTempIndexOrigemVisitante() { return tempIndexOrigemVisitante; }
    public void setTempIndexOrigemVisitante(Integer tempIndexOrigemVisitante) { this.tempIndexOrigemVisitante = tempIndexOrigemVisitante; }
    
    public String getCodigoPublico() { return codigoPublico; }
    public void setCodigoPublico(String codigoPublico) { this.codigoPublico = codigoPublico; }
}