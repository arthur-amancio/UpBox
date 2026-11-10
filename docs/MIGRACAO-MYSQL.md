# PostgreSQL anterior e MySQL

Trocar a URL não converte dados. Nenhum banco da escola foi acessado ou apagado. A versão PostgreSQL e seus guias completos permanecem na tag backup/pre-mysql-2026-11-10. Mantenha uma instalação antiga separada se houver dados que ainda precisem de migração.

## Antes de mudar uma instalação utilizada

1. Identifique o servidor e faça backup completo, verificando a restauração em outro banco.
2. Trabalhe com cópia, nunca no banco principal. Confirme que tem autorização para transferir os dados.
3. Crie um MySQL vazio com InnoDB/utf8mb4_unicode_ci. Para preparar o esquema sem criar a conta padrão, use temporariamente `upbox.admin-inicial.habilitado=false` no mesmo application.properties.
4. Exporte **campos explícitos** das cinco tabelas: usuarios, categorias, produtos, movimentacoes_estoque e registros_operacoes. SQL de pg_dump não é executável diretamente no MySQL.
5. Preserve IDs, hashes BCrypt (não é preciso conhecer senhas), nomes de usuário, perfis, situação, códigos, preços BigDecimal, quantidades, horários e observações. Para usuários antigos, a nova coluna troca_senha_obrigatoria deve ser false: não exigir nova troca nem redefinir senhas existentes.
6. Importe em transação, na ordem usuarios/categorias, produtos, movimentacoes_estoque e registros_operacoes. Não desative chaves estrangeiras e não use comandos que ignorem registros duplicados.
7. Confira contagens, IDs, chaves estrangeiras, unicidade com a collation nova, total de saldos, valores monetários e último horário. Duplicações antigas exigem decisão da equipe; não descarte registros silenciosamente.
8. Confirme AUTO_INCREMENT acima do maior ID importado. Reative a inicialização normal: um administrador já existente, até inativo, impede criação/reset de outra conta.
9. Faça login com contas importadas, confira relatórios, entrada/saída e reinício. Somente troque o ambiente escolar depois dessa homologação e mantenha o backup original.

Os dados reais e a versão do servidor da escola não estão disponíveis nesta execução. Por isso, não declaramos migrada uma instalação real. A equipe deve informar o conjunto de dados para preparar e testar uma conversão específica em cópia antes de descontinuar seu PostgreSQL. Essa pendência não impede iniciar um banco MySQL novo para a demonstração.
