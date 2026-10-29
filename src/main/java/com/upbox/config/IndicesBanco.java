package com.upbox.config;

import lombok.RequiredArgsConstructor;

import org.springframework.boot.*;
import org.springframework.core.annotation.Order;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

/** PostgreSQL exige índice funcional para unicidade sem diferenciar maiúsculas. */
@Component
@RequiredArgsConstructor
@Order(0)
public class IndicesBanco implements ApplicationRunner {
    private final JdbcTemplate jdbc;

    @Override
    public void run(ApplicationArguments args) {
        String produto =
                jdbc.execute((java.sql.Connection c) -> c.getMetaData().getDatabaseProductName());
        if (!"PostgreSQL".equals(produto)) return;
        jdbc.execute(
                "create unique index if not exists uk_categoria_nome_ci on categorias"
                        + " (lower(trim(nome)))");
        jdbc.execute(
                "create unique index if not exists uk_produto_codigo_ci on produtos"
                        + " (upper(trim(codigo)))");
        // Se houver duplicidades antigas, a inicialização falha sem apagar os registros.
        jdbc.execute(
                "create unique index if not exists uk_usuario_login_ci on usuarios"
                        + " (lower(trim(usuario)))");
        jdbc.execute(
                "create unique index if not exists uk_usuario_email_ci on usuarios"
                        + " (lower(trim(email)))");
    }
}
