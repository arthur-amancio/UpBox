package com.upbox.controller;

import com.upbox.model.*;
import com.upbox.repository.UsuarioRepository;
import com.upbox.service.*;

import jakarta.validation.Valid;

import lombok.RequiredArgsConstructor;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.security.Principal;

/** Entrada, saída e consulta de histórico, sem confiar em usuário enviado pelo navegador. */
@Controller
@RequiredArgsConstructor
public class MovimentacaoController {
    private final MovimentacaoEstoqueService movimentacoes;
    private final ProdutoService produtos;
    private final UsuarioRepository usuarios;

    @GetMapping("/movimentacoes")
    public String pagina(
            @ModelAttribute("filtro") FiltroMovimentacao filtro, BindingResult erros, Model m) {
        if (erros.hasErrors())
            throw new IllegalArgumentException("Revise os filtros de movimentação.");
        var f = new MovimentacaoForm();
        f.setProdutoId(filtro.getProduto());
        m.addAttribute("form", f);
        carregar(m, filtro);
        return "movimentacoes";
    }

    @PostMapping("/movimentacoes")
    public String registrar(
            @Valid @ModelAttribute("form") MovimentacaoForm f,
            BindingResult erros,
            Principal principal,
            Model m,
            RedirectAttributes flash) {
        if (!erros.hasErrors())
            try {
                movimentacoes.registrar(
                        f.getProdutoId(),
                        Long.valueOf(principal.getName()),
                        f.getTipo(),
                        f.getQuantidade(),
                        f.getObservacao());
            } catch (IllegalArgumentException ex) {
                erros.reject("dados", ex.getMessage());
            }
        if (erros.hasErrors()) {
            var filtro = new FiltroMovimentacao();
            filtro.setProduto(f.getProdutoId());
            m.addAttribute("filtro", filtro);
            carregar(m, filtro);
            return "movimentacoes";
        }
        flash.addFlashAttribute(
                "sucesso",
                f.getTipo() == TipoMovimentacao.ENTRADA
                        ? "Entrada registrada com sucesso."
                        : "Saída registrada com sucesso.");
        return "redirect:/movimentacoes?produto=" + f.getProdutoId();
    }

    private void carregar(Model m, FiltroMovimentacao filtro) {
        m.addAttribute("movimentacoes", movimentacoes.pesquisar(filtro));
        m.addAttribute("produtosAtivos", produtos.pesquisar("", null, true, ""));
        m.addAttribute("todosProdutos", produtos.listar());
        // A tela precisa apenas de ID e nome, nunca de senhaHash.
        m.addAttribute(
                "responsaveis", usuarios.findAll(org.springframework.data.domain.Sort.by("nome")));
    }
}
