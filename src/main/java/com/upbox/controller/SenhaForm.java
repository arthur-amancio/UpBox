package com.upbox.controller;

import jakarta.validation.constraints.NotBlank;

import lombok.Getter;
import lombok.Setter;

/** Senhas não devem aparecer em toString, logs ou no HTML devolvido. */
@Getter
@Setter
public class SenhaForm {
    @NotBlank(message = "Preencha este campo obrigatório.")
    private String atual;

    @NotBlank(message = "Preencha este campo obrigatório.")
    private String nova;

    @NotBlank(message = "Preencha este campo obrigatório.")
    private String confirmacao;
}
