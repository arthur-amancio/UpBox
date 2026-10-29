package com.upbox.service;

import com.upbox.model.Categoria;
import com.upbox.model.Produto;
import com.upbox.repository.CategoriaRepository;
import com.upbox.repository.ProdutoRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Locale;

/**
 * Concentra as regras de cadastro e consulta de produtos.
 * Os formulários do navegador serão conectados somente após o login real.
 */
@Service
public class ProdutoService {

    private final ProdutoRepository produtoRepository;
    private final CategoriaRepository categoriaRepository;

    public ProdutoService(ProdutoRepository produtoRepository,
                          CategoriaRepository categoriaRepository) {
        this.produtoRepository = produtoRepository;
        this.categoriaRepository = categoriaRepository;
    }

    /**
     * Salva um produto em uma categoria existente e ativa.
     * A unicidade também é protegida pela restrição do banco.
     */
    @Transactional
    public Produto cadastrar(String codigo, String nome, String descricao,
                             Long categoriaId, int quantidade, int estoqueMinimo) {
        if (codigo == null || codigo.isBlank()) {
            throw new IllegalArgumentException("O código é obrigatório.");
        }

        String codigoNormalizado = codigo.trim().toUpperCase(Locale.ROOT);
        if (produtoRepository.existsByCodigo(codigoNormalizado)) {
            throw new IllegalArgumentException("Já existe um produto com esse código.");
        }

        Categoria categoria = categoriaRepository.findById(categoriaId)
                .filter(Categoria::isAtiva)
                .orElseThrow(() -> new IllegalArgumentException("Categoria inválida ou inativa."));

        Produto produto = new Produto(codigoNormalizado, nome, descricao,
                                     categoria, quantidade, estoqueMinimo);
        return produtoRepository.save(produto);
    }

    /** Lista os produtos na ordem do nome. */
    @Transactional(readOnly = true)
    public List<Produto> listar() {
        return produtoRepository.findAll(org.springframework.data.domain.Sort.by("nome"));
    }
}
