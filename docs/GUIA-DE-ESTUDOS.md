# Guia de estudos — entendendo o UpBox funcional

## O caminho de um formulário

Navegador → Controller → Service → Repository → PostgreSQL. O resultado volta como HTML Thymeleaf. O JavaScript não autentica pessoas nem calcula saldos.

- **Entity:** classe persistida em uma tabela, marcada com `@Entity`. Exemplo: `Produto` representa `produtos`.
- **Repository:** interface do Spring Data que consulta e salva entidades, como `findById`. Não decide se uma saída é permitida.
- **Service:** aplica as regras. `MovimentacaoEstoqueService` verifica produto ativo, quantidade e usuário, altera saldo e grava histórico.
- **Controller:** recebe o endereço HTTP e o formulário, apresenta erros e escolhe o HTML. Após sucesso, redireciona para evitar repetir a operação ao atualizar a página.
- **Formulário Java:** `ProdutoForm` contém apenas campos que a tela pode alterar. Não tem quantidade, pois editar cadastro não deve mudar saldo.
- **Thymeleaf:** preenche HTML no servidor; `th:text` escapa dados e `th:action` em formulários POST integra o token CSRF.

## Anotações e relacionamentos

`@Id` identifica a entidade e `@GeneratedValue` deixa o banco gerar o número. `@ManyToOne` significa que vários produtos pertencem a uma categoria; várias movimentações pertencem a um produto e um usuário. `@JoinColumn` aponta a coluna da chave estrangeira. Não apagamos esses registros ao desativar uma conta ou produto.

`fetch=LAZY` adia a leitura de um relacionamento. As consultas de tela usam `join fetch` ou `@EntityGraph` para carregar o que será exibido antes de terminar a transação, mantendo `open-in-view=false`.

`@Enumerated(EnumType.STRING)` salva nomes como `ENTRADA` e `FUNCIONARIO`, em vez da posição numérica do enum.

`@Transactional` agrupa as gravações. Exemplo: uma saída muda saldo e cria histórico; se o histórico ou a auditoria falhar, o banco desfaz tudo. O bloqueio `PESSIMISTIC_WRITE` faz operações concorrentes esperarem o mesmo produto; não basta conferir o saldo apenas no JavaScript.

## Lombok com encapsulamento

As entidades usam `@Getter` e construtor JPA protegido por `@NoArgsConstructor`. Elas não possuem setters livres para saldo, nem `equals`, `hashCode` ou `toString` que percorrem relacionamentos.

Formulários simples usam `@Data`. Senhas não aparecem em `toString`. `@RequiredArgsConstructor` injeta dependências `final` sem repetir construtores triviais. Métodos de negócio continuam escritos em Java convencional.

## Como o login funciona

O Spring Security recebe usuário/e-mail e senha. Busca a conta no PostgreSQL, compara a senha com BCrypt e recusa conta inativa. A sessão HTTP guarda um principal identificado pelo ID da conta. A cada requisição, o servidor verifica atividade, perfil e hash atual para revogar sessões desatualizadas.

O funcionário não ganha acesso administrativo ao esconder/mostrar um botão: as rotas também são protegidas no servidor. O logout exige POST e CSRF. O administrador inicial é criado por configuração local, sem credenciais no Git.

## Roteiro para demonstrar ao professor

1. Entre como administrador e crie um funcionário.
2. Cadastre a categoria Papelaria e o produto Caderno, código `PRD-001`, preço `12.50`, mínimo `3`.
3. Mostre saldo inicial zero e o formulário de edição sem campo para mudar saldo.
4. Registre entrada de 10: saldo 10.
5. Registre saída de 7: saldo 3 e alerta de estoque baixo.
6. Tente saída de 4 e quantidade zero: a operação deve ser recusada, sem novo histórico.
7. Mostre responsável/data no histórico e filtre pelo produto e pelo dia.
8. Abra dashboard e relatórios. Prepare impressão e escolha Salvar como PDF no navegador.
9. Reinicie o programa: dados permanecem no PostgreSQL.
10. Entre como funcionário; consulte estoque e tente uma rota administrativa para demonstrar o bloqueio.

## VS Code e pgAdmin

Abra a raiz do projeto, confira `java -version` e `mvn -version`, configure o arquivo local e execute `mvn clean test` e `mvn spring-boot:run`. Consulte o [guia PostgreSQL](GUIA-POSTGRESQL-PGADMIN.md).

No pgAdmin, abra o banco `upbox` → Schemas → public → Tables. Veja `categorias`, `produtos`, `usuarios`, `movimentacoes_estoque` e `registros_operacoes`. Explique as chaves `categoria_id`, `produto_id` e `usuario_id`. Não exponha `senha_hash` em apresentações e não edite saldo manualmente para simular o sistema.

Cada integrante deve explicar uma regra, mostrar seu teste e percorrer a chamada do controller até o banco. Testes H2 verificam comportamentos rápidos; os testes PostgreSQL comprovam bloqueio e rollback com o banco real.

## Ensaio da apresentação

Use o [roteiro de oito minutos](APRESENTACAO-TCC.md), o [guia da escola](INSTALACAO-ESCOLA.md) e a [matriz de requisitos](MATRIZ-REQUISITOS-TCC.md). A [jornada real com capturas](ENTREGA-7-PREPARACAO-TCC.md) mostra o fluxo verificado no navegador e a persistência após reiniciar.
