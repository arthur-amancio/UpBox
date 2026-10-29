# Relatório final — MVP funcional do UpBox

## 1. Funcionalidades implementadas

Login por usuário ou e-mail com BCrypt, sessões, CSRF, logout, bloqueio de contas inativas e revogação após mudança de perfil/senha. Cadastro administrativo de usuários, ativação/desativação, perfil pessoal, troca de senha e auditoria das principais ações.

Categorias e produtos com listagem, cadastro, edição, validações, pesquisa por nome/código, filtros e desativação lógica. Preço obrigatório em novos cadastros/edições; registros antigos sem preço mostram pendência. Novos produtos pela interface começam em zero; a edição não recebe saldo.

Entradas e saídas reais pelo serviço existente, com usuário da sessão, horário de São Paulo, observação, histórico preservado e filtros. Dashboard e seis relatórios usam o banco. Consulta disponível aos dois perfis; opção/rota de impressão do administrador com CSS para Salvar como PDF pelo navegador.

A logo, cores e componentes anteriores foram reutilizados. Os dados demo, senhas no navegador e scripts de simulação foram retirados. Os originais continuam no histórico Git. `localStorage` é utilizado apenas para preferência de tema.

## 2. Regras de negócio e integridade

- Usuário, e-mail, categoria e código têm verificações de unicidade; índices PostgreSQL também protegem diferenças de caixa/espaços.
- Permissões são verificadas nas rotas, incluindo requisições diretas. Conta inativa não autentica nem mantém sessão utilizável.
- O último administrador ativo não pode ser desativado ou rebaixado. A configuração inicial não recria nem substitui administrador existente, inclusive inativo.
- Senhas exigem pelo menos 8 caracteres, não podem ser em branco e respeitam o limite de 72 bytes do BCrypt.
- Produto exige categoria válida, mínimo não negativo e preço não negativo com até duas casas decimais. Saldo não tem setter nem campo no formulário de edição.
- Movimentação exige inteiro positivo, produto/usuário ativos e saldo suficiente. Não aceita responsável enviado no formulário.
- Saldo, histórico e auditoria participam da mesma transação. Bloqueio pessimista também protege edição/desativação concorrente do produto.
- Categorias inativas não recebem novas associações. Produtos previamente associados permanecem consultáveis e movimentáveis se ativos.
- Desativação preserva associações e histórico. Estoque baixo inclui igualdade com o mínimo.
- Período final inclui o dia inteiro. Dashboard usa hoje e seis dias anteriores para entradas/saídas e dez últimas movimentações.

## 3. Testes executados e resultados

Ambiente de verificação: Java 17.0.17, Maven 3.9.9, PostgreSQL 16.4 e Chromium headless. Ferramentas foram preparadas fora do repositório; nenhuma configuração local ou credencial foi enviada ao Git.

| Entrega | Testes rápidos (`mvn clean test`) | Integração PostgreSQL |
| --- | --- | --- |
| Autenticação | 22 aprovados | Inicialização, administrador BCrypt e auditoria conferidos no banco |
| Cadastros | 26 aprovados | Cadastro/edição, preço e saldo conferidos no banco |
| Movimentações | 30 aprovados | 4 testes automatizados aprovados |
| MVP final | 37 aprovados | 4 testes automatizados aprovados |

Zero falhas/erros nos comandos finais. Os testes rápidos usam H2 **somente nos testes**. `mvn -Ppostgres-it verify` executou a suíte rápida e quatro testes no PostgreSQL real: duas saídas simultâneas, rollback por falha no histórico, rollback após saldo/histórico gravados quando a auditoria falha, e índices de unicidade. As violações de restrição nos logs desses cenários foram provocadas intencionalmente.

