package com.upbox.integration;

import static org.junit.jupiter.api.Assertions.*;

import com.upbox.model.*;
import com.upbox.repository.*;
import com.upbox.service.*;

import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;

import java.math.BigDecimal;
import java.util.concurrent.*;

/** Integração real, optativa. Só aceita o banco exclusivo upbox_test, nunca o banco escolar. */
@SpringBootTest
class PostgresIT {
    @DynamicPropertySource
    static void banco(DynamicPropertyRegistry r) {
        String url =
                System.getenv()
                        .getOrDefault(
                                "UPBOX_TEST_DB_URL", "jdbc:postgresql://localhost:5432/upbox_test");
        // Executado antes da inicialização JPA e do create-drop.
        if (!url.matches("jdbc:postgresql://[^/]+/upbox_test(\\?.*)?"))
            throw new IllegalArgumentException(
                    "Integração exige banco exclusivo chamado upbox_test.");
        r.add("spring.datasource.url", () -> url);
        r.add(
                "spring.datasource.username",
                () -> System.getenv().getOrDefault("UPBOX_TEST_DB_USER", "postgres"));
        r.add(
                "spring.datasource.password",
                () -> System.getenv().getOrDefault("UPBOX_TEST_DB_PASSWORD", ""));
        r.add("spring.datasource.driver-class-name", () -> "org.postgresql.Driver");
        r.add("spring.jpa.hibernate.ddl-auto", () -> "create-drop");
        r.add("upbox.admin-inicial.habilitado", () -> "false");
    }

    @Autowired ProdutoRepository produtos;
    @Autowired CategoriaRepository categorias;
    @Autowired UsuarioRepository usuarios;
    @Autowired MovimentacaoEstoqueRepository movimentacoes;
    @Autowired ProdutoService produtoService;
    @Autowired MovimentacaoEstoqueService service;
    @Autowired JdbcTemplate jdbc;
    Produto produto;
    Usuario usuario;

    @BeforeEach
    void preparar() {
        jdbc.execute(
                "alter table movimentacoes_estoque drop constraint if exists"
                        + " falha_historico_teste");
        jdbc.execute(
                "alter table registros_operacoes drop constraint if exists falha_auditoria_teste");
        jdbc.execute(
                "truncate table movimentacoes_estoque, produtos, categorias, usuarios,"
                        + " registros_operacoes restart identity cascade");
        var c = categorias.save(new Categoria("Papelaria", ""));
        produto =
                produtoService.cadastrar(
                        "P01", "Caderno", "", c.getId(), 5, 3, new BigDecimal("12.50"));
        usuario =
                usuarios.save(
                        new Usuario(
                                "Teste",
                                "teste",
                                "teste@example.com",
                                "hash-ficticio-integracao",
                                PerfilAcesso.FUNCIONARIO));
    }

    @Test
    void duasSaidasSimultaneasNaoPodemRetirarOMesmoSaldo() throws Exception {
        var pool = Executors.newFixedThreadPool(2);
        var prontas = new CountDownLatch(2);
        var iniciar = new CountDownLatch(1);
        Callable<Boolean> saida =
                () -> {
                    prontas.countDown();
                    if (!iniciar.await(10, TimeUnit.SECONDS))
                        throw new IllegalStateException("Sincronização falhou.");
                    try {
                        service.registrar(
                                produto.getId(), usuario.getId(), TipoMovimentacao.SAIDA, 4, "");
                        return true;
                    } catch (IllegalArgumentException ex) {
                        assertEquals("Estoque insuficiente.", ex.getMessage());
                        return false;
                    }
                };
        try {
            var a = pool.submit(saida);
            var b = pool.submit(saida);
            assertTrue(prontas.await(10, TimeUnit.SECONDS));
            iniciar.countDown();
            assertNotEquals(a.get(15, TimeUnit.SECONDS), b.get(15, TimeUnit.SECONDS));
            assertEquals(1, produtos.findById(produto.getId()).orElseThrow().getQuantidade());
            assertEquals(1, movimentacoes.count());
        } finally {
            iniciar.countDown();
            pool.shutdownNow();
        }
    }

