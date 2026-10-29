# Relatório — revisão, interface e preparação do TCC

## 1. Base e análise

A continuação partiu de `feat/dashboard-relatorios` no commit `8723896`. Git, branches, commits e PRs #1–#7 foram consultados; o PR #3 já estava integrado na branch do #2 e os demais permaneceram sem merges. O README e todos os documentos de `docs/`, incluindo o relatório final do MVP, foram lidos. A suíte inicial aprovou 37 testes.

Foram revisadas as cinco entidades, mapeamentos JPA, chaves estrangeiras, limites, unicidade, tipos monetários, encapsulamento, serviços, consultas, controllers, segurança e componentes de interface. Os metadados PostgreSQL confirmaram as associações e o preço numeric(12,2). O nullable de preço foi preservado para o legado documentado. Não foram recriadas entidades/serviços nem alterada a arquitetura.

Os requisitos, casos de uso, dicionário, análise OO e imagens oficiais dos diagramas foram consultados pelo Drive em leitura. A comparação está em [Matriz de requisitos](MATRIZ-REQUISITOS-TCC.md); nenhum original foi alterado.

## 2. Problemas reais e correções

| Problema comprovado | Correção e evidência |
| --- | --- |
| Troca de senha concorrente podia desfazer uma desativação de conta | Novo teste PostgreSQL falhou antes da correção, com ativo=true. A troca agora lê o usuário com bloqueio pessimista e recusa conta inativa. O teste passou depois. |
| Falha de conexão não recebia tratamento amigável MVC | Teste provocou DataAccessResourceFailureException sem tratamento. Handler específico devolve HTTP 503 e mensagem em português, sem SQL na página. |
| Rotas inexistentes mostravam Whitelabel | Chromium confirmou o 404 técnico. Novo template de erro oferece mensagem e retorno à Home. |
| Cabeçalho de erro podia consultar novamente o banco | Advice comum passou a ignorar `/error` e despachos ERROR, para a página de erro não depender de conexão funcional. |
| Tab alcançava links de menu fora da tela | Chromium em 320 px registrou foco em Home com x=-252. O menu fechado agora usa inert; há atalho ao conteúdo, foco visível e controle de foco no menu aberto. |
| Escape não fechava o menu móvel | Adicionados Escape, botão Fechar e retorno do foco ao botão de abertura, verificados no navegador. |

Detalhes: [Entrega 5](ENTREGA-5-REVISAO-TECNICA.md) e [Entrega 6](ENTREGA-6-INTERFACE.md). As proteções de login, CSRF, papéis, saldo e histórico foram mantidas. Não há novo endpoint de diagnóstico em produção.

## 3. Melhorias de experiência

Navegação direta de produto para registrar movimentação ou consultar histórico; saldo atual e prévia informativa; confirmação com produto/tipo/quantidade; confirmação de status com identificação do registro; tabelas roláveis acessíveis pelo teclado; nome longo sem quebrar a página; campos de relatório habilitados conforme aplicação; impressão clara mesmo no tema escuro.

A prévia não grava dados nem substitui o backend. Não se incluiu paginação sem necessidade comprovada. Logo, paleta, fontes e componentes foram preservados; não houve reescrita do CSS ou nova dependência de PDF.

## 4. Testes e evidências

Ambiente: Java 17.0.17, Maven 3.9.9, PostgreSQL 16.4 e Chromium. As ferramentas locais e credenciais temporárias ficaram fora do Git.

| Etapa | `mvn clean test` | `mvn -Ppostgres-it verify` |
| --- | --- | --- |
| Base do MVP | 37 aprovados | Integração anterior preservada |
| Entrega 5 | 39 aprovados | 39 rápidos + 5 PostgreSQL aprovados |
| Entrega 6 | 39 aprovados | 39 rápidos + 5 PostgreSQL aprovados |
| Entrega 7 final | 43 aprovados | 43 rápidos + 6 PostgreSQL aprovados |

