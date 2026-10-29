package com.upbox.service;

import com.upbox.model.*;
import com.upbox.repository.*;

import lombok.RequiredArgsConstructor;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.*;

/** Indicadores reais: entradas e saídas abrangem hoje e os seis dias anteriores. */
@Service
@RequiredArgsConstructor
public class DashboardService {
    private final ProdutoRepository produtos;
    private final MovimentacaoEstoqueRepository movimentacoes;

    @Transactional(readOnly = true)
    public ResumoDashboard consultar() {
        var hoje = LocalDate.now(ZoneId.of("America/Sao_Paulo"));
        var inicio = hoje.minusDays(6).atStartOfDay();
        var fim = hoje.plusDays(1).atStartOfDay();
        var r = new ResumoDashboard();
        r.setTotalProdutos(produtos.count());
        r.setUnidadesDisponiveis(produtos.somarUnidadesAtivas());
        r.setEstoqueBaixo(produtos.pesquisar("", null, true, "BAIXO"));
        r.setEntradasRecentes(movimentacoes.somarQuantidade(TipoMovimentacao.ENTRADA, inicio, fim));
        r.setSaidasRecentes(movimentacoes.somarQuantidade(TipoMovimentacao.SAIDA, inicio, fim));
        r.setUltimasMovimentacoes(movimentacoes.findTop10ByOrderByDataHoraDescIdDesc());
        return r;
    }
}
