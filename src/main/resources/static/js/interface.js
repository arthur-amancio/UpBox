/* Apenas interações visuais. Login, permissões e saldos são validados pelo servidor. */
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
    var fechar = document.getElementById('fecharMenu');
    var telaPequena = window.matchMedia('(max-width: 900px)');

    function menu(aberto, devolverFoco) {
        if (!sidebar) return;
        var mostrar = telaPequena.matches && aberto;
        sidebar.classList.toggle('aberta', mostrar);
        // inert impede que Tab alcance os links do menu enquanto ele está fora da tela.
        sidebar.inert = telaPequena.matches && !mostrar;
        overlay.classList.toggle('ativo', mostrar);
        document.body.classList.toggle('menu-aberto', mostrar);
        botao.setAttribute('aria-expanded', String(mostrar));
        if (mostrar) fechar.focus();
        else if (devolverFoco) botao.focus();
    }
    if (botao) botao.addEventListener('click', function () { menu(!sidebar.classList.contains('aberta'), true); });
    if (fechar) fechar.addEventListener('click', function () { menu(false, true); });
    if (overlay) overlay.addEventListener('click', function () { menu(false, true); });
    telaPequena.addEventListener('change', function () { menu(false, false); });
    menu(false, false);

    document.addEventListener('keydown', function (event) {
        if (!sidebar || !telaPequena.matches || !sidebar.classList.contains('aberta')) return;
        if (event.key === 'Escape') { menu(false, true); return; }
        // Mantém o foco dentro do menu aberto, inclusive ao usar Shift+Tab.
        if (event.key === 'Tab') {
            var opcoes = sidebar.querySelectorAll('a[href], button:not([disabled])');
            var primeiro = opcoes[0], ultimo = opcoes[opcoes.length - 1];
            if (event.shiftKey && document.activeElement === primeiro) {
                event.preventDefault(); ultimo.focus();
            } else if (!event.shiftKey && document.activeElement === ultimo) {
                event.preventDefault(); primeiro.focus();
            }
        }
    });

    var formMovimentacao = document.getElementById('registrar-movimentacao');
    if (formMovimentacao) {
        var produto = document.getElementById('produtoId');
        var quantidade = document.getElementById('quantidade');
        var tipo = document.getElementById('tipo');
        var previa = document.getElementById('previaEstoque');
        function atualizarPrevia() {
            var opcao = produto.options[produto.selectedIndex];
            if (!opcao || !opcao.value) {
                previa.textContent = 'Selecione um produto para consultar o saldo disponível.';
                return;
            }
            var saldo = Number(opcao.dataset.saldo), valor = quantidade.valueAsNumber;
            var texto = 'Saldo disponível: ' + saldo + ' unidade(s).';
            if (Number.isInteger(valor) && valor > 0) {
                var previsto = tipo.value === 'ENTRADA' ? saldo + valor : saldo - valor;
                texto += previsto < 0 || previsto > 2147483647 ? ' Prévia fora do saldo ou limite permitido.' : ' Saldo previsto: ' + previsto + '.';
            }
            previa.textContent = texto;
            formMovimentacao.dataset.confirmar = 'Confirma ' + (tipo.value === 'ENTRADA' ? 'entrada' : 'saída') + ' de ' + quantidade.value + ' unidade(s) de ' + opcao.textContent.trim() + '?';
        }
        produto.addEventListener('change', atualizarPrevia);
        tipo.addEventListener('change', atualizarPrevia);
        quantidade.addEventListener('input', atualizarPrevia);
        atualizarPrevia();
    }

    var filtros = document.getElementById('filtrosRelatorio');
    if (filtros) {
        var relatorio = document.getElementById('relatorio');
        function ajustarFiltros() {
            var historico = relatorio.value === 'HISTORICO';
            var movimentos = historico || relatorio.value === 'ENTRADAS' || relatorio.value === 'SAIDAS';
            document.getElementById('inicio').disabled = !movimentos;
            document.getElementById('fim').disabled = !movimentos;
            document.getElementById('tipo').disabled = !historico;
            if (relatorio.value === 'ENTRADAS') document.getElementById('tipo').value = 'ENTRADA';
            else if (relatorio.value === 'SAIDAS') document.getElementById('tipo').value = 'SAIDA';
            else if (!movimentos) document.getElementById('tipo').value = '';
        }
        relatorio.addEventListener('change', ajustarFiltros);
        ajustarFiltros();
    }

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
