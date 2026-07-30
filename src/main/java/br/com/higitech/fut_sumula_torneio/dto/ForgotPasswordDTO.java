package br.com.higitech.fut_sumula_torneio.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record ForgotPasswordDTO(
    @NotBlank @Size(max = 100) String email
) {}