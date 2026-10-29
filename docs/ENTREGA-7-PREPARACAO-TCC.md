# Entrega 7 — preparação para o TCC e jornada real

## Ambiente e isolamento

Aplicação executada com Java 17.0.17, Maven 3.9.9, PostgreSQL 16.4 e Chromium. A jornada usou um banco novo e isolado de demonstração, com `ddl-auto=update`, sem apagar ou recriar o banco de uso `upbox`. Os testes automatizados destrutivos utilizaram apenas `upbox_test`, protegido pela validação de nome já existente.

Os dados são sintéticos, cadastrados pelos formulários reais e persistidos no PostgreSQL. Não são simulações em JavaScript. As credenciais temporárias ficaram fora do repositório; as capturas não mostram senhas.

## Resultado da jornada

A jornada foi iniciada com banco vazio. A entrada de 10, saída de 7 e tentativa de saída de 4 resultaram em saldo 3, duas movimentações e mensagem de estoque insuficiente. O servidor foi parado e reiniciado; saldo, histórico, usuários e indicadores permaneceram. O administrador inicial não foi duplicado.

| Cenário real | Resultado | Evidência observada |
| --- | --- | --- |
| Acesso sem autenticação | Aprovado | Estoque redirecionou para login, sem apresentar dados internos. |
| Login incorreto | Aprovado | Credenciais incorretas recusadas com mensagem em português. |
| Login administrador | Aprovado | Administrador inicial entrou com a conta local, sem cadastro público. |
| Estado vazio | Aprovado | Dashboard zerado e ausência de produtos ativos apresentada sem dados demonstrativos. |
| Cadastrar categoria | Aprovado | Papelaria cadastrada pelo formulário e listada no banco. |
| Cadastrar produto | Aprovado | Código TCC-001, preço 12,50, mínimo 3 e saldo inicial zero persistidos. |
| Código duplicado | Aprovado | Cadastro duplicado recusado; permaneceu somente um produto. |
| Entrada e estoque normal | Aprovado | Entrada 10 persistida; dashboard com 10 unidades e nenhum alerta de mínimo. |
| Saída normal | Aprovado | Saída 7 persistida; saldo final 3. |
| Saída acima do saldo | Aprovado | Saída 4 recusada; saldo 3 e exatamente duas movimentações preservados. |
| Histórico | Aprovado | Entrada/saída, data/hora, responsável e observação conferidos no HTML e no PostgreSQL. |
| Dashboard com estoque baixo | Aprovado | Total 1 produto, 3 unidades, 1 alerta, entradas 10 e saídas 7. |
| Seis relatórios | Aprovado | Produtos, estoque, baixo, entradas, saídas e histórico consultados com produto/categoria filtrados. |
| Impressão | Aprovado | Botão solicitou window.print; CSS de impressão aplicado e PDF de verificação gerado pelo Chromium. |
| Categoria inativa | Aprovado | Categoria saiu das novas associações sem apagar produto; reativada ao final. |
| Produto inativo | Aprovado | Sem botão Movimentar; saldo/alerta excluídos do dashboard; histórico mantido. Produto reativado para apresentação. |
| Cadastrar funcionário | Aprovado | Conta FUNCIONARIO criada pelo administrador, com senha protegida no banco. |
| Permissões do funcionário | Aprovado | Consultas e formulário de movimentação permitidos; cinco rotas administrativas responderam HTTP 403. |
| Usuário inativo | Aprovado | Sessão existente revogada e novo login recusado; conta reativada pelo administrador. |
| Responsividade e JavaScript | Aprovado | Captura móvel de 390×844 e páginas desktop sem erros JavaScript observados. |
| Persistência após reinício | Aprovado | Saldo 3, duas movimentações e dois usuários preservados; administrador inicial não foi duplicado. Interface e SQL conferidos. |

O histórico também foi consultado pelo dia da jornada após reiniciar a aplicação: as duas movimentações continuaram visíveis. Os seis relatórios foram exercitados com filtros por produto/categoria. A preservação do histórico após desativar produto e usuário e os limites de período também têm testes automatizados.

## Capturas reais

- [Dashboard](evidencias/tcc/dashboard.png): 1 produto, 3 unidades, 1 alerta, entrada 10 e saída 7.
- [Movimentações](evidencias/tcc/movimentacoes.png): formulário, saldo e duas linhas de histórico com responsável.
- [Impressão](evidencias/tcc/relatorio-impressao.png): cabeçalho, filtros e histórico sem navegação lateral.
- [Celular](evidencias/tcc/celular.png): dashboard no perfil de funcionário em 390×844.
- [Registro estruturado com horários UTC](evidencias/tcc/jornada.json).

O botão de impressão foi acionado e a chamada de `window.print` foi verificada. O Chromium gerou um PDF de verificação com o CSS de impressão. Isso não representa teste do diálogo nativo nem da impressora do computador da escola.

## Cobertura adicional

Foram ampliados os testes para histórico após desativação de produto/usuário, categoria inativa com associação já existente, limites dos sete dias do dashboard, correção de preço ausente no legado sem mudar saldo e duas desativações simultâneas que devem preservar um administrador. Não foram criadas novas funcionalidades apenas para aumentar o código.

## Materiais para a equipe

- [Instalação na escola e solução de problemas](INSTALACAO-ESCOLA.md).
- [Apresentação de oito minutos e explicação da arquitetura](APRESENTACAO-TCC.md).
- [Matriz entre requisitos/casos de uso/dicionário/diagramas e código](MATRIZ-REQUISITOS-TCC.md).
- [Guia de estudos](GUIA-DE-ESTUDOS.md).

## Pendências presenciais e documentais

1. Revisar e integrar os PRs respeitando a cadeia; nenhum merge foi realizado nesta execução.
2. Validar a instalação no computador da escola, o pgAdmin e o diálogo nativo de impressão/Salvar como PDF. O banco e a senha da escola não foram acessados.
3. Ensaiar a apresentação com os quatro integrantes e dados de demonstração separados de dados reais.
4. Atualizar os documentos originais do Drive pelos autores, alinhando nomes de perfis, campos e desativação lógica conforme a matriz. Os originais não foram alterados.
5. Preencher preços reais caso existam produtos legados sem preço. Não substituir por valores inventados.

Backup/restauração pelo pgAdmin têm instruções no guia de instalação, mas não foram executados neste ambiente. QR, vendas, notas fiscais, financeiro e multiempresa continuam fora do escopo.

## Testes automatizados e revisão final

`mvn clean test` aprovou 43 testes rápidos. `mvn -Ppostgres-it verify` aprovou os mesmos 43 e mais seis testes com PostgreSQL 16.4. Zero falhas/erros nos comandos finais. `git diff --check` aprovado. O relatório de revisão contém o inventário de arquivos e o encadeamento dos PRs.
