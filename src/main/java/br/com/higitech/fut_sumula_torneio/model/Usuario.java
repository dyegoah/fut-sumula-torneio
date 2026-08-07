package br.com.higitech.fut_sumula_torneio.model;

import java.time.LocalDate;
import java.util.Collection;
import java.util.List;

import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import com.fasterxml.jackson.annotation.JsonIgnore;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.Transient;

@Entity
@Table(name = "tb_usuarios")
public class Usuario implements UserDetails {

    @Id 
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    private String nome;
    
    @Column(unique = true)
    private String login; // Email
    
    @JsonIgnore
    private String senha;
    
    // --- DADOS COMPLETOS PARA O ADMIN ---
    private String cidade;
    private String uf;
    private String nomeLiga;
    private String whatsapp; // Contato
    
    // Novos campos solicitados
    private String genero; // MALE / FEMALE
    private String idioma; // PT / EN / ES
    private LocalDate dataNascimento;
    
    @Column(columnDefinition = "VARCHAR(20) DEFAULT 'ATIVO'")
    private String status; 
    
    @Column(columnDefinition = "VARCHAR(20) DEFAULT 'FREE'")
    private String plano; 
    
    @Column(length = 20)
    private String notaCortesia;
    
    @Column(columnDefinition = "VARCHAR(20) DEFAULT 'TORNEIO'")
    private String sistemaOrigem; 
    
    @Column(length = 50)
    private String pais;

    private LocalDate dataCadastro = LocalDate.now();

    // NOVO CAMPO: Dias de teste específicos do usuário
    private Integer trialDays;
    
    // --- VARIÁVEIS TRANSIENTES (Calculadas na hora, não vão para o banco) ---
    @Transient
    private boolean acessoLiberado;
    
    @Transient
    private long diasRestantes;
    
    @Column(name = "usar_2fa")
    private Boolean usar2fa = false; // Por padrão, usuários comuns não usam 2FA

    @Column(name = "chave_2fa")
    @JsonIgnore
    private String chave2fa; // Guarda o código secreto do aplicativo

    // --- LÓGICA DEFINITIVA DE SEGURANÇA DE ACESSO ---
    public boolean isAcessoLiberado() {
        if ("PREMIUM".equals(this.plano) || "CORTESIA".equals(this.plano)) return true;
        if ("INATIVO".equals(this.status)) return false;

        int diasPermitidos = (this.trialDays != null) ? this.trialDays : 15;
        long diasUso = java.time.temporal.ChronoUnit.DAYS.between(this.dataCadastro, java.time.LocalDate.now());
        return diasUso <= diasPermitidos;
    }

    public long calcularDiasRestantes() {
        if ("PREMIUM".equals(this.plano) || "CORTESIA".equals(this.plano)) return 9999;
        int diasPermitidos = (this.trialDays != null) ? this.trialDays : 15;
        long diasUso = java.time.temporal.ChronoUnit.DAYS.between(this.dataCadastro, java.time.LocalDate.now());
        return Math.max(0, diasPermitidos - diasUso);
    }
  
    // --- GETTERS E SETTERS MANUAIS ---
    public String getPais() { return pais; }
    public void setPais(String pais) { this.pais = pais; }
    
    public Boolean getUsar2fa() { return usar2fa; }
    public void setUsar2fa(Boolean usar2fa) { this.usar2fa = usar2fa; }

    public String getChave2fa() { return chave2fa; }
    public void setChave2fa(String chave2fa) { this.chave2fa = chave2fa; }

    public boolean getAcessoLiberado() { return acessoLiberado; }
    public void setAcessoLiberado(boolean acessoLiberado) { this.acessoLiberado = acessoLiberado; }
    public long getDiasRestantes() { return diasRestantes; }
    public void setDiasRestantes(long diasRestantes) { this.diasRestantes = diasRestantes; }
    
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getNome() { return nome; }
    public void setNome(String nome) { this.nome = nome; }

    public String getLogin() { return login; }
    public void setLogin(String login) { this.login = login; }

    public String getSenha() { return senha; }
    public void setSenha(String senha) { this.senha = senha; }

    public String getCidade() { return cidade; }
    public void setCidade(String cidade) { this.cidade = cidade; }

    public String getUf() { return uf; }
    public void setUf(String uf) { this.uf = uf; }

    public String getNomeLiga() { return nomeLiga; }
    public void setNomeLiga(String nomeLiga) { this.nomeLiga = nomeLiga; }
    
    public String getWhatsapp() { return whatsapp; }
    public void setWhatsapp(String whatsapp) { this.whatsapp = whatsapp; }

    public String getGenero() { return genero; }
    public void setGenero(String genero) { this.genero = genero; }

    public String getIdioma() { return idioma; }
    public void setIdioma(String idioma) { this.idioma = idioma; }

    public LocalDate getDataNascimento() { return dataNascimento; }
    public void setDataNascimento(LocalDate dataNascimento) { this.dataNascimento = dataNascimento; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public String getPlano() { return plano; }
    public void setPlano(String plano) { this.plano = plano; }

    public String getNotaCortesia() { return notaCortesia; }
    public void setNotaCortesia(String notaCortesia) { this.notaCortesia = notaCortesia; }

    public String getSistemaOrigem() { return sistemaOrigem; }
    public void setSistemaOrigem(String sistemaOrigem) { this.sistemaOrigem = sistemaOrigem; }

    public LocalDate getDataCadastro() { return dataCadastro; }
    public void setDataCadastro(LocalDate dataCadastro) { this.dataCadastro = dataCadastro; }

    public Integer getTrialDays() { return trialDays; }
    public void setTrialDays(Integer trialDays) { this.trialDays = trialDays; }

    // --- SPRING SECURITY ---
    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        if ("fut_sumula_pro@hotmail.com".equals(this.login)) {
            return List.of(new SimpleGrantedAuthority("ROLE_ADMIN"), new SimpleGrantedAuthority("ROLE_USER"));
        }
        return List.of(new SimpleGrantedAuthority("ROLE_USER"));
    }
    @Override public String getPassword() { return senha; }
    @Override public String getUsername() { return login; }
    @Override public boolean isAccountNonExpired() { return true; }
    @Override public boolean isAccountNonLocked() { return true; }
    @Override public boolean isCredentialsNonExpired() { return true; }
    @Override public boolean isEnabled() { return true; }
}