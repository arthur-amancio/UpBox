package com.upbox.controller;

import com.upbox.model.PerfilAcesso;

import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import org.junit.jupiter.api.Test;

/** O cadastro inicial não é um cadastro público permanente de administradores. */
class PrimeiroAcessoMvcTest extends CenarioMvc {
    private void deixarBancoVazio() {
        usuarios.deleteAll();
    }

    @Test
    void deveCriarAdministradorUmaVezComBcryptESemExibirSenha() throws Exception {
        deixarBancoVazio();
        mvc.perform(get("/login")).andExpect(redirectedUrl("/primeiro-acesso"));
        mvc.perform(get("/primeiro-acesso")).andExpect(status().isOk());
        mvc.perform(
                        post("/primeiro-acesso")
                                .with(csrf())
                                .param("nome", "Equipe ETEC")
                                .param("usuario", "equipe")
                                .param("email", "equipe@example.com")
                                .param("senha", "senha-teste-123")
                                .param("confirmacao", "senha-teste-123")
                                .param("perfil", "FUNCIONARIO"))
                .andExpect(redirectedUrl("/login?configurado"));
        var criado = usuarios.findAll().get(0);
        assertEquals(com.upbox.model.PerfilAcesso.ADMINISTRADOR, criado.getPerfil());
        assertTrue(encoder.matches("senha-teste-123", criado.getSenhaHash()));
        mvc.perform(post("/primeiro-acesso").with(csrf())).andExpect(status().isForbidden());
        criado.desativar();
        usuarios.saveAndFlush(criado);
        mvc.perform(get("/primeiro-acesso")).andExpect(redirectedUrl("/login"));
        assertEquals(1, usuarios.count());
    }

    @Test
    void deveRecusarAusenciaDeCsrfAcessoRemotoECamposInvalidos() throws Exception {
        deixarBancoVazio();
        mvc.perform(post("/primeiro-acesso")).andExpect(status().isForbidden());
        mvc.perform(
                        get("/primeiro-acesso")
                                .with(
                                        req -> {
                                            req.setRemoteAddr("192.0.2.10");
                                            return req;
                                        }))
                .andExpect(status().isForbidden());
        mvc.perform(
                        post("/primeiro-acesso")
                                .with(csrf())
                                .with(
                                        req -> {
                                            req.setRemoteAddr("192.0.2.10");
                                            return req;
                                        })
                                .header("X-Forwarded-For", "127.0.0.1"))
                .andExpect(status().isForbidden());
        mvc.perform(
                        post("/primeiro-acesso")
                                .with(csrf())
                                .param("nome", "Equipe")
                                .param("usuario", "equipe")
                                .param("email", "equipe@example.com")
                                .param("senha", "curta")
                                .param("confirmacao", "curta"))
                .andExpect(status().isOk())
                .andExpect(model().hasErrors());
        assertEquals(0, usuarios.count());
    }
}
