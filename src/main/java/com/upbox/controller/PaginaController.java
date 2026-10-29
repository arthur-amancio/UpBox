package com.upbox.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

// Cada método responde a uma rota (endereço) e devolve o nome do arquivo HTML
// que está em src/main/resources/templates (sem o ".html").
// Neste protótipo não há regra de negócio nem banco de dados: só navegação.
@Controller
public class PaginaController {

    @GetMapping("/")
    public String inicio() {
        return "index";
    }

    @GetMapping("/login")
    public String login() {
        return "login";
    }

    @GetMapping("/cadastro")
    public String cadastro() {
        return "cadastro";
    }

    @GetMapping("/home")
    public String home() {
        return "home";
    }

    @GetMapping("/dashboard")
    public String dashboard() {
        return "dashboard";
    }

    @GetMapping("/estoque")
    public String estoque() {
        return "estoque";
    }

    @GetMapping("/movimentacoes")
    public String movimentacoes() {
        return "movimentacoes";
    }

    @GetMapping("/relatorios")
    public String relatorios() {
        return "relatorios";
    }

    @GetMapping("/configuracoes")
    public String configuracoes() {
        return "configuracoes";
    }
}
