# UpBox — estoque para o TCC

Sistema integrado em **Java (compilado para 17, compatível com JDK 17 e 21), Spring Boot, Spring MVC, JPA, Thymeleaf, Lombok e MySQL**. Login por usuário/e-mail, permissões, categorias, produtos, estoque, histórico, dashboard e seis relatórios. As fontes são locais; não precisa de internet para usar a interface.

## Executar pelo código

Instale Java **17 ou 21**, Maven e MySQL. Use MySQL Workbench ou o cliente mysql para criar o banco **somente se ausente**:

```sql
CREATE DATABASE IF NOT EXISTS upbox
CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
SELECT VERSION();
```

Clone a `main` (nao precisa abrir VS Code para executar):

```powershell
git clone https://github.com/arthur-amancio/UpBox.git
cd UpBox
```

Edite **src/main/resources/application.properties**. Preencha a senha do seu MySQL; o padrão é localhost:3306/upbox, usuário root. Se o servidor usar outro usuário/porta, ajuste nesse mesmo arquivo. Não envie senha preenchida ao GitHub.

```properties
spring.datasource.url=jdbc:mysql://localhost:3306/upbox
spring.datasource.username=root
spring.datasource.password=
```

**Para iniciar apos o clone:** de dois cliques em `iniciar-upbox.bat`, na raiz do projeto. O script aceita **Java 17 ou 21**, encontra `pom.xml` e executa `mvn spring-boot:run` automaticamente usando o Maven ja instalado. Na primeira execucao, o Maven pode precisar baixar dependencias da internet. Deixe a janela aberta e acompanhe as mensagens de inicializacao.

Se preferir usar o terminal, o comando equivalente e `mvn spring-boot:run`. Para executar os testes rapidos separadamente, use `mvn clean test`.

Abra **http://localhost:8080** no mesmo computador. Não precisa de arquivo local, .env, Docker, Node ou variáveis de ambiente. Configuração externa continua possível pelas opções padrão do Spring, mas não é necessária.

## Acesso inicial: administrador / admin

Em um banco novo, o UpBox cria automaticamente:

- Nome: **Administrador**
- Login: **administrador**
- Senha inicial: **admin**
- Perfil: ADMINISTRADOR, ativo.

Entre com esses dados em http://localhost:8080. No primeiro login, a tela de configurações exige trocar a senha antes de liberar estoque, usuários e demais módulos. Informe **admin** em Senha atual e uma nova senha de pelo menos 8 caracteres. Depois, entre novamente com a senha escolhida.

A senha inicial foi definida expressamente pelo responsável para o ambiente escolar. Ela é gravada com BCrypt. Não é senha de banco nem indicação para ambiente público. CSRF e permissões continuam ativos. A aplicação escuta somente 127.0.0.1 por padrão.

Reiniciar nunca redefine senhas nem duplica conta. Em banco já utilizado, a senha existente é preservada: o padrão vale para a primeira criação. A restrição única protege duas criações simultâneas; a troca usa bloqueio da linha no InnoDB. Não existe cadastro público nem recuperação por reinício.

## Windows com o pacote pronto

Baixe o artefato **UpBox-escola** na CI da main, extraia tudo e edite **application.properties ao lado de upbox.jar**. Execute **iniciar-upbox.bat** e abra localhost:8080. O pacote exige somente Java 17 ou 21 e MySQL; não precisa de Maven.

No pacote ZIP, o mesmo script detecta o JAR pronto e **nao exige Maven**. No codigo clonado, detecta o `pom.xml` e inicia pelo Maven. O script confere Java 17 ou 21 e os arquivos necessarios, orientando erros. Não apaga nem recria banco. Logs técnicos ficam em logs/upbox.log; a senha não é registrada em logs. Use Ctrl+C para parar; os dados permanecem no MySQL. Se mudar server.port, use a nova porta no navegador.

Para gerar o pacote: `mvn clean package`, depois `powershell -File scripts/gerar-pacote.ps1`. O ZIP sai em **target/UpBox-escola.zip**. O script recusa JAR com senha de banco preenchida para impedir distribuição acidental. Não existe src/assembly.

## Estrutura simples

