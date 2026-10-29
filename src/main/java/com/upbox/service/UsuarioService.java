package com.upbox.service;

import com.upbox.model.*;
import com.upbox.repository.UsuarioRepository;

import lombok.RequiredArgsConstructor;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.nio.charset.StandardCharsets;
import java.util.*;

/** Regras de usuários; senha só chega ao banco depois do BCrypt. */
@Service
@RequiredArgsConstructor
@Transactional
public class UsuarioService {
    private final UsuarioRepository usuarios;
    private final PasswordEncoder encoder;
    private final AuditoriaService auditoria;

    public Usuario salvar(Long id, UsuarioForm f) {
        var todos = usuarios.listarComBloqueio();
        validarIdentificacao(todos, id, f.getUsuario(), f.getEmail());
        Usuario u;
        if (id == null) {
            validarSenha(f.getSenha());
            u =
                    new Usuario(
                            f.getNome(),
                            f.getUsuario().toLowerCase(Locale.ROOT),
                            f.getEmail().toLowerCase(Locale.ROOT),
                            encoder.encode(f.getSenha()),
                            f.getPerfil());
        } else {
            u = buscar(id);
            if (u.isAtivo()
                    && u.getPerfil() == PerfilAcesso.ADMINISTRADOR
                    && f.getPerfil() != u.getPerfil()) {
                protegerUltimoAdministrador(todos);
            }
            u.atualizar(f.getNome(), f.getUsuario(), f.getEmail(), f.getPerfil());
        }
        usuarios.saveAndFlush(u);
        auditoria.registrar(
                id == null ? "Usuário cadastrado" : "Usuário atualizado", "Usuário #" + u.getId());
        return u;
    }

    public void alterarStatus(Long id) {
        var todos = usuarios.listarComBloqueio();
        Usuario u = buscar(id);
        if (u.isAtivo()) {
            if (u.getPerfil() == PerfilAcesso.ADMINISTRADOR) protegerUltimoAdministrador(todos);
            u.desativar();
        } else u.ativar();
        auditoria.registrar("Situação do usuário alterada", "Usuário #" + id);
    }

    public void atualizarPerfil(Long id, PerfilForm f) {
        var todos = usuarios.listarComBloqueio();
        Usuario u = buscar(id);
        validarIdentificacao(todos, id, u.getUsuario(), f.getEmail());
        u.atualizar(f.getNome(), u.getUsuario(), f.getEmail(), u.getPerfil());
        usuarios.flush();
        auditoria.registrar("Perfil atualizado", "Usuário #" + id);
    }

    public void alterarSenha(Long id, SenhaForm f) {
        Usuario u = buscar(id);
        if (!encoder.matches(f.getAtual(), u.getSenhaHash()))
            throw new IllegalArgumentException("A senha atual está incorreta.");
        if (!Objects.equals(f.getNova(), f.getConfirmacao()))
            throw new IllegalArgumentException("As senhas informadas não coincidem.");
        validarSenha(f.getNova());
        u.alterarSenhaHash(encoder.encode(f.getNova()));
        auditoria.registrar("Senha alterada", "Usuário #" + id);
    }

    @Transactional(readOnly = true)
    public Usuario buscar(Long id) {
        return usuarios.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Usuário não encontrado."));
    }

    private void validarIdentificacao(List<Usuario> todos, Long id, String login, String email) {
        if (login == null || !login.matches("[A-Za-z0-9._-]{1,80}"))
            throw new IllegalArgumentException("Nome de usuário inválido.");
        for (Usuario outro : todos) {
            if (!Objects.equals(id, outro.getId())
                    && (outro.getUsuario().equalsIgnoreCase(login.trim())
                            || outro.getEmail().equalsIgnoreCase(email.trim()))) {
                throw new IllegalArgumentException(
                        "Este e-mail ou nome de usuário já está cadastrado.");
            }
        }
    }

    private void protegerUltimoAdministrador(List<Usuario> todos) {
        if (todos.stream()
                        .filter(u -> u.isAtivo() && u.getPerfil() == PerfilAcesso.ADMINISTRADOR)
                        .count()
                <= 1)
            throw new IllegalArgumentException("Mantenha pelo menos um administrador ativo.");
    }

    public static void validarSenha(String senha) {
        if (senha == null
                || senha.length() < 8
                || senha.getBytes(StandardCharsets.UTF_8).length > 72)
            throw new IllegalArgumentException(
                    "A senha deve ter pelo menos 8 caracteres e no máximo 72 bytes.");
    }
}
