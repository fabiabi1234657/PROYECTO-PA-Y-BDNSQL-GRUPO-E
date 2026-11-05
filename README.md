# Proyecto PA y BDNSQL - Grupo E

Sistema de facturacion desarrollado con Spring Boot y React bajo arquitectura hexagonal y persistencia poliglota.

## Tecnologias utilizadas
- Java 21 y Spring Boot 3.4.0
- MongoDB (catalogo de productos)
- Apache Cassandra (registro de personas y clientes)
- MySQL (facturas y detalles de factura)
- React 18 (interfaz web)

## Ejecucion del proyecto

### 1. Bases de datos (Docker)
Levantar los contenedores desde la raiz del proyecto:
```bash
docker-compose up -d
```

Crear el keyspace en Cassandra una vez el contenedor este activo:
```bash
docker exec -it facturacion-cassandra cqlsh -e "CREATE KEYSPACE IF NOT EXISTS facturacion_ks WITH replication = {'class': 'SimpleStrategy', 'replication_factor': 1};"
```

### 2. Backend
```bash
mvn spring-boot:run
```
Al iniciar, la aplicacion registra automaticamente datos de prueba en MongoDB y Cassandra si las colecciones/tablas estan vacias.

### 3. Frontend
```bash
cd frontend
npm install
npm start
```
- Frontend: `http://localhost:3000`
- Backend: `http://localhost:8080`

## Endpoints principales
- `GET /api/products` - Listar productos
- `POST /api/products` - Crear producto
- `GET /api/persons` - Listar personas
- `GET /api/persons/categoria/CLIENTE` - Filtrar por categoria
- `POST /api/persons` - Registrar persona
- `POST /api/invoices` - Emitir factura
- `GET /api/invoices` - Listar facturas
