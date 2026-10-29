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

    /**
     * Bloqueia o produto durante uma movimentação.
     * Assim, duas saídas simultâneas não calculam o mesmo saldo antigo.
     * Este método deve ser chamado dentro de uma transação.
     */
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select p from Produto p where p.id = :id")
    Optional<Produto> buscarComBloqueio(@Param("id") Long id);
}
