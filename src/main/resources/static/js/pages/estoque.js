UBPaginas.estoque=function(u,m){
 var adm=UB.adm(u),aba='produtos',f={q:'',cat:'',sit:''},e=UB.esc;
 if(new URLSearchParams(location.search).get('situacao')==='BAIXO')f.sit='BAIXO';
 function campo(l,i,full){return '<div class="campo'+(full?' largura-total':'')+'"><label>'+l+'</label>'+i+'</div>'}
 function render(){
  m.innerHTML='<div class="pagina-cabecalho"><div><h2>Estoque</h2><p>'+(adm?'Cadastre, edite e acompanhe produtos e categorias.':'Consulte e pesquise os produtos cadastrados.')+'</p></div><div class="acoes-cabecalho">'+(adm?'<button class="btn btn-primario" id="novo">'+UB.ic('adicionar')+(aba==='produtos'?' Cadastrar produto':' Nova categoria')+'</button>':'')+'</div></div>'+
  (adm?'<div class="abas"><button class="aba'+(aba==='produtos'?' ativa':'')+'" data-aba="produtos">Produtos</button><button class="aba'+(aba==='categorias'?' ativa':'')+'" data-aba="categorias">Categorias</button></div>':'')+'<div id="aba"></div>';
  m.querySelectorAll('[data-aba]').forEach(function(b){b.onclick=function(){aba=b.dataset.aba;render()}});
  var n=document.getElementById('novo');if(n)n.onclick=function(){aba==='produtos'?formProd():formCat()};
  aba==='produtos'?abaProd():abaCat();
 }
 function abaProd(){
  var db=UB.db(),nb=db.produtos.filter(function(p){return UB.sit(p)==='BAIXO'}).length,a=document.getElementById('aba');
  a.innerHTML=(nb?'<div class="alerta alerta-alerta">'+UB.ic('alerta')+'<span><b>'+nb+'</b> produto(s) com estoque baixo. Verifique a necessidade de reposição.</span></div>':'')+
  '<div class="barra-filtros"><div class="campo-pesquisa">'+UB.ic('busca')+'<input id="fq" placeholder="Pesquisar por nome ou código..." value="'+e(f.q)+'"></div><select id="fc"><option value="">Todas as categorias</option>'+db.categorias.map(function(c){return '<option value="'+c.id+'"'+(String(c.id)===f.cat?' selected':'')+'>'+e(c.nome)+'</option>'}).join('')+'</select><select id="fs"><option value="">Todas as situações</option><option value="NORMAL"'+(f.sit==='NORMAL'?' selected':'')+'>Normal</option><option value="BAIXO"'+(f.sit==='BAIXO'?' selected':'')+'>Estoque baixo</option></select><button class="btn btn-secundario" id="lim">Limpar</button></div><div id="tb"></div>';
  document.getElementById('fq').oninput=function(){f.q=this.value;lista()};document.getElementById('fc').onchange=function(){f.cat=this.value;lista()};document.getElementById('fs').onchange=function(){f.sit=this.value;lista()};
  document.getElementById('lim').onclick=function(){f={q:'',cat:'',sit:''};abaProd()};
  lista();
 }
 function lista(){
  var db=UB.db(),q=f.q.trim().toLowerCase();
  var L=db.produtos.filter(function(p){return(!q||p.nome.toLowerCase().indexOf(q)>-1||p.codigo.toLowerCase().indexOf(q)>-1)&&(!f.cat||String(p.categoriaId)===f.cat)&&(!f.sit||UB.sit(p)===f.sit)});
  var h='<div class="tabela-wrapper"><table class="tabela"><thead><tr><th>Código</th><th>Produto</th><th>Categoria</th><th>Quantidade</th><th>Mínima</th><th>Situação</th><th>Ações</th></tr></thead><tbody>';
  if(!L.length)h+='<tr><td colspan="7" class="tabela-vazia"><div class="estado-vazio">'+UB.ic('estoque')+'<strong>'+UB.MSG[27]+'</strong><span>Ajuste a pesquisa ou os filtros.</span></div></td></tr>';
  L.forEach(function(p){h+='<tr><td>'+e(p.codigo)+'</td><td><div class="celula-produto"><span class="nome-produto">'+e(p.nome)+'</span><span class="desc-produto">'+e(p.descricao)+'</span></div></td><td>'+e(UB.catNome(p.categoriaId))+'</td><td><b>'+p.quantidade+'</b></td><td>'+p.estoqueMinimo+'</td><td>'+UB.badgeSit(p)+'</td><td><div class="acoes-tabela"><button class="btn-icone btn-icone-ver" title="Visualizar" data-ver="'+p.id+'">'+UB.ic('ver')+'</button>'+(adm?'<button class="btn-icone btn-icone-editar" title="Editar" data-ed="'+p.id+'">'+UB.ic('editar')+'</button><button class="btn-icone btn-icone-excluir" title="Excluir" data-ex="'+p.id+'">'+UB.ic('excluir')+'</button>':'')+'</div></td></tr>'});
  var t=document.getElementById('tb');t.innerHTML=h+'</tbody></table></div>';
  function get(id){return UB.db().produtos.filter(function(p){return p.id===+id})[0]}
  t.querySelectorAll('[data-ver]').forEach(function(b){b.onclick=function(){ver(get(b.dataset.ver))}});
  t.querySelectorAll('[data-ed]').forEach(function(b){b.onclick=function(){formProd(get(b.dataset.ed))}});
  t.querySelectorAll('[data-ex]').forEach(function(b){b.onclick=function(){var p=get(b.dataset.ex);UB.confirmar({perigo:1,msg:UB.MSG[25],detalhe:p.codigo+' — '+p.nome,ok:'Sim, excluir',fn:function(){var d=UB.db();d.produtos=d.produtos.filter(function(x){return x.id!==p.id});UB.salvar();UB.log('Produto excluído',p.codigo+' — '+p.nome);UB.toast(UB.MSG[26]);abaProd()}})}});
 }
 function ver(p){
  var mo=UB.modal({largo:1,html:'<h3>'+e(p.nome)+'</h3><div class="info-grid"><div class="info-item"><label>Código</label><p>'+e(p.codigo)+'</p></div><div class="info-item"><label>Categoria</label><p>'+e(UB.catNome(p.categoriaId))+'</p></div><div class="info-item largura-total"><label>Descrição</label><p>'+e(p.descricao||'—')+'</p></div><div class="info-item"><label>Quantidade disponível</label><p>'+p.quantidade+'</p></div><div class="info-item"><label>Quantidade mínima</label><p>'+p.estoqueMinimo+'</p></div><div class="info-item"><label>Situação</label><p>'+UB.badgeSit(p)+'</p></div></div><div class="form-acoes"><a class="btn btn-primario" href="/movimentacoes?produto='+p.id+'">Ver movimentações</a><button class="btn btn-secundario" data-fechar>Fechar</button></div>'});
 }
 function formProd(p){
  var db=UB.db(),ed=!!p;p=p||{};
  var mo=UB.modal({largo:1,html:'<h3>'+(ed?'Editar produto':'Cadastrar produto')+'</h3><div id="er"></div><div class="form-grid">'+campo('Código *','<input id="fCod" value="'+e(p.codigo||'')+'" placeholder="Ex.: PRD-013">')+campo('Nome *','<input id="fNome" value="'+e(p.nome||'')+'">')+campo('Descrição','<textarea id="fDesc" rows="2">'+e(p.descricao||'')+'</textarea>',1)+campo('Categoria *','<select id="fCat"><option value="">Selecione</option>'+db.categorias.map(function(c){return '<option value="'+c.id+'"'+(c.id===p.categoriaId?' selected':'')+'>'+e(c.nome)+'</option>'}).join('')+'</select>')+campo('Quantidade disponível *','<input id="fQtd" type="number" min="0" value="'+(ed?p.quantidade:'')+'"'+(ed?' disabled':'')+'>'+(ed?'<small class="texto-suave">Alterada apenas por entradas e saídas.</small>':''))+campo('Quantidade mínima *','<input id="fMin" type="number" min="0" value="'+(ed?p.estoqueMinimo:'')+'">')+'</div><div class="form-acoes"><button class="btn btn-primario" id="sv">Salvar</button><button class="btn btn-secundario" data-fechar>Cancelar</button></div>'});
  function er(n){mo.querySelector('#er').innerHTML='<div class="alerta alerta-erro">'+UB.ic('alerta')+'<span>'+UB.MSG[n]+'</span></div>'}
  function v(i){return mo.querySelector('#'+i).value.trim()}
  mo.querySelector('#sv').onclick=function(){
   var cod=v('fCod'),nome=v('fNome'),cat=v('fCat'),qt=ed?String(p.quantidade):v('fQtd'),mn=v('fMin');
   if(!cod||!nome||!cat||qt===''||mn==='')return er(3);
   if(+qt<0||+mn<0)return er(32);
   var d=UB.db();if(d.produtos.some(function(x){return x.codigo.toLowerCase()===cod.toLowerCase()&&x.id!==p.id}))return er(23);
   if(ed){var o=d.produtos.filter(function(x){return x.id===p.id})[0];o.codigo=cod;o.nome=nome;o.descricao=v('fDesc');o.categoriaId=+cat;o.estoqueMinimo=+mn;UB.log('Produto atualizado',cod+' — '+nome);UB.toast(UB.MSG[24])}
   else{d.produtos.push({id:UB.prox(d.produtos),codigo:cod,nome:nome,descricao:v('fDesc'),categoriaId:+cat,quantidade:+qt,estoqueMinimo:+mn});UB.log('Produto cadastrado',cod+' — '+nome);UB.toast(UB.MSG[22])}
   UB.salvar();mo.fechar();abaProd();
  };
 }
 function abaCat(){
  var db=UB.db(),a=document.getElementById('aba');
  a.innerHTML='<div class="tabela-wrapper"><table class="tabela"><thead><tr><th>Categoria</th><th>Descrição</th><th>Produtos</th><th>Ações</th></tr></thead><tbody>'+db.categorias.map(function(c){var n=db.produtos.filter(function(p){return p.categoriaId===c.id}).length;return '<tr><td><b>'+e(c.nome)+'</b></td><td>'+e(c.descricao||'—')+'</td><td>'+n+'</td><td><div class="acoes-tabela"><button class="btn-icone btn-icone-editar" title="Editar" data-ec="'+c.id+'">'+UB.ic('editar')+'</button><button class="btn-icone btn-icone-excluir" title="Excluir" data-xc="'+c.id+'">'+UB.ic('excluir')+'</button></div></td></tr>'}).join('')+'</tbody></table></div>';
  function get(id){return UB.db().categorias.filter(function(c){return c.id===+id})[0]}
  a.querySelectorAll('[data-ec]').forEach(function(b){b.onclick=function(){formCat(get(b.dataset.ec))}});
  a.querySelectorAll('[data-xc]').forEach(function(b){b.onclick=function(){var c=get(b.dataset.xc),d=UB.db();
   if(d.produtos.some(function(p){return p.categoriaId===c.id}))return UB.toast(UB.MSG[20],'erro');
   UB.confirmar({perigo:1,msg:UB.MSG[19],detalhe:c.nome,ok:'Sim, excluir',fn:function(){d.categorias=d.categorias.filter(function(x){return x.id!==c.id});UB.salvar();UB.log('Categoria excluída',c.nome);UB.toast(UB.MSG[21]);abaCat()}})}});
 }
 function formCat(c){
  var ed=!!c;c=c||{};
  var mo=UB.modal({largo:1,html:'<h3>'+(ed?'Editar categoria':'Nova categoria')+'</h3><div id="er"></div><div class="form-grid">'+campo('Nome *','<input id="cN" value="'+e(c.nome||'')+'">',1)+campo('Descrição','<textarea id="cD" rows="2">'+e(c.descricao||'')+'</textarea>',1)+'</div><div class="form-acoes"><button class="btn btn-primario" id="sv">Salvar</button><button class="btn btn-secundario" data-fechar>Cancelar</button></div>'});
  function er(n){mo.querySelector('#er').innerHTML='<div class="alerta alerta-erro">'+UB.ic('alerta')+'<span>'+UB.MSG[n]+'</span></div>'}
  mo.querySelector('#sv').onclick=function(){
   var n=mo.querySelector('#cN').value.trim(),ds=mo.querySelector('#cD').value.trim(),d=UB.db();
   if(!n)return er(3);
   if(d.categorias.some(function(x){return x.nome.toLowerCase()===n.toLowerCase()&&x.id!==c.id}))return er(17);
   if(ed){var o=d.categorias.filter(function(x){return x.id===c.id})[0];o.nome=n;o.descricao=ds;UB.log('Categoria atualizada',n);UB.toast(UB.MSG[18])}
   else{d.categorias.push({id:UB.prox(d.categorias),nome:n,descricao:ds});UB.log('Categoria cadastrada',n);UB.toast(UB.MSG[16])}
   UB.salvar();mo.fechar();abaCat();
  };
 }
 render();
};
