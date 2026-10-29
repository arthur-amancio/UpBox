/* Login e cadastro (demonstrativos: sem autenticação real) */
document.addEventListener('DOMContentLoaded',function(){
 var t=document.body.dataset.auth,f=document.getElementById('form'),al=document.getElementById('alvo');
 if(!f)return;
 function erro(m){al.innerHTML='<div class="alerta alerta-erro">'+UB.ic('alerta')+'<span>'+UB.esc(m)+'</span></div>'}
 function v(id){return document.getElementById(id).value.trim()}
 if(t==='login'){
  document.querySelectorAll('[data-u]').forEach(function(b){b.onclick=function(){document.getElementById('usuario').value=b.dataset.u;document.getElementById('senha').value='demo123'}});
  f.onsubmit=function(e){e.preventDefault();var l=v('usuario').toLowerCase(),s=document.getElementById('senha').value;
   if(!l||!s)return erro(UB.MSG[3]);
   var u=UB.db().usuarios.filter(function(x){return x.ativo&&(x.usuario.toLowerCase()===l||x.email.toLowerCase()===l)&&x.senha===s})[0];
   if(!u)return erro(UB.MSG[2]);
   UB.entrar(u.id);UB.log('Login','Acesso ao sistema');UB.flash(UB.MSG[1]);location.href='/home'};
 }else{
  f.onsubmit=function(e){e.preventDefault();
   if(!v('nome')||!v('usuario')||!v('email')||!document.getElementById('senha').value)return erro(UB.MSG[3]);
   if(document.getElementById('senha').value!==document.getElementById('conf').value)return erro(UB.MSG[13]);
   var db=UB.db(),l=v('usuario').toLowerCase(),em=v('email').toLowerCase();
   if(db.usuarios.some(function(x){return x.usuario.toLowerCase()===l||x.email.toLowerCase()===em}))return erro(UB.MSG[8]);
   UB.flash(UB.MSG[7]+' (demonstração: a conta não foi criada de verdade; use as contas de demonstração)');location.href='/login'};
 }
});
