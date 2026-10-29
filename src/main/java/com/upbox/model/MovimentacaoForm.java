package com.upbox.model;

import jakarta.validation.constraints.*;

import lombok.Data;

/** O responsável vem da sessão; não existe usuarioId neste formulário. */
@Data
public class MovimentacaoForm {
    @NotNull private Long produtoId;
    @NotNull private TipoMovimentacao tipo = TipoMovimentacao.ENTRADA;

    @NotNull
    @Min(1)
    private Integer quantidade;

    @Size(max = 500)
    private String observacao;
}
