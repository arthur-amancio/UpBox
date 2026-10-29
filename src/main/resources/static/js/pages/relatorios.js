UBPaginas.relatorios=function(u,m){
 var adm=UB.adm(u),e=UB.esc,db=UB.db(),f={prod:'',cat:'',tipo:'',ini:'',fim:''};
 m.innerHTML='<div class="pagina-cabecalho"><div><h2>Relatórios</h2><p>Resumo dos produtos e das movimentações, com filtros.</p></div><div class="acoes-cabecalho">'+(adm?'<button class="btn btn-primario" id="imp">'+UB.ic('imprimir')+' Imprimir / Salvar em PDF</button>':'')+'</div></div>'+
 (adm?'':'<p class="nota">A impressão/exportação em PDF está disponível apenas para o administrador.</p>')+
 '<div class="barra-filtros"><select id="rP"><option value="">Todos os produtos</option>'+db.produtos.map(function(p){return '<option value="'+p.id+'">'+e(p.nome)+'</option>'}).join('')+'</select><select id="rC"><option value="">Todas as categorias</option>'+db.categorias.map(function(c){return '<option value="'+c.id+'">'+e(c.nome)+'</option>'}).join('')+'</select><select id="rT"><option value="">Todos os tipos de movimentação</option><option value="ENTRADA">Entrada</option><option value="SAIDA">Saída</option></select><input type="date" id="rI" title="Data inicial"><input type="date" id="rF" title="Data final"><button class="btn btn-secundario" id="rL">Limpar</button></div><div id="rel"></div>';
 function $(i){return document.getElementById(i)}
 function desenhar(){
  var d=UB.db(),P=d.produtos.filter(function(p){return(!f.prod||String(p.id)===f.prod)&&(!f.cat||String(p.categoriaId)===f.cat)});
  var M=d.movs.filter(function(x){var t=+new Date(x.dataHora);return(!f.prod||String(x.produtoId)===f.prod)&&(!f.cat||String(x.categoriaId)===f.cat)&&(!f.tipo||x.tipo===f.tipo)&&(!f.ini||t>=+new Date(f.ini+'T00:00:00'))&&(!f.fim||t<=+new Date(f.fim+'T23:59:59'))}).sort(function(a,b){return new Date(b.dataHora)-new Date(a.dataHora)});
  var soma=function(t){return M.filter(function(x){return x.tipo===t}).reduce(function(s,x){return s+x.quantidade},0)};
  function card(ic,cl,v,r){return '<div class="card card-stat"><div class="icone '+cl+'">'+UB.ic(ic)+'</div><div><div class="valor">'+v+'</div><div class="rotulo">'+r+'</div></div></div>'}
  var vazio=!P.length&&!M.length;
  $('rel').innerHTML='<div class="so-impressao"><b>UpBox — Relatório de estoque</b><br>Gerado em '+UB.fmt(new Date().toISOString())+' por '+e(u.nome)+' ('+UB.PERFIL[u.perfil]+'). Dados de demonstração.</div>'+
  (vazio?'<div class="alerta alerta-info">'+UB.ic('alerta')+'<span>'+UB.MSG[37]+'</span></div>':'')+
  '<div class="grid-cards">'+card('estoque','icone-azul',P.length,'Produtos listados')+card('alerta','icone-amarelo',P.filter(function(p){return UB.sit(p)==='BAIXO'}).length,'Com estoque baixo')+card('cima','icone-verde',soma('ENTRADA'),'Unidades de entrada')+card('baixo','icone-vermelho',soma('SAIDA'),'Unidades de saída')+'</div>'+
  '<div class="card" style="margin-bottom:22px"><div class="card-titulo">'+UB.ic('estoque')+' Produtos</div><div class="tabela-wrapper" style="box-shadow:none;border:none"><table class="tabela"><thead><tr><th>Código</th><th>Produto</th><th>Categoria</th><th>Quantidade</th><th>Mínima</th><th>Situação</th></tr></thead><tbody>'+(P.length?P.map(function(p){return '<tr><td>'+e(p.codigo)+'</td><td>'+e(p.nome)+'</td><td>'+e(UB.catNome(p.categoriaId))+'</td><td>'+p.quantidade+'</td><td>'+p.estoqueMinimo+'</td><td>'+UB.badgeSit(p)+'</td></tr>'}).join(''):'<tr><td colspan="6" class="tabela-vazia"><div class="estado-vazio"><strong>'+UB.MSG[27]+'</strong></div></td></tr>')+'</tbody></table></div></div>'+
  '<div class="card"><div class="card-titulo">'+UB.ic('movimentacoes')+' Movimentações</div><div class="tabela-wrapper" style="box-shadow:none;border:none"><table class="tabela"><thead><tr><th>Data/hora</th><th>Produto</th><th>Tipo</th><th>Quantidade</th><th>Responsável</th></tr></thead><tbody>'+(M.length?M.map(function(x){return '<tr><td>'+UB.fmt(x.dataHora)+'</td><td>'+e(x.produtoNome)+'</td><td>'+UB.badgeTipo(x.tipo)+'</td><td>'+x.quantidade+'</td><td>'+e(x.usuarioNome)+'</td></tr>'}).join(''):'<tr><td colspan="5" class="tabela-vazia"><div class="estado-vazio"><strong>'+UB.MSG[35]+'</strong></div></td></tr>')+'</tbody></table></div></div>';
 }
 [['rP','prod'],['rC','cat'],['rT','tipo'],['rI','ini'],['rF','fim']].forEach(function(x){$(x[0]).onchange=function(){f[x[1]]=this.value;desenhar()}});
 $('rL').onclick=function(){f={prod:'',cat:'',tipo:'',ini:'',fim:''};['rP','rC','rT','rI','rF'].forEach(function(i){$(i).value=''});desenhar()};
 if(adm)$('imp').onclick=function(){window.print();UB.log('Relatório exportado','Impressão / PDF');UB.toast(UB.MSG[38]+' (demonstração)')};
 desenhar();
};
