package com.upbox.model;

import jakarta.persistence.*;

/**
 * Agrupa produtos, como Papelaria, Limpeza ou Alimentos.
 */
@Entity
@Table(name = "categorias")
public class Categoria {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, length = 100)
    private String nome;

    @Column(length = 300)
    private String descricao;

    @Column(nullable = false)
    private boolean ativa = true;

    protected Categoria() {
    }

    public Categoria(String nome, String descricao) {
        if (nome == null || nome.isBlank()) {
            throw new IllegalArgumentException("O nome da categoria é obrigatório.");
        }
        this.nome = nome.trim();
        this.descricao = descricao == null ? "" : descricao.trim();
    }

    public Long getId() {
        return id;
    }

    public String getNome() {
        return nome;
    }

    public String getDescricao() {
        return descricao;
    }

    public boolean isAtiva() {
        return ativa;
    }

    public void desativar() {
        this.ativa = false;
    }
}
