package com.upbox.repository;

import com.upbox.model.Categoria;
import org.springframework.data.jpa.repository.JpaRepository;

/** O Spring Data cria automaticamente operações como save e findById. */
public interface CategoriaRepository extends JpaRepository<Categoria, Long> {
    boolean existsByNomeIgnoreCase(String nome);
}
