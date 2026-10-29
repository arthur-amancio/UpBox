package com.upbox.repository;

import com.upbox.model.MovimentacaoEstoque;
import com.upbox.model.TipoMovimentacao;

import org.springframework.data.jpa.repository.*;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;

/** Salva e consulta o histórico de movimentações. */
public interface MovimentacaoEstoqueRepository extends JpaRepository<MovimentacaoEstoque, Long> {
    List<MovimentacaoEstoque> findByProdutoIdOrderByDataHoraDesc(Long produtoId);

    @EntityGraph(attributePaths = {"produto", "produto.categoria", "usuario"})
    List<MovimentacaoEstoque> findTop10ByOrderByDataHoraDescIdDesc();

    @Query(
            "select coalesce(sum(m.quantidade), 0) from MovimentacaoEstoque m "
                    + "where m.tipo = :tipo and m.dataHora >= :inicio and m.dataHora < :fim")
    long somarQuantidade(
            @Param("tipo") TipoMovimentacao tipo,
            @Param("inicio") LocalDateTime inicio,
            @Param("fim") LocalDateTime fim);

    /** Os fetch joins deixam os dados necessários prontos para o HTML. */
    @Query(
            "select m from MovimentacaoEstoque m join fetch m.produto p join fetch p.categoria c"
                + " join fetch m.usuario u where (:produto is null or p.id = :produto) and"
                + " (:categoria is null or c.id = :categoria) and (:usuario is null or u.id ="
                + " :usuario) and (:tipo is null or m.tipo = :tipo) and m.dataHora >= :inicio and"
                + " m.dataHora < :fim order by m.dataHora desc, m.id desc")
    List<MovimentacaoEstoque> pesquisar(
            @Param("produto") Long produto,
            @Param("categoria") Long categoria,
            @Param("usuario") Long usuario,
            @Param("tipo") TipoMovimentacao tipo,
            @Param("inicio") LocalDateTime inicio,
            @Param("fim") LocalDateTime fim);
}
