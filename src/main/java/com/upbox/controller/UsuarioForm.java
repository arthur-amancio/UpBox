package com.upbox.controller;

import com.upbox.entity.PerfilAcesso;

import jakarta.validation.constraints.*;

import lombok.Data;
import lombok.ToString;

/** Dados permitidos no cadastro; nunca recebemos a entidade inteira do navegador. */
@Data
public class UsuarioForm {
    @NotBlank(message = "Preencha este campo obrigatório.")
    @Size(max = 120, message = "Confira o tamanho permitido para este campo.")
    private String nome;

    @NotBlank(message = "Preencha este campo obrigatório.")
    @Size(max = 80, message = "Confira o tamanho permitido para este campo.")
    @Pattern(
            regexp = "[A-Za-z0-9._-]+",
            message = "Use letras, números, ponto, hífen ou sublinhado no usuário.")
    private String usuario;

    @NotBlank(message = "Preencha este campo obrigatório.")
    @Email(message = "Informe um e-mail válido.")
    @Size(max = 150, message = "Confira o tamanho permitido para este campo.")
    private String email;

    @NotNull(message = "Informe um valor para este campo.")
    private PerfilAcesso perfil = PerfilAcesso.FUNCIONARIO;

    @ToString.Exclude private String senha;
}
