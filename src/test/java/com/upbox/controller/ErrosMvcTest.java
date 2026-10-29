package com.upbox.controller;

import static org.hamcrest.Matchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import jakarta.servlet.*;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.*;
import org.springframework.dao.DataAccessResourceFailureException;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

/** Provoca falhas somente nos testes, sem criar endpoints de diagnóstico no sistema. */
@Import(ErrosMvcTest.Config.class)
class ErrosMvcTest extends CenarioMvc {
    @TestConfiguration
    static class Config {
        @Bean
        FalhaBancoController falhaBancoController() {
            return new FalhaBancoController();
        }
    }

    @Controller
    static class FalhaBancoController {
        @GetMapping("/teste/falha-banco")
        String falhar() {
            throw new DataAccessResourceFailureException(
                    "SQL: detalhe interno que não pode aparecer na página.");
        }
    }

    @Test
    void falhaNoBancoDeveMostrarMensagemClaraSemDetalhesTecnicos() throws Exception {
        mvc.perform(get("/teste/falha-banco").session(login("admin")))
                .andExpect(status().isServiceUnavailable())
                .andExpect(content().string(containsString("Não foi possível acessar o banco")))
                .andExpect(content().string(not(containsString("SQL:"))))
                .andExpect(
                        content()
                                .string(not(containsString("DataAccessResourceFailureException"))));
    }

    @Test
    void paginaInexistenteDeveTerMensagemAmigavel() throws Exception {
        mvc.perform(
                        get("/error")
                                .accept(org.springframework.http.MediaType.TEXT_HTML)
                                .session(login("admin"))
                                .with(
                                        r -> {
                                            r.setDispatcherType(DispatcherType.ERROR);
                                            return r;
                                        })
                                .requestAttr(RequestDispatcher.ERROR_STATUS_CODE, 404)
                                .requestAttr(RequestDispatcher.ERROR_REQUEST_URI, "/nao-existe"))
                .andExpect(status().isNotFound())
                .andExpect(content().string(containsString("Página não encontrada")))
                .andExpect(content().string(not(containsString("Whitelabel"))));
    }
}
