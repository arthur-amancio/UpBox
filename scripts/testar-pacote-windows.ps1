param([Parameter(Mandatory = $true)][string]$Pacote)
# Banco novo e descartável da CI. Não acessa instalação escolar.
$ErrorActionPreference = 'Stop'
$raiz = Join-Path $env:RUNNER_TEMP ('upbox-' + [guid]::NewGuid())
$extraido = Join-Path $raiz 'Pasta com espacos'
New-Item -ItemType Directory -Path $raiz | Out-Null
Expand-Archive -Path $Pacote -DestinationPath $extraido
$pasta = Join-Path $extraido 'UpBox-escola'
$mysql = Get-Command mysqld.exe -ErrorAction SilentlyContinue
if ($mysql) { $bin = Split-Path $mysql.Source }
elseif (Test-Path 'C:\mysql\bin\mysqld.exe') { $bin = 'C:\mysql\bin' }
else {
    $download = Join-Path $raiz 'mysql.zip'
    Invoke-WebRequest 'https://cdn.mysql.com/archives/mysql-8.0/mysql-8.0.44-winx64.zip' -OutFile $download
    Expand-Archive $download -DestinationPath (Join-Path $raiz 'mysql')
    $bin = (Get-ChildItem (Join-Path $raiz 'mysql') -Filter mysqld.exe -Recurse | Select-Object -First 1).DirectoryName
}
$dados = Join-Path $raiz 'dados-descartaveis'
$script:processo = $null
$banco = $null
function Sql($consulta) {
    & "$bin\mysql.exe" --no-defaults --protocol=TCP "--host=127.0.0.1" "--port=53316" "--user=root" -N -e $consulta
    if ($LASTEXITCODE -ne 0) { throw 'Falha no MySQL descartável.' }
}
function Parar-UpBox {
    if ($script:processo -and !$script:processo.HasExited) {
        Get-CimInstance Win32_Process -Filter "ParentProcessId = $($script:processo.Id)" |
            ForEach-Object { Stop-Process -Id $_.ProcessId -Force -ErrorAction SilentlyContinue }
        Stop-Process -Id $script:processo.Id -Force -ErrorAction SilentlyContinue
        Start-Sleep -Seconds 2
    }
}

function Iniciar-UpBox {
    $script:processo = Start-Process $env:ComSpec -ArgumentList '/d /c iniciar-upbox.bat --sem-pausa' -WorkingDirectory $pasta -PassThru
    for ($tentativa = 0; $tentativa -lt 60; $tentativa++) {
        if ($script:processo.HasExited) { throw 'O script encerrou antes de iniciar o servidor.' }
        try {
            $resposta = Invoke-WebRequest 'http://127.0.0.1:18082/login' -TimeoutSec 2
            if ($resposta.StatusCode -eq 200) { return }
        } catch { Start-Sleep -Seconds 1 }
    }
    throw 'A aplicação não iniciou em 60 tentativas.'
}

