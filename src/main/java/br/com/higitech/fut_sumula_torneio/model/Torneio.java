package br.com.higitech.fut_sumula_torneio.model;

import java.util.ArrayList;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonIgnore;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.JoinTable;
import jakarta.persistence.ManyToMany;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "tb_torneios")
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class Torneio {

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String nome;
    
    // Enum: MATA_MATA, LIGA, COPA (Certifique-se de que seu Enum existe ou use String)
    private String tipo; 
    
    private Integer quantidadeTimes;
    private boolean idaEVolta;
    
    // Visual
    private Integer modeloTrofeu; // 1 a 5
    private String corTrofeu;

    @ManyToMany
    @JoinTable(
        name = "tb_torneio_times",
        joinColumns = @JoinColumn(name = "torneio_id"),
        inverseJoinColumns = @JoinColumn(name = "time_id")
    )
    private List<Time> times = new ArrayList<>();

    // --- NOVO: VÍNCULO COM O DONO ---
    @ManyToOne
    @JoinColumn(name = "organizador_id")
    @JsonIgnore // Não enviar dados do usuário no JSON do torneio
    private Usuario organizador;

	public Long getId() {
		return id;
	}

	public String getNome() {
		return nome;
	}

	public String getTipo() {
		return tipo;
	}

	public Integer getQuantidadeTimes() {
		return quantidadeTimes;
	}

	public boolean isIdaEVolta() {
		return idaEVolta;
	}

	public Integer getModeloTrofeu() {
		return modeloTrofeu;
	}

	public String getCorTrofeu() {
		return corTrofeu;
	}

	public List<Time> getTimes() {
		return times;
	}

	public Usuario getOrganizador() {
		return organizador;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public void setNome(String nome) {
		this.nome = nome;
	}

	public void setTipo(String tipo) {
		this.tipo = tipo;
	}

	public void setQuantidadeTimes(Integer quantidadeTimes) {
		this.quantidadeTimes = quantidadeTimes;
	}

	public void setIdaEVolta(boolean idaEVolta) {
		this.idaEVolta = idaEVolta;
	}

	public void setModeloTrofeu(Integer modeloTrofeu) {
		this.modeloTrofeu = modeloTrofeu;
	}

	public void setCorTrofeu(String corTrofeu) {
		this.corTrofeu = corTrofeu;
	}

	public void setTimes(List<Time> times) {
		this.times = times;
	}

	public void setOrganizador(Usuario organizador) {
		this.organizador = organizador;
	}
    
    
}