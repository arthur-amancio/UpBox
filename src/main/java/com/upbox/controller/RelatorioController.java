package com.upbox.controller;

import com.upbox.model.*;
import com.upbox.repository.CategoriaRepository;
import com.upbox.service.*;

import lombok.RequiredArgsConstructor;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

import java.time.*;

/** Ambos consultam; a rota de preparação de impressão exige administrador. */
@Controller
@RequiredArgsConstructor
public class RelatorioController {
    private final DashboardService dashboard;
    private final RelatorioService relatorios;
    private final ProdutoService produtos;
    private final CategoriaRepository categorias;
    private final AuditoriaService auditoria;

    @GetMapping("/dashboard")
    public String dashboard(Model m) {
        m.addAttribute("resumo", dashboard.consultar());
        return "dashboard";
    }

    @GetMapping("/relatorios")
    public String consultar(
            @RequestParam(defaultValue = "ESTOQUE") TipoRelatorio relatorio,
            @ModelAttribute("filtro") FiltroMovimentacao filtro,
            BindingResult erros,
            Model m) {
        if (erros.hasErrors())
            throw new IllegalArgumentException("Revise os filtros do relatório.");
        return carregar(relatorio, filtro, false, m);
    }

    @GetMapping("/relatorios/imprimir")
    public String imprimir(
            @RequestParam(defaultValue = "ESTOQUE") TipoRelatorio relatorio,
            @ModelAttribute("filtro") FiltroMovimentacao filtro,
            BindingResult erros,
            Model m) {
        if (erros.hasErrors())
            throw new IllegalArgumentException("Revise os filtros do relatório.");
        String pagina = carregar(relatorio, filtro, true, m);
        auditoria.registrar("Relatório preparado para impressão", relatorio.getTitulo());
        return pagina;
    }

    private String carregar(
            TipoRelatorio tipo, FiltroMovimentacao filtro, boolean impressao, Model m) {
        m.addAttribute("resultado", relatorios.consultar(tipo, filtro));
        m.addAttribute("tipos", TipoRelatorio.values());
        m.addAttribute("relatorio", tipo);
        m.addAttribute("todosProdutos", produtos.listar());
        m.addAttribute("categorias", categorias.findAllByOrderByNomeAsc());
        m.addAttribute("impressao", impressao);
        m.addAttribute("geradoEm", LocalDateTime.now(ZoneId.of("America/Sao_Paulo")));
        return "relatorios";
    }
}
