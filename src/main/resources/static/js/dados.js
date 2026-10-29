/* UpBox protótipo v2 - dados de DEMONSTRAÇÃO e utilitários (sem backend, sem autenticação real). */
(function(){
var CH='upbox_demo_v2',SES='upbox_sessao_v2',TM='upbox_tema_v2',FL='upbox_flash_v2',mem={},cache=null;
function arm(n){return{get:function(k){try{return window[n].getItem(k)}catch(e){return mem[n+k]||null}},set:function(k,v){try{window[n].setItem(k,v)}catch(e){mem[n+k]=v}},del:function(k){try{window[n].removeItem(k)}catch(e){delete mem[n+k]}}}}
var L=arm('localStorage'),S=arm('sessionStorage');
var UB=window.UB={};window.UBPaginas={};
/* Mensagens: Quadro 4 do TCC (Dicionário de Mensagens) */
UB.MSG={1:'Login realizado com sucesso.',2:'Usuário ou senha inválidos.',3:'Preencha todos os campos obrigatórios.',5:'Você não possui permissão para acessar esta funcionalidade.',6:'Logout realizado com sucesso.',7:'Usuário cadastrado com sucesso.',8:'Este e-mail ou nome de usuário já está cadastrado.',9:'Usuário atualizado com sucesso.',10:'Deseja realmente desativar este usuário?',11:'Usuário desativado com sucesso.',12:'Usuário ativado com sucesso.',13:'As senhas informadas não coincidem.',14:'Senha alterada com sucesso.',15:'A senha atual informada está incorreta.',16:'Categoria cadastrada com sucesso.',17:'Já existe uma categoria com esse nome.',18:'Categoria atualizada com sucesso.',19:'Deseja realmente excluir esta categoria?',20:'Não é possível excluir esta categoria, pois existem produtos vinculados a ela.',21:'Categoria excluída com sucesso.',22:'Produto cadastrado com sucesso.',23:'Já existe um produto cadastrado com esse código.',24:'Produto atualizado com sucesso.',25:'Deseja realmente excluir este produto?',26:'Produto excluído com sucesso.',27:'Nenhum produto foi encontrado.',28:'Entrada registrada com sucesso.',29:'Saída registrada com sucesso.',30:'A quantidade informada deve ser maior que zero.',31:'A quantidade solicitada é maior que a disponível no estoque.',32:'Não é permitido deixar a quantidade do produto negativa.',33:'Deseja realmente confirmar esta movimentação?',34:'Produto com estoque baixo. Verifique a necessidade de reposição.',35:'Nenhuma movimentação foi encontrada.',37:'Nenhum registro foi encontrado para gerar o relatório.',38:'Relatório exportado em PDF com sucesso.',40:'Perfil atualizado com sucesso.'};
/* Perfis: nomes dos requisitos/casos de uso do TCC (o dicionário de atributos cita ADMIN/USUARIO: divergência pendente) */
UB.PERFIL={ADMINISTRADOR:'Administrador',FUNCIONARIO:'Funcionário'};
UB.esc=function(s){return String(s==null?'':s).replace(/[&<>"']/g,function(c){return{'&':'&amp;','<':'&lt;','>':'&gt;','"':'&quot;',"'":'&#39;'}[c]})};
function seed(){
 var n=Date.now(),H=36e5;
 var cats=[[1,'Papelaria','Itens de escritório e escola'],[2,'Limpeza','Produtos de limpeza e higiene'],[3,'Bebidas','Bebidas em geral'],[4,'Alimentos','Alimentos não perecíveis']].map(function(c){return{id:c[0],nome:c[1],descricao:c[2]}});
 var pr=[['PRD-001','Caneta esferográfica azul',1,120,30],['PRD-002','Papel sulfite A4 (resma 500 fls)',1,18,20],['PRD-003','Caderno brochura 96 folhas',1,45,15],['PRD-004','Detergente neutro 500 ml',2,60,24],['PRD-005','Desinfetante 2 L',2,9,10],['PRD-006','Esponja multiuso',2,80,30],['PRD-007','Água mineral 500 ml',3,200,60],['PRD-008','Refrigerante lata 350 ml',3,14,24],['PRD-009','Suco de uva integral 1 L',3,36,12],['PRD-010','Café torrado 500 g',4,22,10],['PRD-011','Açúcar refinado 1 kg',4,40,15],['PRD-012','Biscoito cream cracker 400 g',4,8,12]].map(function(p,i){return{id:i+1,codigo:p[0],nome:p[1],descricao:'Item de demonstração',categoriaId:p[2],quantidade:p[3],estoqueMinimo:p[4]}});
 var us=[{id:1,nome:'Administrador (demo)',usuario:'administrador.demo',email:'administrador@demo.upbox',perfil:'ADMINISTRADOR',ativo:true,senha:'demo123'},{id:2,nome:'Funcionário 1 (demo)',usuario:'funcionario.demo',email:'funcionario@demo.upbox',perfil:'FUNCIONARIO',ativo:true,senha:'demo123'},{id:3,nome:'Funcionário 2 (demo)',usuario:'funcionario2.demo',email:'funcionario2@demo.upbox',perfil:'FUNCIONARIO',ativo:false,senha:'demo123'}];
 var mv=[[2,2,'SAIDA',6,2,'Uso no escritório'],[5,8,'SAIDA',10,2,''],[20,5,'SAIDA',4,2,'Limpeza semanal'],[28,1,'ENTRADA',60,1,'Reposição de estoque'],[46,7,'ENTRADA',120,1,'Compra mensal'],[52,12,'SAIDA',7,2,''],[70,10,'SAIDA',3,2,''],[75,4,'ENTRADA',30,1,'Reposição'],[98,9,'SAIDA',6,2,''],[120,3,'ENTRADA',25,1,''],[124,11,'SAIDA',8,2,''],[146,6,'ENTRADA',50,1,''],[170,2,'SAIDA',12,2,'Impressões do mês'],[190,8,'ENTRADA',24,1,'Reposição']].map(function(x,i){var p=pr[x[1]-1];return{id:i+1,produtoId:p.id,produtoNome:p.nome,produtoCodigo:p.codigo,categoriaId:p.categoriaId,tipo:x[2],quantidade:x[3],dataHora:new Date(n-x[0]*H).toISOString(),usuarioId:x[4],usuarioNome:us[x[4]-1].nome,observacao:x[5]}});
 var lg=[[3,1,'Login','Acesso ao sistema'],[28,1,'Movimentação registrada','Entrada de 60 un. — Caneta esferográfica azul'],[50,1,'Produto atualizado','PRD-005 — Desinfetante 2 L']].map(function(x){return{dataHora:new Date(n-x[0]*H).toISOString(),usuario:us[x[1]-1].nome,acao:x[2],detalhe:x[3]}});
 return{categorias:cats,produtos:pr,movs:mv,usuarios:us,log:lg};
}
UB.db=function(){if(!cache){var r=L.get(CH);try{cache=r?JSON.parse(r):null}catch(e){cache=null}if(!cache){cache=seed();L.set(CH,JSON.stringify(cache))}}return cache};
UB.salvar=function(){L.set(CH,JSON.stringify(cache))};
UB.resetar=function(){L.del(CH);cache=null};
UB.prox=function(a){return a.reduce(function(m,x){return Math.max(m,x.id)},0)+1};
UB.sessao=function(){var id=S.get(SES);if(!id)return null;var u=UB.db().usuarios.filter(function(x){return String(x.id)===id&&x.ativo})[0];return u||null};
UB.entrar=function(id){S.set(SES,String(id))};UB.sairSessao=function(){S.del(SES)};
UB.adm=function(u){return u&&u.perfil==='ADMINISTRADOR'};
UB.log=function(acao,det){var db=UB.db(),u=UB.sessao();db.log.unshift({dataHora:new Date().toISOString(),usuario:u?u.nome:'—',acao:acao,detalhe:det||''});db.log=db.log.slice(0,80);UB.salvar()};
UB.flash=function(msg,tipo){var a=[];try{a=JSON.parse(S.get(FL)||'[]')}catch(e){}a.push({msg:msg,tipo:tipo||'sucesso'});S.set(FL,JSON.stringify(a))};
UB.flashLer=function(){var a=[];try{a=JSON.parse(S.get(FL)||'[]')}catch(e){}S.del(FL);return a};
UB.tema=function(){return L.get(TM)||'claro'};
UB.setTema=function(t){L.set(TM,t);document.documentElement.setAttribute('data-tema',t)};
UB.setTema(UB.tema());
UB.sit=function(p){return p.quantidade<=p.estoqueMinimo?'BAIXO':'NORMAL'}; /* TCC: alerta quando quantidade <= mínima */
UB.catNome=function(id){var c=UB.db().categorias.filter(function(x){return x.id===id})[0];return c?c.nome:'—'};
UB.fmt=function(iso){var d=new Date(iso);return d.toLocaleDateString('pt-BR')+' '+d.toLocaleTimeString('pt-BR',{hour:'2-digit',minute:'2-digit'})};
UB.badgeSit=function(p){return UB.sit(p)==='BAIXO'?'<span class="badge badge-baixo">Estoque baixo</span>':'<span class="badge badge-normal">Normal</span>'};
UB.badgeTipo=function(t){return t==='ENTRADA'?'<span class="badge badge-entrada">Entrada</span>':'<span class="badge badge-saida">Saída</span>'};
/* Manual: ÚNICA fonte de conteúdo (Home e Configurações > Manual) */
var A='ADMINISTRADOR',F='FUNCIONARIO';
UB.MANUAL=[
['Entrar e sair do sistema','senha',[A,F],'Informe seu nome de usuário ou e-mail e sua senha na tela de login. Se os dados estiverem incorretos, o sistema avisa. Para encerrar a sessão, use Configurações > Sair. Páginas internas só abrem para usuários autenticados.'],
['Home','home',[A,F],'Página de boas-vindas com a explicação do UpBox, orientações para começar, o manual resumido e a apresentação da equipe.'],
['Dashboard','dashboard',[A,F],'Resumo do estoque: total de produtos, situação geral, produtos com estoque baixo, entradas e saídas recentes e gráficos calculados a partir dos registros.'],
['Consultar e pesquisar produtos','estoque',[A,F],'Em Estoque, pesquise produtos por nome ou código e filtre por categoria e situação (normal ou estoque baixo). Produtos com quantidade igual ou menor que a mínima aparecem como estoque baixo.'],
['Cadastrar, editar e excluir produtos','adicionar',[A],'Em Estoque, use "Cadastrar produto" e informe nome, código, descrição, categoria, quantidade e quantidade mínima. A quantidade de um produto já cadastrado só muda por entradas e saídas.'],
['Categorias','estoque',[A],'Em Estoque > Categorias, cadastre, edite e exclua categorias. Uma categoria com produtos vinculados não pode ser excluída.'],
['Registrar entradas e saídas','movimentacoes',[A,F],'Em Movimentações, escolha o produto, o tipo e a quantidade. A entrada aumenta o estoque; a saída reduz. Não é possível retirar mais do que o disponível nem deixar o estoque negativo.'],
['Histórico de movimentações','movimentacoes',[A,F],'Ainda em Movimentações, consulte o histórico com data, hora, produto, tipo, quantidade e usuário responsável, filtrando por produto, tipo, usuário e período.'],
['Alertas de estoque baixo','alerta',[A,F],'Produtos que atingem ou ficam abaixo da quantidade mínima são destacados no Dashboard e na lista de Estoque.'],
['Relatórios','relatorios',[A,F],'Consulte o resumo dos produtos e das movimentações com filtros por produto, categoria, tipo de movimentação e período.'],
['Imprimir / Salvar em PDF','imprimir',[A],'No relatório, o administrador pode usar "Imprimir / Salvar em PDF" e escolher salvar como PDF na janela de impressão.'],
['Seu perfil, senha e tema','perfil',[A,F],'Em Configurações > Seu perfil, atualize seus dados, altere a senha e escolha entre tema claro e escuro.'],
['Usuários e perfis de acesso','usuarios',[A],'Em Configurações > Usuários, o administrador cadastra, consulta, edita, ativa e desativa usuários e define se o perfil é Administrador ou Funcionário.'],
['Registros de operações','relatorios',[A],'Em Configurações > Registros de operações, o administrador consulta as principais ações realizadas pelos usuários.']
].map(function(x){return{titulo:x[0],icone:x[1],perfis:x[2],texto:x[3]}});
})();
