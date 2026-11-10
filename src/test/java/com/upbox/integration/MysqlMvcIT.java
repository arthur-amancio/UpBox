package com.upbox.integration;

import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import com.upbox.controller.UsuarioForm;
import com.upbox.entity.*;
import com.upbox.repository.*;
import com.upbox.service.*;
import com.upbox.service.UsuarioService;

import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.mock.web.MockHttpSession;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.web.servlet.MockMvc;

/** Jornada MVC com SQL real: o mesmo teste roda nos MySQL 9.6 e 16 da CI. */
@SpringBootTest
@AutoConfigureMockMvc
class MysqlMvcIT {
    @DynamicPropertySource
    static void banco(DynamicPropertyRegistry r) {
        MysqlIT.banco(r);
    }

    @Autowired MockMvc mvc;
    @Autowired JdbcTemplate jdbc;
    @Autowired UsuarioService usuarios;
    @Autowired UsuarioRepository usuarioRepository;
    @Autowired CategoriaRepository categorias;
    @Autowired ProdutoRepository produtos;
    @Autowired MovimentacaoEstoqueRepository movimentacoes;
    @Autowired PasswordEncoder encoder;

    @BeforeEach
    void limparBancoExclusivo() {
        jdbc.execute("delete from movimentacoes_estoque");
        for (String tabela :
                new String[] {"produtos", "categorias", "registros_operacoes", "usuarios"})
            jdbc.execute("delete from " + tabela);
    }

    @Test
    void jornadaComSessaoCadastrosEstoqueRelatoriosEPermissoes() throws Exception {
        String versao = jdbc.queryForObject("select version()", String.class);
        System.out.println("MySQL real da jornada: " + versao);
        UsuarioForm f = new UsuarioForm();
        f.setNome("Administrador");
        f.setUsuario("admin");
        f.setEmail("admin@example.com");
        f.setSenha("senha-teste-123");
        f.setPerfil(PerfilAcesso.ADMINISTRADOR);
        var admin = usuarios.salvar(null, f);
        assertTrue(encoder.matches(f.getSenha(), admin.getSenhaHash()));
        mvc.perform(get("/estoque")).andExpect(status().is3xxRedirection());
        mvc.perform(
                        post("/login")
                                .with(csrf())
                                .param("username", "admin")
                                .param("password", "errada"))
                .andExpect(redirectedUrl("/login?erro"));
        var sessao =
                (MockHttpSession)
                        mvc.perform(
                                        post("/login")
                                                .with(csrf())
                                                .param("username", "admin@example.com")
                                                .param("password", f.getSenha()))
                                .andExpect(redirectedUrl("/home"))
                                .andReturn()
                                .getRequest()
                                .getSession(false);
        mvc.perform(
                        post("/categorias")
                                .session(sessao)
                                .with(csrf())
                                .param("nome", "Papelaria")
                                .param("descricao", "Escola"))
                .andExpect(redirectedUrl("/categorias"));
        long categoria = categorias.findAll().get(0).getId();
        mvc.perform(
                        post("/produtos")
                                .session(sessao)
                                .with(csrf())
                                .param("codigo", "P01")
                                .param("nome", "Caderno")
                                .param("descricao", "Escolar")
                                .param("categoriaId", "" + categoria)
                                .param("estoqueMinimo", "4")
                                .param("preco", "12.50"))
                .andExpect(redirectedUrl("/estoque"));
        long produto = produtos.findAll().get(0).getId();
        for (String tipo : new String[] {"ENTRADA", "SAIDA"}) {
            mvc.perform(
                            post("/movimentacoes")
                                    .session(sessao)
                                    .with(csrf())
                                    .param("produtoId", "" + produto)
                                    .param("tipo", tipo)
                                    .param("quantidade", tipo.equals("ENTRADA") ? "10" : "7")
                                    .param("observacao", "Jornada MySQL"))
                    .andExpect(status().is3xxRedirection());
        }
        mvc.perform(
                        post("/movimentacoes")
                                .session(sessao)
                                .with(csrf())
                                .param("produtoId", "" + produto)
                                .param("tipo", "SAIDA")
                                .param("quantidade", "4"))
                .andExpect(status().isOk())
                .andExpect(model().hasErrors());
        assertEquals(3, produtos.findById(produto).orElseThrow().getQuantidade());
        assertEquals(2, movimentacoes.count());
        assertNotNull(movimentacoes.findAll().get(0).getDataHora());
        mvc.perform(get("/dashboard").session(sessao)).andExpect(status().isOk());
        for (TipoRelatorio tipo : TipoRelatorio.values()) {
            mvc.perform(get("/relatorios").session(sessao).param("relatorio", tipo.name()))
                    .andExpect(status().isOk());
            mvc.perform(
                            get("/relatorios")
                                    .session(sessao)
                                    .param("relatorio", tipo.name())
                                    .param("produto", "" + produto)
                                    .param("inicio", "2020-01-01")
                                    .param("fim", "2035-12-31"))
                    .andExpect(status().isOk());
        }
        mvc.perform(
                        get("/relatorios")
                                .session(sessao)
                                .param("relatorio", "HISTORICO")
                                .param("inicio", "1000-01-01")
                                .param("fim", "9999-12-31"))
                .andExpect(status().isOk());
        f.setNome("Funcionário");
        f.setUsuario("funcionario");
        f.setEmail("func@example.com");
        f.setPerfil(PerfilAcesso.FUNCIONARIO);
        usuarios.salvar(null, f);
        var funcionario =
                (MockHttpSession)
                        mvc.perform(
                                        post("/login")
                                                .with(csrf())
                                                .param("username", f.getUsuario())
                                                .param("password", f.getSenha()))
                                .andExpect(redirectedUrl("/home"))
                                .andReturn()
                                .getRequest()
                                .getSession(false);
        mvc.perform(get("/usuarios").session(funcionario)).andExpect(status().isForbidden());
        mvc.perform(post("/movimentacoes").session(funcionario)).andExpect(status().isForbidden());
        var u =
                usuarioRepository.findAll().stream()
                        .filter(x -> x.getUsuario().equals("funcionario"))
                        .findFirst()
                        .orElseThrow();
        usuarios.alterarStatus(u.getId());
        mvc.perform(get("/estoque").session(funcionario)).andExpect(status().is3xxRedirection());
    }
}
