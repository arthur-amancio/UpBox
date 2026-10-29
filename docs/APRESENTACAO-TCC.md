# Apresentar o UpBox em 8 minutos

## Preparar antes da aula

Use o [guia de instalação](INSTALACAO-ESCOLA.md). Confirme PostgreSQL e UpBox iniciados, contas de administrador/funcionário disponíveis, navegador aberto e terminal com os testes concluídos. Prepare um banco de demonstração, separado de dados de uso real. Não apague tabelas do banco da escola para ensaiar.

O roteiro abaixo usa código `TCC-001`, categoria Papelaria, produto Caderno, preço `12.50` e mínimo `3`. Se esses registros já existirem, escolha outro código ou use os registros do ensaio; não tente cadastrar código duplicado como se fosse sucesso.

## Arquitetura que o grupo precisa explicar

**Formulário no navegador → Controller → Service → Repository → PostgreSQL → página Thymeleaf.**

| Parte | Explicação para a turma | Exemplo real |
| --- | --- | --- |
| Entity | Objeto que representa um registro de tabela. | Produto, Usuario, Categoria, MovimentacaoEstoque |
| Repository | Interface que consulta e salva registros. O Spring Data implementa as operações convencionais. | ProdutoRepository |
| Service | Lugar das regras: conferir saldo, validar atividade e salvar alterações juntas. | MovimentacaoEstoqueService |
| Controller | Recebe a rota e o formulário, chama o serviço e escolhe a página ou redirecionamento. | MovimentacaoController |
| Thymeleaf | Monta o HTML no servidor com os dados reais. `th:text` apresenta texto escapado e `th:field` liga campos ao formulário. | templates/movimentacoes.html |
| Spring Security | Verifica credenciais, mantém sessão e bloqueia rotas conforme o perfil. CSRF protege os formulários POST. | SegurancaConfig e SessaoAtualFilter |
| Lombok | Evita repetir getters e construtores sem abrir setters para saldo. Formulários simples podem usar `@Data`. | Produto e ProdutoForm |

As pastas Java ficam em `src/main/java/com/upbox`. Templates e arquivos visuais ficam em `src/main/resources`. Não há React, API separada ou microsserviços.

## Relacionamentos JPA

- Uma categoria reúne vários produtos; cada produto tem uma categoria. A coluna `produtos.categoria_id` é a chave estrangeira.
- Um produto pode ter várias movimentações; cada movimentação aponta para um produto e um usuário responsável.
- `movimentacoes_estoque.produto_id` e `usuario_id` são chaves estrangeiras obrigatórias.
- `@ManyToOne` representa esses vínculos; não precisamos colocar listas bidirecionais em todas as entidades.
- `@Id` identifica a linha; `@GeneratedValue` deixa o banco gerar o ID. `@Enumerated(STRING)` salva nomes como ENTRADA/SAIDA.
- `LAZY` adia relacionamentos. As consultas de tela carregam os dados necessários com `join fetch` ou `@EntityGraph`, mantendo `open-in-view=false`.
- Desativar conserva vínculos e histórico. Não há exclusão física em cascata dos registros operacionais.

## O que acontece em uma entrada

O controller recebe produto, tipo, quantidade e observação. O usuário responsável vem da sessão, não de um campo editável. O service bloqueia o produto, exige produto/usuário ativos e quantidade inteira positiva. `registrarEntrada(10)` aumenta o saldo. O repository salva a movimentação e o JPA detecta a mudança de saldo.

`@Transactional` faz saldo, histórico e auditoria serem confirmados na mesma transação. Uma falha de gravação desfaz a operação. Não existe um segundo serviço de movimentação repetindo a regra.

## O que acontece em uma saída

Com saldo 10, `registrarSaida(7)` resulta em 3. Uma saída de 4 é recusada, pois só existem 3. O saldo e o histórico continuam iguais após a recusa. O alerta aparece quando saldo **menor ou igual** ao mínimo; saldo 3 e mínimo 3 já significam estoque baixo.

O bloqueio de registro impede duas retiradas de usarem ao mesmo tempo o mesmo saldo antigo. O teste PostgreSQL demonstra duas saídas de 4 sobre saldo 5: somente uma é aprovada.

## Roteiro cronometrado

| Tempo | Mostrar | Explicar |
| --- | --- | --- |
| 0:00–0:45 | Home e objetivo | UpBox organiza estoque de microempresas com perfis administrador e funcionário. |
| 0:45–1:30 | Login e menu | Senha comparada com BCrypt, sessão HTTP e permissões no servidor. A criação de contas é administrativa. |
| 1:30–2:30 | Categoria e produto | Código único, categoria, preço e mínimo. Saldo começa em zero; edição não tem campo para alterá-lo. |
| 2:30–3:30 | Entrada de 10 | Formulário → controller → service → repository → PostgreSQL; responsável vem da sessão. |
| 3:30–4:30 | Saída de 7 e tentativa de 4 | Saldo final 3, alerta de mínimo e recusa sem alteração do histórico. A prévia no navegador não autoriza a operação. |
| 4:30–5:30 | Histórico e dashboard | Produto, quantidade, tipo, data/hora e responsável. Números correspondem ao banco. |
| 5:30–6:15 | Relatório e impressão | Filtros de produto/categoria/período; administrador prepara impressão e usa Salvar como PDF no navegador. |
| 6:15–7:00 | Funcionário | Consulta e movimentação permitidas; cadastros administrativos e preparação de impressão bloqueados. |
| 7:00–8:00 | Testes e pgAdmin | Mostrar resultado dos testes e registros com join. Reiniciar e conferir persistência pode ser demonstrado aqui ou por evidência do ensaio. |

Para quatro integrantes, uma divisão possível é: 1 explica objetivo/login/arquitetura; 2 faz cadastros; 3 demonstra movimentações/regras; 4 apresenta relatórios, testes e banco. Todos devem conhecer o fluxo completo.

## Perguntas que o grupo deve saber responder

- Por que o saldo não tem setter nem campo no formulário de edição?
- Por que validar somente no JavaScript não é suficiente?
- Como o sistema sabe quem registrou uma saída?
- O que acontece se duas pessoas retirarem o mesmo produto ao mesmo tempo?
- O que `@Transactional` evita quando uma gravação falha?
- Por que desativar em vez de apagar um produto com histórico?
- Qual a diferença entre PostgreSQL e pgAdmin?
- Por que testes H2 não comprovam concorrência no PostgreSQL?

## Evidências e pendências documentais

Consulte [a jornada real](ENTREGA-7-PREPARACAO-TCC.md), [as capturas](evidencias/tcc/) e [a matriz com o TCC oficial](MATRIZ-REQUISITOS-TCC.md). Os dados das capturas são sintéticos, cadastrados pelos formulários e persistidos no PostgreSQL, não simulações do navegador.

Os documentos originais do Drive não foram alterados. O grupo ainda precisa alinhar nomes de perfis, campos adicionais e desativação lógica nos diagramas/dicionários formais antes da apresentação.
