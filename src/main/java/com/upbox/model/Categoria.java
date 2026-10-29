package com.upbox.model;

import jakarta.persistence.*;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

/** Agrupa produtos, como Papelaria, Limpeza ou Alimentos. */
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
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

    public Categoria(String nome, String descricao) {
        if (nome == null || nome.isBlank()) {
            throw new IllegalArgumentException("O nome da categoria é obrigatório.");
        }
        this.nome = nome.trim();
        this.descricao = descricao == null ? "" : descricao.trim();
    }

    /** Desativação mantém produtos e histórico vinculados. */
    public void atualizar(String nome, String descricao) {
        if (nome == null || nome.isBlank() || nome.trim().length() > 100)
            throw new IllegalArgumentException(
                    "Informe um nome de categoria com até 100 caracteres.");
        if (descricao != null && descricao.length() > 300)
            throw new IllegalArgumentException("Descrição deve ter até 300 caracteres.");
        this.nome = nome.trim();
        this.descricao = descricao == null ? "" : descricao.trim();
    }

    public void ativar() {
        this.ativa = true;
    }

    public void desativar() {
        this.ativa = false;
    }
}
