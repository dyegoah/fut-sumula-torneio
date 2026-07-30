package br.com.higitech.fut_sumula_torneio.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record AuthenticationDTO(
    @NotBlank @Size(max = 100) String login, 
    @NotBlank @Size(max = 50) String senha
) {}