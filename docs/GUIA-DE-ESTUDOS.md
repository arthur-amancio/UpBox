# Guia de estudos — entendendo o código do UpBox

## Onde fica cada parte?

- \`src/main/java/com/upbox/UpboxApplication.java\`: inicia o servidor Spring Boot.
- \`src/main/java/com/upbox/controller/PaginaController.java\`: recebe endereços como \`/estoque\` e escolhe a página HTML.
- \`src/main/resources/templates/\`: telas HTML usando Thymeleaf.
- \`src/main/resources/static/css/\`: aparência visual.
- \`src/main/resources/static/js/\`: **atualmente** dados e comportamentos demonstrativos.
- \`src/main/java/com/upbox/model/Produto.java\`: primeiro exemplo das regras de estoque escritas em Java. Ainda não está conectado às páginas nem ao banco.
- \`src/test/\`: testes para verificar o comportamento esperado.

## Conceitos para estudar em sequência

1. **Classe:** a receita para construir um objeto, como \`Produto\`.
2. **Atributo:** informação guardada no objeto, como \`quantidade\`.
3. **Construtor:** prepara um novo objeto e valida seus valores iniciais.
4. **Método:** comportamento do objeto, como \`registrarSaida\`.
5. **Encapsulamento:** a tela não pode alterar o saldo diretamente; chama um método com validação.
6. **Controller:** recebe a requisição do navegador e encaminha o trabalho.
7. **Testes:** descrevem situações que devem funcionar e situações que devem ser recusadas.
8. **Repository/JPA:** serão introduzidos quando o grupo definir o banco de dados.

## Exercício prático: uma saída de estoque

Suponha um produto com 10 unidades e mínimo de 3.

- Registrar saída de 4: saldo final = 6; situação normal.
- Registrar saída de 3: saldo final = 3; estoque baixo.
- Registrar saída de 4: operação recusada porque só existem 3 unidades.
- Registrar saída de 0: operação recusada porque a quantidade precisa ser positiva.

Essas situações fazem parte dos testes em \`ProdutoTest.java\`.

## Rodando o projeto

Com Java 17 e Maven instalados, abra a raiz do projeto:

\`\`\`sh
mvn spring-boot:run
\`\`\`

Acesse \`http://localhost:8080\`. **A interface atual ainda é demonstrativa**.

Para executar os testes:

\`\`\`sh
mvn test
\`\`\`

O objetivo é que cada integrante consiga responder: o que este método faz, por que a validação existe, como demonstrar o erro e como o teste comprova a regra.
