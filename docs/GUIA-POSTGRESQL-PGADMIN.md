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

## 5. Usar e conferir os registros

O backend e os formulários estão conectados: login, usuários, categorias, produtos, movimentações, dashboard e relatórios. Crie o primeiro administrador conforme [Entrega 1](ENTREGA-1-AUTENTICACAO.md).

No pgAdmin, abra `upbox > Schemas > public > Tables`, atualize a lista e use **View/Edit Data > All Rows**. Existem `categorias`, `produtos`, `usuarios`, `movimentacoes_estoque` e `registros_operacoes`. Confira o saldo em `produtos.quantidade_estoque`; cada movimentação aponta para produto e usuário pelas chaves estrangeiras.

Para uma demonstração, cadastre pela interface, registre uma entrada de 10 e uma saída de 7, reinicie o servidor e confira o saldo 3. Não altere o saldo pelo pgAdmin para simular movimentações: isso pula regras e histórico. Nunca publique consultas ou capturas com dados sensíveis.

## 6. H2 e PostgreSQL nos testes

`mvn clean test` executa testes rápidos com H2 no escopo de teste. A aplicação utiliza PostgreSQL.

Para integração real, crie outro banco **exclusivo e descartável chamado `upbox_test`**. O perfil `mvn -Ppostgres-it verify` recria/apaga suas tabelas; a URL é verificada antes de iniciar o JPA. Configure as variáveis locais conforme [Entrega 3](ENTREGA-3-MOVIMENTACOES.md). Nunca aponte testes para o banco de uso da escola.

O PostgreSQL 16.4 foi utilizado nesta entrega para verificar a aplicação, persistência após reinício, índices de unicidade, duas saídas simultâneas e rollback por falhas no histórico/auditoria. A CI também executa integração PostgreSQL.
