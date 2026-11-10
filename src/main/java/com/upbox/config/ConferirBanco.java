package com.upbox.config;

import lombok.RequiredArgsConstructor;

import org.springframework.boot.*;
import org.springframework.core.annotation.Order;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

/** Confere as proteções que o MySQL precisa; não altera dados nem recria tabelas. */
@Component
@Order(0)
@RequiredArgsConstructor
public class ConferirBanco implements ApplicationRunner {
    private final JdbcTemplate jdbc;

    @Override
    public void run(ApplicationArguments args) {
        String banco =
                jdbc.execute((java.sql.Connection c) -> c.getMetaData().getDatabaseProductName());
        if (!"MySQL".equals(banco)) return; // H2 aparece somente nos testes rápidos.
        Integer incorretas =
                jdbc.queryForObject(
                        "select count(*) from information_schema.tables where"
                            + " table_schema=database() and table_name in"
                            + " ('usuarios','categorias','produtos','movimentacoes_estoque','registros_operacoes')"
                            + " and engine <> 'InnoDB'",
                        Integer.class);
        if (incorretas != 0)
            throw new IllegalStateException(
                    "Use tabelas InnoDB para preservar transações e chaves estrangeiras.");
        Integer colunas =
                jdbc.queryForObject(
                        "select count(*) from information_schema.columns where"
                            + " table_schema=database() and ((table_name='usuarios' and column_name"
                            + " in ('usuario','email')) or (table_name='categorias' and"
                            + " column_name='nome') or (table_name='produtos' and"
                            + " column_name='codigo')) and collation_name not like '%\\_ci'",
                        Integer.class);
        if (colunas != 0)
            throw new IllegalStateException(
                    "Use collation utf8mb4_unicode_ci para impedir duplicações por"
                            + " maiúsculas/minúsculas.");
    }
}