O navegador verificou login, usuários, perfil, cadastros, pesquisa, saldo inicial zero, menu móvel, entrada de 10, saída de 7 e recusa de saída de 4, mantendo saldo 3 e duas movimentações. Após reiniciar o servidor, os dados permaneceram. Indicadores foram comparados com SQL; os seis relatórios, filtros, preparação de impressão, CSS de impressão e PDF de verificação foram exercitados. Funcionário consultou rotas permitidas e recebeu HTTP 403 nas rotas administrativas. Nenhum erro JavaScript observado.

`git diff --check` aprovado. A CI dos PRs #4, #5 e #6 também foi consultada e estava aprovada, incluindo o job PostgreSQL do #6. O estado da CI do PR final deve ser conferido no próprio PR após publicação.

## 4. Problemas encontrados e resolvidos

- PR #3 estava integrado na branch do PR #2, não na main: a continuação partiu de `chore/preparacao-inicial-tcc`, preservando a cadeia existente.
- O menu e as telas dependiam da sessão fictícia: a estrutura comum passou para fragmentos Thymeleaf sem carregar dados demo.
- O tratamento inicial de sessão expirada interferia na recusa de CSRF: os tratamentos foram separados e os testes verificam HTTP 403 para CSRF ausente.
- O preço não era consenso nos documentos: foi incluído após confirmação do responsável, sem inventar valores para registros antigos.
- Edição concorrente poderia sobrescrever saldo: cadastro/edição não recebe quantidade, e a edição bloqueia o mesmo produto usado nas movimentações.
- Impressão não comprova salvamento: mensagens/auditoria registram preparação, sem sucesso falso de exportação.
- O ambiente não possuía Java 17/Maven/PostgreSQL prontos: foram preparados isoladamente para executar a suíte e testar o banco real.

## 5. Pendências e limites

Não há funcionalidade das quatro entregas deixada simulada. Os PRs precisam de revisão e integração pelo responsável; nenhum merge foi realizado. Preços de eventuais registros antigos devem ser preenchidos com valores reais. Diagramas/dicionários originais do Drive não foram alterados e dependem de autorização dos autores para atualização formal.

Multiempresa, QR, compras/vendas, nota fiscal e integrações não fazem parte deste MVP. O PDF depende da impressão do navegador. O histórico consulta os nomes/categorias atuais dos registros relacionados; não inventa preços ou associações históricas que o modelo anterior não guardou. A auditoria simples cobre as ações principais, sem pretender registrar cada leitura ou constituir trilha inviolável contra alteração direta no banco.

O `ddl-auto=update` permanece para desenvolvimento local, conforme solicitado. Não foi realizada publicação em servidor externo nem teste com a senha ou a instalação PostgreSQL da escola; a verificação usou um PostgreSQL local isolado com dados sintéticos.

## 6. Versionamento

