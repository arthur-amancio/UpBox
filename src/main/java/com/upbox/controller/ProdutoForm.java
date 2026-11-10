package com.upbox.controller;

import jakarta.validation.constraints.*;

import lombok.Data;

import java.math.BigDecimal;

/** Não possui quantidade: saldo é alterado exclusivamente por movimentações. */
@Data
public class ProdutoForm {
    @NotBlank(message = "Preencha este campo obrigatório.")
    @Size(max = 40, message = "Confira o tamanho permitido para este campo.")
    private String codigo;

    @NotBlank(message = "Preencha este campo obrigatório.")
    @Size(max = 120, message = "Confira o tamanho permitido para este campo.")
    private String nome;

    @Size(max = 500, message = "Confira o tamanho permitido para este campo.")
    private String descricao;

    @NotNull(message = "Informe um valor para este campo.")
    private Long categoriaId;

    @NotNull(message = "Informe um valor para este campo.")
    @Min(value = 0, message = "Informe uma quantidade igual ou maior que {value}.")
    private Integer estoqueMinimo;

    @NotNull(message = "Informe um valor para este campo.")
    @DecimalMin(value = "0.00", message = "Informe valor igual ou maior que {value}.")
    @Digits(
            integer = 10,
            fraction = 2,
            message = "Confira a quantidade de dígitos e casas decimais.")
    private BigDecimal preco;
}
