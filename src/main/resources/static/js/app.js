/* UpBox protótipo v2 - interface comum: ícones, toasts, modais e estrutura (menu lateral + cabeçalho) */
(function(){
var P='<path d="',I={
home:P+'M3 10.5 12 3l9 7.5"/><path d="M5 9.5V20a1 1 0 0 0 1 1h3v-6h6v6h3a1 1 0 0 0 1-1V9.5"/>',
dashboard:'<rect x="3" y="3" width="7" height="9" rx="1.5"/><rect x="14" y="3" width="7" height="5" rx="1.5"/><rect x="14" y="12" width="7" height="9" rx="1.5"/><rect x="3" y="16" width="7" height="5" rx="1.5"/>',
estoque:P+'M12 3 20 7.5 12 12 4 7.5Z"/><path d="M4 7.5v9L12 21l8-4.5v-9"/><path d="M12 12v9"/>',
movimentacoes:P+'M3 7h15"/><polyline points="14 3 18 7 14 11"/><path d="M21 17H6"/><polyline points="10 13 6 17 10 21"/>',
usuarios:'<circle cx="9" cy="8" r="3.2"/><path d="M3.5 20c0-3.6 2.9-6 5.5-6s5.5 2.4 5.5 6"/><path d="M16 8.2a3 3 0 1 1 0 5.8"/><path d="M21 20c0-2.8-1.8-5-4-5.7"/>',
relatorios:P+'M7 3h7l4 4v13a1 1 0 0 1-1 1H7a1 1 0 0 1-1-1V4a1 1 0 0 1 1-1Z"/><path d="M14 3v4h4"/><path d="M9 13h6"/><path d="M9 17h6"/><path d="M9 9h2"/>',
manual:P+'M12 6.5c-1.5-1.3-3.6-2-6.5-2A1 1 0 0 0 4.5 5.5v12A1 1 0 0 0 5.5 18.5c2.9 0 5 .7 6.5 2c1.5-1.3 3.6-2 6.5-2a1 1 0 0 0 1-1v-12a1 1 0 0 0-1-1c-2.9 0-5 .7-6.5 2Z"/><path d="M12 6.5v14"/>',
perfil:'<circle cx="12" cy="8" r="4"/><path d="M4 21c0-4 3.6-6.5 8-6.5s8 2.5 8 6.5"/>',
config:'<circle cx="12" cy="12" r="3"/><path d="M19.4 15a1.7 1.7 0 0 0 .3 1.8l.1.1a2 2 0 1 1-2.8 2.8l-.1-.1a1.7 1.7 0 0 0-1.8-.3 1.7 1.7 0 0 0-1 1.5V21a2 2 0 1 1-4 0v-.1a1.7 1.7 0 0 0-1.1-1.5 1.7 1.7 0 0 0-1.8.3l-.1.1a2 2 0 1 1-2.8-2.8l.1-.1a1.7 1.7 0 0 0 .3-1.8 1.7 1.7 0 0 0-1.5-1H3a2 2 0 1 1 0-4h.1a1.7 1.7 0 0 0 1.5-1.1 1.7 1.7 0 0 0-.3-1.8l-.1-.1a2 2 0 1 1 2.8-2.8l.1.1a1.7 1.7 0 0 0 1.8.3H9a1.7 1.7 0 0 0 1-1.5V3a2 2 0 1 1 4 0v.1a1.7 1.7 0 0 0 1 1.5 1.7 1.7 0 0 0 1.8-.3l.1-.1a2 2 0 1 1 2.8 2.8l-.1.1a1.7 1.7 0 0 0-.3 1.8V9a1.7 1.7 0 0 0 1.5 1H21a2 2 0 1 1 0 4h-.1a1.7 1.7 0 0 0-1.5 1Z"/>',
logout:P+'M9 21H6a2 2 0 0 1-2-2V5a2 2 0 0 1 2-2h3"/><polyline points="15 17 20 12 15 7"/><line x1="20" y1="12" x2="8" y2="12"/>',
ver:P+'M2 12s3.6-6.5 10-6.5S22 12 22 12s-3.6 6.5-10 6.5S2 12 2 12Z"/><circle cx="12" cy="12" r="2.8"/>',
editar:P+'M4 16.5V20h3.5L18.4 9.1l-3.5-3.5L4 16.5Z"/><path d="M14.5 4.5l3.5 3.5"/>',
excluir:P+'M4 7h16"/><path d="M9 7V4.5A1.5 1.5 0 0 1 10.5 3h3A1.5 1.5 0 0 1 15 4.5V7"/><path d="M6 7l1 12.5A1.5 1.5 0 0 0 8.5 21h7a1.5 1.5 0 0 0 1.5-1.5L18 7"/>',
adicionar:'<line x1="12" y1="5" x2="12" y2="19"/><line x1="5" y1="12" x2="19" y2="12"/>',
busca:'<circle cx="11" cy="11" r="7"/><line x1="21" y1="21" x2="16" y2="16"/>',
alerta:P+'M12 3 22 20H2Z"/><line x1="12" y1="9.5" x2="12" y2="14"/><circle cx="12" cy="17" r="0.9" fill="currentColor" stroke="none"/>',
sucesso:'<circle cx="12" cy="12" r="9"/><polyline points="8 12.5 11 15.5 16 9"/>',
imprimir:'<polyline points="6 8 6 3 18 3 18 8"/><rect x="4" y="8" width="16" height="8" rx="1.5"/><rect x="7" y="14" width="10" height="7"/>',
senha:'<rect x="5" y="11" width="14" height="9" rx="2"/><path d="M8 11V7a4 4 0 0 1 8 0v4"/>',
cima:'<line x1="12" y1="19" x2="12" y2="5"/><polyline points="6 11 12 5 18 11"/>',
baixo:'<line x1="12" y1="5" x2="12" y2="19"/><polyline points="6 13 12 19 18 13"/>',
foto:P+'M4 8h3l2-3h6l2 3h3v11H4Z"/><circle cx="12" cy="13" r="3.5"/>'};
UB.ic=function(n,c){return '<span class="ic-svg '+(c||'')+'"><svg viewBox="0 0 24 24">'+(I[n]||I.alerta)+'</svg></span>'};
UB.toast=function(msg,tipo){var b=document.getElementById('toasts');if(!b){b=document.createElement('div');b.id='toasts';document.body.appendChild(b)}var t=document.createElement('div');t.className='toast toast-'+(tipo||'sucesso');t.innerHTML=UB.ic(tipo==='erro'||tipo==='alerta'?'alerta':'sucesso')+'<span>'+UB.esc(msg)+'</span>';b.appendChild(t);setTimeout(function(){t.classList.add('sai');setTimeout(function(){t.remove()},350)},4800)};
UB.modal=function(o){var f=document.createElement('div');f.className='modal-fundo ativo';f.innerHTML='<div class="modal-caixa'+(o.largo?' largo':'')+'" role="dialog" aria-modal="true">'+o.html+'</div>';document.body.appendChild(f);function k(e){if(e.key==='Escape')fechar()}function fechar(){f.remove();document.removeEventListener('keydown',k)}document.addEventListener('keydown',k);f.addEventListener('mousedown',function(e){if(e.target===f)fechar()});f.fechar=fechar;f.querySelectorAll('[data-fechar]').forEach(function(b){b.onclick=fechar});return f};
UB.confirmar=function(o){var f=UB.modal({html:'<div class="icone-alerta'+(o.perigo?'':' neutro')+'">'+UB.ic('alerta')+'</div><h3>'+UB.esc(o.titulo||'Confirmar ação')+'</h3><p>'+UB.esc(o.msg)+'</p>'+(o.detalhe?'<p class="detalhe">'+UB.esc(o.detalhe)+'</p>':'')+'<div class="modal-botoes"><button class="btn btn-secundario" data-fechar>Cancelar</button><button class="btn '+(o.perigo?'btn-perigo':'btn-primario')+'" id="cfOk">'+UB.esc(o.ok||'Confirmar')+'</button></div>'});f.querySelector('#cfOk').onclick=function(){f.fechar();o.fn()}};
UB.sair=function(){UB.log('Logout','Sessão encerrada');UB.sairSessao();UB.flash(UB.MSG[6]);location.href='/login'};
function shell(u,pag,titulo){
 var nav=[['home','Home'],['dashboard','Dashboard'],['estoque','Estoque'],['movimentacoes','Movimentações'],['relatorios','Relatórios']];
 var li=nav.map(function(n){return '<li><a href="/'+n[0]+'"'+(pag===n[0]?' class="ativo"':'')+'>'+UB.ic(n[0])+' '+n[1]+'</a></li>'}).join('');
 document.body.innerHTML='<div class="app-shell"><div id="sidebar" class="sidebar"><div class="sidebar-logo"><img src="/images/upbox-logo.png" alt="Logo UpBox"><span class="marca-nome"><b>Up</b>Box</span></div><ul class="sidebar-menu">'+li+'</ul><div class="sidebar-config"><a href="/configuracoes"'+(pag==='configuracoes'?' class="ativo"':'')+'>'+UB.ic('config')+' Configurações</a></div></div><div id="overlaySidebar" class="overlay-sidebar"></div><div class="conteudo"><div class="header"><div class="header-titulo"><button id="btnHamburguer" class="hamburguer" type="button" aria-label="Abrir menu">☰</button><h1>'+UB.esc(titulo)+'</h1></div><div class="header-usuario"><div class="oculto-mobile" style="text-align:right"><div class="nome-usuario">'+UB.esc(u.nome)+'</div><div class="cargo-usuario">'+UB.PERFIL[u.perfil]+'</div></div><div class="avatar">'+UB.esc(u.nome.charAt(0))+'</div></div></div><main class="pagina"><div class="demo-banner"><span><strong>Protótipo visual com dados de demonstração.</strong> Os registros são fictícios e ficam salvos só neste navegador. Perfil em uso: <b>'+UB.PERFIL[u.perfil]+'</b>.</span><a id="resetDemo">Restaurar dados de demonstração</a></div><div id="pg"></div></main></div></div>';
 var sb=document.getElementById('sidebar'),ov=document.getElementById('overlaySidebar');
 document.getElementById('btnHamburguer').onclick=function(){sb.classList.toggle('aberta');ov.classList.toggle('ativo')};
 ov.onclick=function(){sb.classList.remove('aberta');ov.classList.remove('ativo')};
 document.getElementById('resetDemo').onclick=function(){UB.confirmar({msg:'Os dados de demonstração voltarão ao estado inicial.',ok:'Restaurar',fn:function(){UB.resetar();location.reload()}})};
}
document.addEventListener('DOMContentLoaded',function(){
 var pag=document.body.dataset.pagina,tit=document.body.dataset.titulo;
 if(pag){var u=UB.sessao();if(!u){location.replace('/login');return}shell(u,pag,tit);UBPaginas[pag](u,document.getElementById('pg'))}
 UB.flashLer().forEach(function(f){UB.toast(f.msg,f.tipo)});
});
})();
