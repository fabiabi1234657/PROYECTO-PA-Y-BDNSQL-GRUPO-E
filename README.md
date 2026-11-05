# Sistema de Facturación — Arquitectura Hexagonal

## Tecnologías
- **Backend:** Spring Boot 3.4.0, Java 21
- **Bases de datos:** MongoDB (productos), Cassandra (personas), MySQL (facturas)
- **Async:** @Async + ThreadPoolTaskExecutor para backup analítico
- **Frontend:** React 18

## Estructura del proyecto
```
Facturacion.it/
├── pom.xml
├── docker-compose.yml
├── src/main/java/com/facturacion/
│   ├── domain/
│   │   ├── model/        → Product, Person, Invoice, InvoiceDetail, Supplier
│   │   └── port/
│   │       ├── in/       → Puertos de entrada (casos de uso)
│   │       └── out/      → Puertos de salida (repositorios)
│   ├── application/      → Servicios: ProductService, PersonService, InvoiceService
│   └── infrastructure/
│       ├── adapter/
│       │   ├── mongodb/   → ProductMongoAdapter
│       │   ├── cassandra/ → PersonCassandraAdapter
│       │   ├── mysql/     → InvoiceMysqlAdapter
│       │   └── backup/    → InvoiceBackupAdapter (@Async)
│       ├── rest/          → Controllers REST
│       └── config/        → AsyncConfig, ThreadPoolTaskExecutor
└── frontend/              → React App
```

## Pasos para ejecutar

### 1. Levantar bases de datos con Docker
```powershell
cd C:\Users\JORGE\Videos\Captures\Facturacion.it
docker-compose up -d
```

### 2. Esperar que Cassandra esté lista (~30 seg) y crear keyspace
```powershell
docker exec -it facturacion-cassandra cqlsh
```
Dentro de cqlsh ejecutar:
```sql
CREATE KEYSPACE IF NOT EXISTS facturacion_ks
  WITH replication = {'class': 'SimpleStrategy', 'replication_factor': 1};
```

### 3. Compilar y ejecutar el backend
```powershell
cd C:\Users\JORGE\Videos\Captures\Facturacion.it
mvn spring-boot:run
```

### 4. Ejecutar el frontend React
```powershell
cd C:\Users\JORGE\Videos\Captures\Facturacion.it\frontend
npm install
npm start
```

El frontend abre en http://localhost:3000  
El backend corre en http://localhost:8080

## API REST disponible
| Método | URL | Descripción |
|--------|-----|-------------|
| GET | /api/products | Listar productos |
| POST | /api/products | Crear producto |
| GET | /api/persons | Listar personas |
| GET | /api/persons/categoria/CLIENTE | Clientes |
| POST | /api/persons | Crear persona |
| POST | /api/invoices | Emitir factura |
| GET | /api/invoices | Listar facturas |
| GET | /api/invoices/{id} | Obtener factura |

## Concepto de backup asíncrono
Al emitir una factura, el sistema:
1. Guarda la factura en MySQL (síncrono → responde al cliente inmediatamente)
2. Lanza un hilo `backup-thread-*` via `@Async` que simula el envío al motor analítico/ML
3. El cliente **no espera** el backup — lo ve en los logs del servidor
