package com.upbox.service;

import com.upbox.model.*;

import lombok.RequiredArgsConstructor;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/** Reutiliza as consultas dos serviços; filtros são iguais na tela e na impressão. */
@Service
@RequiredArgsConstructor
public class RelatorioService {
    private final ProdutoService produtos;
    private final MovimentacaoEstoqueService movimentacoes;

    @Transactional(readOnly = true)
    public ResultadoRelatorio consultar(TipoRelatorio tipo, FiltroMovimentacao filtro) {
        filtro.validar();
        var r = new ResultadoRelatorio();
        r.setTipo(tipo);
        if (tipo.isMovimentacoes()) {
            // Tipo fixo de relatório prevalece sobre o seletor usado no histórico geral.
            if (tipo == TipoRelatorio.ENTRADAS) filtro.setTipo(TipoMovimentacao.ENTRADA);
            if (tipo == TipoRelatorio.SAIDAS) filtro.setTipo(TipoMovimentacao.SAIDA);
            r.setMovimentacoes(movimentacoes.pesquisar(filtro));
        } else {
            var lista =
                    produtos.pesquisar(
                            "",
                            filtro.getCategoria(),
                            tipo == TipoRelatorio.PRODUTOS ? null : true,
                            tipo == TipoRelatorio.BAIXO ? "BAIXO" : "");
            r.setProdutos(
                    lista.stream()
                            .filter(
                                    p ->
                                            filtro.getProduto() == null
                                                    || p.getId().equals(filtro.getProduto()))
                            .toList());
        }
        return r;
    }
}
