package br.com.higitech.fut_sumula_torneio.model;

import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

@Entity
@Table(name = "tb_eventos_partida")
public class EventoPartida {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "partida_id")
    private Partida partida;

    @ManyToOne
    private Jogador jogador;

    @ManyToOne
    private Time time; // Time a favor do evento

    @Enumerated(EnumType.STRING)
    private TipoEvento tipo; 

    private String minuto; 

    public enum TipoEvento { GOAL, YELLOW, RED, FOUL, OWN_GOAL }

    // --- GETTERS E SETTERS MANUAIS ---

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Partida getPartida() { return partida; }
    public void setPartida(Partida partida) { this.partida = partida; }

    public Jogador getJogador() { return jogador; }
    public void setJogador(Jogador jogador) { this.jogador = jogador; }

    public Time getTime() { return time; }
    public void setTime(Time time) { this.time = time; }

    public TipoEvento getTipo() { return tipo; }
    public void setTipo(TipoEvento tipo) { this.tipo = tipo; }

    public String getMinuto() { return minuto; }
    public void setMinuto(String minuto) { this.minuto = minuto; }
}