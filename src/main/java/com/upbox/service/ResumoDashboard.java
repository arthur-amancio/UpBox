package com.upbox.service;

import com.upbox.entity.MovimentacaoEstoque;
import com.upbox.entity.Produto;

import lombok.Data;

import java.util.List;

/** Dados de apresentação calculados no servidor; não altera o estoque. */
@Data
public class ResumoDashboard {
    private long totalProdutos;
    private long unidadesDisponiveis;
    private long entradasRecentes;
    private long saidasRecentes;
    private List<Produto> estoqueBaixo;
    private List<MovimentacaoEstoque> ultimasMovimentacoes;
}
