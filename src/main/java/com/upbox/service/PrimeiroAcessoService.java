package com.upbox.service;

import com.upbox.model.*;
import com.upbox.repository.UsuarioRepository;

import lombok.RequiredArgsConstructor;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/** Abre a instalação uma única vez; até administrador inativo impede novo cadastro público. */
@Service
@RequiredArgsConstructor
public class PrimeiroAcessoService {
    private final UsuarioRepository usuarios;
    private final UsuarioService usuarioService;
    private final JdbcTemplate jdbc;

    @Transactional(readOnly = true)
    public boolean disponivel() {
        return !usuarios.existsByPerfil(PerfilAcesso.ADMINISTRADOR);
    }

    @Transactional
    public void cadastrar(UsuarioForm form) {
        String banco =
                jdbc.execute((java.sql.Connection c) -> c.getMetaData().getDatabaseProductName());
        if ("PostgreSQL".equals(banco)) {
            // Não existe usuário para bloquear no banco vazio. Esse bloqueio da transação
            // serializa duas primeiras instalações, inclusive em processos diferentes.
            jdbc.execute("select pg_advisory_xact_lock(80502026)");
        }
        if (!disponivel())
            throw new IllegalArgumentException("A configuração inicial já foi concluída.");
        form.setPerfil(PerfilAcesso.ADMINISTRADOR);
        usuarioService.salvar(null, form);
    }
}
