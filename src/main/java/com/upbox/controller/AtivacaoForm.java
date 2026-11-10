package com.upbox.controller;

import jakarta.validation.constraints.NotBlank;

import lombok.Data;
import lombok.ToString;

/** Somente os campos que o instalador pode enviar na ativação da conta automática. */
@Data
public class AtivacaoForm {
    @NotBlank(message = "Informe uma senha de pelo menos 8 caracteres.")
    @ToString.Exclude
    private String senha;

    @NotBlank(message = "Confirme a senha escolhida.")
    @ToString.Exclude
    private String confirmacao;
}
