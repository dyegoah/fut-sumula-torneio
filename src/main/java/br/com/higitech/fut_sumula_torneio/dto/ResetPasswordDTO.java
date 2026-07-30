package br.com.higitech.fut_sumula_torneio.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record ResetPasswordDTO(
    @NotBlank @Size(max = 100) String token, 
    @NotBlank @Size(min = 6, max = 50) String novaSenha
) {}