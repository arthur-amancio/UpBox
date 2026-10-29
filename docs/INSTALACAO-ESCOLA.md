# Instalar e executar o UpBox na escola

## 1. Preparar o computador

Instale JDK 17, Maven, PostgreSQL e pgAdmin. O PostgreSQL é o servidor; pgAdmin é o programa para administrar o servidor. Docker não é necessário. No VS Code, abra a pasta do UpBox que contém `pom.xml`, não apenas `src/`.

Abra Terminal → New Terminal e confira:

```powershell
java -version
mvn -version
```

Ambos devem indicar Java 17. Se algum comando não existir, revise a instalação/PATH e abra outro terminal. As extensões Java do VS Code ajudam na edição, mas os comandos Maven continuam suficientes para executar.

Atualize o repositório e selecione a versão mais recente indicada no README. Não altere a main nem faça merges para apenas demonstrar o sistema.

## 2. Criar o banco no pgAdmin

1. Confirme que o serviço PostgreSQL está em execução.
2. Abra pgAdmin e conecte ao servidor local, normalmente `localhost`, porta `5432`, usuário `postgres`.
3. Use a senha do servidor PostgreSQL definida na instalação. A senha principal do pgAdmin, quando solicitada para abrir o programa, é outra configuração.
4. Em Databases, clique com o botão direito → Create → Database.
5. Nome: `upbox`. Owner: o usuário PostgreSQL da escola, normalmente `postgres`.
6. Salve. O JPA cria/atualiza as tabelas ao iniciar o UpBox; não é necessário desenhar as tabelas manualmente nesta etapa.

## 3. Salvar a conexão no arquivo local

Na raiz do projeto, crie a cópia **somente se ela ainda não existir**:

```powershell
if (!(Test-Path application-local.properties)) {
    Copy-Item application-local.example.properties application-local.properties
}
```

Se já existir uma configuração funcional, preserve-a. Abra o arquivo local e ajuste:

```properties
spring.datasource.url=jdbc:postgresql://localhost:5432/upbox
spring.datasource.username=postgres
spring.datasource.password=SUA_SENHA_LOCAL
```

Substitua o texto de exemplo apenas nesse arquivo. A senha permanece salva no computador e é carregada automaticamente a cada execução. O arquivo continua no `.gitignore`; não o envie ao GitHub nem coloque a senha no `application.properties` versionado. Não é preciso digitar a senha no terminal toda vez.

## 4. Criar o primeiro administrador

No mesmo arquivo local, configure:

```properties
upbox.admin-inicial.habilitado=true
upbox.admin-inicial.nome=Administrador da equipe
upbox.admin-inicial.usuario=administrador
upbox.admin-inicial.email=admin@example.com
upbox.admin-inicial.senha=DEFINA_UMA_SENHA_SOMENTE_NESTE_ARQUIVO_LOCAL
```

A senha escolhida deve ter pelo menos 8 caracteres, não ser em branco e respeitar o limite de 72 bytes do BCrypt. Não use o texto de exemplo como senha da equipe.

Execute:

```powershell
mvn clean test
mvn spring-boot:run
```

Abra http://localhost:8080 e entre com usuário/e-mail e senha que configurou. Depois da primeira criação, remova a linha da senha inicial e altere:

```properties
upbox.admin-inicial.habilitado=false
```

A criação inicial não substitui administrador existente, inclusive inativo. O administrador cadastra os demais integrantes em Usuários. Não existe conta demo nem cadastro público.

## 5. Usar, parar e reiniciar

Cadastre categoria e produto; registre uma entrada e uma saída. Use Logout/Sair ao encerrar sua sessão. Para parar o servidor, pressione Ctrl+C no terminal que executa Spring Boot. Inicie novamente com `mvn spring-boot:run`: os dados continuam no PostgreSQL.

O programa escuta somente em `127.0.0.1` por padrão. Abra a apresentação nesse computador. Os testes de integração não usam o banco de uso `upbox`.

## 6. Consultar os registros no pgAdmin

Abra `upbox → Schemas → public → Tables` e atualize a lista. As tabelas principais são `usuarios`, `categorias`, `produtos`, `movimentacoes_estoque` e `registros_operacoes`.

