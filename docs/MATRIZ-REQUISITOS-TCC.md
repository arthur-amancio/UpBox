# Matriz — TCC oficial e código implementado

## Fontes consultadas em leitura

- [Levantamento de requisitos](https://docs.google.com/document/d/1Ob4jUd3Vfrw_OrCNPFIfd05GrczVTQOF/edit)
- [Modelagem de requisitos](https://docs.google.com/document/d/1i8tkgXbd4-Pm428LkA8EuatMcys5d61c/edit)
- [Lista de casos de uso](https://docs.google.com/document/d/1tCJB16n6O56-aCwrKiSbyWMCa89BoxQ1/edit)
- [Dicionário de atributos](https://docs.google.com/document/d/1XLHzMr0SUnhpkYeJ0zXSUwyKIaRbKSrT/edit)
- [Análise orientada a objetos](https://docs.google.com/document/d/13XwfDT9DxUNNZFC0nWvoVi2f1KJdLPJH/edit)
- [Diagrama de classes — texto](https://docs.google.com/document/d/1kOo16Vz3-ba2V6-DDbxz2J-fkLbb6Xnn/edit)
- [Diagrama de classes — imagem de 28/10](https://drive.google.com/file/d/1MXLw9evsmIToHdghRNyuzbpQeujSclWe/view)
- [Diagrama geral de casos de uso](https://drive.google.com/file/d/1hn_cQ4rRvO0UhI-h5Irx7IqqrBCDCj6f/view)

As duas imagens foram inspecionadas, além dos textos. Nenhum original do Drive foi modificado. As decisões já confirmadas pelo responsável — PostgreSQL, nomes dos perfis, preço obrigatório, usuários internos, desativação lógica e impressão administrativa — foram preservadas.

Os caminhos Java abaixo são relativos a `src/main/java/com/upbox`; testes ficam em `src/test/java/com/upbox`.

## Casos de uso e requisitos funcionais

| Requisito | Implementado? | Arquivo responsável | Evidência | Pendência |
| --- | --- | --- | --- | --- |
| UC01 — Login por usuário ou e-mail | Sim | config/SegurancaConfig.java; repository/UsuarioRepository.java | PaginaControllerTest: login por nome/e-mail, senha incorreta e conta inativa; jornada real | Nenhuma de código |
| UC02 — Encerrar sessão | Sim | config/SegurancaConfig.java; templates/fragments/layout.html | POST com CSRF, sessão invalidada e retorno ao login | Nenhuma de código |
| UC03 — Consultar dashboard | Sim | service/DashboardService.java; controller/RelatorioController.java | RelatorioMvcTest e jornada: vazio, normal, baixo e inativo | Nenhuma de código |
| UC04 — Consultar produtos | Sim | service/ProdutoService.java; templates/estoque.html | CadastroMvcTest e estoque no navegador | Nenhuma de código |
| UC05 — Pesquisar/filtrar por nome, código, categoria e situação | Sim | repository/ProdutoRepository.java; controller/EstoqueController.java | CadastroMvcTest e pesquisa por TCC-001 na jornada | Nenhuma; paginação não justificada nesta revisão |
| UC06 — Registrar entrada | Sim | service/MovimentacaoEstoqueService.java; model/Produto.java | MovimentacaoMvcTest; entrada 10 no navegador e SQL | Nenhuma de código |
| UC07 — Registrar saída sem exceder saldo | Sim | service/MovimentacaoEstoqueService.java; model/Produto.java | Saída 7 e recusa de 4 com saldo 3; testes normais/inválidos | Nenhuma de código |
| UC08 — Histórico preservado | Sim | model/MovimentacaoEstoque.java; repository/MovimentacaoEstoqueRepository.java | Teste após desativar produto/usuário e jornada com produto inativo | Nenhuma de código |
| UC09 — Filtros de histórico | Sim | model/FiltroMovimentacao.java; controller/MovimentacaoController.java | Produto, usuário, tipo, período; teste de fração de segundo no fim do dia | Nenhuma de código |
| UC10 — Alertas de mínimo | Sim | model/Produto.java; service/DashboardService.java | Saldo 3/mínimo 3 gera alerta; inativos não geram alerta | Nenhuma de código |
| UC11 — Consultar relatórios | Sim, ambos os perfis | service/RelatorioService.java; templates/relatorios.html | Seis relatórios e bloqueio da opção administrativa de impressão | Nenhuma de código |
| UC12 — Gerenciar o próprio perfil | Sim | controller/UsuarioController.java; service/UsuarioService.java | Formulário usa ID da sessão; perfil longo validado no navegador | Nenhuma de código |
| UC13 — Alterar senha | Sim | service/UsuarioService.java; repository/UsuarioRepository.java | Senha atual, confirmação, BCrypt, encerramento/revogação; disputa com desativação no PostgreSQL | Nenhuma de código |
| UC14 — Gerenciar usuários | Sim, administrador | controller/UsuarioController.java; service/UsuarioService.java | Cadastro/edição/status; jornada cria funcionário e revoga sessão ao desativar | Nenhuma de código |
| UC15 — Gerenciar permissões | Sim, administrador | config/SegurancaConfig.java; service/UsuarioService.java | Rotas HTTP 403 para funcionário e preservação do último administrador | Atualizar nomes de perfil nos documentos formais |
| UC16 — Gerenciar categorias | Sim, com desativação lógica | controller/EstoqueController.java; service/CategoriaService.java | Cadastro, edição, duplicidade, status e categoria inativa nos testes | Alinhar o termo “excluir” com a decisão de desativar |
| UC17 — Gerenciar produtos | Sim, com desativação lógica | controller/EstoqueController.java; service/ProdutoService.java | Código único, preço, mínimo, edição sem saldo e status | Alinhar o termo “excluir” com a decisão de desativar |
| UC18 — Gerar relatórios | Sim | controller/RelatorioController.java; service/RelatorioService.java | Consulta filtrada e preparação administrativa para impressão | Manter clara a distinção entre consulta e preparação para impressão |
| UC19 — Exportar PDF | Sim, via impressão nativa aprovada | templates/relatorios.html; static/js/interface.js; static/css/style.css | window.print, CSS e PDF de verificação no Chromium | Conferir diálogo nativo no computador da escola; não há biblioteca de PDF |
| UC20 — Consultar registros de operações | Sim, auditoria simples administrativa | service/AuditoriaService.java; model/RegistroOperacao.java; controller/UsuarioController.java | Gravações de ações principais e consulta restrita ao administrador | Auditoria não é trilha inviolável contra alteração direta no banco |
| Quantidade positiva, saldo não negativo e operações simultâneas | Sim | model/Produto.java; service/MovimentacaoEstoqueService.java | ProdutoTest, MovimentacaoMvcTest e PostgresIT (duas saídas simultâneas) | Nenhuma de código |
| Saldo/histórico/auditoria na mesma transação | Sim | service/MovimentacaoEstoqueService.java | PostgresIT força falha no histórico e depois na auditoria, verificando rollback | Nenhuma de código |
| Proteção de páginas e formulários | Sim | config/SegurancaConfig.java; config/SessaoAtualFilter.java | Login, acesso anônimo, CSRF, papéis e sessão inativa testados | Nenhuma de código |
| Persistência após reiniciar | Sim | application.properties; entidades/repositórios JPA | Jornada reinicia aplicação e confere saldo, histórico, usuários e dashboard | Conferir instalação presencial da escola |
| Responsividade e facilidade de uso | Sim, cenários verificados | static/css/style.css; static/js/interface.js; templates/fragments/layout.html | Chromium em 320/390/1365 px, teclado, foco, rolagem e prévia | Sem certificação ou teste em aparelhos físicos nesta revisão |

## Modelo de dados e divergências documentais

| Requisito | Implementado? | Arquivo responsável | Evidência | Pendência |
| --- | --- | --- | --- | --- |
| Usuario, Categoria, Produto e MovimentacaoEstoque | Sim | model/Usuario.java; Categoria.java; Produto.java; MovimentacaoEstoque.java | Mapeamentos e metadados PostgreSQL conferidos | Nenhuma de código |
| Categoria 1 → muitos produtos | Sim, associação unidirecional ManyToOne | model/Produto.java | FK categoria_id e consultas join fetch | O diagrama não exige listas bidirecionais no Java |
| Produto/usuário 1 → muitas movimentações | Sim | model/MovimentacaoEstoque.java | FKs produto_id/usuario_id obrigatórias; histórico preservado | Nenhuma de código |
| Produto.preco BigDecimal | Sim, obrigatório no cadastro/edição | model/Produto.java; ProdutoForm.java | numeric(12,2), validação e R$ 12,50 na interface | Preencher preços reais de eventuais registros antigos sem preço |
| Produto.codigo | Sim | model/Produto.java; repository/ProdutoRepository.java | Requisito formal de código e teste de unicidade | Incluir no diagrama/dicionário, que omitem esse campo |
| Nome de usuário para login | Sim | model/Usuario.java | usuario único e login por usuário/e-mail | Incluir usuario no diagrama/dicionário |
| Perfis ADMIN/USUARIO no diagrama | Equivalência funcional, nomes diferentes | model/PerfilAcesso.java | Código usa ADMINISTRADOR/FUNCIONARIO, conforme decisão anterior do responsável | Atualizar enum nos documentos formais; não renomear o código sem necessidade |
| Senha protegida | Sim, hash BCrypt | model/Usuario.java; config/SegurancaConfig.java | senha_hash; login seguro; sem exposição em páginas | Usar o termo hash BCrypt no lugar de “senha criptografada” na documentação |
| quantidadeEstoque no dicionário | Sim, nome Java abreviado | model/Produto.java | Campo Java quantidade, coluna quantidade_estoque e getters usados nas telas | Registrar a equivalência no dicionário, sem migração desnecessária |
| Registro de operações | Sim, entidade adicional necessária | model/RegistroOperacao.java | UC20 exige registro das ações principais | Incluir RegistroOperacao no diagrama/dicionário formal |
| Operações de autenticação/cadastro atribuídas às classes na análise OO | Responsabilidade distribuída em MVC/serviços | config/SegurancaConfig.java; controller/ e service/ | Entity representa estado/regras; serviço coordena transações; segurança autentica | Alinhar operações sugeridas na análise OO à implementação didática real |

As pendências desta seção são de coerência dos documentos ou de verificação presencial. Não foram usadas para inventar novas funcionalidades ou reescrever os documentos do grupo sem autorização.
