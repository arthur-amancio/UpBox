package com.upbox.repository;

import com.upbox.model.Categoria;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.*;

/** O Spring Data implementa as consultas a partir dos nomes dos métodos. */
public interface CategoriaRepository extends JpaRepository<Categoria, Long> {
    boolean existsByNomeIgnoreCase(String nome);

    Optional<Categoria> findByNomeIgnoreCase(String nome);

    List<Categoria> findAllByOrderByNomeAsc();

    List<Categoria> findByAtivaTrueOrderByNomeAsc();
}
