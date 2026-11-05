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

## Executar no computador da escola

Instale **Java 17, Maven e PostgreSQL**; Git é necessário para clonar o repositório privado. Use pgAdmin para administrar o banco. Não precisa de Docker, Node ou arquivo local adicional.

1. Clone e abra a pasta do pom.xml no VS Code:

   ```powershell
   git clone https://github.com/arthur-amancio/UpBox.git
   cd UpBox
   ```

2. No pgAdmin, confirme o serviço PostgreSQL e crie **upbox** somente se ainda não existir. Faça backup de um banco já utilizado.
3. Edite **src/main/resources/application.properties**. A conexão padrão é localhost:5432/upbox, usuário postgres; mude para root somente se essa conta existir no PostgreSQL. Preencha a senha no campo `spring.datasource.password=${DB_PASSWORD:}` após os dois-pontos e antes da chave final. Não envie a configuração preenchida ao GitHub.
4. Execute:

   ```powershell
   mvn clean test
   mvn spring-boot:run
   ```

5. Abra **http://localhost:8080** no próprio computador. No banco sem administrador, o navegador oferece **Primeiro acesso**. Cadastre nome, usuário, e-mail e senha de login própria. O UpBox usa BCrypt e encerra essa possibilidade após a criação, mesmo se a conta for desativada.
6. Entre com essa conta e cadastre os demais integrantes em Usuários.

A aplicação escuta 127.0.0.1:8080. O cadastro inicial aceita somente conexões locais e mantém CSRF ativo. A senha de login não é a senha do PostgreSQL.

**PostgreSQL 9:** os testes reais cobrem 9.6.24 e 16.4. Outra versão 9.x precisa de homologação adicional; veja [compatibilidade](docs/COMPATIBILIDADE-POSTGRESQL.md).

`application-local.properties` continua como alternativa opcional, preservada se já existir. DB_URL/DB_USER/DB_PASSWORD, SERVER_PORT e SERVER_ADDRESS também são alternativas. Nenhuma delas é requisito para os alunos. `ddl-auto=update` é apenas para desenvolvimento/TCC.

A senha escolar foi retirada da configuração atual. [O histórico ainda exige tratamento antes de tornar o repositório público](docs/CREDENCIAIS-E-HISTORICO.md).

Leia [Instalação Windows](docs/INSTALACAO-ESCOLA.md) para erros comuns, pgAdmin e backup.

## Pacote Windows sem Maven

`mvn clean package` gera **target/upbox-sistema-1.0.0-escola.zip**. Extraia o ZIP, preencha a senha em application.properties ao lado de upbox.jar e execute iniciar-upbox.bat. O pacote exige apenas Java 17 e PostgreSQL; não precisa de Maven para usar. O cadastro do administrador ocorre no navegador local.

A CI oferece o artefato **UpBox-escola** depois de executar o pacote no Windows. A homologação extrai para pasta com espaços, cria um PostgreSQL descartável, abre o JAR pelo .bat, cadastra o administrador com CSRF, reinicia, confere persistência e exercita mensagens de erro. O resultado está no artefato homologacao-windows. Veja [pacote e homologação](docs/PACOTE-E-HOMOLOGACAO.md).

Para impedir distribuição acidental de uma senha preenchida, a fase de empacotamento exige a configuração fonte sem senha; preencha apenas a cópia externa de uso. Esse bloqueio não interfere em `mvn clean test` ou `mvn spring-boot:run`.

## Testes automatizados

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

`entity` contém somente as cinco entidades JPA; `form` recebe formulários/filtros; `model` guarda enums e resumos simples; `repository` acessa o banco; `service` aplica regras; `controller` atende rotas MVC; `config` configura segurança; `templates` apresenta HTML; `static` contém CSS, logo e interações visuais.

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
