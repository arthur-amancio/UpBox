# Credenciais e histórico

A senha compartilhada da escola anteriormente incluída no PR #11 foi removida dos arquivos atuais. Cada integrante deve preencher a conexão somente no próprio computador. Por pedido posterior expresso do responsável, a conta administrador recebe senha inicial admin, gravada com BCrypt e com troca obrigatória antes de acessar módulos. Isso é somente para a instalação escolar local; reiniciar não redefine senhas.

**Remover do arquivo atual não remove do histórico Git.** O repositório deve continuar privado até o responsável tratar a credencial antiga: trocar a senha no servidor, verificar reutilização e revisar commits, branches, cópias e artefatos antes de publicar o código. A equipe precisa autorizar separadamente qualquer limpeza do histórico; esta entrega não executa rebase, force push ou reescrita de commits.

Não reutilize a credencial antiga em outro ambiente. Não publique o application.properties preenchido nem um ZIP/JAR que a contenha. Confira que a configuração está sem senha antes de gerar qualquer pacote distribuível.
