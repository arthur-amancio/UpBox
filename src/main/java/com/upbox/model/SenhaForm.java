package com.upbox.model;

import jakarta.validation.constraints.NotBlank;

import lombok.Getter;
import lombok.Setter;

/** Senhas não devem aparecer em toString, logs ou no HTML devolvido. */
@Getter
@Setter
public class SenhaForm {
    @NotBlank private String atual;
    @NotBlank private String nova;
    @NotBlank private String confirmacao;
}
