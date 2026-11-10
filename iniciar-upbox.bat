@echo off
setlocal
cd /d "%~dp0"

rem O mesmo arquivo funciona no repositorio clonado e no ZIP pronto.
set "UPBOX_JAVA=java"
if defined JAVA_HOME set "UPBOX_JAVA=%JAVA_HOME%\bin\java.exe"
set "UPBOX_VERSION_FILE=%TEMP%\upbox-java-%RANDOM%.txt"
"%UPBOX_JAVA%" -version > "%UPBOX_VERSION_FILE%" 2>&1
if errorlevel 1 goto semjava
set "UPBOX_VERSION="
for /f "usebackq tokens=3" %%v in ("%UPBOX_VERSION_FILE%") do if not defined UPBOX_VERSION set "UPBOX_VERSION=%%~v"
del "%UPBOX_VERSION_FILE%" >nul 2>&1
for /f "tokens=1 delims=." %%v in ("%UPBOX_VERSION%") do if not "%%v"=="17" goto javaerrado

rem Ao clonar, executa diretamente o codigo Java usando o Maven instalado.
rem No pacote ZIP, executa o JAR pronto sem exigir Maven.
if exist "pom.xml" goto codigo
if exist "upbox.jar" goto pacote
goto semprojeto

:codigo
if not exist "src\main\resources\application.properties" goto semconfigfonte
where mvn >nul 2>&1
if errorlevel 1 goto semmaven
if /i "%~1"=="--verificar" (
 echo Java 17, Maven e configuracao do projeto encontrados.
 exit /b 0
)
echo Iniciando UpBox pelo Maven a partir do codigo clonado...
echo Confira antes a senha do MySQL em src\main\resources\application.properties.
echo Acesse http://localhost:8080 quando o Spring Boot iniciar.
echo Em banco novo: usuario administrador, senha inicial admin.
echo No primeiro acesso sera obrigatorio trocar essa senha.
echo Mantenha esta janela aberta. Use Ctrl+C para encerrar.
call mvn spring-boot:run
if errorlevel 1 goto falhafonte
exit /b 0

:pacote
if not exist "application.properties" goto semconfigpacote
if /i "%~1"=="--verificar" (
 echo Java 17, JAR e arquivo de configuracao encontrados.
 exit /b 0
)
if not exist "logs" mkdir logs
echo Iniciando UpBox. Abra http://localhost:8080 quando estiver pronto.
echo Em banco novo: usuario administrador, senha inicial admin.
echo No primeiro acesso sera obrigatorio trocar essa senha.
echo Mantenha esta janela aberta. Use Ctrl+C para encerrar.
echo Detalhes tecnicos: logs\upbox.log
"%UPBOX_JAVA%" -jar "upbox.jar" > "logs\upbox.log" 2>&1
if errorlevel 1 goto falhapacote
exit /b 0

:semjava
del "%UPBOX_VERSION_FILE%" >nul 2>&1
echo ERRO: Java 17 nao encontrado. Instale o JDK 17 e confira JAVA_HOME e PATH.
goto erro
:javaerrado
echo ERRO: encontrado Java %UPBOX_VERSION%. Este projeto requer Java 17.
goto erro
:semmaven
echo ERRO: Maven nao encontrado. Para executar o codigo clonado, instale o Maven e confira o PATH.
goto erro
:semprojeto
echo ERRO: nao encontrei pom.xml nem upbox.jar. Execute este .bat na pasta correta do UpBox.
goto erro
:semconfigfonte
echo ERRO: src\main\resources\application.properties nao encontrado.
goto erro
:semconfigpacote
echo ERRO: application.properties nao encontrado ao lado do JAR.
goto erro
:falhafonte
echo ERRO: o Maven ou o Spring Boot falhou. Confira a mensagem acima.
echo Confira o servidor MySQL, o banco upbox, usuario, senha e a porta 8080.
goto erro
:falhapacote
echo ERRO: o UpBox nao conseguiu iniciar ou foi interrompido.
echo Confira o servidor MySQL, banco upbox, usuario e senha em application.properties.
echo Confira tambem a porta 8080. Detalhes em logs\upbox.log.
echo Este script nao apaga nem recria bancos de dados.
:erro
if "%~1"=="" pause
exit /b 1
