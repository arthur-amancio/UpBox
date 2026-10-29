# UpBox — sistema de gerenciamento de estoque (TCC)

Projeto acadêmico com **Java 17, Spring Boot e Thymeleaf**.

## Estado atual

O UpBox possui um **protótipo visual navegável**. Login, usuários, produtos, categorias e movimentações ainda usam dados demonstrativos salvos no navegador (`localStorage`). **Não existe autenticação real nem banco de dados nesta versão. Não utilize dados reais.**

A evolução para um sistema funcional será feita em etapas, com testes, revisão e comentários em português para apoiar os estudos do grupo.

## Como executar

Requisitos: JDK 17 e Maven 3.6.3 ou superior, disponíveis no PATH.

```sh
mvn spring-boot:run
```

Abra http://localhost:8080. Contas *apenas demonstrativas*: `administrador.demo` e `funcionario.demo`, senha `demo123`.

Para rodar os testes:

```sh
mvn clean test
```

## Documentação para a equipe

- [Plano de desenvolvimento](docs/PLANO-DESENVOLVIMENTO.md): objetivos, módulos, regras, cronograma de PRs e decisões pendentes.
- [Guia de estudos](docs/GUIA-DE-ESTUDOS.md): como entender classes, métodos, rotas e testes do projeto.
- [Alinhamento com a documentação oficial](docs/ALINHAMENTO-DOCUMENTACAO-DRIVE.md): diferenças encontradas no Drive, decisões pendentes e prioridades.

## Organização

- `src/main/java/com/upbox/controller`: rotas web.
- `src/main/java/com/upbox/model`: regras de domínio, iniciadas pela classe didática `Produto` (ainda não conectada às telas).
- `src/main/resources/templates`: páginas Thymeleaf.
- `src/main/resources/static`: CSS, JavaScript e imagens do protótipo.
- `src/test`: testes automatizados.

Os pacotes `service`, `repository` e `config` serão criados quando houver operações, persistência e segurança que precisem deles. As dependências atuais são Spring MVC, Thymeleaf e testes; não é necessário instalar banco para executar esta etapa.

## Executar no VS Code

1. Instale o JDK 17, Maven e as extensões **Extension Pack for Java** e **Spring Boot Extension Pack**.
2. Abra a pasta que contém `pom.xml` e aguarde a importação do Maven.
3. No terminal integrado, confira `java -version` e `mvn -version`: ambos devem usar Java 17. Se necessário, ajuste `JAVA_HOME` para o JDK e reabra o VS Code.
4. Execute `mvn clean test` e depois `mvn spring-boot:run`, ou execute `UpboxApplication` pelo botão Run do editor.
5. Abra http://localhost:8080. Para encerrar pelo terminal, use `Ctrl+C`.

O servidor escuta apenas em `127.0.0.1` por padrão, pois este protótipo não possui autenticação real. Se a porta estiver ocupada, no PowerShell use:

```powershell
$env:SERVER_PORT = '8081'
mvn spring-boot:run
```

Nesse caso, abra http://localhost:8081. A variável vale para o terminal atual; `Remove-Item Env:SERVER_PORT` restaura a porta padrão nas próximas execuções.

## Configuração local

O arquivo `.env.example` documenta `SERVER_ADDRESS` e `SERVER_PORT`. **Spring Boot não lê `.env` automaticamente**: configure as variáveis no terminal, como no exemplo acima. Não há credenciais de banco nesta etapa.

Para ajustes exclusivos da sua máquina, também é possível criar `src/main/resources/application-local.properties` (ignorado pelo Git) e executar `mvn spring-boot:run "-Dspring-boot.run.profiles=local"`. Não salve senhas em arquivos versionados. `.env`, logs e `target/` também são ignorados.

## Antes do cadastro real de produtos

Confirme as decisões de banco, perfis, criação de contas, preço e desativação descritas em `docs/ALINHAMENTO-DOCUMENTACAO-DRIVE.md`. A próxima etapa deve implementar autenticação e permissões no servidor, persistência com código único e validações, e só então ligar os formulários ao backend. A classe `Produto` atual é didática e não salva registros. Até lá, todas as telas continuam sendo demonstrações com dados no navegador.
