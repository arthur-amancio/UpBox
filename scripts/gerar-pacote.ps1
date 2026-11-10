# Execute depois de mvn clean package. Só empacota arquivos públicos, nunca a configuração preenchida.
$ErrorActionPreference = 'Stop'
$raiz = Split-Path $PSScriptRoot -Parent
$jar = Join-Path $raiz 'target/upbox-sistema-1.0.0.jar'
if (!(Test-Path $jar)) { throw 'Gere o JAR com mvn clean package primeiro.' }
Add-Type -AssemblyName System.IO.Compression.FileSystem
$arquivo = [IO.Compression.ZipFile]::OpenRead($jar)
try {
    $entrada = $arquivo.GetEntry('BOOT-INF/classes/application.properties')
    $leitor = [IO.StreamReader]::new($entrada.Open())
    try { $config = $leitor.ReadToEnd() } finally { $leitor.Dispose() }
    if ($config -notmatch '(?m)^spring.datasource.password=\s*$') {
        throw 'JAR contém senha preenchida. Limpe a configuração fonte e gere novamente antes de distribuir.'
    }
} finally { $arquivo.Dispose() }
$pasta = Join-Path $raiz 'target/pacote/UpBox-escola'
if (Test-Path (Split-Path $pasta)) { Remove-Item (Split-Path $pasta) -Recurse -Force }
New-Item -ItemType Directory $pasta -Force | Out-Null
Copy-Item $jar (Join-Path $pasta 'upbox.jar')
[IO.File]::WriteAllText((Join-Path $pasta 'application.properties'), $config)
Copy-Item (Join-Path $raiz 'iniciar-upbox.bat') $pasta
Copy-Item (Join-Path $raiz 'distribuicao/LEIA-ME.txt') $pasta
Compress-Archive -Path $pasta -DestinationPath (Join-Path $raiz 'target/UpBox-escola.zip') -Force
Write-Host 'Pacote criado em target/UpBox-escola.zip. Não alterou nenhum banco.'
