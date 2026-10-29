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
 * Registra entradas e saídas no banco.
 *
 * @Transactional garante que o saldo e o histórico sejam salvos juntos.
 * Este serviço ainda NÃO deve ser exposto em rotas públicas sem autenticação.
 */
@Service
public class MovimentacaoEstoqueService {

    private final ProdutoRepository produtoRepository;
    private final UsuarioRepository usuarioRepository;
    private final MovimentacaoEstoqueRepository movimentacaoRepository;

    public MovimentacaoEstoqueService(ProdutoRepository produtoRepository,
                                     UsuarioRepository usuarioRepository,
                                     MovimentacaoEstoqueRepository movimentacaoRepository) {
        this.produtoRepository = produtoRepository;
        this.usuarioRepository = usuarioRepository;
        this.movimentacaoRepository = movimentacaoRepository;
    }

    /**
     * produtoId identifica o item, e usuarioId identifica quem realizou a operação.
     * O futuro controller deve obter usuarioId da sessão autenticada, nunca do formulário.
     */
    @Transactional
    public MovimentacaoEstoque registrar(Long produtoId, Long usuarioId,
                                         TipoMovimentacao tipo, int quantidade,
                                         String observacao) {
        if (tipo == null || quantidade <= 0) {
            throw new IllegalArgumentException("Informe o tipo e uma quantidade maior que zero.");
        }

        // Bloqueia o saldo até finalizar o registro no banco.
        Produto produto = produtoRepository.buscarComBloqueio(produtoId)
                .orElseThrow(() -> new IllegalArgumentException("Produto não encontrado."));
        if (!produto.isAtivo()) {
            throw new IllegalArgumentException("Produto inativo.");
        }

        Usuario usuario = usuarioRepository.findById(usuarioId)
                .filter(Usuario::isAtivo)
                .orElseThrow(() -> new IllegalArgumentException("Usuário inválido ou inativo."));

        if (tipo == TipoMovimentacao.ENTRADA) {
            produto.registrarEntrada(quantidade);
        } else {
            produto.registrarSaida(quantidade);
        }

        // A alteração de saldo é detectada pelo JPA e salva na mesma transação.
        MovimentacaoEstoque movimentacao = new MovimentacaoEstoque(
                produto, usuario, tipo, quantidade,
                LocalDateTime.now(ZoneId.of("America/Sao_Paulo")), observacao);

        return movimentacaoRepository.save(movimentacao);
    }
}
