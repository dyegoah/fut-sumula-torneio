package br.com.higitech.fut_sumula_torneio.dto;

import java.time.LocalDate;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record RegisterDTO(
    @NotBlank @Size(max = 100) String nome,
    @NotBlank @Size(max = 100) String login,
    @NotBlank @Size(min = 6, max = 50) String senha,
    @Size(max = 100) String nomeLiga,
    @Size(max = 100) String cidade,
    @Size(max = 2) String uf,
    @Size(max = 50) String pais,
    @Size(max = 20) String whatsapp,
    @Size(max = 20) String genero,
    @Size(max = 20) String idioma,
    LocalDate dataNascimento,
    @Size(max = 50) String sistemaOrigem,
    Integer trialDays
) {}