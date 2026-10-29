# UpBox — plano de desenvolvimento do TCC

> Estado em 29/10/2026: o repositório contém um **protótipo visual**, não um sistema conectado a banco de dados. Este documento é um planejamento técnico proposto com base no código atual; os requisitos definitivos devem ser conferidos com o grupo e o orientador.

## 1. Objetivo

Transformar o protótipo UpBox em um sistema web de controle de estoque simples, que permita registrar produtos, categorias e movimentações de entrada/saída, consultar saldos e mostrar relatórios. O código deve ser explicável pelos integrantes do grupo.

**Princípios:** usar Java 17, Spring Boot, Spring MVC, Thymeleaf, HTML, CSS e JavaScript básico já presentes. Preferir classes curtas, nomes claros e soluções que a equipe saiba manter. Preservar a interface existente quando ela atender aos requisitos.

## 2. Diagnóstico do código recebido

- Existem páginas de login, cadastro, home, dashboard, estoque, movimentações, relatórios e configurações.
- O Java serve as páginas pelo \`PaginaController\`, mas não processa dados de estoque.
- A lógica de produtos, categorias, movimentações e usuários fica em \`static/js\`, com dados fictícios gravados no \`localStorage\` do navegador.
- Login, papéis e senhas são **demonstrativos**; senhas e verificações existem no JavaScript, portanto **não protegem dados reais**.
- Há CSS e identidade visual aproveitáveis. O arquivo JavaScript do protótipo é condensado e pouco didático.
- Não havia testes automatizados nem banco de dados configurado.

**Consequência:** não utilizar o protótipo atual com dados pessoais ou empresariais reais. Novas operações de escrita no backend só podem ser habilitadas com autenticação/autorização efetivas.

## 3. Escopo mínimo (MVP)

| Módulo | Funcionalidades | Critério de aceitação |
| --- | --- | --- |
| Acesso | Login, logout, administrador e funcionário | O servidor identifica o usuário e impede ações não autorizadas |
| Categorias | Cadastrar, listar, editar, desativar/excluir se permitido | Não deixar produtos vinculados apontarem para categoria inexistente |
| Produtos | Cadastrar, listar, pesquisar e editar dados | Código único; nome obrigatório; quantidades válidas |
| Movimentações | Registrar entrada e saída com responsável e data | Atualiza saldo e histórico juntos, sem permitir saldo negativo |
| Estoque baixo | Exibir produtos com saldo menor ou igual ao mínimo | Alertas correspondem aos saldos persistidos |
| Dashboard | Resumo de produtos e movimentações | Indicadores calculados com dados do banco |
| Relatórios | Consultar registros e imprimir pelo navegador | Filtros correspondem aos dados persistidos |

**Fora do MVP, salvo exigência documentada:** compras, vendas, nota fiscal, códigos de barras, notificações externas, aplicativos móveis, integrações, vários estabelecimentos e múltiplos depósitos. Não incluir funcionalidades apenas para aumentar o projeto.

## 4. Regras de negócio

1. Produto: código único e não vazio, nome não vazio, saldo inicial e estoque mínimo não negativos.
2. Movimentação: tipo ENTRADA ou SAIDA, produto existente, quantidade inteira maior que zero.
3. Saída não pode superar o saldo disponível; saldo nunca pode ficar negativo.
4. Movimentações precisam registrar data/hora e usuário responsável. Histórico não deve ser apagado como efeito colateral da exclusão de um produto; preferir desativá-lo.
5. Saldo deve mudar por operação de estoque, não por edição arbitrária do cadastro.
6. Quando houver banco, salvar movimentação e saldo **na mesma transação**. Verificar concorrência (duas saídas simultâneas) com bloqueio de registro ou atualização condicional.
7. Administrador gerencia cadastros e usuários; funcionário consulta e registra movimentações, sujeito à validação no servidor. Confirmar permissões com o grupo e o documento do TCC.

## 5. Arquitetura ensinável

Fluxo: formulário Thymeleaf -> Controller -> Service (validação/regra) -> Repository (JPA) -> banco.

Pacotes, quando necessários:
- \`controller/\`: rotas HTTP e ligação com as telas.
- \`model/\`: classes que representam produtos, categorias, usuários e movimentações.
- \`repository/\`: acesso aos registros do banco.
- \`service/\`: operações que precisam de validações, transações ou coordenação.
- \`config/\`: configurações de segurança quando a etapa exigir.

**Evitar:** frontend separado, APIs REST desnecessárias, bibliotecas para tudo, arquitetura hexagonal, microsserviços, DTO/mappers genéricos e interfaces criadas sem necessidade.

## 6. Etapas de execução

### PR 1 — Planejamento, primeira regra Java e testes (este PR)

- Documentar o estado atual e como estudar o código.
- Criar a classe simples \`Produto\` para **exercitar as regras de entrada/saída em Java**, ainda sem persistência.
- Criar testes unitários dessas regras e testes de navegação das rotas existentes.
- Manter telas e lógica demonstrativa intactas. **Não alegar que o estoque real já funciona.**

### PR 2 — Definição de banco e segurança inicial

- Confirmar com os integrantes/orientador se o banco exigido é MySQL ou PostgreSQL; não adicionar dois drivers.
- Definir a política de criação de contas (cadastro público atual é apenas visual; recomendamos criação pelo administrador para o MVP).
- Preparar o banco, conexão por variáveis de ambiente e autenticação no servidor com hash de senhas (BCrypt). Contas demonstrativas só em ambiente de demonstração, nunca em produção.

### PR 3 — Categorias e produtos reais

- Entidades e repositórios JPA, restrições de unicidade e formulários Thymeleaf.
- Cadastro, edição, listagem, filtros e mensagens de validação.
- Substituir as ações fictícias desse módulo, preservando o layout.
- Testar com banco real.

### PR 4 — Movimentações persistidas e integridade

- Entrada, saída e histórico.
- Verificar saldo disponível, concorrência e transação única.
- Testar retirada acima do saldo, quantidade zero, estoque mínimo e operações simultâneas.

### PR 5 — Dashboard, relatórios e configurações

- Apresentar números reais sem duplicar a regra de negócio no JavaScript.
- Relatórios filtrados; usar impressão nativa para PDF em vez de biblioteca complexa.
- Revisar permissões e configurações necessárias.

### PR 6 — Finalização e apresentação

- Jornada real de navegador, testes de regressão, segurança básica, acessibilidade e responsividade.
- Manual com capturas **reais** do UpBox, instruções de instalação e roteiro de demonstração.
- Revisar o que cada integrante sabe explicar antes da apresentação.

## 7. Padrão de comentários e revisão

- Comentários em português em classes, métodos importantes e regras de negócio; explicar **por que**, não repetir literalmente cada linha.
- Classes e métodos com nomes descritivos; não compactar JavaScript em uma linha.
- Uma alteração pequena por PR, com objetivo, antes/depois, testes e impacto documentados.
- Nenhum merge automático sem revisão.
- Não enviar senhas, tokens, arquivos .env nem dados reais de alunos/empresas.

## 8. Decisões pendentes com o grupo

- Qual banco é obrigatório (MySQL citado no pom.xml ou PostgreSQL estudado em aula)?
- É permitido criar conta livremente ou somente o administrador cadastra funcionários?
- Quais ações o funcionário poderá realizar?
- A exclusão de produtos deve ser desativação para preservar o histórico?
- Qual é o prazo, os casos de uso e os requisitos escritos aprovados pelo orientador?

Até essas respostas, manter as decisões reversíveis e limitar o trabalho inicial a código que independa do banco.
