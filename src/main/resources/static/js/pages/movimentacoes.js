UBPaginas.movimentacoes=function(u,m){
 var e=UB.esc,tipo='ENTRADA',qs=new URLSearchParams(location.search),f={prod:qs.get('produto')||'',tipo:'',usu:'',ini:'',fim:''};
 function opts(sel,fn){return UB.db().produtos.map(function(p){return '<option value="'+p.id+'"'+(String(p.id)===String(sel)?' selected':'')+'>'+e(fn(p))+'</option>'}).join('')}
 m.innerHTML='<div class="pagina-cabecalho"><div><h2>Movimentações</h2><p>Registre entradas e saídas e consulte o histórico completo.</p></div></div>'+
 '<div class="card" style="margin-bottom:22px"><div class="card-titulo">'+UB.ic('adicionar')+' Registrar movimentação</div><div class="tipo-sel"><label class="tipo-op e sel" id="oE"><input type="radio" name="tp" value="ENTRADA" checked>'+UB.ic('cima')+'<span><strong>Entrada</strong><small>Aumenta a quantidade em estoque</small></span></label><label class="tipo-op s" id="oS"><input type="radio" name="tp" value="SAIDA">'+UB.ic('baixo')+'<span><strong>Saída</strong><small>Reduz a quantidade disponível</small></span></label></div><div id="er"></div><div class="form-grid"><div class="campo"><label>Produto *</label><select id="mP"><option value="">Selecione um produto</option>'+opts('',function(p){return p.codigo+' — '+p.nome+' (disponível: '+p.quantidade+')'})+'</select></div><div class="campo"><label>Quantidade *</label><input id="mQ" type="number" min="1" step="1"></div><div class="campo largura-total"><label>Observação</label><input id="mO" placeholder="Opcional"></div></div><div class="previa" id="pv">Selecione um produto para ver o saldo.</div><div class="form-acoes"><button class="btn btn-primario" id="reg">Registrar entrada</button></div></div>'+
 '<div class="card-titulo" style="margin:6px 0 12px">'+UB.ic('relatorios')+' Histórico de movimentações</div><div class="barra-filtros"><select id="hP"><option value="">Todos os produtos</option>'+opts(f.prod,function(p){return p.nome})+'</select><select id="hT"><option value="">Todos os tipos</option><option value="ENTRADA">Entrada</option><option value="SAIDA">Saída</option></select><select id="hU"><option value="">Todos os usuários</option>'+UB.db().usuarios.map(function(x){return '<option value="'+x.id+'">'+e(x.nome)+'</option>'}).join('')+'</select><input type="date" id="hI" title="Data inicial"><input type="date" id="hF" title="Data final"><button class="btn btn-secundario" id="hL">Limpar</button></div><div id="hist"></div>';
 function $(i){return document.getElementById(i)}
 function prod(){var v=$('mP').value;return v?UB.db().produtos.filter(function(p){return p.id===+v})[0]:null}
 function previa(){var p=prod(),q=parseInt($('mQ').value,10)||0;if(!p){$('pv').innerHTML='Selecione um produto para ver o saldo.';return}
  var n=tipo==='ENTRADA'?p.quantidade+q:p.quantidade-q;$('pv').innerHTML='Saldo atual: <b>'+p.quantidade+'</b> &rarr; após a '+(tipo==='ENTRADA'?'entrada':'saída')+': <b>'+n+'</b>'}
 function setTipo(t){tipo=t;$('oE').classList.toggle('sel',t==='ENTRADA');$('oS').classList.toggle('sel',t==='SAIDA');$('reg').textContent='Registrar '+(t==='ENTRADA'?'entrada':'saída');previa()}
 $('oE').onclick=function(){setTipo('ENTRADA')};$('oS').onclick=function(){setTipo('SAIDA')};$('mP').onchange=previa;$('mQ').oninput=previa;
 function er(n){$('er').innerHTML=n?'<div class="alerta alerta-erro">'+UB.ic('alerta')+'<span>'+UB.MSG[n]+'</span></div>':''}
 $('reg').onclick=function(){er(0);var p=prod(),q=$('mQ').value.trim();
  if(!p||q==='')return er(3);
  var n=Number(q);if(!(n>0)||n%1)return er(30);
  if(tipo==='SAIDA'&&n>p.quantidade)return er(31);
  UB.confirmar({msg:UB.MSG[33],detalhe:(tipo==='ENTRADA'?'Entrada':'Saída')+' de '+n+' un. — '+p.nome,ok:'Confirmar',fn:function(){
   var d=UB.db(),o=d.produtos.filter(function(x){return x.id===p.id})[0];
   o.quantidade+=tipo==='ENTRADA'?n:-n;
   d.movs.push({id:UB.prox(d.movs),produtoId:o.id,produtoNome:o.nome,produtoCodigo:o.codigo,categoriaId:o.categoriaId,tipo:tipo,quantidade:n,dataHora:new Date().toISOString(),usuarioId:u.id,usuarioNome:u.nome,observacao:$('mO').value.trim()});
   UB.salvar();UB.log('Movimentação registrada',(tipo==='ENTRADA'?'Entrada':'Saída')+' de '+n+' un. — '+o.nome);
   UB.toast(UB.MSG[tipo==='ENTRADA'?28:29]);if(UB.sit(o)==='BAIXO')UB.toast(o.nome+': '+UB.MSG[34],'alerta');
   $('mQ').value='';$('mO').value='';$('mP').innerHTML='<option value="">Selecione um produto</option>'+opts(o.id,function(x){return x.codigo+' — '+x.nome+' (disponível: '+x.quantidade+')'});previa();hist()}})};
 function hist(){
  var L=UB.db().movs.filter(function(x){var t=+new Date(x.dataHora);return(!f.prod||String(x.produtoId)===f.prod)&&(!f.tipo||x.tipo===f.tipo)&&(!f.usu||String(x.usuarioId)===f.usu)&&(!f.ini||t>=+new Date(f.ini+'T00:00:00'))&&(!f.fim||t<=+new Date(f.fim+'T23:59:59'))}).sort(function(a,b){return new Date(b.dataHora)-new Date(a.dataHora)});
  $('hist').innerHTML='<div class="tabela-wrapper"><table class="tabela"><thead><tr><th>Data/hora</th><th>Produto</th><th>Tipo</th><th>Quantidade</th><th>Usuário responsável</th><th>Observação</th></tr></thead><tbody>'+(L.length?L.map(function(x){return '<tr><td>'+UB.fmt(x.dataHora)+'</td><td>'+e(x.produtoNome)+'</td><td>'+UB.badgeTipo(x.tipo)+'</td><td>'+x.quantidade+'</td><td>'+e(x.usuarioNome)+'</td><td>'+e(x.observacao||'—')+'</td></tr>'}).join(''):'<tr><td colspan="6" class="tabela-vazia"><div class="estado-vazio">'+UB.ic('movimentacoes')+'<strong>'+UB.MSG[35]+'</strong></div></td></tr>')+'</tbody></table></div>'}
 [['hP','prod'],['hT','tipo'],['hU','usu'],['hI','ini'],['hF','fim']].forEach(function(x){$(x[0]).onchange=function(){f[x[1]]=this.value;hist()}});
 $('hL').onclick=function(){f={prod:'',tipo:'',usu:'',ini:'',fim:''};['hP','hT','hU','hI','hF'].forEach(function(i){$(i).value=''});hist()};
 hist();
};
