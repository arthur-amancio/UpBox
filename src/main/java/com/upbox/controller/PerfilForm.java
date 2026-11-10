package com.upbox.controller;

import jakarta.validation.constraints.*;

import lombok.Data;

/** Perfil não permite alterar permissões ou identidade de outro usuário. */
@Data
public class PerfilForm {
    @NotBlank(message = "Preencha este campo obrigatório.")
    @Size(max = 120, message = "Confira o tamanho permitido para este campo.")
    private String nome;

    @NotBlank(message = "Preencha este campo obrigatório.")
    @Email(message = "Informe um e-mail válido.")
    @Size(max = 150, message = "Confira o tamanho permitido para este campo.")
    private String email;
}
