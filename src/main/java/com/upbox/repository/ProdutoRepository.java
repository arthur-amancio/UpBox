package com.upbox.repository;

import com.upbox.model.Produto;

import jakarta.persistence.LockModeType;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

/** Consultas e gravação de produtos no banco. */
public interface ProdutoRepository extends JpaRepository<Produto, Long> {

    boolean existsByCodigo(String codigo);

    Optional<Produto> findByCodigo(String codigo);

    /** Fetch join carrega a categoria antes de sair da transação (open-in-view=false). */
    @Query(
            "select p from Produto p join fetch p.categoria c where (:busca = '' or lower(p.nome)"
                + " like lower(concat('%', :busca, '%')) or lower(p.codigo) like lower(concat('%',"
                + " :busca, '%'))) and (:categoria is null or c.id = :categoria) and (:ativo is"
                + " null or p.ativo = :ativo) and (:situacao = '' or (:situacao = 'BAIXO' and"
                + " p.quantidade <= p.estoqueMinimo) or (:situacao = 'NORMAL' and p.quantidade >"
                + " p.estoqueMinimo)) order by p.nome, p.id")
    java.util.List<Produto> pesquisar(
            @Param("busca") String busca,
            @Param("categoria") Long categoria,
            @Param("ativo") Boolean ativo,
            @Param("situacao") String situacao);

    @Query("select p from Produto p join fetch p.categoria where p.id = :id")
    Optional<Produto> buscarDetalhado(@Param("id") Long id);

    /**
     * Bloqueia o produto durante uma movimentação. Assim, duas saídas simultâneas não calculam o
     * mesmo saldo antigo. Este método deve ser chamado dentro de uma transação.
     */
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select p from Produto p where p.id = :id")
    Optional<Produto> buscarComBloqueio(@Param("id") Long id);
}
