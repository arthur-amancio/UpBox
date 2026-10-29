package com.upbox.controller;

import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import com.upbox.model.*;
import com.upbox.service.*;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

/** Confere valores, filtros e permissões do dashboard e dos seis relatórios. */
class RelatorioMvcTest extends CenarioMvc {
    @Autowired DashboardService dashboard;
    @Autowired RelatorioService relatorios;
    @Autowired MovimentacaoEstoqueService estoque;

    private Produto produto(String codigo, Categoria c, int saldo, int minimo) {
        return produtoService.cadastrar(
                codigo,
                "Produto " + codigo,
                "",
                c.getId(),
                saldo,
                minimo,
                new java.math.BigDecimal("1.50"));
    }

    @Test
    void deveMostrarEstadoVazioSemDadosDemonstrativos() throws Exception {
        var s = login("funcionario");
        mvc.perform(get("/dashboard").session(s)).andExpect(status().isOk());
        mvc.perform(get("/relatorios").session(s))
                .andExpect(status().isOk())
                .andExpect(
                        content()
                                .string(
                                        org.hamcrest.Matchers.containsString(
                                                "Nenhum produto foi encontrado.")))
                .andExpect(
                        content()
                                .string(
                                        org.hamcrest.Matchers.not(
                                                org.hamcrest.Matchers.containsString(
                                                        "Preparar impressão/PDF"))));
        var r = dashboard.consultar();
        assertEquals(0, r.getTotalProdutos());
        assertEquals(0, r.getUnidadesDisponiveis());
        assertEquals(0, r.getEntradasRecentes());
    }

    @Test
    void deveCalcularIndicadoresDoBancoSemContarSaldoInativo() {
        var c = categorias.save(new Categoria("Teste", ""));
        var p1 = produto("P1", c, 10, 3);
        var p2 = produto("P2", c, 2, 2);
        var inativo = produto("P3", c, 99, 5);
        inativo.desativar();
        produtos.saveAndFlush(inativo);
        estoque.registrar(p1.getId(), funcionario.getId(), TipoMovimentacao.SAIDA, 7, "");
        estoque.registrar(p2.getId(), funcionario.getId(), TipoMovimentacao.ENTRADA, 5, "");
        var r = dashboard.consultar();
        assertEquals(3, r.getTotalProdutos());
        assertEquals(10, r.getUnidadesDisponiveis());
        assertEquals(1, r.getEstoqueBaixo().size());
        assertEquals(5, r.getEntradasRecentes());
        assertEquals(7, r.getSaidasRecentes());
        assertEquals(2, r.getUltimasMovimentacoes().size());
    }

    @Test
    void deveSepararRelatoriosDeProdutosEstoqueEBaixo() throws Exception {
        var c = categorias.save(new Categoria("Teste", ""));
        var p1 = produto("P1", c, 3, 3);
        var p2 = produto("P2", c, 9, 1);
        p2.desativar();
        produtos.saveAndFlush(p2);
        var filtro = new FiltroMovimentacao();
        assertEquals(2, relatorios.consultar(TipoRelatorio.PRODUTOS, filtro).getProdutos().size());
        assertEquals(1, relatorios.consultar(TipoRelatorio.ESTOQUE, filtro).getProdutos().size());
        assertEquals(1, relatorios.consultar(TipoRelatorio.BAIXO, filtro).getProdutos().size());
        var s = login("admin");
        for (var tipo : TipoRelatorio.values())
            mvc.perform(get("/relatorios").session(s).param("relatorio", tipo.name()))
                    .andExpect(status().isOk());
    }

    @Test
    void deveFiltrarEntradasSaidasProdutoCategoriaEPeriodo() throws Exception {
        var c1 = categorias.save(new Categoria("Papelaria", ""));
        var c2 = categorias.save(new Categoria("Limpeza", ""));
        var p1 = produto("P1", c1, 3, 3);
        var p2 = produto("P2", c2, 3, 3);
        var dia = java.time.LocalDate.of(2026, 10, 29);
        movimentacoes.save(
                new MovimentacaoEstoque(
                        p1,
                        funcionario,
                        TipoMovimentacao.ENTRADA,
                        4,
                        dia.atTime(23, 59, 59, 999000000),
                        ""));
        movimentacoes.save(
                new MovimentacaoEstoque(
                        p1, funcionario, TipoMovimentacao.SAIDA, 2, dia.atTime(12, 0), ""));
        movimentacoes.save(
                new MovimentacaoEstoque(
                        p2, funcionario, TipoMovimentacao.ENTRADA, 8, dia.atTime(12, 0), ""));
        var f = new FiltroMovimentacao();
        f.setCategoria(c1.getId());
        f.setProduto(p1.getId());
        f.setInicio(dia);
        f.setFim(dia);
        var entradas = relatorios.consultar(TipoRelatorio.ENTRADAS, f);
        assertEquals(1, entradas.getMovimentacoes().size());
        assertEquals(4, entradas.getTotalUnidades());
        var saidas = relatorios.consultar(TipoRelatorio.SAIDAS, f);
        assertEquals(1, saidas.getMovimentacoes().size());
        assertEquals(2, saidas.getTotalUnidades());
        mvc.perform(
                        get("/relatorios")
                                .session(login("funcionario"))
                                .param("relatorio", "HISTORICO")
                                .param("categoria", c1.getId().toString())
                                .param("inicio", "2026-10-29")
                                .param("fim", "2026-10-29"))
                .andExpect(status().isOk());
        f.setInicio(dia.plusDays(1));
        assertThrows(
                IllegalArgumentException.class,
                () -> relatorios.consultar(TipoRelatorio.HISTORICO, f));
    }

    @Test
    void somenteAdministradorPodePrepararImpressao() throws Exception {
        mvc.perform(get("/relatorios/imprimir").session(login("funcionario")))
                .andExpect(status().isForbidden());
        mvc.perform(
                        get("/relatorios/imprimir")
                                .session(login("admin"))
                                .param("relatorio", "ESTOQUE"))
                .andExpect(status().isOk())
                .andExpect(model().attribute("impressao", true))
                .andExpect(
                        content()
                                .string(
                                        org.hamcrest.Matchers.containsString(
                                                "Imprimir / Salvar como PDF")))
                .andExpect(
                        content()
                                .string(
                                        org.hamcrest.Matchers.not(
                                                org.hamcrest.Matchers.containsString(
                                                        "exportado em PDF com sucesso"))));
    }
}
