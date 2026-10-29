package com.upbox.service;

import com.upbox.model.*;
import com.upbox.repository.*;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.TestPropertySource;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Testa a gravação de produtos e movimentações.
 *
 * H2 é usado só nestes testes; o programa utiliza PostgreSQL.
 * Em cada teste, o Spring desfaz a transação para manter os dados isolados.
 */
@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.ANY)
@TestPropertySource(properties = "spring.jpa.hibernate.ddl-auto=create-drop")
@Import({ProdutoService.class, MovimentacaoEstoqueService.class})
class EstoquePersistenciaTest {

    @Autowired private CategoriaRepository categorias;
    @Autowired private ProdutoRepository produtos;
    @Autowired private UsuarioRepository usuarios;
    @Autowired private MovimentacaoEstoqueRepository movimentacoes;
    @Autowired private ProdutoService produtoService;
    @Autowired private MovimentacaoEstoqueService movimentacaoService;

    private Produto criarProduto(int saldo) {
        Categoria categoria = categorias.save(new Categoria("Papelaria", "Materiais"));
        return produtoService.cadastrar("PRD-001", "Caderno", "96 folhas",
                                        categoria.getId(), saldo, 3);
    }

    private Usuario criarUsuario() {
        // O valor é fictício; ainda não há autenticação nesta etapa.
        return usuarios.save(new Usuario("Funcionário Teste", "teste", "teste@exemplo.com",
                                         "hash-ficticio-teste", PerfilAcesso.FUNCIONARIO));
    }

    @Test
    void deveSalvarProdutoComCategoria() {
        Produto produto = criarProduto(10);

        Produto salvo = produtos.findById(produto.getId()).orElseThrow();
        assertEquals("PRD-001", salvo.getCodigo());
        assertEquals("Papelaria", salvo.getCategoria().getNome());
        assertEquals(10, salvo.getQuantidade());
    }

    @Test
    void deveRecusarCodigoDuplicado() {
        criarProduto(10);
        Categoria outra = categorias.save(new Categoria("Limpeza", ""));

        assertThrows(IllegalArgumentException.class,
                () -> produtoService.cadastrar("prd-001", "Outro", "", outra.getId(), 0, 2));
    }

    @Test
    void deveSalvarEntradaEHistoricoJuntos() {
        Produto produto = criarProduto(10);
        Usuario usuario = criarUsuario();

        movimentacaoService.registrar(produto.getId(), usuario.getId(),
                                     TipoMovimentacao.ENTRADA, 5, "Compra");

        assertEquals(15, produtos.findById(produto.getId()).orElseThrow().getQuantidade());
        var historico = movimentacoes.findByProdutoIdOrderByDataHoraDesc(produto.getId());
        assertEquals(1, historico.size());
        assertEquals(TipoMovimentacao.ENTRADA, historico.get(0).getTipo());
        assertEquals(usuario.getId(), historico.get(0).getUsuario().getId());
    }

    @Test
    void deveRejeitarSaidaAcimaDoSaldoSemRegistrarHistorico() {
        Produto produto = criarProduto(3);
        Usuario usuario = criarUsuario();

        assertThrows(IllegalArgumentException.class,
                () -> movimentacaoService.registrar(produto.getId(), usuario.getId(),
                                                    TipoMovimentacao.SAIDA, 4, ""));

        assertEquals(3, produtos.findById(produto.getId()).orElseThrow().getQuantidade());
        assertTrue(movimentacoes.findByProdutoIdOrderByDataHoraDesc(produto.getId()).isEmpty());
    }

    @Test
    void deveImpedirMovimentacaoDeProdutoInativo() {
        Produto produto = criarProduto(3);
        Usuario usuario = criarUsuario();
        produto.desativar();

        assertThrows(IllegalArgumentException.class,
                () -> movimentacaoService.registrar(produto.getId(), usuario.getId(),
                                                    TipoMovimentacao.ENTRADA, 1, ""));
    }

    @Test
    void deveImpedirMovimentacaoDeUsuarioInativo() {
        Produto produto = criarProduto(3);
        Usuario usuario = criarUsuario();
        usuario.desativar();

        assertThrows(IllegalArgumentException.class,
                () -> movimentacaoService.registrar(produto.getId(), usuario.getId(),
                                                    TipoMovimentacao.SAIDA, 1, ""));
    }
}
