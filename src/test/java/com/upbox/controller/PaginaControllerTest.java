package com.upbox.controller;

import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import com.upbox.model.*;
import com.upbox.repository.*;

import org.junit.jupiter.api.*;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

/** Exercita a autenticação real e os formulários com o filtro de segurança habilitado. */
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class PaginaControllerTest extends CenarioMvc {
    @Test
    void deveProtegerRotasEManterLoginPublico() throws Exception {
        mvc.perform(get("/login")).andExpect(status().isOk());
        for (String rota :
                new String[] {
                    "/home",
                    "/estoque",
                    "/dashboard",
                    "/movimentacoes",
                    "/relatorios",
                    "/configuracoes",
                    "/usuarios"
                }) mvc.perform(get(rota)).andExpect(status().is3xxRedirection());
        mvc.perform(post("/login").param("username", "admin").param("password", "senha-teste-123"))
                .andExpect(status().isForbidden());
    }

    @Test
    void deveAutenticarPorNomeOuEmailSemDiferenciarCaixa() throws Exception {
        var sessao = login("ADMIN@EXAMPLE.COM");
        for (String rota :
                new String[] {
                    "/home",
                    "/estoque",
                    "/dashboard",
                    "/movimentacoes",
                    "/relatorios",
                    "/configuracoes",
                    "/usuarios",
                    "/usuarios/novo",
                    "/registros"
                }) mvc.perform(get(rota).session(sessao)).andExpect(status().isOk());
        login("ADMIN");
        assertTrue(encoder.matches("senha-teste-123", admin.getSenhaHash()));
        assertNotEquals("senha-teste-123", admin.getSenhaHash());
    }

    @Test
    void deveRecusarSenhaIncorretaEUsuarioInativo() throws Exception {
        mvc.perform(
                        post("/login")
                                .with(csrf())
                                .param("username", "admin")
                                .param("password", "errada"))
                .andExpect(redirectedUrl("/login?erro"));
        funcionario.desativar();
        usuarios.saveAndFlush(funcionario);
        mvc.perform(
                        post("/login")
                                .with(csrf())
                                .param("username", "funcionario")
                                .param("password", "senha-teste-123"))
                .andExpect(redirectedUrl("/login?erro"));
    }

    @Test
    void deveRevogarSessaoAposDesativacaoOuMudancaDePerfil() throws Exception {
        var sessao = login("funcionario");
        funcionario.desativar();
        usuarios.saveAndFlush(funcionario);
        mvc.perform(get("/home").session(sessao)).andExpect(redirectedUrl("/login?expirada"));
        var outra = login("admin");
        admin.atualizar(
                admin.getNome(), admin.getUsuario(), admin.getEmail(), PerfilAcesso.FUNCIONARIO);
        usuarios.saveAndFlush(admin);
        mvc.perform(get("/usuarios").session(outra)).andExpect(redirectedUrl("/login?expirada"));
    }

    @Test
    void funcionarioNaoPodeGerenciarUsuariosNemImprimir() throws Exception {
        var sessao = login("funcionario");
        for (String rota :
                new String[] {
                    "/usuarios", "/usuarios/novo", "/cadastro", "/registros", "/relatorios/imprimir"
                }) mvc.perform(get(rota).session(sessao)).andExpect(status().isForbidden());
        mvc.perform(post("/usuarios").session(sessao).with(csrf()))
                .andExpect(status().isForbidden());
    }

    @Test
    void deveCadastrarUsuarioEImpedirDuplicidade() throws Exception {
        var sessao = login("admin");
        mvc.perform(
                        post("/usuarios")
                                .session(sessao)
                                .with(csrf())
                                .param("nome", "Novo")
                                .param("usuario", "novo")
                                .param("email", "novo@example.com")
                                .param("perfil", "FUNCIONARIO")
                                .param("senha", "senha-nova-123"))
                .andExpect(redirectedUrl("/usuarios"));
        var novo = usuarios.findByUsuarioIgnoreCaseOrEmailIgnoreCase("novo", "novo").get(0);
        assertTrue(encoder.matches("senha-nova-123", novo.getSenhaHash()));
        mvc.perform(
                        post("/usuarios")
                                .session(sessao)
                                .with(csrf())
                                .param("nome", "Outro")
                                .param("usuario", "NOVO")
                                .param("email", "outro@example.com")
                                .param("perfil", "FUNCIONARIO")
                                .param("senha", "senha-nova-123"))
                .andExpect(status().isOk())
                .andExpect(model().attributeHasErrors("form"));
        assertEquals(3, usuarios.count());
    }

    @Test
    void deveProtegerUltimoAdministradorEEncerrarSessao() throws Exception {
        assertThrows(IllegalArgumentException.class, () -> service.alterarStatus(admin.getId()));
        var sessao = login("admin");
        mvc.perform(post("/logout").session(sessao)).andExpect(status().isForbidden());
        mvc.perform(post("/logout").session(sessao).with(csrf()))
                .andExpect(redirectedUrl("/login?logout"));
        assertTrue(sessao.isInvalid());
    }

    @Test
    void deveAlterarSenhaSomenteComSenhaAtualCorreta() throws Exception {
        var s = login("funcionario");
        mvc.perform(
                        post("/configuracoes/senha")
                                .session(s)
                                .with(csrf())
                                .param("atual", "errada")
                                .param("nova", "nova-senha-123")
                                .param("confirmacao", "nova-senha-123"))
                .andExpect(redirectedUrl("/configuracoes"));
        mvc.perform(
                        post("/configuracoes/senha")
                                .session(s)
                                .with(csrf())
                                .param("atual", "senha-teste-123")
                                .param("nova", "nova-senha-123")
                                .param("confirmacao", "nova-senha-123"))
                .andExpect(redirectedUrl("/login?senhaAlterada"));
        assertTrue(s.isInvalid());
        assertTrue(
                encoder.matches(
                        "nova-senha-123",
                        usuarios.findById(funcionario.getId()).orElseThrow().getSenhaHash()));
    }
}