| Entrega | PR | Base | Commit de implementação |
| --- | --- | --- | --- |
| Autenticação | [#4](https://github.com/arthur-amancio/UpBox/pull/4) | `chore/preparacao-inicial-tcc` | [66c1d31](https://github.com/arthur-amancio/UpBox/commit/66c1d31) |
| Cadastros | [#5](https://github.com/arthur-amancio/UpBox/pull/5) | `feat/autenticacao-usuarios` | [ac1a52b](https://github.com/arthur-amancio/UpBox/commit/ac1a52b) |
| Movimentações | [#6](https://github.com/arthur-amancio/UpBox/pull/6) | `feat/categorias-produtos` | [71b8a83](https://github.com/arthur-amancio/UpBox/commit/71b8a83) |

A última entrega usa `feat/dashboard-relatorios`, baseada em `feat/movimentacoes-estoque`; seu PR e commit são acrescentados ao publicar. Nenhum histórico compartilhado foi reescrito.

## 7. Executar no computador

1. Instale JDK 17, Maven e PostgreSQL; abra a pasta do projeto no VS Code.
2. Antes dos merges, obtenha a branch completa:

   ```powershell
   git fetch origin
   git switch feat/dashboard-relatorios
   ```

3. No pgAdmin, crie `upbox`. Copie `application-local.example.properties` para `application-local.properties` na raiz e configure a conexão local.
4. Configure `upbox.admin-inicial.*`, senha somente no arquivo local e `habilitado=true`. Após criar o administrador, remova a senha inicial e desabilite a opção.
5. Execute `mvn clean test` e `mvn spring-boot:run`. Abra http://localhost:8080.
6. Cadastre categoria/produto, faça uma entrada e uma saída, consulte relatórios e confira os registros no pgAdmin.

Consulte o README e os guias das entregas para o roteiro completo e para testar PostgreSQL no banco exclusivo `upbox_test`. A senha da escola permanece no arquivo local ignorado pelo Git; não é solicitada a cada execução.

## 8. Inventário de arquivos

Comparação com a base que já havia recebido o PR #3. As remoções são dos scripts/templates demonstrativos substituídos pelo fluxo real.

| Situação | Arquivo |
| --- | --- |
| Alterado | `.github/workflows/testes.yml` |
| Alterado | `README.md` |
| Alterado | `application-local.example.properties` |
| Alterado | `docs/ALINHAMENTO-DOCUMENTACAO-DRIVE.md` |
| Criado | `docs/ENTREGA-1-AUTENTICACAO.md` |
| Criado | `docs/ENTREGA-2-CADASTROS.md` |
| Criado | `docs/ENTREGA-3-MOVIMENTACOES.md` |
| Criado | `docs/ENTREGA-4-RELATORIOS.md` |
| Alterado | `docs/GUIA-DE-ESTUDOS.md` |
| Alterado | `docs/GUIA-POSTGRESQL-PGADMIN.md` |
| Alterado | `docs/PLANO-DESENVOLVIMENTO.md` |
| Criado | `docs/RELATORIO-FINAL-MVP.md` |
| Alterado | `pom.xml` |
| Criado | `src/main/java/com/upbox/config/AdministradorInicial.java` |
| Criado | `src/main/java/com/upbox/config/IndicesBanco.java` |
| Criado | `src/main/java/com/upbox/config/SegurancaConfig.java` |
| Criado | `src/main/java/com/upbox/config/SessaoAtualFilter.java` |
| Criado | `src/main/java/com/upbox/config/UsuarioAutenticado.java` |
| Criado | `src/main/java/com/upbox/controller/DadosComunsController.java` |
| Criado | `src/main/java/com/upbox/controller/ErroControllerAdvice.java` |
| Criado | `src/main/java/com/upbox/controller/EstoqueController.java` |
| Criado | `src/main/java/com/upbox/controller/MovimentacaoController.java` |
| Alterado | `src/main/java/com/upbox/controller/PaginaController.java` |
| Criado | `src/main/java/com/upbox/controller/RelatorioController.java` |
| Criado | `src/main/java/com/upbox/controller/UsuarioController.java` |
| Alterado | `src/main/java/com/upbox/model/Categoria.java` |
| Criado | `src/main/java/com/upbox/model/CategoriaForm.java` |
| Criado | `src/main/java/com/upbox/model/FiltroMovimentacao.java` |
| Alterado | `src/main/java/com/upbox/model/MovimentacaoEstoque.java` |
| Criado | `src/main/java/com/upbox/model/MovimentacaoForm.java` |
| Criado | `src/main/java/com/upbox/model/PerfilForm.java` |
| Alterado | `src/main/java/com/upbox/model/Produto.java` |
| Criado | `src/main/java/com/upbox/model/ProdutoForm.java` |
| Criado | `src/main/java/com/upbox/model/RegistroOperacao.java` |
| Criado | `src/main/java/com/upbox/model/ResultadoRelatorio.java` |
| Criado | `src/main/java/com/upbox/model/ResumoDashboard.java` |
| Criado | `src/main/java/com/upbox/model/SenhaForm.java` |
| Criado | `src/main/java/com/upbox/model/TipoRelatorio.java` |
| Alterado | `src/main/java/com/upbox/model/Usuario.java` |
| Criado | `src/main/java/com/upbox/model/UsuarioForm.java` |
| Alterado | `src/main/java/com/upbox/repository/CategoriaRepository.java` |
| Alterado | `src/main/java/com/upbox/repository/MovimentacaoEstoqueRepository.java` |
| Alterado | `src/main/java/com/upbox/repository/ProdutoRepository.java` |
| Criado | `src/main/java/com/upbox/repository/RegistroOperacaoRepository.java` |
| Alterado | `src/main/java/com/upbox/repository/UsuarioRepository.java` |
| Criado | `src/main/java/com/upbox/service/AuditoriaService.java` |
| Criado | `src/main/java/com/upbox/service/CategoriaService.java` |
| Criado | `src/main/java/com/upbox/service/DashboardService.java` |
| Alterado | `src/main/java/com/upbox/service/MovimentacaoEstoqueService.java` |
| Alterado | `src/main/java/com/upbox/service/ProdutoService.java` |
| Criado | `src/main/java/com/upbox/service/RelatorioService.java` |
| Criado | `src/main/java/com/upbox/service/UsuarioService.java` |
| Criado | `src/main/resources/ValidationMessages.properties` |
| Alterado | `src/main/resources/application.properties` |
| Criado | `src/main/resources/messages.properties` |
| Alterado | `src/main/resources/static/css/style.css` |
| Removido | `src/main/resources/static/js/app.js` |
| Removido | `src/main/resources/static/js/dados.js` |
| Criado | `src/main/resources/static/js/interface.js` |
| Removido | `src/main/resources/static/js/pages/auth.js` |
| Removido | `src/main/resources/static/js/pages/configuracoes.js` |
| Removido | `src/main/resources/static/js/pages/dashboard.js` |
| Removido | `src/main/resources/static/js/pages/estoque.js` |
| Removido | `src/main/resources/static/js/pages/home.js` |
| Removido | `src/main/resources/static/js/pages/movimentacoes.js` |
| Removido | `src/main/resources/static/js/pages/relatorios.js` |
| Criado | `src/main/resources/templates/acesso-negado.html` |
| Removido | `src/main/resources/templates/cadastro.html` |
| Criado | `src/main/resources/templates/categoria-form.html` |
| Criado | `src/main/resources/templates/categorias.html` |
| Alterado | `src/main/resources/templates/configuracoes.html` |
| Alterado | `src/main/resources/templates/dashboard.html` |
| Criado | `src/main/resources/templates/erro.html` |
| Alterado | `src/main/resources/templates/estoque.html` |
| Alterado | `src/main/resources/templates/fragments/layout.html` |
| Criado | `src/main/resources/templates/fragments/tabelas.html` |
| Alterado | `src/main/resources/templates/home.html` |
| Removido | `src/main/resources/templates/index.html` |
| Alterado | `src/main/resources/templates/login.html` |
| Alterado | `src/main/resources/templates/movimentacoes.html` |
| Criado | `src/main/resources/templates/produto-form.html` |
| Criado | `src/main/resources/templates/registros.html` |
| Alterado | `src/main/resources/templates/relatorios.html` |
| Criado | `src/main/resources/templates/usuario-form.html` |
| Criado | `src/main/resources/templates/usuarios.html` |
| Criado | `src/test/java/com/upbox/controller/CadastroMvcTest.java` |
| Criado | `src/test/java/com/upbox/controller/CenarioMvc.java` |
| Criado | `src/test/java/com/upbox/controller/MovimentacaoMvcTest.java` |
| Alterado | `src/test/java/com/upbox/controller/PaginaControllerTest.java` |
| Criado | `src/test/java/com/upbox/controller/RelatorioMvcTest.java` |
| Criado | `src/test/java/com/upbox/integration/PostgresIT.java` |
| Alterado | `src/test/java/com/upbox/service/EstoquePersistenciaTest.java` |
| Criado | `src/test/resources/application-test.properties` |
