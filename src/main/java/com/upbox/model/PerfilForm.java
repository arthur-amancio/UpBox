package com.upbox.model;

import jakarta.validation.constraints.*;

import lombok.Data;

/** Perfil não permite alterar permissões ou identidade de outro usuário. */
@Data
public class PerfilForm {
    @NotBlank
    @Size(max = 120)
    private String nome;

    @NotBlank
    @Email
    @Size(max = 150)
    private String email;
}
