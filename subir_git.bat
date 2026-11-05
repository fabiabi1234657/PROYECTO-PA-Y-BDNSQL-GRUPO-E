@echo off
title Subir proyecto a GitHub
color 0A
echo.
echo ============================================================
echo   SUBIENDO PROYECTO A GITHUB
echo ============================================================
echo.

cd /d "C:\Users\JORGE\Videos\Captures\Facturacion.it"

echo [1] Inicializando repositorio git...
git init

echo.
echo [2] Agregando todos los archivos...
git add .

echo.
echo [3] Creando commit inicial...
git commit -m "feat: Sistema de Facturacion - Arquitectura Hexagonal Spring Boot + React"

echo.
echo [4] Configurando rama principal como main...
git branch -M main

echo.
echo [5] Conectando con GitHub...
git remote remove origin 2>nul
git remote add origin https://github.com/fabiabi1234657/PROYECTO-PA-Y-BDNSQL-GRUPO-E.git

echo.
echo [6] Subiendo al repositorio...
git push -u origin main

echo.
if %errorlevel% equ 0 (
    color 0A
    echo ============================================================
    echo   LISTO! Proyecto subido exitosamente a GitHub
    echo   https://github.com/fabiabi1234657/PROYECTO-PA-Y-BDNSQL-GRUPO-E
    echo ============================================================
) else (
    color 0C
    echo ============================================================
    echo   Hubo un error al subir. Revisa:
    echo   1. Que tengas sesion iniciada en GitHub
    echo   2. Que tengas permisos en el repositorio
    echo   Intenta: git push -u origin main
    echo ============================================================
)
echo.
pause
