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

## 3. Configuracao pronta para o PostgreSQL da escola

Nesta versao, `src/main/resources/application.properties` ja usa por padrao o banco `upbox`, o usuario PostgreSQL `postgres` e a senha escolar informada pelo responsavel. Portanto, **na escola nao precisa copiar nem editar outro arquivo para conectar ao banco**, caso o servidor use essas credenciais e a porta 5432.

A senha escolar foi mantida na configuracao versionada por pedido do responsavel, embora isso seja menos seguro: todos que possuem acesso ao repositorio privado podem le-la. Nao utilize essas credenciais em servidores acessiveis pela internet.

Em computadores com outra configuracao, crie `application-local.properties` na raiz usando o exemplo `application-local.example.properties`, descomente os campos do banco e ajuste usuario/senha/porta. O arquivo local fica ignorado pelo Git.

O **login do UpBox** e independente da senha de PostgreSQL. Para criar o primeiro administrador do sistema, siga o [guia de instalacao](INSTALACAO-ESCOLA.md).

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

## 7. Instalação e solução de problemas na escola

O [guia de instalação](INSTALACAO-ESCOLA.md) detalha configuração local sem sobrescrever arquivo existente, administrador inicial, consultas com colunas próprias para apresentação, erros de conexão/porta e instruções simples de backup/restauração pelo pgAdmin. As verificações presenciais que ainda faltam estão registradas na entrega 7.
