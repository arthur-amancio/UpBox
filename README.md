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

## Executar no VS Code

Instale **JDK 17**, **Maven** e **PostgreSQL**. pgAdmin é opcional para administrar o servidor. Docker não é necessário.

1. Abra a pasta do `pom.xml` no VS Code.
2. No pgAdmin, crie o banco `upbox`.
3. Na raiz do projeto, copie o arquivo de exemplo:

   ```powershell
   if (!(Test-Path application-local.properties)) {
       Copy-Item application-local.example.properties application-local.properties
   }
   ```

4. Preserve uma configuração local já funcional. Configure usuário, porta e senha do PostgreSQL no arquivo local. Ele é carregado automaticamente, fica salvo no computador e está no `.gitignore`.
5. Para criar o primeiro administrador, configure `upbox.admin-inicial.*` nesse mesmo arquivo, incluindo uma senha local de pelo menos 8 caracteres, e defina `habilitado=true`.
6. Execute:

   ```powershell
   mvn clean test
   mvn spring-boot:run
   ```

7. Acesse http://localhost:8080. Depois da primeira criação, remova a senha inicial e defina `habilitado=false`. Uma conta de administrador existente nunca é substituída, mesmo que esteja inativa.
8. Cadastre categoria e produto; registre uma entrada e uma saída. Consulte o dashboard e o histórico.

A aplicação usa `127.0.0.1:8080` por padrão. `SERVER_PORT`, `SERVER_ADDRESS` e `DB_*` continuam disponíveis; `ddl-auto=update` é somente para desenvolvimento local.

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

O trabalho está em PRs encadeados, sem merge automático. Para executar todas as entregas antes da integração, use a branch `docs/preparacao-tcc`; a `main` não foi modificada.

## Revisão e apresentação do TCC

A revisão técnica corrigiu a disputa entre troca de senha e desativação, tratou falhas de banco e melhorou a navegação por teclado, a prévia informativa de saldo e os filtros. Não houve troca de arquitetura ou de identidade visual.

- [Entrega 5 — revisão técnica e evidências das correções](docs/ENTREGA-5-REVISAO-TECNICA.md)
- [Entrega 6 — interface e teclado](docs/ENTREGA-6-INTERFACE.md)
- [Entrega 7 — jornada real, capturas e pendências](docs/ENTREGA-7-PREPARACAO-TCC.md)
- [Instalação na escola, erros comuns e backup](docs/INSTALACAO-ESCOLA.md)
- [Roteiro de apresentação de 8 minutos](docs/APRESENTACAO-TCC.md)
- [Matriz com o TCC oficial](docs/MATRIZ-REQUISITOS-TCC.md)
- [Relatório desta revisão](docs/RELATORIO-REVISAO-TCC.md)

Para obter a revisão completa antes dos merges:

```powershell
git fetch origin
git switch docs/preparacao-tcc
```

O [relatório do MVP](docs/RELATORIO-FINAL-MVP.md) permanece como histórico das entregas 1–4. As entregas 5–7 e o relatório de revisão registram os resultados atuais.
