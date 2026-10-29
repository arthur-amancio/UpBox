package com.upbox.controller;

import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import com.upbox.model.*;

import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockHttpSession;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;

/** Cadastros exercitados pelas mesmas rotas e validações usadas no navegador. */
class CadastroMvcTest extends CenarioMvc {
    private MockHttpServletRequestBuilder produto(String rota, MockHttpSession s, Long categoria) {
        return post(rota)
                .session(s)
                .with(csrf())
                .param("codigo", "prd-001")
                .param("nome", "Caderno")
                .param("descricao", "96 folhas")
                .param("categoriaId", categoria.toString())
                .param("estoqueMinimo", "3")
                .param("preco", "12.50");
    }

    @Test
    void deveCadastrarEditarEDesativarCategoriaSemApagarProdutos() throws Exception {
        var s = login("admin");
        mvc.perform(
                        post("/categorias")
                                .session(s)
                                .with(csrf())
                                .param("nome", "Papelaria")
                                .param("descricao", "Materiais"))
                .andExpect(redirectedUrl("/categorias"));
        var c = categorias.findByNomeIgnoreCase("Papelaria").orElseThrow();
        mvc.perform(post("/categorias").session(s).with(csrf()).param("nome", "PAPELARIA"))
                .andExpect(model().attributeHasErrors("form"));
        mvc.perform(post("/categorias").session(s).with(csrf()).param("nome", " "))
                .andExpect(model().attributeHasErrors("form"));
        mvc.perform(produto("/produtos", s, c.getId())).andExpect(redirectedUrl("/estoque"));
        mvc.perform(get("/categorias/" + c.getId() + "/editar").session(s))
                .andExpect(status().isOk());
        mvc.perform(
                        post("/categorias/" + c.getId())
                                .session(s)
                                .with(csrf())
                                .param("nome", "Material escolar"))
                .andExpect(redirectedUrl("/categorias"));
        mvc.perform(post("/categorias/" + c.getId() + "/status").session(s).with(csrf()))
                .andExpect(redirectedUrl("/categorias"));
        assertFalse(categorias.findById(c.getId()).orElseThrow().isAtiva());
        assertEquals(1, produtos.count());
        mvc.perform(get("/estoque").session(s)).andExpect(status().isOk());
    }

    @Test
    void deveCadastrarProdutoPesquisarEImpedirAlteracaoLivreDoSaldo() throws Exception {
        var s = login("admin");
        var c = categorias.save(new Categoria("Papelaria", ""));
        mvc.perform(produto("/produtos", s, c.getId()).param("quantidade", "999"))
                .andExpect(redirectedUrl("/estoque"));
        var p = produtos.findAll().get(0);
        assertEquals("PRD-001", p.getCodigo());
        assertEquals(0, p.getQuantidade());
        assertTrue(p.isEstoqueBaixo());
        assertEquals(0, new java.math.BigDecimal("12.50").compareTo(p.getPreco()));
        mvc.perform(produto("/produtos", s, c.getId()))
                .andExpect(model().attributeHasErrors("form"));
        mvc.perform(get("/produtos/" + p.getId() + "/editar").session(s))
                .andExpect(status().isOk());
        mvc.perform(produto("/produtos/" + p.getId(), s, c.getId()).param("quantidade", "100"))
                .andExpect(redirectedUrl("/estoque"));
        assertEquals(0, produtos.findById(p.getId()).orElseThrow().getQuantidade());
        mvc.perform(
                        get("/estoque")
                                .session(s)
                                .param("busca", "caderno")
                                .param("categoria", c.getId().toString())
                                .param("situacao", "BAIXO"))
                .andExpect(status().isOk())
                .andExpect(model().attribute("produtos", org.hamcrest.Matchers.hasSize(1)));
        mvc.perform(get("/estoque").session(s).param("busca", "PRD-001"))
                .andExpect(model().attribute("produtos", org.hamcrest.Matchers.hasSize(1)));
        mvc.perform(post("/produtos/" + p.getId() + "/status").session(s).with(csrf()))
                .andExpect(redirectedUrl("/estoque"));
        assertFalse(produtos.findById(p.getId()).orElseThrow().isAtivo());
    }

    @Test
    void deveValidarPrecoMinimoECategoria() throws Exception {
        var s = login("admin");
        var c = categorias.save(new Categoria("Papelaria", ""));
        for (String preco : new String[] {"", "-0.01", "1.001", "10000000000.00"})
            mvc.perform(
                            produto("/produtos", s, c.getId())
                                    .with(
                                            r -> {
                                                r.setParameter("preco", preco);
                                                return r;
                                            }))
                    .andExpect(model().attributeHasErrors("form"));
        mvc.perform(
                        produto("/produtos", s, c.getId())
                                .with(
                                        r -> {
                                            r.setParameter("estoqueMinimo", "-1");
                                            return r;
                                        }))
                .andExpect(model().attributeHasErrors("form"));
        c.desativar();
        categorias.saveAndFlush(c);
        mvc.perform(produto("/produtos", s, c.getId()))
                .andExpect(model().attributeHasErrors("form"));
        assertEquals(0, produtos.count());
    }

    @Test
    void funcionarioConsultaMasNaoGerenciaCadastros() throws Exception {
        var s = login("funcionario");
        var c = categorias.save(new Categoria("Papelaria", ""));
        mvc.perform(get("/estoque").session(s)).andExpect(status().isOk());
        mvc.perform(get("/categorias").session(s)).andExpect(status().isForbidden());
        mvc.perform(get("/produtos/novo").session(s)).andExpect(status().isForbidden());
        mvc.perform(produto("/produtos", s, c.getId())).andExpect(status().isForbidden());
        mvc.perform(post("/categorias").session(s).with(csrf()).param("nome", "Teste"))
                .andExpect(status().isForbidden());
    }
}
