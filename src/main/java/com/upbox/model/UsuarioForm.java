package com.upbox.model;

import jakarta.validation.constraints.*;

import lombok.Data;
import lombok.ToString;

/** Dados permitidos no cadastro; nunca recebemos a entidade inteira do navegador. */
@Data
public class UsuarioForm {
    @NotBlank
    @Size(max = 120)
    private String nome;

    @NotBlank
    @Size(max = 80)
    @Pattern(
            regexp = "[A-Za-z0-9._-]+",
            message = "Use letras, números, ponto, hífen ou sublinhado no usuário.")
    private String usuario;

    @NotBlank
    @Email
    @Size(max = 150)
    private String email;

    @NotNull private PerfilAcesso perfil = PerfilAcesso.FUNCIONARIO;
    @ToString.Exclude private String senha;
}
