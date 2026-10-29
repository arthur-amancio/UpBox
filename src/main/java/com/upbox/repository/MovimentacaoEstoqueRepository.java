package com.upbox.repository;

import com.upbox.model.MovimentacaoEstoque;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

/** Salva e consulta o histórico de movimentações. */
public interface MovimentacaoEstoqueRepository extends JpaRepository<MovimentacaoEstoque, Long> {
    List<MovimentacaoEstoque> findByProdutoIdOrderByDataHoraDesc(Long produtoId);
}
