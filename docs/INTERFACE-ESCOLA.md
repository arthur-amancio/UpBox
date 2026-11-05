# Interface e verificação visual

Em 05/11/2026 foram feitas capturas reais com Chromium 154, nas resoluções **1366×768**, **1024×768** e **390×844**, antes e depois dos ajustes. Os dados são fictícios de homologação. A identidade âmbar, preto/branco, logo e as famílias Plus Jakarta Sans/Poppins foram mantidas.

## Ajustes

- Fontes locais: três arquivos WOFF2, aproximadamente 43 KB, com licenças OFL na pasta static/fonts. Não é necessário acessar Google Fonts.
- Filtros ocupam a largura disponível, com duas colunas no celular; botões, alturas e espaçamentos consistentes.
- Indicadores se organizam em cinco, três ou duas colunas conforme a tela; números legíveis e com alinhamento estável.
- Primeiro acesso mais organizado, formulário em duas colunas no computador e uma no celular.
- Aviso de rolagem somente quando a tabela tem colunas fora da área visível; tabela continua acessível por teclado.
- Respeito a prefers-reduced-motion, ajustes de contraste e impressão A4 sem menus/ações.

## Evidências versionadas

As pastas [antes](evidencias/versao-escola/antes/) e [depois](evidencias/versao-escola/depois/) contêm login, primeiro acesso, dashboard e estoque nas três resoluções. Foram conferidas também categorias, produto, movimentações, usuários, relatórios, formulários e configurações. A jornada completa e todas as capturas de homologação estão disponíveis nos artefatos da entrega.

| Resolução | Estoque antes | Estoque depois |
| --- | --- | --- |
| 1366×768 | [Captura](evidencias/versao-escola/antes/estoque-1366x768.png) | [Captura](evidencias/versao-escola/depois/estoque-1366x768.png) |
| 1024×768 | [Captura](evidencias/versao-escola/antes/estoque-1024x768.png) | [Captura](evidencias/versao-escola/depois/estoque-1024x768.png) |
| 390×844 | [Captura](evidencias/versao-escola/antes/estoque-390x844.png) | [Captura](evidencias/versao-escola/depois/estoque-390x844.png) |

[Resultado automatizado do navegador](evidencias/versao-escola/navegador.json): sem erro JavaScript, sem rolagem horizontal da página e sem requisição externa. Fontes carregadas localmente. O menu abre por Enter e fecha por Escape devolvendo foco; a tabela rola por setas. Funcionário consulta relatórios e recebe 403 nas rotas administrativas; ações administrativas não aparecem no menu.

[PDF A4 real](evidencias/versao-escola/relatorio-A4.pdf): uma página, 842,88×595,92 pontos, paisagem, gerado pelo Chromium usando o CSS de impressão. O diálogo nativo e a impressora/projetor da escola ainda precisam ser conferidos presencialmente; não foram simulados como evidência.

As regras continuam nos controllers/serviços: nenhum cálculo ou permissão foi transferido para JavaScript. `mvn clean package` passou com os 45 testes rápidos antes de publicar esta etapa.
