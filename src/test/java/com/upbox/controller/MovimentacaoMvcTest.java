package com.upbox.controller;

import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import com.upbox.model.*;
import com.upbox.service.MovimentacaoEstoqueService;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

/** Entradas e saídas pelo formulário, incluindo tentativas de adulteração. */
class MovimentacaoMvcTest extends CenarioMvc {
    @Autowired MovimentacaoEstoqueService movimentacaoService;

    private Produto produto() {
        var c = categorias.save(new Categoria("Papelaria", ""));
        return produtoService.cadastrar(
                "P01", "Caderno", "", c.getId(), 10, 3, new java.math.BigDecimal("12.50"));
    }

    @Test
    void deveRegistrarEntradaESaidaComUsuarioDaSessao() throws Exception {
        var p = produto();
        var s = login("funcionario");
        mvc.perform(
                        post("/movimentacoes")
                                .session(s)
                                .with(csrf())
                                .param("produtoId", p.getId().toString())
                                .param("usuarioId", admin.getId().toString())
                                .param("tipo", "ENTRADA")
                                .param("quantidade", "5")
                                .param("observacao", "Compra"))
                .andExpect(redirectedUrl("/movimentacoes?produto=" + p.getId()));
        mvc.perform(
                        post("/movimentacoes")
                                .session(s)
                                .with(csrf())
                                .param("produtoId", p.getId().toString())
                                .param("tipo", "SAIDA")
                                .param("quantidade", "12"))
                .andExpect(redirectedUrl("/movimentacoes?produto=" + p.getId()));
        assertEquals(3, produtos.findById(p.getId()).orElseThrow().getQuantidade());
        var filtro = new FiltroMovimentacao();
        filtro.setProduto(p.getId());
        var lista = movimentacaoService.pesquisar(filtro);
        assertEquals(2, lista.size());
        assertTrue(
                lista.stream().allMatch(m -> m.getUsuario().getId().equals(funcionario.getId())));
        mvc.perform(get("/movimentacoes").session(s).param("produto", p.getId().toString()))
                .andExpect(status().isOk());
    }

    @Test
    void deveRecusarQuantidadesInvalidasESaidaAcimaDoSaldo() throws Exception {
        var p = produto();
        var s = login("funcionario");
        for (String quantidade : new String[] {"0", "-1", "1.5", "2147483648", "11"})
            mvc.perform(
                            post("/movimentacoes")
                                    .session(s)
                                    .with(csrf())
                                    .param("produtoId", p.getId().toString())
                                    .param("tipo", "SAIDA")
                                    .param("quantidade", quantidade))
                    .andExpect(status().isOk())
                    .andExpect(model().attributeHasErrors("form"));
        assertEquals(10, produtos.findById(p.getId()).orElseThrow().getQuantidade());
        assertEquals(0, movimentacoes.count());
    }

    @Test
    void deveRecusarProdutoInativoETipoInvalido() throws Exception {
        var p = produto();
        var s = login("admin");
        p.desativar();
        produtos.saveAndFlush(p);
        mvc.perform(
                        post("/movimentacoes")
                                .session(s)
                                .with(csrf())
                                .param("produtoId", p.getId().toString())
                                .param("tipo", "ENTRADA")
                                .param("quantidade", "1"))
                .andExpect(model().attributeHasErrors("form"));
        mvc.perform(
                        post("/movimentacoes")
                                .session(s)
                                .with(csrf())
                                .param("produtoId", p.getId().toString())
                                .param("tipo", "AJUSTE")
                                .param("quantidade", "1"))
                .andExpect(model().attributeHasErrors("form"));
        assertEquals(0, movimentacoes.count());
    }

    @Test
    void deveFiltrarPeriodoIncluindoUltimoInstanteDoDia() throws Exception {
        var p = produto();
        var dia = java.time.LocalDate.of(2026, 10, 29);
        movimentacoes.save(
                new MovimentacaoEstoque(
                        p,
                        funcionario,
                        TipoMovimentacao.ENTRADA,
                        1,
                        dia.atTime(23, 59, 59, 999000000),
                        "Fim do dia"));
        movimentacoes.save(
                new MovimentacaoEstoque(
                        p,
                        funcionario,
                        TipoMovimentacao.ENTRADA,
                        1,
                        dia.plusDays(1).atStartOfDay(),
                        "Dia seguinte"));
        var s = login("funcionario");
        mvc.perform(
                        get("/movimentacoes")
                                .session(s)
                                .param("inicio", dia.toString())
                                .param("fim", dia.toString()))
                .andExpect(status().isOk())
                .andExpect(model().attribute("movimentacoes", org.hamcrest.Matchers.hasSize(1)));
        mvc.perform(
                        get("/movimentacoes")
                                .session(s)
                                .param("inicio", "2026-10-30")
                                .param("fim", "2026-10-29"))
                .andExpect(status().isBadRequest());
    }
}
