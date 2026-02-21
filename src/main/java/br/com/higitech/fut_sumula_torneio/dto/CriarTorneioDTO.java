package br.com.higitech.fut_sumula_torneio.dto;

import java.util.List;

import lombok.Data;

@Data
public class CriarTorneioDTO {
    private String nome;
    private String tipo;
    private Integer quantidadeTimes;
    private boolean idaEVolta;
    private boolean usarCalendario;
    private Integer modeloTrofeu;
    private String corTrofeu;
    private List<Long> timesIds; // IDs dos times selecionados
	public String getNome() {
		return nome;
	}
	public String getTipo() {
		return tipo;
	}
	public Integer getQuantidadeTimes() {
		return quantidadeTimes;
	}
	public Boolean getIdaEVolta() {
		return idaEVolta;
	}
	public Boolean getUsarCalendario() {
		return usarCalendario;
	}
	public Integer getModeloTrofeu() {
		return modeloTrofeu;
	}
	public String getCorTrofeu() {
		return corTrofeu;
	}
	public List<Long> getTimesIds() {
		return timesIds;
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
	public void setIdaEVolta(Boolean idaEVolta) {
		this.idaEVolta = idaEVolta;
	}
	public void setUsarCalendario(Boolean usarCalendario) {
		this.usarCalendario = usarCalendario;
	}
	public void setModeloTrofeu(Integer modeloTrofeu) {
		this.modeloTrofeu = modeloTrofeu;
	}
	public void setCorTrofeu(String corTrofeu) {
		this.corTrofeu = corTrofeu;
	}
	public void setTimesIds(List<Long> timesIds) {
		this.timesIds = timesIds;
	}
    
    
}