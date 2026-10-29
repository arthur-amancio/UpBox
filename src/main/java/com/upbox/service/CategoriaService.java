package com.upbox.service;

import com.upbox.model.*;
import com.upbox.repository.CategoriaRepository;

import lombok.RequiredArgsConstructor;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Objects;

/** Cadastro e desativação lógica sem remover associações dos produtos. */
@Service
@RequiredArgsConstructor
public class CategoriaService {
    private final CategoriaRepository categorias;
    private final AuditoriaService auditoria;

    @Transactional
    public Categoria salvar(Long id, CategoriaForm f) {
        if (f.getNome() == null || f.getNome().isBlank())
            throw new IllegalArgumentException("Nome obrigatório.");
        categorias
                .findByNomeIgnoreCase(f.getNome().trim())
                .ifPresent(
                        c -> {
                            if (!Objects.equals(c.getId(), id))
                                throw new IllegalArgumentException(
                                        "Já existe uma categoria com esse nome.");
                        });
        Categoria c = id == null ? new Categoria(f.getNome(), f.getDescricao()) : buscar(id);
        c.atualizar(f.getNome(), f.getDescricao());
        categorias.saveAndFlush(c);
        auditoria.registrar(
                id == null ? "Categoria cadastrada" : "Categoria atualizada",
                "Categoria #" + c.getId());
        return c;
    }

    @Transactional
    public void alterarStatus(Long id) {
        var c = buscar(id);
        if (c.isAtiva()) c.desativar();
        else c.ativar();
        auditoria.registrar("Situação da categoria alterada", "Categoria #" + id);
    }

    @Transactional(readOnly = true)
    public Categoria buscar(Long id) {
        return categorias
                .findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Categoria não encontrada."));
    }
}
