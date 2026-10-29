package com.upbox.controller;

import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import com.upbox.model.*;
import com.upbox.repository.*;
import com.upbox.service.UsuarioService;

import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.mock.web.MockHttpSession;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

/** Ambiente comum dos testes HTTP: cada cenário começa sem dados de outros testes. */
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
abstract class CenarioMvc {
    @Autowired MockMvc mvc;
    @Autowired UsuarioRepository usuarios;
    @Autowired RegistroOperacaoRepository registros;
    @Autowired PasswordEncoder encoder;
    @Autowired UsuarioService service;
    Usuario admin, funcionario;

    @Autowired ProdutoRepository produtos;
    @Autowired CategoriaRepository categorias;
    @Autowired MovimentacaoEstoqueRepository movimentacoes;
    @Autowired com.upbox.service.ProdutoService produtoService;

    @BeforeEach
    void preparar() {
        movimentacoes.deleteAll();
        produtos.deleteAll();
        categorias.deleteAll();
        registros.deleteAll();
        usuarios.deleteAll();
        admin =
                usuarios.save(
                        new Usuario(
                                "Administrador",
                                "admin",
                                "admin@example.com",
                                encoder.encode("senha-teste-123"),
                                PerfilAcesso.ADMINISTRADOR));
        funcionario =
                usuarios.save(
                        new Usuario(
                                "Funcionário",
                                "funcionario",
                                "func@example.com",
                                encoder.encode("senha-teste-123"),
                                PerfilAcesso.FUNCIONARIO));
    }

    MockHttpSession login(String nome) throws Exception {
        return (MockHttpSession)
                mvc.perform(
                                post("/login")
                                        .with(csrf())
                                        .param("username", nome)
                                        .param("password", "senha-teste-123"))
                        .andExpect(status().is3xxRedirection())
                        .andExpect(redirectedUrl("/home"))
                        .andReturn()
                        .getRequest()
                        .getSession(false);
    }
}