try {
    Write-Host "Inicializando cluster MySQL descartável em $dados"
    & "$bin\mysqld.exe" --no-defaults --initialize-insecure "--basedir=$(Split-Path $bin)" "--datadir=$dados"
    if ($LASTEXITCODE -ne 0) { throw 'Não inicializou o MySQL descartável.' }
    Write-Host "MySQL de teste em $bin"
    & "$bin\mysqld.exe" --no-defaults --version
    $banco = Start-Process "$bin\mysqld.exe" -ArgumentList "--no-defaults --datadir=`"$dados`" --port=53316 --bind-address=127.0.0.1 --mysqlx=0 --character-set-server=utf8mb4 --collation-server=utf8mb4_unicode_ci --console" -RedirectStandardOutput (Join-Path $raiz 'mysql-out.log') -RedirectStandardError (Join-Path $raiz 'mysql-err.log') -PassThru
    Write-Host "Aguardando MySQL na porta 53316, PID $($banco.Id)"
    $pronto = $false
    for ($i=0; $i -lt 60; $i++) {
        $banco.Refresh()
        if ($banco.HasExited) { throw 'O processo MySQL encerrou; veja os logs de diagnóstico.' }
        $socket = [System.Net.Sockets.TcpClient]::new()
        try {
            $tentativa = $socket.ConnectAsync('127.0.0.1', 53316)
            if ($tentativa.Wait(1000) -and $socket.Connected) { $pronto=$true; break }
        } catch { } finally { $socket.Dispose() }
        Start-Sleep -Seconds 1
    }
    if (!$pronto) { throw 'MySQL descartável não ficou pronto.' }
    Sql 'CREATE DATABASE upbox_test CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci'
    $versao = Sql 'SELECT VERSION()'
    $env:SPRING_DATASOURCE_URL = 'jdbc:mysql://127.0.0.1:53316/upbox_test'
    $env:SPRING_DATASOURCE_USERNAME = 'root'
    $env:SPRING_DATASOURCE_PASSWORD = ''
    $env:SERVER_PORT = '18082'
    Push-Location $pasta
    try {
        & $env:ComSpec /d /c 'iniciar-upbox.bat --verificar'
        if ($LASTEXITCODE -ne 0) { throw 'Verificação Java/JAR/configuração falhou.' }
    } finally { Pop-Location }
    Iniciar-UpBox
    $pagina = Invoke-WebRequest 'http://127.0.0.1:18082/login' -SessionVariable sessao
    $token = [regex]::Match($pagina.Content, 'name="_csrf"[^>]*value="([^"]+)"').Groups[1].Value
    if ([int](Sql 'SELECT COUNT(*) FROM upbox_test.usuarios') -ne 1) { throw 'Conta automática ausente ou duplicada.' }
    $pagina = Invoke-WebRequest 'http://127.0.0.1:18082/login' -Method Post -WebSession $sessao -Body @{_csrf=$token; username='administrador'; password='admin'}
    if ($pagina.Content -notmatch 'Primeiro acesso:') { throw 'A senha inicial não exigiu troca.' }
    $token = [regex]::Match($pagina.Content, 'name="_csrf"[^>]*value="([^"]+)"').Groups[1].Value
    Invoke-WebRequest 'http://127.0.0.1:18082/configuracoes/senha' -Method Post -WebSession $sessao -Body @{
        _csrf=$token; atual='admin'; nova='senha-teste-windows-123'; confirmacao='senha-teste-windows-123'
    } | Out-Null
    Parar-UpBox
    Iniciar-UpBox
    if ([int](Sql 'SELECT COUNT(*) FROM upbox_test.usuarios') -ne 1) { throw 'Duplicação após reinício.' }
    $pagina = Invoke-WebRequest 'http://127.0.0.1:18082/login' -SessionVariable retorno
    $token = [regex]::Match($pagina.Content, 'name="_csrf"[^>]*value="([^"]+)"').Groups[1].Value
    $pagina = Invoke-WebRequest 'http://127.0.0.1:18082/login' -Method Post -WebSession $retorno -Body @{_csrf=$token; username='administrador'; password='senha-teste-windows-123'}
    if ($pagina.Content -notmatch 'Bem-vindo') { throw 'Login após reinício não preservou a nova senha.' }
    Parar-UpBox
    $env:SPRING_DATASOURCE_URL = 'jdbc:mysql://127.0.0.1:1/upbox_test?connectTimeout=2000'
    Push-Location $pasta
    try {
        $mensagem = & $env:ComSpec /d /c 'iniciar-upbox.bat --sem-pausa' 2>&1 | Out-String
        # Confere os elementos essenciais do diagnostico, sem depender de uma frase exata.
        if ($LASTEXITCODE -ne 1 -or $mensagem -notmatch 'MySQL' -or $mensagem -notmatch 'application[.]properties') {
            throw 'Mensagem de conexao invalida ausente ou incompleta.'
        }
        $anterior = $env:JAVA_HOME
        try {
            $env:JAVA_HOME = Join-Path $raiz 'java-inexistente'
            $mensagemJava = & $env:ComSpec /d /c 'iniciar-upbox.bat --verificar' 2>&1 | Out-String
            if ($LASTEXITCODE -ne 1 -or $mensagemJava -notmatch 'Java nao encontrado') { throw 'Mensagem Java ausente.' }
        } finally { $env:JAVA_HOME=$anterior }
    } finally { Pop-Location }
    $json = @{resultado='aprovado'; commit=$env:GITHUB_SHA; mysql=$versao; sistema='Windows'; java=21; contaAutomatica=$true; loginPadraoETrocaObrigatoria=$true; reinicioSemDuplicacao=$true; caminhoComEspacos=$true; mensagemConexao=$true; mensagemJava=$true} | ConvertTo-Json
    $json | Set-Content (Join-Path $env:GITHUB_WORKSPACE 'target/homologacao-windows.json')
    Write-Host $json
} finally {
    Parar-UpBox
    if (Test-Path (Join-Path $pasta 'logs/upbox.log')) { Copy-Item (Join-Path $pasta 'logs/upbox.log') (Join-Path $env:GITHUB_WORKSPACE 'target/windows-upbox.log') }
    foreach ($log in @((Join-Path $raiz 'mysql-out.log'), (Join-Path $raiz 'mysql-err.log'))) {
        if (Test-Path $log) {
            Get-Content $log | Write-Host
            Get-Content $log | Add-Content (Join-Path $env:GITHUB_WORKSPACE 'target/windows-mysql.log')
        }
    }
    Get-ChildItem $dados -Filter '*.err' -ErrorAction SilentlyContinue | ForEach-Object { Get-Content $_.FullName | Write-Host }
    if ($banco -and !$banco.HasExited) { Stop-Process -Id $banco.Id -Force }
}
# Os testes de erro retornam 1 de propósito; chegando aqui, todas as verificações passaram.
exit 0
