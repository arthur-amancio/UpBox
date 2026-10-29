# Entrega 4 — dashboard, relatórios e remoção das simulações

## Indicadores

O dashboard mostra total de produtos cadastrados (incluindo inativos), soma de unidades dos produtos ativos, alertas de produtos ativos com saldo menor ou igual ao mínimo, unidades de entrada/saída de hoje e dos seis dias anteriores, além das dez últimas movimentações. As datas usam São Paulo. Banco vazio mostra zero e mensagens de ausência de registros.

## Relatórios

- Produtos cadastrados: inclui ativos e inativos.
- Estoque atual: saldo dos produtos ativos.
- Estoque abaixo ou no mínimo: produtos ativos com saldo menor ou igual ao mínimo.
- Entradas e Saídas: histórico do tipo escolhido.
- Histórico por período: entradas/saídas com filtros opcionais.

Produto e categoria filtram todos os relatórios. Período e tipo de movimentação filtram o histórico. Entradas e Saídas fixam o respectivo tipo; a tela informa essa regra. A data final inclui o dia inteiro. A categoria do histórico é a associação atual do produto; não reconstruímos categorias nem preços históricos que o sistema anterior não registrou.

## Consulta e PDF

Ambos os perfis consultam e filtram. Somente o administrador recebe a opção e a rota `/relatorios/imprimir`. Ela prepara o mesmo resultado filtrado, com título, horário, responsável e filtros. Clique em Imprimir / Salvar como PDF e escolha o destino no navegador. Não há biblioteca de PDF nem mensagem falsa de arquivo salvo.

Essa permissão controla a opção oferecida pelo sistema; não pretende impedir a impressão nativa de uma página que o funcionário já pode consultar. A auditoria registra “Relatório preparado para impressão”, sem afirmar que houve salvamento.

## Remoções justificadas

Os scripts `dados.js`, `app.js` e `js/pages/` e os templates antigos de início/cadastro foram removidos porque não são mais usados e continham autenticação, cadastros e saldos fictícios. O histórico Git preserva os originais. O template temporário de módulo pendente também foi removido porque todos os módulos foram conectados.

A logo, folha de estilos, cores e componentes visuais continuam no projeto. `interface.js` cuida apenas de menu, confirmação, impressão e preferência de tema; só o tema pode ser armazenado no navegador.

## Regras complementares

A criação do administrador inicial verifica qualquer administrador existente, inclusive inativo, e nunca substitui a conta. Preços ausentes em registros anteriores devem ser informados pelo administrador; não criamos preços fictícios para preencher o legado.

## Verificação final

Java 17.0.17: `mvn clean test` aprovou 37 testes. `mvn -Ppostgres-it verify` aprovou a suíte rápida e 4 testes com PostgreSQL 16.4. Zero falhas/erros nos comandos finais.

Chromium + PostgreSQL validaram persistência após reinício, indicadores comparados com SQL, seis relatórios, filtros, CSS de impressão/PDF, cadastro de funcionário, permissões e menu móvel, sem erros JavaScript. Consulte o relatório final para a lista de cenários e arquivos.
