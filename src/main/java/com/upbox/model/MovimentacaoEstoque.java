package com.upbox.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;

/**
 * Registro permanente de uma entrada ou saída no estoque.
 *
 * Cada movimentação pertence a um produto e ao usuário responsável.
 */
@Entity
@Table(name = "movimentacoes_estoque")
public class MovimentacaoEstoque {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "produto_id", nullable = false)
    private Produto produto;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "usuario_id", nullable = false)
    private Usuario usuario;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 10)
    private TipoMovimentacao tipo;

    @Column(nullable = false)
    private int quantidade;

    @Column(name = "data_hora", nullable = false)
    private LocalDateTime dataHora;

    @Column(length = 500)
    private String observacao;

    protected MovimentacaoEstoque() {
    }

    public MovimentacaoEstoque(Produto produto, Usuario usuario,
                              TipoMovimentacao tipo, int quantidade,
                              LocalDateTime dataHora, String observacao) {
        if (produto == null || usuario == null || tipo == null || dataHora == null) {
            throw new IllegalArgumentException("Movimentação sem informações obrigatórias.");
        }
        if (quantidade <= 0) {
            throw new IllegalArgumentException("A quantidade deve ser maior que zero.");
        }

        this.produto = produto;
        this.usuario = usuario;
        this.tipo = tipo;
        this.quantidade = quantidade;
        this.dataHora = dataHora;
        this.observacao = observacao == null ? "" : observacao.trim();
    }

    public Long getId() {
        return id;
    }

    public Produto getProduto() {
        return produto;
    }

    public Usuario getUsuario() {
        return usuario;
    }

    public TipoMovimentacao getTipo() {
        return tipo;
    }

    public int getQuantidade() {
        return quantidade;
    }

    public LocalDateTime getDataHora() {
        return dataHora;
    }

    public String getObservacao() {
        return observacao;
    }
}
