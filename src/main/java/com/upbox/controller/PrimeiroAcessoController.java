package com.upbox.controller;

import com.upbox.model.UsuarioForm;
import com.upbox.service.PrimeiroAcessoService;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;

import lombok.RequiredArgsConstructor;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

import java.util.Objects;
import java.util.Set;

/** Primeiro administrador pelo navegador do próprio computador, com CSRF e BCrypt. */
@Controller
@RequiredArgsConstructor
public class PrimeiroAcessoController {
    private final PrimeiroAcessoService primeiroAcesso;

    private boolean local(HttpServletRequest req) {
        // Não confiamos em X-Forwarded-For enviado pelo navegador.
        return Set.of("127.0.0.1", "::1", "0:0:0:0:0:0:0:1").contains(req.getRemoteAddr());
    }

    @GetMapping("/primeiro-acesso")
    public String pagina(HttpServletRequest req, HttpServletResponse res, Model m) {
        if (!local(req)) {
            res.setStatus(403);
            return "acesso-negado";
        }
        if (!primeiroAcesso.disponivel()) return "redirect:/login";
        m.addAttribute("form", new UsuarioForm());
        return "primeiro-acesso";
    }

    @PostMapping("/primeiro-acesso")
    public String cadastrar(
            @Valid @ModelAttribute("form") UsuarioForm form,
            BindingResult erros,
            @RequestParam(defaultValue = "") String confirmacao,
            HttpServletRequest req,
            HttpServletResponse res) {
        if (!local(req) || !primeiroAcesso.disponivel()) {
            res.setStatus(403);
            return "acesso-negado";
        }
        if (!Objects.equals(form.getSenha(), confirmacao))
            erros.reject("senha", "As senhas informadas não coincidem.");
        if (!erros.hasErrors()) {
            try {
                primeiroAcesso.cadastrar(form);
            } catch (IllegalArgumentException ex) {
                erros.reject("dados", ex.getMessage());
            }
        }
        if (erros.hasErrors()) return "primeiro-acesso";
        return "redirect:/login?configurado";
    }
}
