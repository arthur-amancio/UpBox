UBPaginas.configuracoes=function(u,m){
 var adm=UB.adm(u),e=UB.esc;
 var secs=[['perfil','perfil','Seu perfil'],['manual','manual','Manual']].concat(adm?[['usuarios','usuarios','Usuários'],['registros','relatorios','Registros de operações']]:[]);
 var sec='perfil';
 function campo(l,i,full){return '<div class="campo'+(full?' largura-total':'')+'"><label>'+l+'</label>'+i+'</div>'}
 m.innerHTML='<div class="pagina-cabecalho"><div><h2>Configurações</h2><p>Conta, manual e opções do sistema.</p></div></div><div class="cfg-layout"><nav class="cfg-menu">'+secs.map(function(s){return '<a data-s="'+s[0]+'">'+UB.ic(s[1])+s[2]+'</a>'}).join('')+'<a class="sair" id="bSair">'+UB.ic('logout')+'Sair</a></nav><div class="cfg-corpo" id="cp"></div></div>';
 document.getElementById('bSair').onclick=function(){UB.confirmar({msg:'Deseja encerrar a sessão?',ok:'Sair',fn:UB.sair})};
 m.querySelectorAll('[data-s]').forEach(function(a){a.onclick=function(){location.hash=a.dataset.s}});
 function abrir(){
  var h=location.hash.replace('#','')||'perfil';
  if(['usuarios','registros'].indexOf(h)>-1&&!adm){UB.toast(UB.MSG[5],'erro');h='perfil'}
  if(!secs.some(function(s){return s[0]===h}))h='perfil';
  sec=h;m.querySelectorAll('[data-s]').forEach(function(a){a.classList.toggle('ativo',a.dataset.s===sec)});
  ({perfil:perfil,manual:manual,usuarios:usuarios,registros:registros})[sec]();
 }
 function cp(){return document.getElementById('cp')}
 function msg(el,n,t){el.innerHTML='<div class="alerta alerta-'+(t||'erro')+'">'+UB.ic('alerta')+'<span>'+UB.MSG[n]+'</span></div>'}
 function perfil(){
  var db=UB.db(),me=db.usuarios.filter(function(x){return x.id===u.id})[0],t=UB.tema();
  cp().innerHTML='<div class="form-card"><div class="card-titulo">'+UB.ic('perfil')+' Dados pessoais</div><div id="m1"></div><div class="form-grid">'+campo('Nome completo','<input id="pN" value="'+e(me.nome)+'">',1)+campo('Nome de usuário','<input value="'+e(me.usuario)+'" disabled>')+campo('E-mail','<input id="pE" type="email" value="'+e(me.email)+'">')+campo('Perfil de acesso','<span class="badge badge-perfil">'+UB.PERFIL[me.perfil]+'</span>')+'</div><div class="form-acoes"><button class="btn btn-primario" id="sp">Salvar alterações</button></div></div>'+
  '<div class="form-card"><div class="card-titulo">'+UB.ic('config')+' Aparência</div><div class="temas"><label class="tema-op'+(t==='claro'?' sel':'')+'"><input type="radio" name="tm" value="claro"'+(t==='claro'?' checked':'')+'><span class="tema-amostra" style="background:#FAF7F2"></span>Tema claro</label><label class="tema-op'+(t==='escuro'?' sel':'')+'"><input type="radio" name="tm" value="escuro"'+(t==='escuro'?' checked':'')+'><span class="tema-amostra" style="background:#16100A"></span>Tema escuro</label></div></div>'+
  '<div class="form-card"><div class="card-titulo">'+UB.ic('senha')+' Alterar senha</div><div id="m2"></div><div class="form-grid">'+campo('Senha atual','<input id="s0" type="password">',1)+campo('Nova senha','<input id="s1" type="password">')+campo('Confirmar nova senha','<input id="s2" type="password">')+'</div><div class="form-acoes"><button class="btn btn-primario" id="ss">Alterar senha</button></div></div>';
  var $=function(i){return document.getElementById(i)};
  $('sp').onclick=function(){var n=$('pN').value.trim(),em=$('pE').value.trim();if(!n||!em)return msg($('m1'),3);
   if(db.usuarios.some(function(x){return x.id!==me.id&&x.email.toLowerCase()===em.toLowerCase()}))return msg($('m1'),8);
   me.nome=n;me.email=em;UB.salvar();UB.log('Perfil atualizado','Dados pessoais');UB.flash(UB.MSG[40]);location.reload()};
  m.querySelectorAll('[name=tm]').forEach(function(r){r.onchange=function(){UB.setTema(r.value);perfil()}});
  $('ss').onclick=function(){var a=$('s0').value,b=$('s1').value,c=$('s2').value;if(!a||!b||!c)return msg($('m2'),3);
   if(a!==me.senha)return msg($('m2'),15);if(b!==c)return msg($('m2'),13);
   me.senha=b;UB.salvar();UB.log('Senha alterada','');msg($('m2'),14,'sucesso');$('s0').value=$('s1').value=$('s2').value=''};
 }
 function manual(){
  cp().innerHTML='<p class="nota">Mesmo conteúdo do resumo exibido na Home, para o perfil '+UB.PERFIL[u.perfil]+'.</p>'+UB.MANUAL.filter(function(x){return x.perfis.indexOf(u.perfil)>-1}).map(function(x){return '<details class="manual-item"><summary>'+UB.ic(x.icone)+e(x.titulo)+(x.perfis.length===1?'<span class="tag-adm">Administrador</span>':'')+'</summary><p>'+e(x.texto)+'</p></details>'}).join('');
 }
 function usuarios(){
  var db=UB.db();
  cp().innerHTML='<div style="display:flex;justify-content:flex-end;margin-bottom:14px"><button class="btn btn-primario" id="nu">'+UB.ic('adicionar')+' Novo usuário</button></div><div class="tabela-wrapper"><table class="tabela"><thead><tr><th>Nome</th><th>Usuário</th><th>E-mail</th><th>Perfil</th><th>Situação</th><th>Ações</th></tr></thead><tbody>'+db.usuarios.map(function(x){return '<tr><td><b>'+e(x.nome)+'</b></td><td>'+e(x.usuario)+'</td><td>'+e(x.email)+'</td><td><span class="badge badge-perfil">'+UB.PERFIL[x.perfil]+'</span></td><td>'+(x.ativo?'<span class="badge badge-normal">Ativo</span>':'<span class="badge badge-neutro">Inativo</span>')+'</td><td><div class="acoes-tabela"><button class="btn-icone btn-icone-editar" title="Editar" data-ed="'+x.id+'">'+UB.ic('editar')+'</button>'+(x.id===u.id?'':'<button class="btn btn-secundario btn-sm" data-at="'+x.id+'">'+(x.ativo?'Desativar':'Ativar')+'</button>')+'</div></td></tr>'}).join('')+'</tbody></table></div>';
  document.getElementById('nu').onclick=function(){form()};
  var get=function(id){return UB.db().usuarios.filter(function(x){return x.id===+id})[0]};
  cp().querySelectorAll('[data-ed]').forEach(function(b){b.onclick=function(){form(get(b.dataset.ed))}});
  cp().querySelectorAll('[data-at]').forEach(function(b){b.onclick=function(){var x=get(b.dataset.at);
   function ok(){x.ativo=!x.ativo;UB.salvar();UB.log(x.ativo?'Usuário ativado':'Usuário desativado',x.nome);UB.toast(UB.MSG[x.ativo?12:11]);usuarios()}
   x.ativo?UB.confirmar({perigo:1,msg:UB.MSG[10],detalhe:x.nome,ok:'Desativar',fn:ok}):ok()}});
 }
 function form(x){
  var ed=!!x;x=x||{};
  var mo=UB.modal({largo:1,html:'<h3>'+(ed?'Editar usuário':'Novo usuário')+'</h3><div id="er"></div><div class="form-grid">'+campo('Nome completo *','<input id="uN" value="'+e(x.nome||'')+'">',1)+campo('Nome de usuário *','<input id="uU" value="'+e(x.usuario||'')+'">')+campo('E-mail *','<input id="uE" type="email" value="'+e(x.email||'')+'">')+campo('Perfil de acesso *','<select id="uP"><option value="FUNCIONARIO"'+(x.perfil==='FUNCIONARIO'?' selected':'')+'>Funcionário</option><option value="ADMINISTRADOR"'+(x.perfil==='ADMINISTRADOR'?' selected':'')+'>Administrador</option></select>')+(ed?'':campo('Senha inicial *','<input id="uS" type="password">'))+'</div><div class="form-acoes"><button class="btn btn-primario" id="sv">Salvar</button><button class="btn btn-secundario" data-fechar>Cancelar</button></div>'});
  var v=function(i){return mo.querySelector('#'+i).value.trim()};
  var er=function(n){msg(mo.querySelector('#er'),n)};
  mo.querySelector('#sv').onclick=function(){var d=UB.db(),n=v('uN'),us=v('uU'),em=v('uE');
   if(!n||!us||!em||(!ed&&!v('uS')))return er(3);
   if(d.usuarios.some(function(y){return y.id!==x.id&&(y.usuario.toLowerCase()===us.toLowerCase()||y.email.toLowerCase()===em.toLowerCase())}))return er(8);
   if(ed){var o=d.usuarios.filter(function(y){return y.id===x.id})[0];o.nome=n;o.usuario=us;o.email=em;o.perfil=v('uP');UB.log('Usuário atualizado',n);UB.toast(UB.MSG[9])}
   else{d.usuarios.push({id:UB.prox(d.usuarios),nome:n,usuario:us,email:em,perfil:v('uP'),ativo:true,senha:v('uS')});UB.log('Usuário cadastrado',n);UB.toast(UB.MSG[7])}
   UB.salvar();mo.fechar();usuarios()};
 }
 function registros(){
  var L=UB.db().log;
  cp().innerHTML='<p class="nota">Principais ações realizadas pelos usuários (as 80 mais recentes).</p><div class="tabela-wrapper"><table class="tabela"><thead><tr><th>Data/hora</th><th>Usuário</th><th>Ação</th><th>Detalhe</th></tr></thead><tbody>'+L.map(function(x){return '<tr><td>'+UB.fmt(x.dataHora)+'</td><td>'+e(x.usuario)+'</td><td><b>'+e(x.acao)+'</b></td><td>'+e(x.detalhe||'—')+'</td></tr>'}).join('')+'</tbody></table></div>';
 }
 window.addEventListener('hashchange',abrir);abrir();
};
