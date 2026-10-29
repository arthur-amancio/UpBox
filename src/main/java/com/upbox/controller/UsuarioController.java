package com.upbox.controller;

import com.upbox.model.*;
import com.upbox.repository.*;
import com.upbox.service.UsuarioService;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;

import lombok.RequiredArgsConstructor;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.security.Principal;

/** Formulários de usuários e perfil; autorização é definida na configuração de segurança. */
@Controller
@RequiredArgsConstructor
public class UsuarioController {
    private final UsuarioService service;
    private final UsuarioRepository usuarios;
    private final RegistroOperacaoRepository registros;

    @GetMapping("/usuarios")
    public String listar(Model m) {
        m.addAttribute(
                "usuarios", usuarios.findAll(org.springframework.data.domain.Sort.by("nome")));
        return "usuarios";
    }

    @GetMapping("/usuarios/novo")
    public String novo(Model m) {
        m.addAttribute("form", new UsuarioForm());
        return "usuario-form";
    }

    @GetMapping("/usuarios/{id}/editar")
    public String editar(@PathVariable Long id, Model m) {
        var u = service.buscar(id);
        var f = new UsuarioForm();
        f.setNome(u.getNome());
        f.setUsuario(u.getUsuario());
        f.setEmail(u.getEmail());
        f.setPerfil(u.getPerfil());
        m.addAttribute("form", f);
        m.addAttribute("id", id);
        return "usuario-form";
    }

    @PostMapping({"/usuarios", "/usuarios/{id}"})
    public String salvar(
            @PathVariable(required = false) Long id,
            @Valid @ModelAttribute("form") UsuarioForm f,
            BindingResult erros,
            Model m,
            RedirectAttributes flash) {
        m.addAttribute("id", id);
        if (!erros.hasErrors())
            try {
                service.salvar(id, f);
            } catch (IllegalArgumentException ex) {
                erros.reject("dados", ex.getMessage());
            }
        if (erros.hasErrors()) {
            f.setSenha(null);
            return "usuario-form";
        }
        flash.addFlashAttribute("sucesso", "Usuário salvo com sucesso.");
        return "redirect:/usuarios";
    }

    @PostMapping("/usuarios/{id}/status")
    public String status(@PathVariable Long id, RedirectAttributes f) {
        try {
            service.alterarStatus(id);
            f.addFlashAttribute("sucesso", "Situação atualizada.");
        } catch (IllegalArgumentException ex) {
            f.addFlashAttribute("erro", ex.getMessage());
        }
        return "redirect:/usuarios";
    }

    @GetMapping("/configuracoes")
    public String perfil(Principal p, Model m) {
        var u = service.buscar(Long.valueOf(p.getName()));
        var f = new PerfilForm();
        f.setNome(u.getNome());
        f.setEmail(u.getEmail());
        m.addAttribute("perfilForm", f);
        m.addAttribute("senhaForm", new SenhaForm());
        return "configuracoes";
    }

    @PostMapping("/configuracoes/perfil")
    public String perfil(
            @Valid @ModelAttribute PerfilForm f,
            BindingResult erros,
            Principal p,
            RedirectAttributes flash) {
        try {
            if (erros.hasErrors())
                throw new IllegalArgumentException("Preencha nome e e-mail válidos.");
            service.atualizarPerfil(Long.valueOf(p.getName()), f);
            flash.addFlashAttribute("sucesso", "Perfil atualizado.");
        } catch (IllegalArgumentException ex) {
            flash.addFlashAttribute("erro", ex.getMessage());
        }
        return "redirect:/configuracoes";
    }

    @PostMapping("/configuracoes/senha")
    public String senha(
            @Valid @ModelAttribute SenhaForm f,
            BindingResult erros,
            Principal p,
            HttpServletRequest req,
            RedirectAttributes flash) {
        try {
            if (erros.hasErrors())
                throw new IllegalArgumentException("Preencha os três campos de senha.");
            service.alterarSenha(Long.valueOf(p.getName()), f);
            req.getSession().invalidate();
            org.springframework.security.core.context.SecurityContextHolder.clearContext();
            return "redirect:/login?senhaAlterada";
        } catch (IllegalArgumentException ex) {
            flash.addFlashAttribute("erro", ex.getMessage());
            return "redirect:/configuracoes";
        }
    }

    @GetMapping("/registros")
    public String registros(Model m) {
        m.addAttribute("registros", registros.findTop80ByOrderByDataHoraDescIdDesc());
        return "registros";
    }
}
