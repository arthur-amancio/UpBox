param([Parameter(Mandatory = $true)][string]$Pacote)

# Homologação da CI: cluster e banco novos, nunca a instalação de uso da escola.
$ErrorActionPreference = 'Stop'
$raiz = Join-Path $env:RUNNER_TEMP ('upbox-' + [guid]::NewGuid())
$extraido = Join-Path $raiz 'Pasta com espacos'
New-Item -ItemType Directory -Path $raiz | Out-Null
Expand-Archive -Path $Pacote -DestinationPath $extraido
$pasta = Join-Path $extraido 'UpBox-escola'
$pg = Get-ChildItem 'C:\Program Files\PostgreSQL\*\bin\pg_ctl.exe' -ErrorAction SilentlyContinue |
    Sort-Object FullName -Descending | Select-Object -First 1
if (!$pg) {
    # Alternativa oficial caso a imagem Windows não traga PostgreSQL instalado.
    $binarios = Join-Path $raiz 'postgres.zip'
    Invoke-WebRequest 'https://get.enterprisedb.com/postgresql/postgresql-16.4-1-windows-x64-binaries.zip' -OutFile $binarios
    Expand-Archive $binarios -DestinationPath (Join-Path $raiz 'pg')
    $pg = Get-Item (Join-Path $raiz 'pg\pgsql\bin\pg_ctl.exe')
}
$pgBin = $pg.DirectoryName
$dados = Join-Path $raiz 'dados-descartaveis'
$script:processo = $null
$iniciouBanco = $false

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
    & "$pgBin\initdb.exe" -D $dados -U upbox_test -A trust
    if ($LASTEXITCODE -ne 0) { throw 'initdb falhou no cluster descartável.' }
    & "$pgBin\pg_ctl.exe" -D $dados -l (Join-Path $raiz 'postgres.log') -o '-p 55439 -h 127.0.0.1' -w start
    if ($LASTEXITCODE -ne 0) { throw 'PostgreSQL de testes não iniciou.' }
    $iniciouBanco = $true
    & "$pgBin\createdb.exe" -h 127.0.0.1 -p 55439 -U upbox_test upbox_test
    if ($LASTEXITCODE -ne 0) { throw 'Não foi possível criar upbox_test.' }
    $versao = & "$pgBin\psql.exe" -h 127.0.0.1 -p 55439 -U upbox_test -d upbox_test -Atc 'select version()'
    $env:DB_URL = 'jdbc:postgresql://127.0.0.1:55439/upbox_test'
    $env:DB_USER = 'upbox_test'
    $env:DB_PASSWORD = ''
    $env:SERVER_PORT = '18082'
    Push-Location $pasta
    try {
        & $env:ComSpec /d /c 'iniciar-upbox.bat --verificar'
        if ($LASTEXITCODE -ne 0) { throw 'Verificação Java 17/JAR/configuração falhou.' }
    } finally { Pop-Location }
    Iniciar-UpBox
    $pagina = Invoke-WebRequest 'http://127.0.0.1:18082/primeiro-acesso' -SessionVariable sessao
    $token = [regex]::Match($pagina.Content, 'name="_csrf"[^>]*value="([^"]+)"').Groups[1].Value
    if (!$token) { throw 'Formulário inicial sem token CSRF.' }
    Invoke-WebRequest 'http://127.0.0.1:18082/primeiro-acesso' -Method Post -WebSession $sessao -Body @{
        _csrf = $token; nome = 'Equipe Windows'; usuario = 'admin'; email = 'admin@example.com'
        senha = 'senha-teste-windows-123'; confirmacao = 'senha-teste-windows-123'
    } | Out-Null
    Parar-UpBox
    Iniciar-UpBox
    $contas = & "$pgBin\psql.exe" -h 127.0.0.1 -p 55439 -U upbox_test -d upbox_test -Atc "select count(*) from usuarios where perfil='ADMINISTRADOR'"
    if ([int]$contas -ne 1) { throw 'Conta não persistiu ou foi duplicada após reinício.' }
    $pagina = Invoke-WebRequest 'http://127.0.0.1:18082/primeiro-acesso'
    if ($pagina.Content -notmatch 'name="username"') { throw 'Cadastro inicial não fechou após reinício.' }
    Parar-UpBox

    # Exercita mensagens do .bat sem expor stack trace na janela do aluno.
    $env:DB_URL = 'jdbc:postgresql://127.0.0.1:1/upbox_test?connectTimeout=2'
    Push-Location $pasta
    try {
        $mensagem = & $env:ComSpec /d /c 'iniciar-upbox.bat --sem-pausa' 2>&1 | Out-String
        if ($LASTEXITCODE -ne 1 -or $mensagem -notmatch 'Confira o servico PostgreSQL') { throw 'Mensagem de conexão inválida não foi apresentada.' }
        $javaAnterior = $env:JAVA_HOME
        try {
            $env:JAVA_HOME = Join-Path $raiz 'java-inexistente'
            $mensagemJava = & $env:ComSpec /d /c 'iniciar-upbox.bat --verificar' 2>&1 | Out-String
            if ($LASTEXITCODE -ne 1 -or $mensagemJava -notmatch 'Java 17 nao encontrado') { throw 'Mensagem de Java ausente não foi apresentada.' }
        } finally { $env:JAVA_HOME = $javaAnterior }
    } finally { Pop-Location }
    @{resultado='aprovado'; commit=$env:GITHUB_SHA; postgres=$versao; sistema='Windows'; java=17; pacoteExtraido=$true; caminhoComEspacos=$true; primeiroAcessoCsrf=$true; reinicioSemDuplicacao=$true; mensagemConexao=$true; mensagemJava=$true} |
        ConvertTo-Json | Set-Content (Join-Path $env:GITHUB_WORKSPACE 'target\homologacao-windows.json')
} finally {
    Parar-UpBox
    if (Test-Path (Join-Path $pasta 'logs\upbox.log')) {
        Copy-Item (Join-Path $pasta 'logs\upbox.log') (Join-Path $env:GITHUB_WORKSPACE 'target\windows-upbox.log')
    }
    if ($iniciouBanco) { & "$pgBin\pg_ctl.exe" -D $dados -m fast -w stop }
}
