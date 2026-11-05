> Instalação atual: siga [INSTALACAO-ESCOLA.md](INSTALACAO-ESCOLA.md). A conexão é preenchida em application.properties; a senha escolar antiga foi removida. Arquivo local é opcional.

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

## 3. Preencher a conexão

Edite `src/main/resources/application.properties`: localhost:5432/upbox, usuário postgres (ou root se existente) e senha preenchida somente neste computador. Nenhuma senha escolar é distribuída. O arquivo local é apenas alternativa opcional e não deve sobrescrever uma configuração funcional.

A senha do PostgreSQL não é a senha de login. Crie o administrador pelo navegador local na primeira execução, seguindo [Instalação](INSTALACAO-ESCOLA.md). O histórico da antiga credencial deve ser tratado antes de tornar o repositório público; veja [Credenciais](CREDENCIAIS-E-HISTORICO.md).

## 4. Executar

```powershell
mvn clean test
mvn spring-boot:run
```

Ao iniciar pela primeira vez, o JPA pode criar as tabelas `categorias`, `produtos`, `usuarios` e `movimentacoes_estoque`. Verifique no pgAdmin em **Databases > upbox > Schemas > public > Tables** (Refresh).

**Atenção:** `spring.jpa.hibernate.ddl-auto=update` é uma facilidade **apenas para desenvolvimento local e TCC**. Não use em produção sem migrations.

## 5. Usar e conferir os registros

O backend e os formulários estão conectados: login, usuários, categorias, produtos, movimentações, dashboard e relatórios. Crie o primeiro administrador pelo navegador local conforme [Instalação](INSTALACAO-ESCOLA.md).

No pgAdmin, abra `upbox > Schemas > public > Tables`, atualize a lista e use **View/Edit Data > All Rows**. Existem `categorias`, `produtos`, `usuarios`, `movimentacoes_estoque` e `registros_operacoes`. Confira o saldo em `produtos.quantidade_estoque`; cada movimentação aponta para produto e usuário pelas chaves estrangeiras.

Para uma demonstração, cadastre pela interface, registre uma entrada de 10 e uma saída de 7, reinicie o servidor e confira o saldo 3. Não altere o saldo pelo pgAdmin para simular movimentações: isso pula regras e histórico. Nunca publique consultas ou capturas com dados sensíveis.

## 6. H2 e PostgreSQL nos testes

`mvn clean test` executa testes rápidos com H2 no escopo de teste. A aplicação utiliza PostgreSQL.

Para integração real, crie outro banco **exclusivo e descartável chamado `upbox_test`**. O perfil `mvn -Ppostgres-it verify` recria/apaga suas tabelas; a URL é verificada antes de iniciar o JPA. Configure as variáveis locais conforme [Entrega 3](ENTREGA-3-MOVIMENTACOES.md). Nunca aponte testes para o banco de uso da escola.

Os PostgreSQL 9.6.24 e 16.4 foram utilizados na homologação atual para verificar a aplicação, persistência após reinício, índices de unicidade, duas saídas simultâneas e rollback por falhas no histórico/auditoria. A CI também executa integração PostgreSQL.

## 7. Instalação e solução de problemas na escola

O [guia de instalação](INSTALACAO-ESCOLA.md) detalha configuração local sem sobrescrever arquivo existente, administrador inicial, consultas com colunas próprias para apresentação, erros de conexão/porta e instruções simples de backup/restauração pelo pgAdmin. As verificações presenciais que ainda faltam estão registradas na entrega 7.