Zero falhas/erros nos comandos finais. Os testes rápidos de persistência usam H2 apenas em teste. A integração real usa somente o banco exclusivo `upbox_test`; o nome é validado antes de iniciar o JPA. Nenhum teste destrutivo foi apontado ao banco de uso `upbox`.

A cobertura adicional inclui conta desativada em disputa com senha, duas desativações administrativas simultâneas, histórico/relatórios após desativar usuário e produto, categoria inativa, limites dos sete dias do dashboard e preço ausente no legado sem mudar saldo. Permanecem os testes de login, permissões, CSRF, entradas/saídas, quantidades inválidas, duplicidades, produto inativo, concorrência e rollback.

A jornada no navegador registrou **21 cenários aprovados**, desde banco vazio até reinício, incluindo erro de login, cadastros, entrada 10, saída 7, recusa de saída 4, dashboard, seis relatórios, impressão, funcionário, produto/categoria inativos e revogação de sessão. Após reiniciar: saldo 3, duas movimentações e dois usuários preservados; o administrador inicial não foi duplicado. Os resultados foram conferidos pela interface e por SQL.

As verificações de interface abrangeram 320×568, 390×844 e 1365×900, Tab/Shift+Tab/Escape, foco, rolagem, cancelamento de confirmação, nome longo e impressão no tema escuro. Nenhum erro JavaScript foi observado.

- [Jornada detalhada](ENTREGA-7-PREPARACAO-TCC.md)
- [Registro estruturado com horários UTC](evidencias/tcc/jornada.json)
- [Dashboard](evidencias/tcc/dashboard.png)
- [Movimentações](evidencias/tcc/movimentacoes.png)
- [Impressão](evidencias/tcc/relatorio-impressao.png)
- [Celular](evidencias/tcc/celular.png)

`git diff --check` aprovado. A CI dos PRs #8 e #9 foi consultada e estava aprovada nos jobs de testes e PostgreSQL. O estado da entrega final pode ser conferido no próprio PR após publicação.

## 5. Versionamento

