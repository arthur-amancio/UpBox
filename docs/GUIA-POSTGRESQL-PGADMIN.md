# UpBox — PostgreSQL e pgAdmin na escola

## 1. O que cada programa faz?

- **PostgreSQL**: servidor que guarda as tabelas e registros.
- **pgAdmin**: interface para administrar o servidor; não é o banco em si.
- **Spring Data JPA**: biblioteca que relaciona classes `@Entity` a tabelas.

## 2. Criar o banco no pgAdmin

1. Confirme que o serviço PostgreSQL está instalado e em execução no computador.
2. Abra o **pgAdmin** e conecte-se ao servidor local com a senha definida na instalação.
3. Clique em **Databases > Create > Database...**.
4. Defina **Database = upbox** e **Owner = postgres** (caso este seja o usuário da escola).
5. Salve. Não é preciso criar as tabelas manualmente nesta fase.

Se preferir usar o Query Tool conectado ao banco `postgres`, execute:

```sql
CREATE DATABASE upbox;
```

## 3. Guardar a senha no computador, sem publicá-la no Git

Na **raiz do projeto** (pasta do `pom.xml`), copie:

```powershell
Copy-Item application-local.example.properties application-local.properties
```

Abra `application-local.properties` e substitua o texto `COLOQUE_A_SENHA_LOCAL_AQUI` pela senha do PostgreSQL da escola.

Exemplo de configuração local (senha fictícia):

```properties
spring.datasource.url=jdbc:postgresql://localhost:5432/upbox
spring.datasource.username=postgres
spring.datasource.password=MINHA_SENHA_LOCAL
```

O arquivo `application.properties` importa automaticamente o arquivo local, quando ele existe. `application-local.properties` está no `.gitignore`; assim a senha permanece salva **no seu computador** e não aparece no GitHub.

**Importante:** se a senha real tiver sido colocada em um arquivo versionado, não execute `git add .` até corrigir isso. O GitHub guarda o histórico de commits.

## 4. Executar

```powershell
mvn clean test
mvn spring-boot:run
```

Ao iniciar pela primeira vez, o JPA pode criar as tabelas `categorias`, `produtos`, `usuarios` e `movimentacoes_estoque`. Verifique no pgAdmin em **Databases > upbox > Schemas > public > Tables** (Refresh).

**Atenção:** `spring.jpa.hibernate.ddl-auto=update` é uma facilidade **apenas para desenvolvimento local e TCC**. Não use em produção sem migrations.

## 5. Estado do sistema nesta etapa

- **Implementado no backend:** entidades, repositórios, cadastro de produto por serviço e movimentações transacionais com saldo protegido.
- **Ainda não implementado:** login real, autorização no servidor e formulários conectados ao PostgreSQL.
- **A interface continua demonstrativa**: seus dados ficam no `localStorage`. O sistema não deve ser publicado na internet com dados reais nesta etapa.
- O `MovimentacaoEstoqueService` é interno; futuramente o usuário responsável deverá vir da sessão autenticada (nunca de um campo enviado pelo navegador).

**Os testes H2 exercitam as entidades e serviços**, mas a conexão final ainda precisa ser verificada com um PostgreSQL local. Uma etapa posterior deve acrescentar testes de integração com PostgreSQL para validar os bloqueios sob concorrência.
