package com.upbox.model;

import jakarta.persistence.*;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.Locale;

/**
 * Produto cadastrado no estoque.
 *
 * <p>A anotação @Entity informa ao JPA que os dados desta classe serão salvos na tabela "produtos"
 * do banco de dados.
 */
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
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

    // Nullable somente para compatibilidade com produtos antigos sem preço conhecido.
    // Os cadastros e edições do serviço sempre exigem preço informado.
    @Column(precision = 12, scale = 2)
    private java.math.BigDecimal preco;

    /**
     * Construtor utilizado nos exercícios anteriores, sem persistência. Para cadastrar no banco,
     * informe também uma categoria.
     */
    public Produto(String codigo, String nome, int quantidade, int estoqueMinimo) {
        this(codigo, nome, null, null, quantidade, estoqueMinimo);
    }

    /** Prepara um produto real e verifica os dados essenciais. */
    public Produto(
            String codigo,
            String nome,
            String descricao,
            Categoria categoria,
            int quantidade,
            int estoqueMinimo) {
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

    /** Edita dados cadastrais sem tocar na quantidade disponível. */
    public void atualizarDados(
            String codigo,
            String nome,
            String descricao,
            Categoria categoria,
            int estoqueMinimo,
            java.math.BigDecimal preco) {
        if (codigo == null
                || codigo.isBlank()
                || codigo.trim().length() > 40
                || nome == null
                || nome.isBlank()
                || nome.trim().length() > 120
                || categoria == null
                || estoqueMinimo < 0)
            throw new IllegalArgumentException("Revise código, nome, categoria e estoque mínimo.");
        if (descricao != null && descricao.length() > 500)
            throw new IllegalArgumentException("Descrição deve ter até 500 caracteres.");
        if (preco == null
                || preco.signum() < 0
                || preco.scale() > 2
                || preco.compareTo(new java.math.BigDecimal("9999999999.99")) > 0)
            throw new IllegalArgumentException(
                    "Informe preço não negativo, com até duas casas decimais.");
        this.codigo = codigo.trim().toUpperCase(Locale.ROOT);
        this.nome = nome.trim();
        this.descricao = descricao == null ? "" : descricao.trim();
        this.categoria = categoria;
        this.estoqueMinimo = estoqueMinimo;
        this.preco = preco;
    }

    public void ativar() {
        this.ativo = true;
    }

    /** O alerta aparece também quando o saldo é exatamente o mínimo. */
    public boolean isEstoqueBaixo() {
        return quantidade <= estoqueMinimo;
    }

    /** Entrada soma unidades ao saldo, protegendo contra valores inválidos. */
    public void registrarEntrada(int quantidade) {
        validarQuantidadeMovimentada(quantidade);

        // Um int tem um limite máximo. Esta verificação evita o estouro.
        if (this.quantidade > Integer.MAX_VALUE - quantidade) {
            throw new IllegalArgumentException("Quantidade acima do limite permitido.");
        }

        this.quantidade += quantidade;
    }

    /** Saída subtrai unidades, mas nunca permite saldo negativo. */
    public void registrarSaida(int quantidade) {
        validarQuantidadeMovimentada(quantidade);

        if (quantidade > this.quantidade) {
            throw new IllegalArgumentException("Estoque insuficiente.");
        }

        this.quantidade -= quantidade;
    }

    /** Desativa o produto sem apagar as movimentações antigas. */
    public void desativar() {
        this.ativo = false;
    }

    private void validarQuantidadeMovimentada(int quantidade) {
        if (quantidade <= 0) {
            throw new IllegalArgumentException("A quantidade deve ser maior que zero.");
        }
    }
}
