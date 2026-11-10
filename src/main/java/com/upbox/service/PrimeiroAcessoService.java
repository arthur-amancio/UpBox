package com.upbox.service;

import com.upbox.entity.*;
import com.upbox.repository.UsuarioRepository;

import lombok.RequiredArgsConstructor;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Cria a conta padrão automaticamente; a senha inicial admin precisa ser trocada no primeiro login.
 */
@Service
@RequiredArgsConstructor
public class PrimeiroAcessoService {
    private final UsuarioRepository usuarios;
    private final PasswordEncoder encoder;

    /** A restrição UNIQUE do login decide qual instância pode criar a primeira conta. */
    @Transactional
    public void criarContaPadrao() {
        if (usuarios.existsByPerfil(PerfilAcesso.ADMINISTRADOR)) return;
        if (usuarios.findByUsuarioIgnoreCase("administrador").isPresent())
            throw new IllegalArgumentException(
                    "O login administrador já existe. Revise a conta sem sobrescrevê-la.");
        Usuario u =
                new Usuario(
                        "Administrador",
                        "administrador",
                        "administrador@upbox.local",
                        encoder.encode("admin"),
                        PerfilAcesso.ADMINISTRADOR);
        u.exigirTrocaSenha();
        usuarios.saveAndFlush(u);
    }
}
