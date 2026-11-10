package com.upbox.config;

import com.upbox.entity.PerfilAcesso;
import com.upbox.repository.UsuarioRepository;
import com.upbox.service.PrimeiroAcessoService;

import lombok.RequiredArgsConstructor;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.*;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Component;

/** Executado ao iniciar: cria uma conta em banco novo, mas nunca redefine uma existente. */
@Component
@RequiredArgsConstructor
public class AdministradorInicial implements ApplicationRunner {
    private final PrimeiroAcessoService primeiroAcesso;
    private final UsuarioRepository usuarios;

    @Value("${upbox.admin-inicial.habilitado:true}")
    private boolean habilitado;

    @Override
    public void run(ApplicationArguments args) {
        if (!habilitado) return; // Os testes comuns controlam os próprios usuários.
        try {
            primeiroAcesso.criarContaPadrao();
        } catch (DataIntegrityViolationException ex) {
            // Outra instância pode ter criado o mesmo login. A transação dela já terminou.
            if (!usuarios.existsByPerfil(PerfilAcesso.ADMINISTRADOR)) throw ex;
        }
    }
}
