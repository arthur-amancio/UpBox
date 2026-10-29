# UpBox — sistema de gerenciamento de estoque (TCC)

Projeto acadêmico com **Java 17, Spring Boot e Thymeleaf**.

## Estado atual

O UpBox possui um **protótipo visual navegável**. Login, usuários, produtos, categorias e movimentações ainda usam dados demonstrativos salvos no navegador (`localStorage`). **Não existe autenticação real nem banco de dados nesta versão. Não utilize dados reais.**

A evolução para um sistema funcional será feita em etapas, com testes, revisão e comentários em português para apoiar os estudos do grupo.

## Como executar

Requisitos: Java 17 e Maven.

```sh
mvn spring-boot:run
```

Abra http://localhost:8080. Contas *apenas demonstrativas*: `administrador.demo` e `funcionario.demo`, senha `demo123`.

Para rodar os testes:

```sh
mvn test
```

## Documentação para a equipe

- [Plano de desenvolvimento](docs/PLANO-DESENVOLVIMENTO.md): objetivos, módulos, regras, cronograma de PRs e decisões pendentes.
- [Guia de estudos](docs/GUIA-DE-ESTUDOS.md): como entender classes, métodos, rotas e testes do projeto.

## Organização

- `src/main/java/com/upbox/controller`: rotas web.
- `src/main/java/com/upbox/model`: regras de domínio, iniciadas pela classe didática `Produto` (ainda não conectada às telas).
- `src/main/resources/templates`: páginas Thymeleaf.
- `src/main/resources/static`: CSS, JavaScript e imagens do protótipo.
- `src/test`: testes automatizados.
