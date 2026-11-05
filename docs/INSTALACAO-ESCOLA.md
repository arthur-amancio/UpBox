# Instalação Windows — UpBox

## 1. Preparar

**Pacote pronto:** extraia o ZIP inteiro, instale Java 17 e PostgreSQL, edite application.properties ao lado do JAR e execute iniciar-upbox.bat. Não precisa de Maven, Git ou VS Code para somente utilizar esse pacote. Ele registra detalhes em logs/upbox.log e não apaga bancos. `iniciar-upbox.bat --verificar` confere Java/JAR/arquivo; não testa a conexão. Para mudar a porta, ajuste server.port e abra o endereço correspondente.

**Código-fonte:** siga os passos Maven/VS Code abaixo. Gerar o pacote é uma tarefa diferente de executar o sistema; o empacotamento bloqueia senha preenchida no fonte para não enviá-la a terceiros.

Instale JDK **17**, Maven, PostgreSQL e pgAdmin. No VS Code abra a pasta do pom.xml. `java -version` e `mvn -version` devem apontar Java 17. Os integrantes precisam de acesso ao repositório privado. Clone a main ou, durante a revisão desta entrega, a branch informada no PR final; não faça merges para instalar.

## 2. Banco

Inicie o serviço PostgreSQL. No pgAdmin conecte ao servidor localhost, porta 5432, usuário postgres. O pgAdmin não substitui o servidor. Confirme `SELECT version();`: homologamos **9.6.24 e 16.4**; outra versão exige teste adicional.

Crie o banco upbox **somente se ausente**. Não apague nem recrie um banco existente. Faça backup antes de atualizar uma instalação utilizada.

## 3. Um único arquivo de configuração

Edite `src/main/resources/application.properties` ao executar pelo Maven. Na distribuição JAR, edite `application.properties` ao lado do JAR. Exemplo de conexão, sem senha real:

```properties
spring.datasource.url=jdbc:postgresql://localhost:5432/upbox
spring.datasource.username=postgres
spring.datasource.password=PREENCHA_SOMENTE_NESTE_COMPUTADOR
```

Substitua o texto da senha, não o use literalmente. O arquivo do projeto também aceita `${DB_PASSWORD:}`: pode escrever a senha depois dos dois-pontos ou substituir o valor inteiro. O usuário pode ser root se essa conta existir no servidor; não confunda conta do Windows com conta do PostgreSQL.

Não precisa criar application-local.properties. Um arquivo local funcional já existente continua sendo carregado opcionalmente; preserve-o e lembre que seus campos substituem os padrões. Variáveis de ambiente continuam opcionais. Não envie senha preenchida ao GitHub.

## 4. Primeiro administrador pelo navegador

Execute `mvn clean test` e `mvn spring-boot:run`. Abra http://localhost:8080 no computador em que a aplicação está rodando. O primeiro acesso permite criar nome, usuário, e-mail e senha de login. Não reutilize a senha do banco.

A senha deve ter ao menos oito caracteres e no máximo 72 bytes. CSRF permanece ativo; a senha é gravada com BCrypt. O backend impede dois administradores iniciais concorrentes no PostgreSQL. Após a primeira conta, o cadastro inicial fecha, inclusive se o administrador for desativado. Não apague usuários para tentar reabri-lo. Não existe cadastro público permanente.

A configuração antiga `upbox.admin-inicial.*` continua somente como alternativa optativa para quem já a utiliza; nunca substitui uma conta existente.

## 5. Demonstração e reinício

Cadastre categoria, produto e funcionário. Registre entrada 10 e saída 7; recuse saída 4. O saldo é 3, com duas movimentações. Confira dashboard, seis relatórios, permissões e impressão. Não edite saldo pelo pgAdmin.

Pare com Ctrl+C e inicie novamente. Os registros devem permanecer. A aplicação usa somente 127.0.0.1 por padrão; não é preciso abrir firewall para demonstrar nesse computador.

## 6. Conferir no pgAdmin

Atualize upbox → Schemas → public → Tables. Consulte usuarios, categorias, produtos, movimentacoes_estoque e registros_operacoes. Selecione campos como nome, código, quantidade e data; não projete senha_hash.

## 7. Testes

`mvn clean test` usa H2 no perfil test. Integração PostgreSQL somente no banco **descartável upbox_test**: veja COMPATIBILIDADE-POSTGRESQL.md. Esse perfil apaga/recria tabelas e recusa outro nome de banco.

## 8. Problemas comuns

| Mensagem/sintoma | Solução |
| --- | --- |
| Conexão recusada | Inicie PostgreSQL e confira host/porta |
| Senha do banco inválida | Confira usuário e senha no application.properties; pgAdmin pode ter outra senha principal |
| Banco upbox não existe | Crie apenas o banco ausente no servidor correto |
| Porta 8080 ocupada | Encerre outra instância ou configure server.port=8081 e use esse endereço |
| Java incorreto | Instale Java 17 e ajuste JAVA_HOME/PATH; reabra terminal |
| Configuração parece ignorada | Confira arquivo local opcional antigo e variáveis de ambiente |
| Falha em índice único | Analise duplicidades antigas em cópia; não apague tabelas |
| Sessão/formulário expirado | Entre novamente e reabra formulário; não desative CSRF |
| Primeiro acesso indisponível | Já existe administrador, ou o navegador não está no próprio computador |

Os detalhes ficam nos logs. Não use create ou create-drop no banco upbox. Ddl-auto=update é somente para desenvolvimento/TCC.

## 9. Backup simples pelo pgAdmin

1. Pare movimentações durante o backup da demonstração.
2. Clique no banco `upbox` → Backup; escolha formato Custom e um arquivo fora do repositório.
3. Confira a mensagem de conclusão do pgAdmin e guarde o arquivo com data.
4. Para ensaiar restauração, crie **outro banco**, como `upbox_restaurado`, e use Restore nesse banco vazio com o arquivo Custom.
5. Confira as tabelas e registros restaurados antes de considerar o backup utilizável. Não restaure por cima do banco em uso apenas para testar.

Esses passos são instruções para uso no pgAdmin da escola. O pgAdmin e o backup/restauração não foram executados no ambiente desta revisão; a integração da aplicação foi testada com PostgreSQL local isolado.
