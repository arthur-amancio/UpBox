package com.upbox.controller;

import com.upbox.repository.UsuarioRepository;

import lombok.RequiredArgsConstructor;

import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;

/** Disponibiliza apenas os dados necessários para o cabeçalho, sem expor senhaHash. */
@ControllerAdvice
@RequiredArgsConstructor
public class DadosComunsController {
    private final UsuarioRepository usuarios;

    @ModelAttribute
    public void usuarioAtual(Principal principal, Model model) {
        if (principal != null)
            usuarios.findById(Long.valueOf(principal.getName()))
                    .ifPresent(
                            u -> {
                                model.addAttribute("nomeAtual", u.getNome());
                                model.addAttribute("perfilAtual", u.getPerfil().name());
                            });
    }
}
