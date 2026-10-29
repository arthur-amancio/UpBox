package com.upbox.controller;

import com.upbox.model.*;
import com.upbox.repository.CategoriaRepository;
import com.upbox.service.*;

import jakarta.validation.Valid;

import lombok.RequiredArgsConstructor;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

/** Consultas para ambos os perfis; rotas de escrita são exclusivas do administrador. */
@Controller
@RequiredArgsConstructor
public class EstoqueController {
    private final ProdutoService produtos;
    private final CategoriaService categorias;
    private final CategoriaRepository categoriaRepository;

    @GetMapping("/estoque")
    public String estoque(
            @RequestParam(defaultValue = "") String busca,
            @RequestParam(required = false) Long categoria,
            @RequestParam(required = false) Boolean ativo,
            @RequestParam(defaultValue = "") String situacao,
            Model m) {
        m.addAttribute("produtos", produtos.pesquisar(busca, categoria, ativo, situacao));
        m.addAttribute("categorias", categoriaRepository.findAllByOrderByNomeAsc());
        m.addAttribute("busca", busca);
        m.addAttribute("categoria", categoria);
        m.addAttribute("ativo", ativo);
        m.addAttribute("situacao", situacao);
        return "estoque";
    }

    @GetMapping("/categorias")
    public String categorias(Model m) {
        m.addAttribute("categorias", categoriaRepository.findAllByOrderByNomeAsc());
        return "categorias";
    }

    @GetMapping("/categorias/nova")
    public String nova(Model m) {
        m.addAttribute("form", new CategoriaForm());
        return "categoria-form";
    }

    @GetMapping("/categorias/{id}/editar")
    public String categoria(@PathVariable Long id, Model m) {
        var c = categorias.buscar(id);
        var f = new CategoriaForm();
        f.setNome(c.getNome());
        f.setDescricao(c.getDescricao());
        m.addAttribute("id", id);
        m.addAttribute("form", f);
        return "categoria-form";
    }

    @PostMapping({"/categorias", "/categorias/{id}"})
    public String salvarCategoria(
            @PathVariable(required = false) Long id,
            @Valid @ModelAttribute("form") CategoriaForm f,
            BindingResult erros,
            Model m,
            RedirectAttributes flash) {
        m.addAttribute("id", id);
        if (!erros.hasErrors())
            try {
                categorias.salvar(id, f);
            } catch (IllegalArgumentException ex) {
                erros.reject("dados", ex.getMessage());
            }
        if (erros.hasErrors()) return "categoria-form";
        flash.addFlashAttribute("sucesso", "Categoria salva com sucesso.");
        return "redirect:/categorias";
    }

    @PostMapping("/categorias/{id}/status")
    public String statusCategoria(@PathVariable Long id, RedirectAttributes f) {
        categorias.alterarStatus(id);
        f.addFlashAttribute("sucesso", "Situação da categoria atualizada.");
        return "redirect:/categorias";
    }

    @GetMapping("/produtos/novo")
    public String novo(Model m) {
        m.addAttribute("form", new ProdutoForm());
        opcoes(m, null);
        return "produto-form";
    }

    @GetMapping("/produtos/{id}/editar")
    public String produto(@PathVariable Long id, Model m) {
        var p = produtos.buscar(id);
        var f = new ProdutoForm();
        f.setCodigo(p.getCodigo());
        f.setNome(p.getNome());
        f.setDescricao(p.getDescricao());
        f.setCategoriaId(p.getCategoria().getId());
        f.setEstoqueMinimo(p.getEstoqueMinimo());
        f.setPreco(p.getPreco());
        m.addAttribute("form", f);
        m.addAttribute("id", id);
        opcoes(m, id);
        return "produto-form";
    }

    @PostMapping({"/produtos", "/produtos/{id}"})
    public String salvarProduto(
            @PathVariable(required = false) Long id,
            @Valid @ModelAttribute("form") ProdutoForm f,
            BindingResult erros,
            Model m,
            RedirectAttributes flash) {
        m.addAttribute("id", id);
        if (!erros.hasErrors())
            try {
                produtos.salvar(id, f);
            } catch (IllegalArgumentException ex) {
                erros.reject("dados", ex.getMessage());
            }
        if (erros.hasErrors()) {
            opcoes(m, id);
            return "produto-form";
        }
        flash.addFlashAttribute("sucesso", "Produto salvo com sucesso.");
        return "redirect:/estoque";
    }

    @PostMapping("/produtos/{id}/status")
    public String statusProduto(@PathVariable Long id, RedirectAttributes f) {
        produtos.alterarStatus(id);
        f.addFlashAttribute("sucesso", "Situação do produto atualizada.");
        return "redirect:/estoque";
    }

    private void opcoes(Model m, Long id) {
        // A categoria atual inativa continua visível para não desfazer vínculos antigos.
        var lista = new java.util.ArrayList<>(categoriaRepository.findByAtivaTrueOrderByNomeAsc());
        if (id != null) {
            var atual = produtos.buscar(id);
            if (lista.stream().noneMatch(c -> c.getId().equals(atual.getCategoria().getId())))
                lista.add(atual.getCategoria());
            m.addAttribute("saldo", atual.getQuantidade());
        }
        m.addAttribute("categorias", lista);
    }
}
