@echo off
title Insertar Datos de Prueba
color 0E
echo.
echo ============================================================
echo   INSERTANDO DATOS DE PRUEBA
echo ============================================================
echo.

echo [1] Creando productos en MongoDB...
curl -s -X POST http://localhost:8080/api/products ^
  -H "Content-Type: application/json" ^
  -d "{\"nombre\":\"Laptop HP\",\"descripcion\":\"Laptop 15 pulgadas Intel i5\",\"costo\":1500000,\"precioVenta\":2200000,\"imageUrls\":[],\"proveedor\":{\"nombre\":\"TechDist\",\"contacto\":\"ventas@techdist.com\",\"telefono\":\"3101234567\"}}"
echo.

curl -s -X POST http://localhost:8080/api/products ^
  -H "Content-Type: application/json" ^
  -d "{\"nombre\":\"Mouse Logitech\",\"descripcion\":\"Mouse inalambrico ergonomico\",\"costo\":45000,\"precioVenta\":85000,\"imageUrls\":[],\"proveedor\":{\"nombre\":\"TechDist\",\"contacto\":\"ventas@techdist.com\",\"telefono\":\"3101234567\"}}"
echo.

curl -s -X POST http://localhost:8080/api/products ^
  -H "Content-Type: application/json" ^
  -d "{\"nombre\":\"Teclado Mecanico\",\"descripcion\":\"Teclado mecanico RGB switches blue\",\"costo\":120000,\"precioVenta\":220000,\"imageUrls\":[],\"proveedor\":{\"nombre\":\"GameGear\",\"contacto\":\"info@gamegear.co\",\"telefono\":\"3209876543\"}}"
echo.

echo [2] Creando clientes en Cassandra...
curl -s -X POST http://localhost:8080/api/persons ^
  -H "Content-Type: application/json" ^
  -d "{\"documento\":\"1001234567\",\"nombre\":\"Carlos Ramirez\",\"correo\":\"carlos@gmail.com\",\"telefono\":\"3151234567\",\"ciudad\":\"Bogota\",\"categoria\":\"CLIENTE\"}"
echo.

curl -s -X POST http://localhost:8080/api/persons ^
  -H "Content-Type: application/json" ^
  -d "{\"documento\":\"1009876543\",\"nombre\":\"Ana Torres\",\"correo\":\"ana@hotmail.com\",\"telefono\":\"3167654321\",\"ciudad\":\"Medellin\",\"categoria\":\"CLIENTE\"}"
echo.

curl -s -X POST http://localhost:8080/api/persons ^
  -H "Content-Type: application/json" ^
  -d "{\"documento\":\"1005551234\",\"nombre\":\"Luis Gomez\",\"correo\":\"luis@empresa.co\",\"telefono\":\"3123334444\",\"ciudad\":\"Cali\",\"categoria\":\"EMPLEADO\"}"
echo.

echo.
echo ============================================================
echo   Datos de prueba insertados!
echo   Abre http://localhost:3000 para ver el sistema
echo ============================================================
pause
