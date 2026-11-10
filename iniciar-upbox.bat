@echo off
setlocal
cd /d "%~dp0"
set "UPBOX_JAVA=java"
if defined JAVA_HOME set "UPBOX_JAVA=%JAVA_HOME%\bin\java.exe"
set "UPBOX_VERSION_FILE=%TEMP%\upbox-java-%RANDOM%.txt"
"%UPBOX_JAVA%" -version > "%UPBOX_VERSION_FILE%" 2>&1
if errorlevel 1 goto semjava
set "UPBOX_VERSION="
for /f "usebackq tokens=3" %%v in ("%UPBOX_VERSION_FILE%") do if not defined UPBOX_VERSION set "UPBOX_VERSION=%%~v"
del "%UPBOX_VERSION_FILE%" >nul 2>&1
for /f "tokens=1 delims=." %%v in ("%UPBOX_VERSION%") do if not "%%v"=="17" goto javaerrado
if not exist "upbox.jar" goto semjar
if not exist "application.properties" goto semconfig
if /i "%~1"=="--verificar" (
 echo Java 17, JAR e arquivo de configuracao encontrados.
 exit /b 0
)
if not exist "logs" mkdir logs
echo Iniciando UpBox. Aguarde alguns segundos e abra http://localhost:8080
echo Mantenha esta janela aberta. Use Ctrl+C para encerrar.
echo Detalhes tecnicos: logs\upbox.log
"%UPBOX_JAVA%" -jar "upbox.jar" > "logs\upbox.log" 2>&1
if errorlevel 1 goto falha
exit /b 0
:semjava
del "%UPBOX_VERSION_FILE%" >nul 2>&1
echo ERRO: Java 17 nao encontrado. Instale o JDK 17 e confira JAVA_HOME e PATH.
goto erro
:javaerrado
echo ERRO: encontrado Java %UPBOX_VERSION%. Este pacote requer Java 17.
goto erro
:semjar
echo ERRO: upbox.jar nao encontrado. Extraia todo o ZIP antes de iniciar.
goto erro
:semconfig
echo ERRO: application.properties nao encontrado ao lado do JAR.
goto erro
:falha
echo ERRO: o UpBox nao conseguiu iniciar ou foi interrompido.
echo Confira o servico MySQL, banco upbox, usuario e senha em application.properties.
echo Confira tambem se a porta 8080 esta livre. Detalhes em logs\upbox.log.
echo Este script nao apaga nem recria bancos de dados.
:erro
if "%~1"=="" pause
exit /b 1
