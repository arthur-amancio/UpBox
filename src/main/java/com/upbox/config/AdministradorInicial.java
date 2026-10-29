package com.upbox.config;

import com.upbox.model.*;
import com.upbox.repository.UsuarioRepository;
import com.upbox.service.UsuarioService;

import jakarta.validation.Validator;

import lombok.RequiredArgsConstructor;

import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Component;

/** Criação optativa por arquivo local; nunca substitui administrador existente. */
@Component
@RequiredArgsConstructor
public class AdministradorInicial implements ApplicationRunner {
    private final Environment env;
    private final UsuarioRepository usuarios;
    private final UsuarioService service;
    private final Validator validator;

    @Override
    public void run(ApplicationArguments args) {
        if (!env.getProperty("upbox.admin-inicial.habilitado", Boolean.class, false)
                || usuarios.existsByPerfilAndAtivoTrue(PerfilAcesso.ADMINISTRADOR)) return;
        UsuarioForm f = new UsuarioForm();
        f.setNome(env.getProperty("upbox.admin-inicial.nome"));
        f.setUsuario(env.getProperty("upbox.admin-inicial.usuario"));
        f.setEmail(env.getProperty("upbox.admin-inicial.email"));
        f.setSenha(env.getProperty("upbox.admin-inicial.senha"));
        f.setPerfil(PerfilAcesso.ADMINISTRADOR);
        if (!validator.validate(f).isEmpty())
            throw new IllegalArgumentException("Revise os dados locais do administrador inicial.");
        service.salvar(null, f);
    }
}
