# Entrega 2 — categorias e produtos

## Como cadastrar

1. Entre como administrador e abra Estoque → Gerenciar categorias → Nova categoria.
2. Informe nome e descrição opcional. O nome não pode repetir outra categoria, mesmo com letras maiúsculas diferentes.
3. Em Estoque → Cadastrar produto, informe código único, nome, categoria ativa, estoque mínimo e preço unitário não negativo com até duas casas decimais.
4. O produto começa com saldo zero. Na entrega de movimentações, uma entrada registrará o recebimento com responsável e horário.
5. Para editar, use Editar na linha do produto. O formulário não possui campo de quantidade; mesmo um envio adulterado desse campo não muda o saldo.

O funcionário consulta, pesquisa e filtra produtos, mas não gerencia cadastros. As permissões são verificadas no servidor, incluindo acessos diretos às rotas.

## Desativação e histórico

Produtos e categorias são desativados, sem exclusão física. Uma categoria inativa não recebe novas associações, mas vínculos antigos permanecem. Produto ativo continua movimentável mesmo se sua categoria tiver sido desativada. Um produto inativo não pode ser movimentado.

O preço foi incluído conforme decisão do responsável pelo projeto. A coluna aceita ausência apenas para preservar registros antigos que ainda não têm preço conhecido; a interface mostra “Preço pendente”. Novo cadastro e edição exigem preenchimento. Não usamos zero como preço fictício de um produto antigo nem recalculamos o passado.

## Explicando em aula

`ProdutoForm` usa Lombok `@Data` e validações para os campos permitidos. Não é uma Entity e não possui saldo. O controller recebe esse formulário, o service verifica código/categoria e o repository persiste. A Entity possui métodos de negócio e getters, sem setter de quantidade. O saldo baixo inclui o limite exato: saldo 3 e mínimo 3 já geram alerta.

As consultas carregam a categoria dentro da transação com `join fetch`, pois `open-in-view=false`. Isso evita tentar consultar um relacionamento lazy depois que a transação já terminou. A edição bloqueia o mesmo registro usado por movimentações para não sobrescrever operações simultâneas.

No pgAdmin, atualize Tables e consulte `categorias` e `produtos` com View/Edit Data → All Rows. Prefira a interface para mudar dados e preservar validações/auditoria. Índices funcionais PostgreSQL protegem nomes de categorias e códigos sem diferenciar caixa. Corrija duplicidades antigas antes de reiniciar; o sistema não remove registros para criar índices.

## Verificação desta entrega

Java 17.0.17 e `mvn clean test`: 26 testes, zero falhas/erros. Incluem campos inválidos, duplicidades, adulteração de saldo e permissões. Chromium conectado ao PostgreSQL 16.4: cadastro/edição de categoria e produto, preço persistido, saldo zero, pesquisa e layouts desktop/móvel. Nenhum erro JavaScript observado.
