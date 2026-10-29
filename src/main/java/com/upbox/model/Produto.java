package com.upbox.model;

import jakarta.persistence.*;
import java.util.Locale;

/**
 * Produto cadastrado no estoque.
 *
 * A anotação @Entity informa ao JPA que os dados desta classe serão salvos
 * na tabela "produtos" do banco de dados.
 */
@Entity
@Table(name = "produtos")
public class Produto {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // O código é único para evitar dois cadastros do mesmo item.
    @Column(nullable = false, unique = true, length = 40)
    private String codigo;

    @Column(nullable = false, length = 120)
    private String nome;

    @Column(length = 500)
    private String descricao;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "categoria_id", nullable = false)
    private Categoria categoria;

    @Column(name = "quantidade_estoque", nullable = false)
    private int quantidade;

    @Column(name = "estoque_minimo", nullable = false)
    private int estoqueMinimo;

    @Column(nullable = false)
    private boolean ativo = true;

    // O JPA precisa deste construtor para reconstruir produtos do banco.
    protected Produto() {
    }

    /**
     * Construtor utilizado nos exercícios anteriores, sem persistência.
     * Para cadastrar no banco, informe também uma categoria.
     */
    public Produto(String codigo, String nome, int quantidade, int estoqueMinimo) {
        this(codigo, nome, null, null, quantidade, estoqueMinimo);
    }

    /**
     * Prepara um produto real e verifica os dados essenciais.
     */
    public Produto(String codigo, String nome, String descricao,
                   Categoria categoria, int quantidade, int estoqueMinimo) {
        if (codigo == null || codigo.isBlank()) {
            throw new IllegalArgumentException("O código é obrigatório.");
        }
        if (nome == null || nome.isBlank()) {
            throw new IllegalArgumentException("O nome é obrigatório.");
        }
        if (quantidade < 0 || estoqueMinimo < 0) {
            throw new IllegalArgumentException("As quantidades não podem ser negativas.");
        }

        this.codigo = codigo.trim().toUpperCase(Locale.ROOT);
        this.nome = nome.trim();
        this.descricao = descricao == null ? "" : descricao.trim();
        this.categoria = categoria;
        this.quantidade = quantidade;
        this.estoqueMinimo = estoqueMinimo;
    }

    public Long getId() {
        return id;
    }

    public String getCodigo() {
        return codigo;
    }

    public String getNome() {
        return nome;
    }

    public String getDescricao() {
        return descricao;
    }

    public Categoria getCategoria() {
        return categoria;
    }

    public int getQuantidade() {
        return quantidade;
    }

    public int getEstoqueMinimo() {
        return estoqueMinimo;
    }

    public boolean isAtivo() {
        return ativo;
    }

    /** O alerta aparece também quando o saldo é exatamente o mínimo. */
    public boolean isEstoqueBaixo() {
        return quantidade <= estoqueMinimo;
    }

    /**
     * Entrada soma unidades ao saldo, protegendo contra valores inválidos.
     */
    public void registrarEntrada(int quantidade) {
        validarQuantidadeMovimentada(quantidade);

        // Um int tem um limite máximo. Esta verificação evita o estouro.
        if (this.quantidade > Integer.MAX_VALUE - quantidade) {
            throw new IllegalArgumentException("Quantidade acima do limite permitido.");
        }

        this.quantidade += quantidade;
    }

    /**
     * Saída subtrai unidades, mas nunca permite saldo negativo.
     */
    public void registrarSaida(int quantidade) {
        validarQuantidadeMovimentada(quantidade);

        if (quantidade > this.quantidade) {
            throw new IllegalArgumentException("Estoque insuficiente.");
        }

        this.quantidade -= quantidade;
    }

    /**
     * Desativa o produto sem apagar as movimentações antigas.
     */
    public void desativar() {
        this.ativo = false;
    }

    private void validarQuantidadeMovimentada(int quantidade) {
        if (quantidade <= 0) {
            throw new IllegalArgumentException("A quantidade deve ser maior que zero.");
        }
    }
}
