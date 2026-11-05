@echo off
title Sistema de Facturacion - Iniciando...
color 0A
echo.
echo ============================================================
echo   SISTEMA DE FACTURACION - Inicio automatico
echo ============================================================
echo.

cd /d "C:\Users\JORGE\Videos\Captures\Facturacion.it"

REM ---------- 1. Verificar Docker ----------
echo [1/5] Verificando Docker...
docker info >nul 2>&1
if %errorlevel% neq 0 (
    echo  ERROR: Docker no esta corriendo. Abre Docker Desktop primero.
    pause
    exit /b 1
)
echo  Docker OK

REM ---------- 2. Levantar contenedores ----------
echo.
echo [2/5] Levantando MySQL, MongoDB y Cassandra...
docker-compose up -d
echo  Contenedores iniciados.

REM ---------- 3. Esperar a Cassandra ----------
echo.
echo [3/5] Esperando a que Cassandra este lista (45 seg)...
timeout /t 45 /nobreak

REM ---------- 4. Crear keyspace en Cassandra ----------
echo.
echo [4/5] Creando keyspace en Cassandra...
docker exec facturacion-cassandra cqlsh -e "CREATE KEYSPACE IF NOT EXISTS facturacion_ks WITH replication = {'class': 'SimpleStrategy', 'replication_factor': 1};"
if %errorlevel% neq 0 (
    echo  AVISO: No se pudo crear el keyspace automaticamente.
    echo  Puedes hacerlo manualmente con:
    echo    docker exec -it facturacion-cassandra cqlsh
    echo    CREATE KEYSPACE IF NOT EXISTS facturacion_ks WITH replication = {'class':'SimpleStrategy','replication_factor':1};
) else (
    echo  Keyspace creado OK.
)

REM ---------- 5. Iniciar backend en nueva ventana ----------
echo.
echo [5/5] Iniciando backend Spring Boot...
start "Backend - Spring Boot" cmd /k "cd /d C:\Users\JORGE\Videos\Captures\Facturacion.it && mvn spring-boot:run"

REM ---------- 6. Esperar backend y arrancar frontend ----------
echo.
echo Esperando 30 seg para que el backend arranque...
timeout /t 30 /nobreak

echo.
echo [+] Iniciando frontend React...
start "Frontend - React" cmd /k "cd /d C:\Users\JORGE\Videos\Captures\Facturacion.it\frontend && npm install && npm start"

echo.
echo ============================================================
echo   LISTO!
echo   Backend:  http://localhost:8080
echo   Frontend: http://localhost:3000
echo ============================================================
echo.
pause