No Query Tool, execute exemplos com os campos que deseja apresentar:

```sql
SELECT id, nome, usuario, email, perfil, ativo FROM usuarios ORDER BY id;
SELECT codigo, nome, quantidade_estoque, estoque_minimo, preco, ativo
FROM produtos ORDER BY nome;
SELECT m.data_hora, p.codigo, p.nome AS produto, u.nome AS responsavel,
       m.tipo, m.quantidade, m.observacao
FROM movimentacoes_estoque m
JOIN produtos p ON p.id = m.produto_id
JOIN usuarios u ON u.id = m.usuario_id
ORDER BY m.data_hora DESC, m.id DESC;
```

Não projete `senha_hash`. Evite mudar saldo diretamente no pgAdmin: isso pula validações, histórico e auditoria. Use os formulários do sistema para demonstrar as regras.

## 7. Executar os testes

```powershell
mvn clean test
```

Os testes rápidos usam H2 apenas no ambiente de testes. Para testar PostgreSQL real, crie **outro banco exclusivo e descartável chamado `upbox_test`**. As tabelas desse banco serão recriadas/apagadas:

```powershell
$env:UPBOX_TEST_DB_URL = 'jdbc:postgresql://localhost:5432/upbox_test'
$env:UPBOX_TEST_DB_USER = 'postgres'
$env:UPBOX_TEST_DB_PASSWORD = 'SUA_SENHA_LOCAL'
mvn -Ppostgres-it verify
Remove-Item Env:UPBOX_TEST_DB_PASSWORD
```

O perfil rejeita outro nome de banco antes de iniciar o JPA. Não use `upbox` para testes destrutivos. As variáveis acima são exclusivas do banco de testes e não substituem a configuração local persistente da aplicação.

## 8. Problemas comuns

| Sintoma | O que conferir |
| --- | --- |
| Connection refused | Serviço PostgreSQL iniciado; host/porta corretos no arquivo local |
| Password authentication failed | Usuário e senha do servidor PostgreSQL no arquivo local; não confundir com a senha principal do pgAdmin |
| Database upbox does not exist | Criar o banco no servidor/porta indicados na URL |
| Senha local parece ignorada | Executar a partir da pasta do pom.xml; nome exato application-local.properties; conferir variáveis de ambiente que possam sobrescrever a configuração |
| Porta 8080 ocupada | Encerrar outro UpBox ou adicionar `server.port=8081` ao arquivo local e acessar localhost:8081 |
| Comando mvn/java não encontrado | Instalação e PATH; reabrir terminal após corrigir |
| Versão do Java incorreta | Conferir java -version e mvn -version; ajustar JAVA_HOME/PATH para JDK 17 |
| Erro ao criar índice único | Verificar duplicidades antigas de usuário/e-mail/categoria/código; não apagar tabelas para contornar |
| Conta não entra | Conferir atividade, usuário/e-mail e senha; a criação inicial não redefine conta existente |
| Sessão expirada ou formulário antigo | Fazer login novamente e reabrir o formulário, mantendo CSRF habilitado |
| Não vejo tabelas/registros | Atualizar o pgAdmin e confirmar banco, servidor e porta; usar a mesma URL da aplicação |

Leia os detalhes técnicos no terminal. Páginas comuns não devem mostrar SQL ou stack traces. `ddl-auto=update` é mantido somente para desenvolvimento/TCC; não apague tabelas para resolver erros de conexão.

## 9. Backup simples pelo pgAdmin

1. Pare movimentações durante o backup da demonstração.
2. Clique no banco `upbox` → Backup; escolha formato Custom e um arquivo fora do repositório.
3. Confira a mensagem de conclusão do pgAdmin e guarde o arquivo com data.
4. Para ensaiar restauração, crie **outro banco**, como `upbox_restaurado`, e use Restore nesse banco vazio com o arquivo Custom.
5. Confira as tabelas e registros restaurados antes de considerar o backup utilizável. Não restaure por cima do banco em uso apenas para testar.

Esses passos são instruções para uso no pgAdmin da escola. O pgAdmin e o backup/restauração não foram executados no ambiente desta revisão; a integração da aplicação foi testada com PostgreSQL local isolado.
