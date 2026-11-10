package com.upbox.controller;

import jakarta.validation.constraints.*;

import lombok.Data;

/** Somente os dados cadastrais permitidos no formulário. */
@Data
public class CategoriaForm {
    @NotBlank(message = "Preencha este campo obrigatório.")
    @Size(max = 100, message = "Confira o tamanho permitido para este campo.")
    private String nome;

    @Size(max = 300, message = "Confira o tamanho permitido para este campo.")
    private String descricao;
}
