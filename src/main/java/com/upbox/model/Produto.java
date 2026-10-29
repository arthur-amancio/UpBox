package com.upbox.model;

/**
 * Representa um produto do estoque.
 *
 * Esta primeira versão ensina as regras de quantidade em Java.
 * Ainda não está ligada ao banco de dados nem às telas demonstrativas.
 */
public class Produto {

    // Dados básicos do produto e seu saldo atual.
    private final String codigo;
    private final String nome;
    private int quantidade;
    private final int estoqueMinimo;

    /**
     * Cria um produto, verificando os campos obrigatórios.
     * Um produto novo pode ter saldo zero, mas nunca negativo.
     */
    public Produto(String codigo, String nome, int quantidade, int estoqueMinimo) {
        if (codigo == null || codigo.isBlank()) {
            throw new IllegalArgumentException("O código é obrigatório.");
        }
        if (nome == null || nome.isBlank()) {
            throw new IllegalArgumentException("O nome é obrigatório.");
        }
        if (quantidade < 0 || estoqueMinimo < 0) {
            throw new IllegalArgumentException("As quantidades não podem ser negativas.");
        }

        this.codigo = codigo.trim();
        this.nome = nome.trim();
        this.quantidade = quantidade;
        this.estoqueMinimo = estoqueMinimo;
    }

    public String getCodigo() {
        return codigo;
    }

    public String getNome() {
        return nome;
    }

    public int getQuantidade() {
        return quantidade;
    }

    public int getEstoqueMinimo() {
        return estoqueMinimo;
    }

    /**
     * Mostra o alerta quando o saldo é menor ou igual ao mínimo.
     */
    public boolean isEstoqueBaixo() {
        return quantidade <= estoqueMinimo;
    }

    /**
     * Registra a entrada, somando a quantidade ao saldo.
     */
    public void registrarEntrada(int quantidade) {
        validarQuantidadeMovimentada(quantidade);

        // Impede que uma soma muito grande ultrapasse o limite do tipo int.
        if (this.quantidade > Integer.MAX_VALUE - quantidade) {
            throw new IllegalArgumentException("Quantidade acima do limite permitido.");
        }

        this.quantidade += quantidade;
    }

    /**
     * Registra a saída, sem deixar o estoque negativo.
     */
    public void registrarSaida(int quantidade) {
        validarQuantidadeMovimentada(quantidade);

        if (quantidade > this.quantidade) {
            throw new IllegalArgumentException("Estoque insuficiente.");
        }

        this.quantidade -= quantidade;
    }

    /**
     * Regra compartilhada por entrada e saída: movimentar pelo menos uma unidade.
     */
    private void validarQuantidadeMovimentada(int quantidade) {
        if (quantidade <= 0) {
            throw new IllegalArgumentException("A quantidade deve ser maior que zero.");
        }
    }
}
