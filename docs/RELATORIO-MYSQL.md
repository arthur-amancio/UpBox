# Entrega MySQL — evidências e limites

## Auditoria e preservação

A main original era `351715fe17a357cd7272e41766b800e0ab6b8509`, com CI aprovada. Foram examinados PRs #1–#16, branches locais/remotas e seus commits. Todas as branches, inclusive a preliminar refactor/upbox-simples-mysql, estavam contidas nessa main, sem commits exclusivos. Foi publicada a tag **backup/pre-mysql-2026-11-10**, antes de qualquer edição. Um bundle adicional foi guardado fora do repositório.

Foi reutilizada apenas a branch preliminar para esta entrega, com um único PR para main. Integração e exclusão de branches exigem checks aprovados e nova conferência de ancestralidade/dependências. A execução e o resultado final ficam nos checks/registro do PR e no relatório da entrega; não há rebase ou force push.

## Alterações

- MySQL Connector/J no lugar de PostgreSQL e dialect comunitário. SQL exclusivo e locks pg_advisory foram retirados.
- Cinco entidades preservadas. InnoDB, chaves estrangeiras e unicidade com collation _ci; sem setter de saldo. Transações/bloqueios pessimistas preservados.
- Apenas entity, repository, service, controller e config no Java principal. Formulários ficam em controller; resumos/tipos de relatório em service; dois enums de domínio em entity.
- Configuração normal apenas em application.properties, root/localhost:3306/upbox e senha vazia para preencher. Sem import obrigatório ou automático de arquivo local.
- Conta administrador automática com senha inicial admin por pedido posterior expresso do responsável. BCrypt/CSRF e troca obrigatória no primeiro login, sem redefinição no reinício. Restrição única protege criação concorrente; lock da linha protege troca concorrente.
- Páginas próprias preservadas; removido CSS de modais sem uso. Fontes offline, identidade âmbar/preto/branco, teclado, tabelas responsivas e impressão continuam.
- Mensagens de validação em anotações/HTML, sem messages.properties ou ValidationMessages.properties.
- ZIP por script PowerShell simples, sem src/assembly e sem plugins Ant/Assembly. .bat continua verificando Java/JAR/configuração e orientando falhas.
- Guias obsoletos removidos da árvore atual, preservados no histórico/tag. Materiais acadêmicos foram mantidos; originais do Drive não foram alterados.

## Problemas comprovados durante a migração

1. MySQL recusou o ano 10000 usado como limite de consultas. O filtro agora respeita DATETIME 1000–9999 e usa fim inclusivo com microssegundos; a suíte verifica relatórios com/sem filtros e datas limite.
2. As falhas injetadas por CHECK dependiam de versões do servidor. Os testes de rollback agora usam triggers apenas no banco descartável, disponíveis no 5.7 e 8. Os testes continuam exigindo saldo/histórico/auditoria revertidos.
3. O driver classifica algumas violações de forma diferente. Os testes conferem DataAccessException e, principalmente, os efeitos transacionais, sem depender da classificação específica PostgreSQL.
4. A instrução posterior do responsável definiu administrador/admin. A versão final remove a ativação pública e exige troca pelo formulário autenticado de Configurações; testes conferem que nenhuma URL contorna essa exigência.

## Testes e execução reproduzível

- `mvn clean test`: suíte original preservada/adaptada, mais regressão da troca inicial após reinício.
- `mvn -Pmysql-it verify`: MySQL **5.7.44** e **8.0.44**, reais, em bancos exclusivos upbox_test. Nove testes de integração cobrem MVC, login/BCrypt, CSRF, permissões, cadastros, quantidades, concorrência, rollback, troca inicial, histórico, dashboard, seis relatórios e persistência no reinício.
- Windows: a CI gera o ZIP, extrai em pasta com espaços, inicia MySQL descartável e o JAR pelo .bat, confirma conta automática/troca inicial local com CSRF e persistência sem duplicação. Confere mensagens de Java ausente e conexão recusada. O JSON do job informa versão exata/commit.
- Navegador: Chromium real com MySQL 8.0.44, troca inicial, login, categorias/produtos, entrada 10/saída 7/recusa de 4, histórico, dashboard, usuários/relatórios, capturas em 1366×768, 1024×768 e 390×844 e PDF A4. Evidências selecionadas em evidencias/mysql.

Os resultados finais devem ser conferidos no último commit da CI, inclusive depois do merge na main. H2 serve somente para testes rápidos e nunca é apresentado como prova de compatibilidade MySQL. Nenhum teste destrutivo usa upbox.

## Limites explícitos

A versão exata da escola não foi informada. Não é possível garantir qualquer versão antiga/futura ou MariaDB sem testar. MySQL 5.7 está fora de suporte e Hibernate 6.5 recomenda MySQL 8; a homologação 5.7 demonstra somente os fluxos reais cobertos nesta aplicação, sem afirmar suporte oficial do framework.

Não há acesso a dados reais PostgreSQL da escola. Não ocorreu conversão automática: antes de descontinuar um PostgreSQL com dados importantes, é obrigatório preparar/testar uma migração específica em cópia conforme MIGRACAO-MYSQL.md. A versão anterior permanece na tag de backup.

A impressora, projetor e equipamentos físicos da ETEC precisam de conferência presencial. A credencial escolar antiga permanece no histórico; precisa ser trocada/revisada antes de tornar o repositório público.
