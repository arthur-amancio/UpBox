package com.upbox.integration;

import static org.junit.jupiter.api.Assertions.*;

import com.upbox.UpboxApplication;
import com.upbox.model.*;
import com.upbox.repository.*;
import com.upbox.service.*;
import java.math.BigDecimal;
import org.junit.jupiter.api.Test;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.jdbc.core.JdbcTemplate;

/** Fecha e abre a aplicação com o mesmo banco descartável, sem recriar as tabelas. */
class PostgresReinicioIT {
  private ConfigurableApplicationContext iniciar(String ddl) {
    String url =
        System.getenv()
            .getOrDefault("UPBOX_TEST_DB_URL", "jdbc:postgresql://localhost:5432/upbox_test");
    if (!url.matches("jdbc:postgresql://[^/]+/upbox_test(\\?.*)?"))
      throw new IllegalArgumentException("Reinício exige o banco exclusivo upbox_test.");
    return new SpringApplicationBuilder(UpboxApplication.class)
        .run(
            "--server.port=0",
            "--spring.datasource.url=" + url,
            "--spring.datasource.username="
                + System.getenv().getOrDefault("UPBOX_TEST_DB_USER", "postgres"),
            "--spring.datasource.password="
                + System.getenv().getOrDefault("UPBOX_TEST_DB_PASSWORD", ""),
            "--spring.jpa.hibernate.ddl-auto=" + ddl,
            "--upbox.admin-inicial.habilitado=false");
  }

  @Test
  void reinicioMantemIdentificadoresSaldoHistoricoEConta() {
    Long produtoId;
    Long usuarioId;
    try (var app = iniciar("update")) {
      app.getBean(JdbcTemplate.class)
          .execute(
              "truncate table movimentacoes_estoque, produtos, categorias, usuarios,"
                  + " registros_operacoes restart identity cascade");
      UsuarioForm f = new UsuarioForm();
      f.setNome("Admin reinício");
      f.setUsuario("admin");
      f.setEmail("admin@example.com");
      f.setSenha("senha-teste-123");
      f.setPerfil(PerfilAcesso.ADMINISTRADOR);
      usuarioId = app.getBean(UsuarioService.class).salvar(null, f).getId();
      var categoria = app.getBean(CategoriaRepository.class).save(new Categoria("Papelaria", ""));
      var p =
          app.getBean(ProdutoService.class)
              .cadastrar("R01", "Caderno", "", categoria.getId(), 0, 2, new BigDecimal("10.50"));
      produtoId = p.getId();
      app.getBean(MovimentacaoEstoqueService.class)
          .registrar(produtoId, usuarioId, TipoMovimentacao.ENTRADA, 10, "Reinício");
      app.getBean(MovimentacaoEstoqueService.class)
          .registrar(produtoId, usuarioId, TipoMovimentacao.SAIDA, 7, "Reinício");
    }
    try (var app = iniciar("validate")) {
      var p = app.getBean(ProdutoRepository.class).findById(produtoId).orElseThrow();
      assertEquals(3, p.getQuantidade());
      assertEquals(1, app.getBean(UsuarioRepository.class).count());
      assertTrue(app.getBean(UsuarioRepository.class).existsById(usuarioId));
      assertEquals(2, app.getBean(MovimentacaoEstoqueRepository.class).count());
    }
  }
}
