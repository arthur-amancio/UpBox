package com.upbox.model;

import lombok.Data;

import java.util.List;

/** Resultado pronto para consulta ou impressão; não guarda preços históricos fictícios. */
@Data
public class ResultadoRelatorio {
    private TipoRelatorio tipo;
    private List<Produto> produtos = List.of();
    private List<MovimentacaoEstoque> movimentacoes = List.of();

    public long getTotalUnidades() {
        return tipo.isMovimentacoes()
                ? movimentacoes.stream().mapToLong(MovimentacaoEstoque::getQuantidade).sum()
                : produtos.stream().mapToLong(Produto::getQuantidade).sum();
    }
}