```text
src/main/java/com/upbox/
  UpboxApplication.java
  entity/       # as cinco entidades e seus dois enums
  repository/   # consultas ao banco
  service/      # regras de negócio, resumos e tipos de relatório
  controller/   # rotas e formulários simples
  config/       # segurança, criação inicial e conferência do banco
```

**Entity → Repository → Service → Controller → Thymeleaf**: a entidade representa a tabela; o repository consulta/salva; o service valida e coordena transações; o controller recebe formulários e escolhe a página; Thymeleaf apresenta HTML. A requisição percorre o caminho inverso até o banco.

As entidades são **Usuario, Categoria, Produto, MovimentacaoEstoque e RegistroOperacao**. Produtos pertencem a categorias; movimentações referenciam produto/usuário. Não há exclusão física pela interface. Saldo não tem setter: entradas e saídas passam pelo serviço, com transação e bloqueio. Se falhar histórico/auditoria, tudo é desfeito.

Os formulários são páginas próprias, sem modais. Validações Java têm mensagens em português nas anotações e no HTML; não há arquivos messages.properties ou ValidationMessages.properties.

## Testes

```powershell
mvn clean test
```

Os testes rápidos usam H2. Para integração real, crie **outro banco descartável chamado upbox_test**, nunca use upbox:

```sql
CREATE DATABASE IF NOT EXISTS upbox_test
CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
```

```powershell
$env:UPBOX_TEST_DB_URL='jdbc:mysql://localhost:3306/upbox_test'
$env:UPBOX_TEST_DB_USER='root'
$env:UPBOX_TEST_DB_PASSWORD='SENHA_APENAS_DO_TESTE'
mvn -Pmysql-it verify
Remove-Item Env:UPBOX_TEST_DB_PASSWORD
```

As suítes recusam outro nome de banco antes de inicializar JPA. Testam autenticação/permissões, cadastros, estoque, rollback, concorrência, troca inicial e reinício. A CI também executa o ZIP/.bat em Windows com MySQL descartável.

A homologação inicial usa **MySQL 5.7.44 e 8.0.44**. Isso não promete funcionamento em toda versão antiga/futura nem em MariaDB. A versão exata da ETEC ainda precisa ser conferida com SELECT VERSION(); consulte o relatório da CI. MySQL 5.7 está fora de suporte; a compatibilidade da aplicação precisa ser verificada, mesmo quando o framework recomenda MySQL 8.

## Problemas comuns

| Sintoma | Conferir |
| --- | --- |
| Conexão recusada | Serviço MySQL ativo, host/porta no application.properties |
| Access denied | Usuário/senha do MySQL; não é a senha de login do UpBox |
| Unknown database | Criar upbox somente se ausente no servidor correto |
| Porta 8080 ocupada | Encerrar outra instância ou mudar server.port |
| Java incorreto | Confira `java -version` e `mvn -version`: Java 17 ou 21 (Maven deve usar a versão instalada) |
| Conta automática não entra | Em banco novo use administrador/admin; em banco existente a senha anterior é mantida |
| Primeiro acesso não libera módulos | Troque a senha inicial em Configurações; reiniciar não redefine senha |
| Falha de duplicidade | Conferir códigos, nomes e e-mails; não apagar tabelas |
| Tabelas/collation inválidas | Usar InnoDB e collation com _ci; não converter dados sem backup |

`ddl-auto=update` é apenas para desenvolvimento/TCC. Não use create/create-drop no banco de uso. Antes de atualizar uma instalação, faça backup e teste restauração em outro banco. PostgreSQL não é convertido automaticamente para MySQL: leia [migração](docs/MIGRACAO-MYSQL.md).

## Material acadêmico e histórico

[Guia de estudos](docs/GUIA-DE-ESTUDOS.md), [apresentação](docs/APRESENTACAO-TCC.md) e [matriz oficial](docs/MATRIZ-REQUISITOS-TCC.md). Os guias antigos estão preservados no histórico/tag **backup/pre-mysql-2026-11-10**. A antiga credencial escolar também permanece no histórico: trocá-la e revisar o histórico antes de tornar público; esta entrega não reescreve commits.
