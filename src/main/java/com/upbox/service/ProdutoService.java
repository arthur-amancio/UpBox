package com.upbox.service;

import com.upbox.model.*;
import com.upbox.repository.*;

import lombok.RequiredArgsConstructor;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.*;

/** Regras de cadastro: nenhum método de edição recebe saldo. */
@Service
@RequiredArgsConstructor
public class ProdutoService {
    private final ProdutoRepository produtoRepository;
    private final CategoriaRepository categoriaRepository;
    private final AuditoriaService auditoria;

    /** Mantém o cadastro de serviço usado pelos testes de persistência anteriores. */
    @Transactional
    public Produto cadastrar(
            String codigo,
            String nome,
            String descricao,
            Long categoriaId,
            int quantidade,
            int estoqueMinimo,
            BigDecimal preco) {
        Categoria categoria =
                categoriaRepository
                        .findById(categoriaId)
                        .filter(Categoria::isAtiva)
                        .orElseThrow(
                                () ->
                                        new IllegalArgumentException(
                                                "Categoria inválida ou inativa."));
        verificarCodigo(codigo, null);
        Produto p = new Produto(codigo, nome, descricao, categoria, quantidade, estoqueMinimo);
        p.atualizarDados(codigo, nome, descricao, categoria, estoqueMinimo, preco);
        produtoRepository.saveAndFlush(p);
        auditoria.registrar("Produto cadastrado", "Produto #" + p.getId());
        return p;
    }

    @Transactional
    public Produto salvar(Long id, ProdutoForm f) {
        if (id == null)
            return cadastrar(
                    f.getCodigo(),
                    f.getNome(),
                    f.getDescricao(),
                    f.getCategoriaId(),
                    0,
                    f.getEstoqueMinimo(),
                    f.getPreco());
        // Usa o mesmo bloqueio das movimentações; a edição nunca sobrescreve saldo concorrente.
        Produto p =
                produtoRepository
                        .buscarComBloqueio(id)
                        .orElseThrow(() -> new IllegalArgumentException("Produto não encontrado."));
        verificarCodigo(f.getCodigo(), id);
        Categoria c =
                categoriaRepository
                        .findById(f.getCategoriaId())
                        .orElseThrow(
                                () -> new IllegalArgumentException("Categoria não encontrada."));
        // Uma categoria desativada pode permanecer no cadastro já associado.
        if (!c.isAtiva() && !c.getId().equals(p.getCategoria().getId()))
            throw new IllegalArgumentException("Selecione uma categoria ativa.");
        p.atualizarDados(
                f.getCodigo(),
                f.getNome(),
                f.getDescricao(),
                c,
                f.getEstoqueMinimo(),
                f.getPreco());
        produtoRepository.flush();
        auditoria.registrar("Produto atualizado", "Produto #" + id);
        return p;
    }

    @Transactional
    public void alterarStatus(Long id) {
        Produto p =
                produtoRepository
                        .buscarComBloqueio(id)
                        .orElseThrow(() -> new IllegalArgumentException("Produto não encontrado."));
        if (p.isAtivo()) p.desativar();
        else p.ativar();
        auditoria.registrar("Situação do produto alterada", "Produto #" + id);
    }

    @Transactional(readOnly = true)
    public Produto buscar(Long id) {
        return produtoRepository
                .buscarDetalhado(id)
                .orElseThrow(() -> new IllegalArgumentException("Produto não encontrado."));
    }

    @Transactional(readOnly = true)
    public List<Produto> listar() {
        return pesquisar("", null, null, "");
    }

    @Transactional(readOnly = true)
    public List<Produto> pesquisar(String busca, Long categoria, Boolean ativo, String situacao) {
        if (!List.of("", "BAIXO", "NORMAL").contains(situacao))
            throw new IllegalArgumentException("Situação inválida.");
        return produtoRepository.pesquisar(busca.trim(), categoria, ativo, situacao);
    }

    private void verificarCodigo(String codigo, Long id) {
        if (codigo == null || codigo.isBlank())
            throw new IllegalArgumentException("Código obrigatório.");
        produtoRepository
                .findByCodigo(codigo.trim().toUpperCase(Locale.ROOT))
                .ifPresent(
                        p -> {
                            if (!Objects.equals(p.getId(), id))
                                throw new IllegalArgumentException(
                                        "Já existe um produto com esse código.");
                        });
    }
}
