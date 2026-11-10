package com.upbox.integration;

import static org.junit.jupiter.api.Assertions.*;

import com.upbox.controller.SenhaForm;
import com.upbox.entity.*;
import com.upbox.entity.Categoria;
import com.upbox.entity.Produto;
import com.upbox.entity.Usuario;
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
class MysqlIT {
    @DynamicPropertySource
    static void banco(DynamicPropertyRegistry r) {
        String url =
                System.getenv()
                        .getOrDefault(
                                "UPBOX_TEST_DB_URL", "jdbc:mysql://localhost:3306/upbox_test");
        // Executado antes da inicialização JPA e do create-drop.
        if (!url.matches("jdbc:mysql://[^/]+/upbox_test(\\?.*)?"))
            throw new IllegalArgumentException(
                    "Integração exige banco exclusivo chamado upbox_test.");
        r.add("spring.datasource.url", () -> url);
        r.add(
                "spring.datasource.username",
                () -> System.getenv().getOrDefault("UPBOX_TEST_DB_USER", "root"));
        r.add(
                "spring.datasource.password",
                () -> System.getenv().getOrDefault("UPBOX_TEST_DB_PASSWORD", ""));
        r.add("spring.datasource.driver-class-name", () -> "com.mysql.cj.jdbc.Driver");
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
    @Autowired PrimeiroAcessoService primeiroAcesso;
    Produto produto;
    Usuario usuario;

    @Test
    void primeirasInstalacoesConcorrentesCriamETrocamSenhaDeSomenteUmaConta() throws Exception {
        var pool = Executors.newFixedThreadPool(2);
        var inicio = new CountDownLatch(1);
        Callable<Void> criar =
                () -> {
                    inicio.await();
                    try {
                        primeiroAcesso.criarContaPadrao();
                    } catch (org.springframework.dao.DataIntegrityViolationException ex) {
                        assertTrue(usuarios.existsByPerfil(PerfilAcesso.ADMINISTRADOR));
                    }
                    return null;
                };
        try {
            var a = pool.submit(criar);
            var b = pool.submit(criar);
            inicio.countDown();
            a.get(15, TimeUnit.SECONDS);
            b.get(15, TimeUnit.SECONDS);
            assertEquals(
                    1,
                    usuarios.findAll().stream()
                            .filter(u -> u.getPerfil() == PerfilAcesso.ADMINISTRADOR)
                            .count());
            assertTrue(
                    usuarios.findByUsuarioIgnoreCase("administrador")
                            .orElseThrow()
                            .isTrocaSenhaObrigatoria());
            Callable<Boolean> ativar =
                    () -> {
                        try {
                            var f = new SenhaForm();
                            f.setAtual("admin");
                            f.setNova("senha-teste-123");
                            f.setConfirmacao("senha-teste-123");
                            usuarioService.alterarSenha(
                                    usuarios.findByUsuarioIgnoreCase("administrador")
                                            .orElseThrow()
                                            .getId(),
                                    f);
                            return true;
                        } catch (IllegalArgumentException ex) {
                            return false;
                        }
                    };
            var c = pool.submit(ativar);
            var d = pool.submit(ativar);
            assertNotEquals(c.get(15, TimeUnit.SECONDS), d.get(15, TimeUnit.SECONDS));
            assertFalse(
                    usuarios.findByUsuarioIgnoreCase("administrador")
                            .orElseThrow()
                            .isTrocaSenhaObrigatoria());
        } finally {
            pool.shutdownNow();
        }
    }

    @BeforeEach
    void preparar() {

        jdbc.execute("delete from movimentacoes_estoque");
        for (String tabela :
                new String[] {"produtos", "categorias", "registros_operacoes", "usuarios"})
            jdbc.execute("delete from " + tabela);
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
                "create trigger falha_historico_teste before insert on movimentacoes_estoque for"
                        + " each row signal sqlstate '45000' set message_text='Falha simulada no"
                        + " historico'");
        try {
            assertThrows(
                    org.springframework.dao.DataAccessException.class,
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
            jdbc.execute("drop trigger falha_historico_teste");
        }
    }

    @Test
    void deveDesfazerSaldoEHistoricoJaGravadosSeAuditoriaFalhar() {
        jdbc.execute(
                "create trigger falha_auditoria_teste before insert on registros_operacoes for each"
                        + " row signal sqlstate '45000' set message_text='Falha simulada na"
                        + " auditoria'");
        try {
            assertThrows(
                    org.springframework.dao.DataAccessException.class,
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
            jdbc.execute("drop trigger falha_auditoria_teste");
        }
    }

    @Test
    void mysqlDeveImpedirDuplicidadeSemDiferenciarCaixa() {
        assertThrows(
                org.springframework.dao.DataAccessException.class,
                () ->
                        jdbc.update(
                                "insert into categorias (nome,descricao,ativa) values"
                                        + " ('PAPELARIA','',true)"));
        assertThrows(
                org.springframework.dao.DataAccessException.class,
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
        // Pausa somente a comparação de senha; transações e bloqueios usam MySQL real.
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

    @Test
    void desativacoesSimultaneasDevemPreservarUmAdministrador() throws Exception {
        var a =
                usuarios.save(
                        new Usuario(
                                "Admin A",
                                "admin.a",
                                "a@example.com",
                                "hash-ficticio",
                                PerfilAcesso.ADMINISTRADOR));
        var b =
                usuarios.save(
                        new Usuario(
                                "Admin B",
                                "admin.b",
                                "b@example.com",
                                "hash-ficticio",
                                PerfilAcesso.ADMINISTRADOR));
        var inicio = new CountDownLatch(1);
        var pool = Executors.newFixedThreadPool(2);
        try {
            java.util.function.Function<Long, Callable<Boolean>> desativar =
                    id ->
                            () -> {
                                if (!inicio.await(10, TimeUnit.SECONDS))
                                    throw new IllegalStateException("Sincronização falhou.");
                                try {
                                    usuarioService.alterarStatus(id);
                                    return true;
                                } catch (IllegalArgumentException ex) {
                                    assertEquals(
                                            "Mantenha pelo menos um administrador ativo.",
                                            ex.getMessage());
                                    return false;
                                }
                            };
            var primeiro = pool.submit(desativar.apply(a.getId()));
            var segundo = pool.submit(desativar.apply(b.getId()));
            inicio.countDown();
            assertNotEquals(primeiro.get(15, TimeUnit.SECONDS), segundo.get(15, TimeUnit.SECONDS));
            assertEquals(
                    1,
                    usuarios.findAll().stream()
                            .filter(u -> u.isAtivo() && u.getPerfil() == PerfilAcesso.ADMINISTRADOR)
                            .count());
        } finally {
            inicio.countDown();
            pool.shutdownNow();
        }
    }
}
