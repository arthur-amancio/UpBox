package com.upbox.controller;

import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockHttpSession;

/** A senha inicial funciona, mas não libera módulos antes de ser trocada. */
class PrimeiroAcessoMvcTest extends CenarioMvc {
    @org.springframework.beans.factory.annotation.Autowired
    com.upbox.service.PrimeiroAcessoService primeiroAcesso;

    private MockHttpSession entrarPrimeiraVez() throws Exception {
        usuarios.deleteAll();
        primeiroAcesso.criarContaPadrao();
        assertTrue(encoder.matches("admin", usuarios.findAll().get(0).getSenhaHash()));
        return (MockHttpSession)
                mvc.perform(
                                post("/login")
                                        .with(csrf())
                                        .param("username", "administrador")
                                        .param("password", "admin"))
                        .andExpect(redirectedUrl("/configuracoes"))
                        .andReturn()
                        .getRequest()
                        .getSession(false);
    }

    @Test
    void deveEntrarComPadraoExigirTrocaEPermitirNovoLogin() throws Exception {
        var s = entrarPrimeiraVez();
        mvc.perform(get("/estoque").session(s)).andExpect(redirectedUrl("/configuracoes"));
        mvc.perform(get("/configuracoes").session(s)).andExpect(status().isOk());
        mvc.perform(
                        post("/configuracoes/senha")
                                .session(s)
                                .with(csrf())
                                .param("atual", "admin")
                                .param("nova", "nova-senha-123")
                                .param("confirmacao", "nova-senha-123"))
                .andExpect(redirectedUrl("/login?senhaAlterada"));
        var u = usuarios.findAll().get(0);
        assertFalse(u.isTrocaSenhaObrigatoria());
        assertTrue(encoder.matches("nova-senha-123", u.getSenhaHash()));
        mvc.perform(
                        post("/login")
                                .with(csrf())
                                .param("username", "administrador")
                                .param("password", "nova-senha-123"))
                .andExpect(redirectedUrl("/home"));
    }

    @Test
    void naoPodePularTrocaNemUsarSenhaInvalida() throws Exception {
        var s = entrarPrimeiraVez();
        mvc.perform(post("/configuracoes/perfil").session(s).with(csrf()))
                .andExpect(redirectedUrl("/configuracoes"));
        mvc.perform(post("/configuracoes/senha").session(s)).andExpect(status().isForbidden());
        mvc.perform(
                        post("/configuracoes/senha")
                                .session(s)
                                .with(csrf())
                                .param("atual", "admin")
                                .param("nova", "curta")
                                .param("confirmacao", "curta"))
                .andExpect(redirectedUrl("/configuracoes"));
        assertTrue(usuarios.findAll().get(0).isTrocaSenhaObrigatoria());
    }

    @Test
    void reiniciarNaoPodeRedefinirSenhaNemDuplicarConta() throws Exception {
        entrarPrimeiraVez();
        var u = usuarios.findAll().get(0);
        String hash = u.getSenhaHash();
        primeiroAcesso.criarContaPadrao();
        assertEquals(1, usuarios.count());
        assertEquals(hash, usuarios.findAll().get(0).getSenhaHash());
        u.desativar();
        usuarios.saveAndFlush(u);
        primeiroAcesso.criarContaPadrao();
        assertFalse(usuarios.findAll().get(0).isAtivo());
        assertEquals(1, usuarios.count());
    }
}
