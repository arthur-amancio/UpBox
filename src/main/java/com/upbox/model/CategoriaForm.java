package com.upbox.model;

import jakarta.validation.constraints.*;

import lombok.Data;

/** Somente os dados cadastrais permitidos no formulário. */
@Data
public class CategoriaForm {
    @NotBlank
    @Size(max = 100)
    private String nome;

    @Size(max = 300)
    private String descricao;
}
