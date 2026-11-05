package com.upbox.controller;

import com.upbox.service.PrimeiroAcessoService;

import lombok.RequiredArgsConstructor;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

import java.security.Principal;

/** Navegação; os módulos conectados recebem controllers próprios. */
@Controller
@RequiredArgsConstructor
public class PaginaController {
    private final PrimeiroAcessoService primeiroAcesso;

    @GetMapping("/")
    public String inicio(Principal p) {
        return p == null ? "redirect:/login" : "redirect:/home";
    }

    @GetMapping("/login")
    public String login() {
        return primeiroAcesso.disponivel() ? "redirect:/primeiro-acesso" : "login";
    }

    @GetMapping("/home")
    public String home() {
        return "home";
    }

    @GetMapping("/cadastro")
    public String cadastro() {
        return "redirect:/usuarios/novo";
    }

    @GetMapping("/acesso-negado")
    public String negado(jakarta.servlet.http.HttpServletResponse res) {
        res.setStatus(403);
        return "acesso-negado";
    }
}
