package com.upbox.service;

import com.upbox.model.*;
import com.upbox.repository.MovimentacaoEstoqueRepository;
import com.upbox.repository.ProdutoRepository;
import com.upbox.repository.UsuarioRepository;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.ZoneId;

/**
 * Registra entradas e saídas no banco. @Transactional garante que o saldo e o histórico sejam
 * salvos juntos. O controller exige autenticação e obtém o responsável da sessão.
 */
@Service
@lombok.RequiredArgsConstructor
public class MovimentacaoEstoqueService {

    private final ProdutoRepository produtoRepository;
    private final UsuarioRepository usuarioRepository;
    private final MovimentacaoEstoqueRepository movimentacaoRepository;
    private final AuditoriaService auditoria;

    /**
     * produtoId identifica o item, e usuarioId identifica quem realizou a operação. O controller
     * obtém usuarioId da sessão autenticada, nunca do formulário.
     */
    @Transactional
    public MovimentacaoEstoque registrar(
            Long produtoId,
            Long usuarioId,
            TipoMovimentacao tipo,
            int quantidade,
            String observacao) {
        if (produtoId == null || usuarioId == null)
            throw new IllegalArgumentException("Informe produto e usuário responsável.");
        if (observacao != null && observacao.length() > 500)
            throw new IllegalArgumentException("Observação deve ter até 500 caracteres.");
        if (tipo == null || quantidade <= 0) {
            throw new IllegalArgumentException("Informe o tipo e uma quantidade maior que zero.");
        }

        // Bloqueia o saldo até finalizar o registro no banco.
        Produto produto =
                produtoRepository
                        .buscarComBloqueio(produtoId)
                        .orElseThrow(() -> new IllegalArgumentException("Produto não encontrado."));
        if (!produto.isAtivo()) {
            throw new IllegalArgumentException("Produto inativo.");
        }

        Usuario usuario =
                usuarioRepository
                        .findById(usuarioId)
                        .filter(Usuario::isAtivo)
                        .orElseThrow(
                                () -> new IllegalArgumentException("Usuário inválido ou inativo."));

        if (tipo == TipoMovimentacao.ENTRADA) {
            produto.registrarEntrada(quantidade);
        } else {
            produto.registrarSaida(quantidade);
        }

        // A alteração de saldo é detectada pelo JPA e salva na mesma transação.
        MovimentacaoEstoque movimentacao =
                new MovimentacaoEstoque(
                        produto,
                        usuario,
                        tipo,
                        quantidade,
                        LocalDateTime.now(ZoneId.of("America/Sao_Paulo")),
                        observacao);

        movimentacaoRepository.saveAndFlush(movimentacao);
        auditoria.registrar(
                "Movimentação " + tipo, "Produto #" + produtoId + ", quantidade " + quantidade);
        return movimentacao;
    }

    /** Consulta dentro de transação, carregando produto, categoria e responsável. */
    @Transactional(readOnly = true)
    public java.util.List<MovimentacaoEstoque> pesquisar(FiltroMovimentacao filtro) {
        filtro.validar();
        return movimentacaoRepository.pesquisar(
                filtro.getProduto(),
                filtro.getCategoria(),
                filtro.getUsuario(),
                filtro.getTipo(),
                filtro.inicioEfetivo(),
                filtro.fimExclusivo());
    }
}
