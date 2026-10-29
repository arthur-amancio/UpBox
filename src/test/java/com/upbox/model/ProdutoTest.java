package com.upbox.model;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Cada teste descreve uma situação que pode acontecer no estoque.
 * Assim, o grupo pode demonstrar as regras sem depender do navegador.
 */
class ProdutoTest {

    @Test
    void deveSomarQuandoRegistrarEntrada() {
        Produto produto = new Produto("P01", "Caderno", 10, 3);

        produto.registrarEntrada(5);

        assertEquals(15, produto.getQuantidade());
    }

    @Test
    void deveSubtrairQuandoRegistrarSaida() {
        Produto produto = new Produto("P01", "Caderno", 10, 3);

        produto.registrarSaida(4);

        assertEquals(6, produto.getQuantidade());
    }

    @Test
    void deveRejeitarSaidaAcimaDoSaldoSemAlterarEstoque() {
        Produto produto = new Produto("P01", "Caderno", 10, 3);

        assertThrows(IllegalArgumentException.class, () -> produto.registrarSaida(11));
        assertEquals(10, produto.getQuantidade());
    }

    @Test
    void deveRejeitarQuantidadeZeroOuNegativa() {
        Produto produto = new Produto("P01", "Caderno", 10, 3);

        assertThrows(IllegalArgumentException.class, () -> produto.registrarEntrada(0));
        assertThrows(IllegalArgumentException.class, () -> produto.registrarSaida(-1));
        assertEquals(10, produto.getQuantidade());
    }

    @Test
    void deveAlertarQuandoSaldoChegarAoMinimo() {
        Produto produto = new Produto("P01", "Caderno", 10, 3);

        produto.registrarSaida(7);

        assertTrue(produto.isEstoqueBaixo());
    }

    @Test
    void devePermitirSaidaQueZeraOEstoque() {
        Produto produto = new Produto("P01", "Caderno", 3, 3);

        produto.registrarSaida(3);

        assertEquals(0, produto.getQuantidade());
        assertTrue(produto.isEstoqueBaixo());
    }

    @Test
    void deveRejeitarProdutoComCamposInvalidos() {
        assertThrows(IllegalArgumentException.class, () -> new Produto("", "Caderno", 1, 1));
        assertThrows(IllegalArgumentException.class, () -> new Produto("P01", " ", 1, 1));
        assertThrows(IllegalArgumentException.class, () -> new Produto("P01", "Caderno", -1, 1));
        assertThrows(IllegalArgumentException.class, () -> new Produto("P01", "Caderno", 1, -1));
    }

    @Test
    void deveEvitarEstouroDaQuantidade() {
        Produto produto = new Produto("P01", "Caderno", Integer.MAX_VALUE, 1);

        assertThrows(IllegalArgumentException.class, () -> produto.registrarEntrada(1));
        assertEquals(Integer.MAX_VALUE, produto.getQuantidade());
    }
}
