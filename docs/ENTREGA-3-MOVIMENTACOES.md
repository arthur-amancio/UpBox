# Entrega 3 — entradas, saídas e histórico

## Exemplo para apresentar ao professor

1. Cadastre um produto com mínimo 3. O saldo inicial é zero.
2. Em Movimentações, selecione o produto, Entrada, quantidade 10 e observação opcional. Confirme: saldo 10 e um registro no histórico.
3. Registre Saída de 7: saldo 3, no limite do estoque mínimo.
4. Tente Saída de 4: a operação é recusada, mantendo saldo 3 e histórico inalterado.
5. Desative o produto e tente movimentá-lo: o servidor recusa, mesmo com uma requisição manual.

O responsável vem da sessão, não do formulário. O sistema registra horário de São Paulo. Valores zero, negativos, fracionários, maiores que o limite inteiro, tipo inválido e observação acima de 500 caracteres são recusados.

## Por que @Transactional e bloqueio?

`MovimentacaoEstoqueService.registrar` é o único serviço de movimentação. `@Transactional` agrupa alteração de saldo, histórico e auditoria. Se alguma gravação falhar, tudo volta ao estado anterior. `PESSIMISTIC_WRITE` bloqueia o registro do produto até terminar: duas saídas de 4 sobre saldo 5 não podem ambas ser aprovadas. A segunda espera, lê saldo 1 e é recusada.

O histórico não possui edição nem exclusão. Produto e usuário são relacionamentos obrigatórios; a desativação preserva os registros. Os filtros incluem produto, responsável, tipo e período. A data final é inclusiva: consultar 29/10 considera até o início de 30/10, sem perder frações de segundo.

## Testar PostgreSQL real sem Docker obrigatório

Crie **outro banco chamado `upbox_test`** no pgAdmin. Ele é exclusivo para testes: tabelas e dados serão recriados/apagados. Nunca use o banco `upbox` da escola.

No terminal PowerShell do VS Code:

```powershell
$env:UPBOX_TEST_DB_URL = 'jdbc:postgresql://localhost:5432/upbox_test'
$env:UPBOX_TEST_DB_USER = 'postgres'
$env:UPBOX_TEST_DB_PASSWORD = 'SUA_SENHA_LOCAL'
mvn -Ppostgres-it verify
Remove-Item Env:UPBOX_TEST_DB_PASSWORD
```

A senha fica apenas no terminal local, não no Git. O teste rejeita uma URL que não aponte ao banco `upbox_test`, antes de iniciar o JPA. O comando executa os testes rápidos e os testes PostgreSQL: concorrência, rollback por falha no histórico/auditoria e índices de unicidade. `mvn clean test` sozinho usa H2 para os testes de persistência e não comprova bloqueio no PostgreSQL.

Pelo pgAdmin, consulte `produtos.quantidade_estoque` e `movimentacoes_estoque`, com suas chaves `produto_id` e `usuario_id`. Não altere saldos manualmente para demonstrar o fluxo, pois isso pula as regras do serviço.

## Verificação desta entrega

Java 17.0.17: `mvn clean test` aprovou 30 testes rápidos. `mvn -Ppostgres-it verify` aprovou os mesmos 30 testes e mais 4 testes com PostgreSQL 16.4: saídas simultâneas, falha no histórico, falha posterior na auditoria e unicidade sem diferenciar caixa. Zero falhas/erros de teste. As violações de restrição exibidas no log desses cenários são intencionais para provar rollback.

Chromium + PostgreSQL: entrada de 10, saída de 7 e recusa de saída de 4 mantiveram saldo 3 e exatamente duas movimentações persistidas. Sem erros JavaScript. A CI ganhou um job PostgreSQL, além do job de testes rápidos.
