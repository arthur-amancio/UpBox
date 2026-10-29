package com.upbox.repository;

import com.upbox.model.RegistroOperacao;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

/** Consulta administrativa limitada às operações mais recentes. */
public interface RegistroOperacaoRepository extends JpaRepository<RegistroOperacao, Long> {
    List<RegistroOperacao> findTop80ByOrderByDataHoraDescIdDesc();
}
