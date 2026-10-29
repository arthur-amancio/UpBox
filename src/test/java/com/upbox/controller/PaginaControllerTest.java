package com.upbox.controller;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.view;

/**
 * Testa se as páginas existentes continuam acessíveis no protótipo.
 * A verificação de login será diferente quando houver autenticação real.
 */
@WebMvcTest(PaginaController.class)
class PaginaControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Test
    void deveAbrirAsPaginasDoPrototipo() throws Exception {
        // Cada rota deve devolver o arquivo HTML correspondente.
        String[][] paginas = {
            {"/", "index"},
            {"/login", "login"},
            {"/cadastro", "cadastro"},
            {"/home", "home"},
            {"/dashboard", "dashboard"},
            {"/estoque", "estoque"},
            {"/movimentacoes", "movimentacoes"},
            {"/relatorios", "relatorios"},
            {"/configuracoes", "configuracoes"}
        };

        for (String[] pagina : paginas) {
            mockMvc.perform(get(pagina[0]))
                    .andExpect(status().isOk())
                    .andExpect(view().name(pagina[1]));
        }
    }
}
