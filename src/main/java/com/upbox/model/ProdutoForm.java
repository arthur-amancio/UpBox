package com.upbox.model;

import jakarta.validation.constraints.*;

import lombok.Data;

import java.math.BigDecimal;

/** Não possui quantidade: saldo é alterado exclusivamente por movimentações. */
@Data
public class ProdutoForm {
    @NotBlank
    @Size(max = 40)
    private String codigo;

    @NotBlank
    @Size(max = 120)
    private String nome;

    @Size(max = 500)
    private String descricao;

    @NotNull private Long categoriaId;

    @NotNull
    @Min(0)
    private Integer estoqueMinimo;

    @NotNull
    @DecimalMin("0.00")
    @Digits(integer = 10, fraction = 2)
    private BigDecimal preco;
}
