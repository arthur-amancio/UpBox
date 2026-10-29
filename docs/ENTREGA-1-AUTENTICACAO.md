# Entrega 1 — autenticação e usuários

O login agora é feito pelo servidor, com Spring Security e sessão HTTP. O navegador envia usuário/e-mail e senha por formulário com token CSRF; o BCrypt compara a senha ao hash salvo. O principal da sessão contém o ID estável do usuário. Cada requisição verifica se conta, perfil e senha continuam válidos. Logout é POST, não um link GET.

## Primeiro administrador no VS Code

1. Instale Java 17, Maven e PostgreSQL. Abra a pasta que contém `pom.xml`.
2. Crie o banco `upbox` pelo pgAdmin e copie `application-local.example.properties` para `application-local.properties` na raiz.
3. Configure a conexão e a senha do PostgreSQL nesse arquivo ignorado pelo Git.
4. Configure os campos `upbox.admin-inicial.*`, incluindo senha de no mínimo 8 caracteres (até 72 bytes), e defina `habilitado=true`.
5. Execute `mvn clean test` e `mvn spring-boot:run`; acesse http://localhost:8080.
6. Depois da primeira criação, remova a senha inicial e defina `habilitado=false`. O sistema não substitui um administrador existente, inclusive inativo.

Não há conta demo ou cadastro público. O administrador abre Usuários para cadastrar colegas. Ambos os perfis podem atualizar nome/e-mail e trocar sua própria senha informando a senha atual. A troca encerra a sessão. Não é permitido desativar ou rebaixar o último administrador ativo.

## Classes para apresentar

- Entity: representa uma tabela. `Usuario` guarda o hash, nunca a senha em texto puro.
- Repository: busca/salva entidades; o Spring Data implementa operações convencionais.
- Service: aplica regras como unicidade e proteção do último administrador.
- Controller: recebe formulário, verifica erros e escolhe HTML ou redirecionamento.
- `@Transactional`: confirma todas as alterações juntas; uma exceção desfaz a operação, inclusive a auditoria.
- Lombok: gera getters e construtor protegido do JPA. Não gera setters para saldo ou hash.

A auditoria registra ações principais, sem senhas. Os índices PostgreSQL impedem usuários/e-mails duplicados sem diferenciar caixa. Havendo duplicidades antigas, corrija-as pelo pgAdmin antes de reiniciar; nenhum registro é apagado automaticamente.

Nesta entrega, estoque, movimentações, dashboard e relatórios permanecem indisponíveis até suas entregas, sem misturar dados demo com contas reais. Os scripts originais estão preservados no histórico e não são carregados pelas telas convertidas.

## Verificação desta entrega

Java 17.0.17 e `mvn clean test`: 22 testes, zero falhas/erros. Chromium: login por e-mail, páginas de usuários/perfil/auditoria, menu móvel e logout, sem erros JavaScript. Aplicação iniciada no PostgreSQL 16.4; criação do administrador com BCrypt e auditoria verificadas no banco. Esses testes reais não substituem os testes de concorrência previstos para a entrega de movimentações.
