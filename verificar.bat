@echo off
title Verificacion del Sistema
color 0B
echo.
echo ============================================================
echo   VERIFICACION DEL SISTEMA DE FACTURACION
echo ============================================================
echo.

echo [Contenedores Docker]
docker ps --format "table {{.Names}}\t{{.Status}}\t{{.Ports}}"

echo.
echo [Probando Backend - API Productos]
curl -s http://localhost:8080/api/products
echo.

echo [Probando Backend - API Personas]
curl -s http://localhost:8080/api/persons
echo.

echo [Probando Backend - API Facturas]
curl -s http://localhost:8080/api/invoices
echo.

echo.
echo ============================================================
echo   Si ves [] o {} los endpoints responden correctamente.
echo   Si ves error de conexion, el backend aun no arranco.
echo ============================================================
pause
