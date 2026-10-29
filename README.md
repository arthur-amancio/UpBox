# UpBox — sistema de gerenciamento de estoque (TCC)

Projeto didático com **Java 17, Spring Boot, Thymeleaf, Spring Data JPA e PostgreSQL**.

## Estado atual

O UpBox agora possui **entidades JPA, repositórios e serviços de estoque** no servidor, com testes automatizados.
Porém, **as telas continuam demonstrativas**: login, usuários e movimentações feitos pela interface ainda usam
dados fictícios no navegador (`localStorage`). Não existe autenticação real; não utilize dados reais.

As classes do backend já representam `Produto`, `Categoria`, `Usuario` e `MovimentacaoEstoque`.
O serviço de movimentações modifica o saldo e salva o histórico na mesma transação,
mas ainda não possui um controller acessível ao navegador, por segurança.

## Executar no VS Code com PostgreSQL

Requisitos: **JDK 17**, **Maven**, **PostgreSQL instalado e em execução** e opcionalmente **pgAdmin** para administrar o banco.

1. Abra o pgAdmin, conecte ao servidor PostgreSQL e crie o banco de dados `upbox`.
2. Na pasta do projeto (onde está o `pom.xml`), copie o exemplo local:

   ```powershell
   Copy-Item application-local.example.properties application-local.properties
   ```

3. Edite `application-local.properties` e **coloque a senha da instalação da escola**, além de usuário/porta se forem diferentes.
   O arquivo fica salvo no PC, é carregado automaticamente pelo Spring Boot e está no `.gitignore`.
4. Execute:

   ```powershell
   mvn clean test
   mvn spring-boot:run
   ```

5. Abra http://localhost:8080 e confira as tabelas em **pgAdmin → Databases → upbox → Schemas → public → Tables**.

**Atenção:** pgAdmin é um administrador gráfico; ele não substitui o servidor PostgreSQL.

Leia as instruções completas em [Guia PostgreSQL e pgAdmin](docs/GUIA-POSTGRESQL-PGADMIN.md).

O arquivo `application.properties` fica versionado **sem senha de verdade**.
É possível guardar a senha permanentemente no `application-local.properties` do computador,
sem expor a credencial no histórico do GitHub.

O servidor escuta apenas em `127.0.0.1` por padrão. Para alterar a porta:

```powershell
$env:SERVER_PORT = '8081'
mvn spring-boot:run
```

## Arquivos principais

- `src/main/java/com/upbox/model`: entidades de produtos, categorias, usuários e movimentações.
- `src/main/java/com/upbox/repository`: operações com o banco de dados.
- `src/main/java/com/upbox/service`: cadastro de produtos e movimentações transacionais.
- `src/main/java/com/upbox/controller`: rotas das páginas existentes (ainda demonstrativas).
- `src/main/resources/templates`: páginas Thymeleaf.
- `src/main/resources/static`: CSS, imagens e JS demonstrativo.
- `src/test`: testes de domínio, das rotas e da persistência.

## Documentos da equipe

- [Plano de desenvolvimento](docs/PLANO-DESENVOLVIMENTO.md)
- [Guia de estudos](docs/GUIA-DE-ESTUDOS.md)
- [Alinhamento com o Drive](docs/ALINHAMENTO-DOCUMENTACAO-DRIVE.md)
- [Guia PostgreSQL e pgAdmin](docs/GUIA-POSTGRESQL-PGADMIN.md)

## Próxima etapa

Criar autenticação real e autorização de administrador/funcionário, substituir
gradualmente os formulários simulados pelos formulários conectados aos serviços
e testar a concorrência com PostgreSQL real. Não publicar o protótipo atual na internet
nem permitir endpoints de escrita desprotegidos.