| Entrega | Branch de origem | Branch de destino | PR | Commit |
| --- | --- | --- | --- | --- |
| 5 — revisão técnica | `fix/revisao-tecnica-mvp` | `feat/dashboard-relatorios` | [#8](https://github.com/arthur-amancio/UpBox/pull/8) | [6e1e586](https://github.com/arthur-amancio/UpBox/commit/6e1e586) |
| 6 — interface | `feat/usabilidade-acessibilidade` | `fix/revisao-tecnica-mvp` | [#9](https://github.com/arthur-amancio/UpBox/pull/9) | [8f7f30c](https://github.com/arthur-amancio/UpBox/commit/8f7f30c) |

A entrega 7 usa `docs/preparacao-tcc`, baseada em `feat/usabilidade-acessibilidade`; seus links são registrados após publicar. Não houve merge, alteração direta da main ou reescrita de histórico compartilhado.

## 6. Executar a versão mais recente

```powershell
git fetch origin
git switch docs/preparacao-tcc
```

Instale Java 17, Maven, PostgreSQL e pgAdmin. Abra a raiz do projeto no VS Code e crie `upbox` no pgAdmin. Copie o exemplo local somente se `application-local.properties` ainda não existir; preserve conexão/senha já funcionais. Configure a conexão e, no primeiro uso, `upbox.admin-inicial.*` com `habilitado=true` e senha escolhida apenas no arquivo local.

```powershell
mvn clean test
mvn spring-boot:run
```

Abra http://localhost:8080. Após criar o administrador, remova a senha inicial e defina `habilitado=false`; a senha do PostgreSQL pode continuar salva no arquivo local ignorado pelo Git. O [guia da escola](INSTALACAO-ESCOLA.md) contém passos de pgAdmin, solução de conexão/porta e instruções de backup/restauração.

## 7. Pendências para a apresentação

- Revisão e integração dos PRs, respeitando a cadeia existente.
- Instalação presencial, pgAdmin e diálogo nativo de impressão no computador da escola; não foram acessados banco/senha da escola.
- Ensaio dos integrantes com o [roteiro de oito minutos](APRESENTACAO-TCC.md).
- Atualização formal dos diagramas/dicionários pelos autores: nomes de perfis, código/nome de usuário, RegistroOperacao, hash BCrypt e desativação lógica. As divergências constam da matriz e não foram corrigidas no Drive sem autorização.
- Preenchimento de preços reais caso existam produtos antigos sem preço.

O backup/restauração pelo pgAdmin foi documentado, mas não executado neste ambiente. Não foram adicionados QR, vendas, financeiro, notas fiscais, multiempresa ou integrações. Não há funcionalidade nova deixada simulada.

## 8. Arquivos criados/alterados nesta revisão

Comparação com `feat/dashboard-relatorios` no commit `8723896`.

| Situação | Arquivo |
| --- | --- |
| Alterado | `README.md` |
| Alterado | `docs/ALINHAMENTO-DOCUMENTACAO-DRIVE.md` |
| Criado | `docs/APRESENTACAO-TCC.md` |
| Criado | `docs/ENTREGA-5-REVISAO-TECNICA.md` |
| Criado | `docs/ENTREGA-6-INTERFACE.md` |
| Criado | `docs/ENTREGA-7-PREPARACAO-TCC.md` |
| Alterado | `docs/GUIA-DE-ESTUDOS.md` |
| Alterado | `docs/GUIA-POSTGRESQL-PGADMIN.md` |
| Criado | `docs/INSTALACAO-ESCOLA.md` |
| Criado | `docs/MATRIZ-REQUISITOS-TCC.md` |
| Alterado | `docs/PLANO-DESENVOLVIMENTO.md` |
| Alterado | `docs/RELATORIO-FINAL-MVP.md` |
| Criado | `docs/RELATORIO-REVISAO-TCC.md` |
| Criado | `docs/evidencias/tcc/celular.png` |
| Criado | `docs/evidencias/tcc/dashboard.png` |
| Criado | `docs/evidencias/tcc/jornada.json` |
| Criado | `docs/evidencias/tcc/movimentacoes.png` |
| Criado | `docs/evidencias/tcc/relatorio-impressao.png` |
| Alterado | `src/main/java/com/upbox/controller/DadosComunsController.java` |
| Alterado | `src/main/java/com/upbox/controller/ErroControllerAdvice.java` |
| Alterado | `src/main/java/com/upbox/repository/UsuarioRepository.java` |
| Alterado | `src/main/java/com/upbox/service/UsuarioService.java` |
| Alterado | `src/main/resources/static/css/style.css` |
| Alterado | `src/main/resources/static/js/interface.js` |
| Alterado | `src/main/resources/templates/categorias.html` |
| Criado | `src/main/resources/templates/error.html` |
| Alterado | `src/main/resources/templates/estoque.html` |
| Alterado | `src/main/resources/templates/fragments/layout.html` |
| Alterado | `src/main/resources/templates/fragments/tabelas.html` |
| Alterado | `src/main/resources/templates/movimentacoes.html` |
| Alterado | `src/main/resources/templates/registros.html` |
| Alterado | `src/main/resources/templates/relatorios.html` |
| Alterado | `src/main/resources/templates/usuarios.html` |
| Alterado | `src/test/java/com/upbox/controller/CadastroMvcTest.java` |
| Criado | `src/test/java/com/upbox/controller/ErrosMvcTest.java` |
| Alterado | `src/test/java/com/upbox/controller/MovimentacaoMvcTest.java` |
| Alterado | `src/test/java/com/upbox/controller/RelatorioMvcTest.java` |
| Alterado | `src/test/java/com/upbox/integration/PostgresIT.java` |
