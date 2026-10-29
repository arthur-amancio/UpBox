# UpBox — evolução funcional do MVP

## Estado atual

O protótipo foi conectado ao backend em quatro entregas encadeadas. O sistema utiliza Java 17, Spring Boot, MVC, JPA, PostgreSQL, Thymeleaf, Lombok e Spring Security. As telas não dependem mais de login, senhas ou saldos fictícios no navegador.

Os PRs anteriores foram preservados: #1 contém a base didática, #2 a preparação local e #3 a persistência. O PR #3 já havia sido integrado na branch `chore/preparacao-inicial-tcc`; essa foi a base da continuação, sem alterar a `main`.

## Entregas

| Entrega | Branch | Base inicial | Conteúdo |
| --- | --- | --- | --- |
| Autenticação e usuários | `feat/autenticacao-usuarios` | `chore/preparacao-inicial-tcc` | Login real, BCrypt, sessão, papéis, perfil, senha e auditoria |
| Categorias e produtos | `feat/categorias-produtos` | `feat/autenticacao-usuarios` | Formulários, preço, filtros, validações e desativação |
| Movimentações | `feat/movimentacoes-estoque` | `feat/categorias-produtos` | Entrada/saída, responsável, histórico, transação e concorrência |
| Dashboard e relatórios | `feat/dashboard-relatorios` | `feat/movimentacoes-estoque` | Indicadores reais, seis relatórios, impressão e remoção das simulações |

Nenhum merge é automático. Revisar e integrar respeitando as dependências. Enquanto não houver integração da cadeia, a branch da última entrega contém o MVP completo.

## Regras principais

- Cadastro de usuários, categorias e produtos é exclusivo do administrador.
- Produtos têm código único, preço obrigatório em cadastros/edições e mínimo não negativo; registros legados sem preço mostram pendência.
- Formulários novos criam produto com saldo zero; edição não recebe quantidade.
- Movimentação exige quantidade inteira positiva, produto/usuário ativos e saldo suficiente para saída.
- Saldo, histórico e auditoria são gravados juntos. Bloqueio de produto protege operações simultâneas e edição concorrente.
- Estoque baixo inclui o limite exato do mínimo. Desativação preserva associações e histórico.
- Ambos os perfis consultam dashboard e relatórios; a opção/rota de impressão é administrativa.

## Arquitetura ensinável

Formulário Thymeleaf → Controller → Service → Repository → banco. Sem frontend separado, APIs REST desnecessárias, microsserviços ou padrões empresariais adicionais.

Entidades encapsulam mudanças; formulários limitam campos editáveis. Os serviços existentes foram reaproveitados, principalmente `MovimentacaoEstoqueService`. Comentários em português explicam regras e anotações relevantes. A aparência reutiliza a logo, cores e componentes anteriores.

## Validação e documentação

Cada etapa executou `mvn clean test`, revisão do diff, commit, push e PR. A partir de movimentações, há testes reais PostgreSQL para concorrência, rollback e índices, além de cenários no Chromium. A CI também verifica integração PostgreSQL.

Os guias `ENTREGA-1` a `ENTREGA-4`, o guia de estudos, o guia pgAdmin e o relatório final descrevem uso, decisões e evidências. Os documentos oficiais do Drive foram consultados sem alterações.

## Limites e próximos passos

Estão fora deste MVP: multiempresa, QR, compras/vendas, notas fiscais, integrações externas e geração de PDF por biblioteca. O PDF usa impressão do navegador. Antes de outro ambiente além do desenvolvimento/TCC, planejar configurações de implantação e migrações versionadas; o `ddl-auto=update` foi mantido para o uso local solicitado.

A equipe precisa revisar os PRs, preencher preços reais de eventuais registros antigos e atualizar formalmente os diagramas/dicionários do Drive com autorização dos autores. Essas ações não são simuladas pelo sistema.

## Continuação — revisão e preparação do TCC

| Entrega | Branch | Base | Conteúdo |
| --- | --- | --- | --- |
| 5 | `fix/revisao-tecnica-mvp` | `feat/dashboard-relatorios` | Correção da concorrência de senha/desativação, erros amigáveis e testes de regressão |
| 6 | `feat/usabilidade-acessibilidade` | `fix/revisao-tecnica-mvp` | Menu/teclado, navegação de produto, prévia, filtros e impressão |
| 7 | `docs/preparacao-tcc` | `feat/usabilidade-acessibilidade` | Testes adicionais, jornada real, capturas, instalação, apresentação e matriz oficial |

Não houve refatoração geral, merge nem alteração direta da main. A versão completa da revisão está na branch da entrega 7 enquanto a cadeia não for integrada. Consulte o relatório da revisão para commits, PRs e evidências.
