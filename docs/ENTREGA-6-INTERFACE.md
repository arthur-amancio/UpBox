# Entrega 6 — usabilidade, responsividade e teclado

## Alterações pontuais

A logo, paleta, fontes e componentes existentes foram mantidos. Não houve troca de framework, reescrita de CSS nem mudança nas regras do backend.

- Menu móvel: links ocultos ficam fora do Tab; o menu pode ser fechado por botão, Escape ou área externa. O foco volta ao botão de abertura. Tab/Shift+Tab permanecem dentro do menu aberto. O menu rola em telas baixas.
- Navegação: atalho “Pular para o conteúdo”, foco visível e tabelas identificadas como regiões roláveis acessíveis pelo teclado.
- Cabeçalho: nomes longos não aumentam a largura da página; o texto completo permanece no atributo de título.
- Estoque: “Movimentar” abre o formulário com produto ativo selecionado; “Histórico” abre diretamente a seção de histórico. A edição continua sem campo de saldo. Produtos inativos não recebem o botão Movimentar.
- Categorias/produtos: confirmação identifica o registro e informa a preservação do histórico. Funcionários continuam sem ações administrativas.
- Movimentações: saldo atual e prévia informativa aparecem abaixo dos campos; a confirmação inclui tipo, produto e quantidade. Nenhuma prévia grava ou autoriza operações. O servidor continua decidindo com transação e bloqueio.
- Relatórios: datas só ficam habilitadas para movimentações; o seletor de tipo só fica livre no histórico. Entradas/Saídas fixam o tipo. A consulta sem JavaScript continua possível após selecionar o relatório e consultar.
- Impressão: tabelas e títulos usam cores claras de papel mesmo quando a tela está no tema escuro. Continua sendo impressão nativa, sem dependência de PDF.

Não foi incluída paginação: não houve evidência de volume que justificasse alterar consultas e navegação nesta entrega.

## Evidências de antes e depois

Antes, o primeiro Tab em tela de 320 px focava Home fora da tela (`x=-252`), e Escape não fechava o menu. Depois, o primeiro foco é o atalho visível; o próximo é o botão de menu. Abrir, percorrer, fechar e retornar ao conteúdo funcionaram com teclado.

A prévia foi exercitada em um produto com saldo 3: entrada de 2 mostrou saldo previsto 5. Cancelar a confirmação manteve o saldo 3 no PostgreSQL. Isso prova que a prévia não grava dados.

## Verificação manual no Chromium

| Cenário | Resultado |
| --- | --- |
| 320×568, 390×844 e 1365×900 | Layout utilizável e menu ajustado à largura |
| Tab, Shift+Tab e Escape | Menu oculto não recebe foco; menu aberto contém o foco; fechar devolve foco |
| Tela baixa | Botão Sair alcançável pela rolagem do menu |
| Produto vindo de Estoque | Produto selecionado e saldo disponível exibido |
| Confirmação cancelada | Nenhuma movimentação/saldo alterado |
| Relatórios de estoque/histórico/entradas | Controles habilitados conforme aplicação real |
| Nome de 120 caracteres | Sem aumento da largura da página; título conserva nome completo |
| Impressão a partir do tema escuro | Papel branco, texto legível e navegação oculta |
| JavaScript | Nenhum erro observado |

Os testes foram feitos com dados sintéticos em PostgreSQL local isolado, sem acessar banco ou senha da escola.

## Testes automatizados

`mvn clean test`: 39 testes aprovados. `mvn -Ppostgres-it verify`: 39 testes rápidos e 5 testes PostgreSQL 16.4 aprovados, sem falhas/erros. `git diff --check` aprovado. O backend permaneceu com as proteções de sessão, CSRF, permissões e saldo da entrega anterior.
