/* Apenas interações visuais. Login, permissões e dados são tratados pelo servidor. */
document.addEventListener('DOMContentLoaded', function () {
    document.querySelectorAll('.sidebar a').forEach(function (link) {
        if (link.pathname === window.location.pathname) {
            link.classList.add('ativo');
            link.setAttribute('aria-current', 'page');
        }
    });
    var sidebar = document.getElementById('sidebar');
    var overlay = document.getElementById('overlaySidebar');
    var botao = document.getElementById('btnHamburguer');
    function menu(aberto) {
        sidebar.classList.toggle('aberta', aberto);
        overlay.classList.toggle('ativo', aberto);
        botao.setAttribute('aria-expanded', String(aberto));
    }
    if (botao) botao.addEventListener('click', function () { menu(!sidebar.classList.contains('aberta')); });
    if (overlay) overlay.addEventListener('click', function () { menu(false); });
    document.querySelectorAll('form[data-confirmar]').forEach(function (form) {
        form.addEventListener('submit', function (event) {
            if (!window.confirm(form.dataset.confirmar)) event.preventDefault();
        });
    });
    document.querySelectorAll('[data-imprimir]').forEach(function (b) {
        b.addEventListener('click', function () { window.print(); });
    });
    try {
        document.documentElement.dataset.tema = localStorage.getItem('upbox-tema') || 'claro';
        var tema = document.getElementById('alternarTema');
        if (tema) tema.addEventListener('click', function () {
            var proximo = document.documentElement.dataset.tema === 'escuro' ? 'claro' : 'escuro';
            document.documentElement.dataset.tema = proximo;
            localStorage.setItem('upbox-tema', proximo);
        });
    } catch (e) { /* O tema funciona sem armazenamento persistente. */ }
});
