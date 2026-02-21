package br.com.higitech.fut_sumula_torneio.dto;

import java.time.LocalDate;

public record RegisterDTO(
    String nome,
    String login,
    String senha,
    String nomeLiga,
    String cidade,
    String uf,
    String whatsapp,
    String genero,
    String idioma,
    LocalDate dataNascimento,
    String sistemaOrigem,
    Integer trialDays
) {}