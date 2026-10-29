# UpBox — sistema de gerenciamento de estoque

TCC didático em **Java 17, Spring Boot, Spring MVC, Spring Data JPA, PostgreSQL e Thymeleaf**. Interface original preservada, com formulários reais, Lombok e JavaScript apenas para interações visuais.

## Funcionalidades

- Login por usuário ou e-mail, BCrypt, sessão, logout e CSRF.
- Administrador gerencia usuários, perfis, categorias e produtos; funcionário consulta e registra movimentações.
- Perfil, troca de senha, ativação/desativação e auditoria das ações principais.
- Produtos com código único, categoria, descrição, preço, mínimo, pesquisa e filtros.
- Entradas, saídas e histórico com responsável autenticado; transação e bloqueio protegem o saldo.
- Dashboard com indicadores reais e seis relatórios filtráveis. Administrador prepara impressão/Salvar como PDF pelo navegador.

Não há contas demo ou cadastro público. Produtos começam com saldo zero; o saldo muda por movimentação. Não existe exclusão física de produtos, categorias ou usuários pela interface.

## Executar a partir da main no computador da escola

Instale **JDK 17**, **Maven** e o servidor **PostgreSQL**. O **pgAdmin III** e um programa para administrar o PostgreSQL; nao substitui o banco. Docker nao e necessario.

1. Clone o projeto e abra a pasta do `pom.xml` no VS Code:

   ```powershell
   git clone https://github.com/arthur-amancio/UpBox.git
   cd UpBox
   ```

   Repositorio privado: cada integrante precisa ter permissao de acesso no GitHub.

2. Abra o pgAdmin III e conecte-se ao PostgreSQL local usando o usuario `postgres`. Se o banco `upbox` ainda nao existir, conecte ao banco administrativo `postgres` e execute no Query Tool:

   ```sql
   CREATE DATABASE upbox;
   ```

   Se o banco ja existir, nao execute esse comando novamente. O JPA cria/atualiza as tabelas dentro de `upbox` na primeira inicializacao.

3. A conexao **ja esta configurada** no `src/main/resources/application.properties` para o ambiente da escola:
   - servidor: `localhost:5432`
   - banco: `upbox`
   - usuario: `postgres`
   - senha: a senha padrao escolar informada pelo responsavel, incluida no arquivo

   Nao e necessario copiar arquivo local para conectar ao PostgreSQL escolar. **Atencao:** por decisao do responsavel, essa credencial conhecida esta versionada no repositorio privado. Ela nao deve ser reutilizada fora do laboratorio nem em ambiente de producao. Se a instalacao usar outra senha, use `DB_PASSWORD` ou o arquivo local ignorado pelo Git.

4. **A senha do PostgreSQL NAO e a senha de login do UpBox.** Para criar a primeira conta administradora, na raiz do projeto crie o arquivo local, apenas se ainda nao existir:

   ```powershell
   if (!(Test-Path application-local.properties)) {
       Copy-Item application-local.example.properties application-local.properties
   }
   ```

   Edite `application-local.properties`: defina um e-mail valido, usuario e senha proprio para o administrador, descomente `upbox.admin-inicial.senha` e altere `upbox.admin-inicial.habilitado=true`. A senha deve possuir pelo menos 8 caracteres e nao pode ser a senha do banco. Depois da criacao do usuario, remova a senha de administrador inicial do arquivo e defina `habilitado=false`. **Preserve** um arquivo local que ja esteja funcionando.

5. Inicie:

   ```powershell
   mvn clean test
   mvn spring-boot:run
   ```

6. Acesse **http://localhost:8080** e entre com o administrador que acabou de criar. Cadastre categoria/produto, faca entradas e saidas e confira o dashboard e os relatorios.

A aplicacao usa `127.0.0.1:8080` por padrao. `DB_URL`, `DB_USER`, `DB_PASSWORD`, `SERVER_PORT` e `SERVER_ADDRESS` permitem configurar outro ambiente. `ddl-auto=update` e somente para desenvolvimento local.

Leia [Instalacao na escola](docs/INSTALACAO-ESCOLA.md) para instrucao detalhada de pgAdmin III e solucao de problemas.

## Testes

```powershell
mvn clean test
```

Os testes rápidos usam H2 apenas no escopo de teste. Para PostgreSQL real, crie o banco **exclusivo e descartável `upbox_test`** e siga o [guia da entrega 3](docs/ENTREGA-3-MOVIMENTACOES.md):

```powershell
$env:UPBOX_TEST_DB_URL = 'jdbc:postgresql://localhost:5432/upbox_test'
$env:UPBOX_TEST_DB_USER = 'postgres'
$env:UPBOX_TEST_DB_PASSWORD = 'SUA_SENHA_LOCAL'
mvn -Ppostgres-it verify
Remove-Item Env:UPBOX_TEST_DB_PASSWORD
```

Esse perfil recria as tabelas do banco de teste e verifica concorrência, rollback e unicidade. Ele rejeita outro nome de banco antes de iniciar o JPA. A CI executa testes rápidos e integração PostgreSQL.

## Organização e guias

`model` contém entidades/formulários; `repository` acessa o banco; `service` aplica regras; `controller` atende rotas MVC; `config` configura segurança; `templates` apresenta HTML; `static` contém CSS, logo e interações visuais.

- [Guia de estudos e roteiro da apresentação](docs/GUIA-DE-ESTUDOS.md)
- [PostgreSQL e pgAdmin](docs/GUIA-POSTGRESQL-PGADMIN.md)
- [Autenticação e administrador inicial](docs/ENTREGA-1-AUTENTICACAO.md)
- [Categorias, produtos e preço](docs/ENTREGA-2-CADASTROS.md)
- [Movimentações e testes reais](docs/ENTREGA-3-MOVIMENTACOES.md)
- [Dashboard e relatórios](docs/ENTREGA-4-RELATORIOS.md)
- [Relatório final e inventário das mudanças](docs/RELATORIO-FINAL-MVP.md)
- [Alinhamento com a documentação oficial](docs/ALINHAMENTO-DOCUMENTACAO-DRIVE.md)

A versao integrada e distribuida pela branch **main**. Os PRs de desenvolvimento continuam disponiveis como historico no GitHub.

## Revisão e apresentação do TCC

A revisão técnica corrigiu a disputa entre troca de senha e desativação, tratou falhas de banco e melhorou a navegação por teclado, a prévia informativa de saldo e os filtros. Não houve troca de arquitetura ou de identidade visual.

- [Entrega 5 — revisão técnica e evidências das correções](docs/ENTREGA-5-REVISAO-TECNICA.md)
- [Entrega 6 — interface e teclado](docs/ENTREGA-6-INTERFACE.md)
- [Entrega 7 — jornada real, capturas e pendências](docs/ENTREGA-7-PREPARACAO-TCC.md)
- [Instalação na escola, erros comuns e backup](docs/INSTALACAO-ESCOLA.md)
- [Roteiro de apresentação de 8 minutos](docs/APRESENTACAO-TCC.md)
- [Matriz com o TCC oficial](docs/MATRIZ-REQUISITOS-TCC.md)
- [Relatório desta revisão](docs/RELATORIO-REVISAO-TCC.md)

Para receber a versao mais recente, use `git switch main` e `git pull origin main`.
