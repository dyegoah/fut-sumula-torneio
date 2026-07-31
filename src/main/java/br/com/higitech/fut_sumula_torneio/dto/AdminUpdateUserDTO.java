package br.com.higitech.fut_sumula_torneio.dto;

import java.time.LocalDate;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Size;

public record AdminUpdateUserDTO(
    @Size(max = 100) String nome,
    @Email @Size(max = 100) String login,
    @Size(max = 100) String cidade,
    @Size(max = 2) String uf,
    @Size(max = 20) String genero,
    @Size(max = 20) String idioma,
    Integer trialDays,
    @Size(min = 6, max = 50) String novaSenha,
    LocalDate dataNascimento
) {}