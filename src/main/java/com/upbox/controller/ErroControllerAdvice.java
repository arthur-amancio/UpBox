package com.upbox.controller;

import org.springframework.dao.*;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

/** Mensagens compreensíveis sem devolver SQL ou detalhes internos ao navegador. */
@ControllerAdvice
@lombok.extern.slf4j.Slf4j
public class ErroControllerAdvice {
    @ExceptionHandler({
        DataIntegrityViolationException.class,
        IllegalArgumentException.class,
        MethodArgumentTypeMismatchException.class,
        CannotAcquireLockException.class
    })
    @ResponseStatus(org.springframework.http.HttpStatus.BAD_REQUEST)
    public String erro(Exception ex, Model m) {
        String mensagem = "Não foi possível concluir. Verifique os dados e tente novamente.";
        if (ex instanceof CannotAcquireLockException)
            mensagem = "Produto em uso por outra operação. Tente novamente.";
        else if (ex instanceof DataIntegrityViolationException)
            mensagem =
                    "Os dados conflitam com um registro existente ou ultrapassam os limites"
                            + " permitidos.";
        else if (ex instanceof IllegalArgumentException) mensagem = ex.getMessage();
        m.addAttribute("erro", mensagem);
        return "erro";
    }

    /** Falhas de conexão não são erros de preenchimento e não devem expor SQL. */
    @ExceptionHandler(DataAccessException.class)
    @ResponseStatus(org.springframework.http.HttpStatus.SERVICE_UNAVAILABLE)
    public String bancoIndisponivel(DataAccessException ex, Model m) {
        // Não registra parâmetros, mensagens SQL ou dados do usuário.
        log.error("Falha de acesso ao banco. Tipo: {}", ex.getClass().getSimpleName());
        m.addAttribute(
                "erro", "Não foi possível acessar o banco de dados. Tente novamente em instantes.");
        return "erro";
    }
}
