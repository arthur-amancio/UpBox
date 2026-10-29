package com.upbox.model;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

/** Tipos oferecidos pela interface, sem criar uma API separada. */
@Getter
@RequiredArgsConstructor
public enum TipoRelatorio {
    PRODUTOS("Produtos cadastrados", false),
    ESTOQUE("Estoque atual", false),
    BAIXO("Estoque abaixo ou no mínimo", false),
    ENTRADAS("Entradas", true),
    SAIDAS("Saídas", true),
    HISTORICO("Histórico por período", true);
    private final String titulo;
    private final boolean movimentacoes;
}