    @Test
    void deveDesfazerOperacaoSeHistoricoFalhar() {
        jdbc.execute(
                "alter table movimentacoes_estoque add constraint falha_historico_teste check"
                        + " (observacao <> 'FORCAR_FALHA')");
        try {
            assertThrows(
                    org.springframework.dao.DataIntegrityViolationException.class,
                    () ->
                            service.registrar(
                                    produto.getId(),
                                    usuario.getId(),
                                    TipoMovimentacao.SAIDA,
                                    2,
                                    "FORCAR_FALHA"));
            assertEquals(5, produtos.findById(produto.getId()).orElseThrow().getQuantidade());
            assertEquals(0, movimentacoes.count());
        } finally {
            jdbc.execute("alter table movimentacoes_estoque drop constraint falha_historico_teste");
        }
    }

    @Test
    void deveDesfazerSaldoEHistoricoJaGravadosSeAuditoriaFalhar() {
        jdbc.execute(
                "alter table registros_operacoes add constraint falha_auditoria_teste check (acao"
                        + " not like 'Movimentação%')");
        try {
            assertThrows(
                    org.springframework.dao.DataIntegrityViolationException.class,
                    () ->
                            service.registrar(
                                    produto.getId(),
                                    usuario.getId(),
                                    TipoMovimentacao.SAIDA,
                                    2,
                                    ""));
            assertEquals(5, produtos.findById(produto.getId()).orElseThrow().getQuantidade());
            assertEquals(0, movimentacoes.count());
        } finally {
            jdbc.execute("alter table registros_operacoes drop constraint falha_auditoria_teste");
        }
    }

    @Test
    void postgresDeveImpedirDuplicidadeSemDiferenciarCaixa() {
        assertThrows(
                org.springframework.dao.DataIntegrityViolationException.class,
                () ->
                        jdbc.update(
                                "insert into categorias (nome,descricao,ativa) values"
                                        + " ('PAPELARIA','',true)"));
        assertThrows(
                org.springframework.dao.DataIntegrityViolationException.class,
                () ->
                        jdbc.update(
                                "insert into produtos"
                                    + " (codigo,nome,categoria_id,quantidade_estoque,estoque_minimo,ativo,preco)"
                                    + " values ('p01','Outro',?,0,0,true,1)",
                                produto.getCategoria().getId()));
    }

    @org.springframework.boot.test.mock.mockito.SpyBean
    org.springframework.security.crypto.password.PasswordEncoder encoder;

    @Autowired UsuarioService usuarioService;

    @Test
    void trocaDeSenhaConcorrenteNaoPodeReativarUsuarioDesativado() throws Exception {
        usuario.alterarSenhaHash(encoder.encode("senha-atual-teste"));
        usuarios.saveAndFlush(usuario);
        var senha = new SenhaForm();
        senha.setAtual("senha-atual-teste");
        senha.setNova("nova-senha-teste");
        senha.setConfirmacao("nova-senha-teste");
        var senhaConferida = new CountDownLatch(1);
        var continuar = new CountDownLatch(1);
        // Pausa somente a comparação de senha; transações e bloqueios usam PostgreSQL real.
        org.mockito.Mockito.doAnswer(
                        invocacao -> {
                            senhaConferida.countDown();
                            if (!continuar.await(10, TimeUnit.SECONDS))
                                throw new IllegalStateException("Tempo de espera excedido.");
                            return invocacao.callRealMethod();
                        })
                .when(encoder)
                .matches(
                        org.mockito.ArgumentMatchers.eq("senha-atual-teste"),
                        org.mockito.ArgumentMatchers.anyString());
        var pool = Executors.newFixedThreadPool(2);
        try {
            var troca = pool.submit(() -> usuarioService.alterarSenha(usuario.getId(), senha));
            assertTrue(senhaConferida.await(10, TimeUnit.SECONDS));
            var desativacao = pool.submit(() -> usuarioService.alterarStatus(usuario.getId()));
            try {
                // Sem bloqueio, a desativação termina enquanto a senha ainda lê uma cópia antiga.
                desativacao.get(1, TimeUnit.SECONDS);
            } catch (TimeoutException esperado) {
                // Com bloqueio, a desativação espera a troca de senha terminar.
            } finally {
                continuar.countDown();
            }
            troca.get(15, TimeUnit.SECONDS);
            desativacao.get(15, TimeUnit.SECONDS);
            var atual = usuarios.findById(usuario.getId()).orElseThrow();
            assertFalse(
                    atual.isAtivo(),
                    "A troca de senha não pode desfazer a desativação concorrente.");
            assertTrue(encoder.matches("nova-senha-teste", atual.getSenhaHash()));
        } finally {
            continuar.countDown();
            pool.shutdownNow();
            org.mockito.Mockito.reset(encoder);
        }
    }
}
