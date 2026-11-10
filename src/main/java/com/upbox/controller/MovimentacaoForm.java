package com.upbox.controller;

import com.upbox.entity.TipoMovimentacao;

import jakarta.validation.constraints.*;

import lombok.Data;

/** O responsável vem da sessão; não existe usuarioId neste formulário. */
@Data
public class MovimentacaoForm {
    @NotNull(message = "Informe um valor para este campo.")
    private Long produtoId;

    @NotNull(message = "Informe um valor para este campo.")
    private TipoMovimentacao tipo = TipoMovimentacao.ENTRADA;

    @NotNull(message = "Informe um valor para este campo.")
    @Min(value = 1, message = "Informe uma quantidade igual ou maior que {value}.")
    private Integer quantidade;

    @Size(max = 500, message = "Confira o tamanho permitido para este campo.")
    private String observacao;
}
