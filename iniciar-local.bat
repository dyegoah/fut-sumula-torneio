@echo off
setlocal

rem Sempre entra na pasta onde este arquivo e o pom.xml estao.
cd /d "%~dp0"

echo ==========================================================
echo   FUT-SUMULA TORNEIO - INICIALIZACAO LOCAL
echo ==========================================================
echo.
echo Pasta do projeto:
echo %CD%
echo.

if not exist "pom.xml" (
    echo [ERRO] O arquivo pom.xml nao foi encontrado nesta pasta.
    echo Coloque este arquivo iniciar-local.bat na raiz do projeto.
    echo.
    pause
    exit /b 1
)

if not exist "src\main\resources\application-local.properties" (
    echo [ERRO] O arquivo application-local.properties ainda nao existe.
    echo.
    echo Copiando o arquivo de exemplo...
    copy /Y "src\main\resources\application-local.example.properties" "src\main\resources\application-local.properties" >nul
    echo.
    echo O arquivo foi criado em:
    echo src\main\resources\application-local.properties
    echo.
    echo Abra esse arquivo e preencha:
    echo 1. spring.datasource.password
    echo 2. spring.mail.password
    echo.
    echo Depois execute iniciar-local.bat novamente.
    echo.
    pause
    exit /b 1
)

where java >nul 2>nul
if errorlevel 1 (
    echo [ERRO] Java nao foi encontrado no PATH.
    echo Instale ou configure o Java 17 antes de continuar.
    echo.
    pause
    exit /b 1
)

if exist "mvnw.cmd" (
    echo Iniciando com Maven Wrapper...
    echo A aplicacao devera abrir em http://localhost:8011
    echo.
    call mvnw.cmd clean spring-boot:run
) else (
    where mvn >nul 2>nul
    if errorlevel 1 (
        echo [ERRO] Nem mvnw.cmd nem o comando mvn foram encontrados.
        echo Instale o Maven ou restaure o Maven Wrapper do projeto.
        echo.
        pause
        exit /b 1
    )

    echo Iniciando com Maven instalado no Windows...
    echo A aplicacao devera abrir em http://localhost:8011
    echo.
    call mvn -f pom.xml clean spring-boot:run
)

if errorlevel 1 (
    echo.
    echo ==========================================================
    echo A aplicacao nao iniciou. Leia o erro acima.
    echo ==========================================================
    pause
    exit /b 1
)

endlocal
