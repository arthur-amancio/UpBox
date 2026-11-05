# Pacote final e homologação

## Gerar e usar

Na raiz do projeto, com Java 17 e Maven, execute `mvn clean package`. O Maven executa os testes, cria o JAR Spring Boot e gera `target/upbox-sistema-1.0.0-escola.zip`. JARs/ZIPs não são versionados no Git.

O ZIP contém uma pasta UpBox-escola com upbox.jar, application.properties, iniciar-upbox.bat, LEIA-ME.txt e guias. O .bat e o LEIA-ME têm terminações de linha Windows. O JAR não contém H2, contas ou senha de banco. As fontes e suas licenças estão dentro do JAR.

Extraia tudo antes de executar. Preencha somente a configuração externa da instalação; não precisa de application-local.properties. Instale Java 17/PostgreSQL e crie upbox se ausente. O .bat verifica Java 17, arquivo e JAR, inicia o servidor e orienta erros de conexão/porta; não cria/apaga bancos. `--verificar` não testa o servidor PostgreSQL. Se alterar server.port, use a porta escolhida no navegador.

A configuração fonte com senha impede **empacotar**: isso protege contra distribuir uma credencial preenchida pelo integrante. Foi testado em cópia isolada com valor fictício: prepare-package falhou com mensagem clara. Os comandos clean test e spring-boot:run continuam permitidos para quem preenche a configuração fonte no próprio computador.

## Evidências verificáveis

- Java 17.0.17, Spring Boot 3.3.4, Hibernate 6.5.3.Final, community-dialects 6.5.3.Final e JDBC 42.7.4.
- PostgreSQL real **9.6.24** e **16.4**, bancos descartáveis: 45 testes rápidos + nove integrações por servidor. A CI repete as duas versões.
- Cadastro inicial local, CSRF, BCrypt, recusa após configuração/inatividade, duas instalações concorrentes, usuários/perfis, categorias/produtos, entradas/saídas, rollback, concorrência, seis relatórios e reinício preservado.
- [Navegador e capturas antes/depois](INTERFACE-ESCOLA.md), com teclado, fontes offline e PDF A4 real nas três resoluções pedidas.
- ZIP conferido: configuração externa e interna sem senha, script CRLF, JAR sem H2 e sem arquivo local obrigatório. O pacote extraído também é iniciado na homologação com configuração externa e dados preservados após reinício.

O job **distribuicao-windows** da CI executa, em Windows com Java 17, `scripts/testar-pacote-windows.ps1`. Esse roteiro cria um cluster PostgreSQL novo e o banco upbox_test, extrai o ZIP em caminho com espaços, inicia o .bat, cadastra administrador com CSRF, encerra e reinicia, confere conta única e bloqueio do primeiro acesso. Também testa mensagens de conexão recusada e Java ausente. Nenhuma instalação de banco de uso é alterada.

O resultado exato, versão do PostgreSQL Windows e commit ficam em **homologacao-windows.json**, no artefato homologacao-windows da execução correspondente. O ZIP é publicado no artefato **UpBox-escola** somente após o job passar. Consulte os checks do último commit: não use um verde de commit anterior para declarar esta homologação aprovada.

## Limites e verificações presenciais

- Confirme a versão real do laboratório com `SELECT version();`. Outra versão 9.x exige teste adicional; não afirmamos suporte a toda a série 9.
- PostgreSQL 9 está fora de suporte. A compatibilidade foi demonstrada para a aplicação, sem transformar o Hibernate padrão em suporte oficial ao banco antigo.
- O Windows da CI não é o computador da ETEC: instalação/PATH, serviço PostgreSQL, senha/usuário, porta, pgAdmin, projetor e impressora precisam de conferência presencial.
- Faça backup e restaure uma cópia antes de atualizar dados existentes. Não faça testes destrutivos em upbox.
- O navegador testado foi Chromium 154. Navegadores antigos do laboratório precisam de conferência de teclado/layout/rolagem/impressão.
- A antiga credencial saiu dos arquivos atuais, mas ainda existe no histórico. O responsável deve trocar/revisar a credencial antes de publicar o repositório; não reescrevemos o histórico.
- Os ajustes formais do TCC listados em MATRIZ-REQUISITOS-TCC.md continuam sob responsabilidade da equipe; os originais não foram editados.

## Entregas para revisão

1. Compatibilidade PostgreSQL: fix/postgresql-96-escola, base main.
2. Instalação e primeiro acesso: feat/instalacao-primeiro-acesso, base da etapa 1.
3. Organização Java: refactor/entidades-formularios-didaticos, base da etapa 2.
4. Interface: feat/interface-final-escola, base da etapa 3.
5. Pacote e homologação: build/pacote-final-escola, base da etapa 4.

Não foram feitos merges automáticos, rebase ou force push. Até a equipe integrar a cadeia, a versão final está na branch da etapa 5; a main continua preservada.
