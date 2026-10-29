# UpBox — alinhamento entre Drive, requisitos e código

> Revisão documental em 29/10/2026. Fonte: [pasta do UpBox no Google Drive](https://drive.google.com/drive/folders/1HduDoOHXHC-WpA0TgFztmEgA7RVMQC1D). **Não altera os documentos do grupo.** As decisões listadas abaixo ainda precisam de validação dos integrantes/orientador.

## 1. Documentos principais consultados

- [Cronograma de entrega](https://docs.google.com/document/d/1ebiHL1YzoaF_sGTwUMZqn29SPalI9DSq/edit)
- [Briefing](https://docs.google.com/document/d/1LB-slWI3NFevn5WNa8eMUg2kMKnTa-Kg/edit)
- [Levantamento de requisitos](https://docs.google.com/document/d/1Ob4jUd3Vfrw_OrCNPFIfd05GrczVTQOF/edit)
- [Modelagem de requisitos](https://docs.google.com/document/d/1i8tkgXbd4-Pm428LkA8EuatMcys5d61c/edit)
- [Lista de 20 casos de uso](https://docs.google.com/document/d/1tCJB16n6O56-aCwrKiSbyWMCa89BoxQ1/edit)
- [Dicionário de atributos](https://docs.google.com/document/d/1XLHzMr0SUnhpkYeJ0zXSUwyKIaRbKSrT/edit)
- [Dicionário de mensagens](https://docs.google.com/document/d/1pS863aXK2-ujtniD3dpPhL_H1JFl0B3e/edit)
- [Análise orientada a objetos](https://docs.google.com/document/d/13XwfDT9DxUNNZFC0nWvoVi2f1KJdLPJH/edit)
- [Tecnologias utilizadas](https://docs.google.com/document/d/1bqTZX6S4i5_P9o_bJrdyNWcPF8Ht04mj/edit)
- [Sprint 4 — redação consolidada](https://docs.google.com/document/d/1Ci9SUmXRfAxJ6DKbfsDXLcF4fu-PuCd3/edit)

## 2. Escopo documentado que deve orientar a implementação

O objetivo do UpBox é controlar o estoque de microempresas: cadastro/consulta/edição de produtos e categorias, entrada e saída, histórico, saldo mínimo, dashboard, relatórios, login e papéis de administrador/funcionário.

Os [20 casos de uso](https://docs.google.com/document/d/1tCJB16n6O56-aCwrKiSbyWMCa89BoxQ1/edit) definem:

- UC01–UC02: login e logout (ambos).
- UC03–UC05: dashboard, consulta, pesquisa e filtros de produtos (ambos).
- UC06–UC10: entrada, saída, histórico, filtros e alertas (ambos).
- UC11–UC13: consulta de relatórios, atualização de perfil e senha (ambos).
- UC14–UC20: gerenciamento de usuários/permissões/categorias/produtos, geração/exportação de relatórios e auditoria (administrador).

Este conjunto é a **referência funcional preferencial**, sem tornar o conteúdo imutável; alterações exigem atualização coerente na documentação e no código.

## 3. Divergências / decisões a resolver

| ID | Evidência | Diferença ou risco | Ação proposta |
| --- | --- | --- | --- |
| D01 | Dicionário: `PerfilAcesso` = `ADMIN` / `USUARIO`; lista de casos: Administrador / Funcionário; protótipo JavaScript = `ADMINISTRADOR` / `FUNCIONARIO` | O mesmo papel possui nomes diferentes; isso quebra checagens e documentação | Fixar `ADMINISTRADOR` e `FUNCIONARIO` em código/diagramas/dicionário, se aprovado pelo grupo |
| D02 | Dicionário de atributos define `Produto.preco: BigDecimal` e `Produto.ativo: Boolean`; protótipo de estoque não os contém; modelagem formal de requisitos não exige preço explicitamente | Implementar campos sem acordo gera divergência e migrações posteriores | Confirmar se preço é escopo; recomendar `ativo` para preservar histórico ao desativar produto |
| D03 | Modelagem exige código de produto e mensagem MSG23 exige código único; dicionário de atributos não lista `Produto.codigo` | Modelo formal incompleto | Incluir `codigo: String` como obrigatório e único na versão aprovada do dicionário/diagrama |
| D04 | Tela inicial/cadastro do protótipo sugere auto-cadastro; UC14 e MSG08 tratam cadastro pelo administrador; Sprint 1 descreve cada usuário com ambiente próprio | Ambiguidade entre sistema interno de uma empresa e SaaS com contas públicas/multiempresa | Recomendar MVP de **uma organização**, usuários criados pelo administrador. Remover/desativar acesso público ao cadastro quando autenticação real chegar. Validar com grupo |
| D05 | UC17 e MSG25/26 falam em excluir produtos; dicionário traz `ativo` e UC08 exige histórico | Exclusão física pode invalidar histórico | Preferir desativação lógica; esclarecer se mensagem deve dizer 'desativar' |
| D06 | Documento 'Tecnologias utilizadas' cita código QR; casos de uso e modelagem de requisitos não definem operação de QR | Funcionalidade não especificada com critérios de aceitação | Tratar QR como **possível melhoria futura**, a menos que orientador confirme obrigatoriedade |
| D07 | Dicionário de movimentação lista apenas campos próprios; análise OO descreve associações com produto e usuário | Não explicita integridade dos relacionamentos no dicionário | Garantir `produto` e `usuario` como associações JPA, preservar dados necessários a auditoria |
| D08 | `pom.xml` menciona MySQL como futura implementação; documentos não fixam SGBD | Escolha técnica ainda aberta | Escolher **um** SGBD (MySQL ou PostgreSQL) e documentar instalação |
| D09 | Protótipo guarda senha, sessão e saldos no navegador; documentos exigem autenticação e auditoria reais | Risco de achar que o protótipo já cumpre os requisitos | Migrar validação/permissões para backend; senhas com hash; banco persistente; registrar usuário/data em toda movimentação |
| D10 | Dicionário de mensagens inclui MSG04 de sessão expirada, MSG36/38 de relatório; protótipo sinaliza exportação mesmo sem confirmar que houve salvamento | Mensagens de sucesso potencialmente incorretas | Emitir sucesso apenas depois de operação confirmada; usar impressão do navegador para 'Salvar como PDF' sem alegar arquivo gerado antecipadamente |

### Proposta de modelo mínimo para o MVP

- `Usuario`: id, nome, nomeUsuario, email, senhaHash, perfil, ativo.
- `Categoria`: id, nome, descricao, ativa.
- `Produto`: id, codigo, nome, descricao, categoria, quantidadeEstoque, estoqueMinimo, ativo; **preco apenas se confirmado**.
- `MovimentacaoEstoque`: id, produto, usuario, tipo (ENTRADA/SAIDA), quantidade, dataHora, observacao.

O domínio atual `model/Produto.java` no PR #1 é somente um exercício inicial de regras de quantidade, **não o modelo JPA final**. Não adicionar JPA, tipos extras nem campos especulativos antes da decisão sobre o banco.

## 4. Prazo e prioridade

O [cronograma](https://docs.google.com/document/d/1ebiHL1YzoaF_sGTwUMZqn29SPalI9DSq/edit) contém:
- **29/10**: análise orientada a objetos, diagrama de classes e dicionário de atributos.
- **05/11**: protótipos, conteúdos de tecnologia e considerações finais.
- **19/11 e 26/11**: pré-bancas previstas.
- **03/12**: banca geral / banner.

As datas são as previstas pelo documento (não prova de entrega ou agendamento definitivo). Priorizar coerência dos diagramas e demonstrar um percurso real (login → produto → entrada → saída → relatório). Não investir em módulos novos enquanto o fluxo básico não estiver persistente e testado.

## 5. Plano de ação imediato

1. Apresentar esta tabela aos integrantes, escolher nomes de perfis, regra de cadastro, destino dos produtos excluídos, presença de preço/QR e SGBD.
2. Corrigir dicionário de atributos/diagrama de classe **na documentação do grupo, com autorização deles**.
3. Definir o banco e criar PR específico para persistência/autenticação; não editar a `main` diretamente.
4. Substituir demonstrações JavaScript por formulários/consultas reais por módulo, com testes e comentários pedagógicos em português.
5. Revisar a documentação de funcionalidades a cada PR concluído. Não declarar pronta uma função apenas por sua tela existir.

## 6. Identidade visual preservada

O [briefing](https://docs.google.com/document/d/1LB-slWI3NFevn5WNa8eMUg2kMKnTa-Kg/edit) define laranja principal `#E39A3C`, laranja claro `#F2B15E`, laranja escuro `#A6631A`, fundo `#FAF7F2`, preto amarronzado `#241A10` e fonte Plus Jakarta Sans. Preservar identidade e telas, corrigindo apenas problemas reais de uso/responsividade.
