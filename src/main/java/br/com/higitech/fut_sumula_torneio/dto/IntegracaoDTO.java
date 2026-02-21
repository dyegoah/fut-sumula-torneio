package br.com.higitech.fut_sumula_torneio.dto;

import java.time.LocalDate;

public record IntegracaoDTO(
    String nome,
    String email,
    String senha, // Pode vir crua para ser criptografada aqui
    String cidade,
    String uf,
    String whatsapp,
    String genero,
    String idioma,
    LocalDate dataNascimento
) {}