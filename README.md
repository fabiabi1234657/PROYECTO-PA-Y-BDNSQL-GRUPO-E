# Proyecto PA y BDNSQL - Grupo E

Sistema integral de facturación electrónica y analítica predictiva desarrollado bajo **Arquitectura Hexagonal**, **Persistencia Políglota** y **Machine Learning sobre Apache Druid**.

---

## Tecnologías Utilizadas
- **Java 21 y Spring Boot 3.4.0** (Backend Hexagonal, Puertos y Adaptadores)
- **MongoDB** (Catálogo dinámico de productos y proveedores)
- **Apache Cassandra** (Registro tabular distribuido de personas y clientes)
- **MySQL 8.0** (Persistencia transaccional ACID de facturas)
- **Apache Druid 31.0** (Respaldo analítico OLAP y agregación dimensional)
- **Python 3.10+ / Scikit-Learn** (Modelo de Regresión Lineal Múltiple para predicción de demanda)
- **React 18** (Interfaz de usuario moderna con navegación por módulos)

---

## Ejecución del Proyecto

### 1. Bases de Datos (Docker)
Levantar los contenedores desde la raíz del proyecto:
```bash
docker-compose up -d
```

Crear el keyspace en Cassandra una vez el contenedor esté activo:
```bash
docker exec -it facturacion-cassandra cqlsh -e "CREATE KEYSPACE IF NOT EXISTS facturacion_ks WITH replication = {'class': 'SimpleStrategy', 'replication_factor': 1};"
```

### 2. Backend (Spring Boot)
```bash
./mvnw spring-boot:run
```
- API REST: `http://localhost:8080`

### 3. Modelo de Machine Learning (Python)
Para ejecutar la demostración en consola o activar el microservicio REST:
```bash
# Demostración interactiva en consola
python ml/run_ml.py

# Microservicio API REST opcional (puerto 5000)
python ml/api_service.py
```

### 4. Frontend (React)
```bash
cd frontend
npm install
npm start
```
- Aplicación Web: `http://localhost:3000`

---

## Módulos y Funcionalidades del Frontend

1. **🧾 Facturar:** Emisión interactiva de facturas con cálculo en tiempo real de subtotal, IVA (19%) y total. Soporta carga automática de recomendaciones calculadas por Machine Learning.
2. **📜 Historial de Facturas (MySQL):** Auditoría completa de facturas emitidas, búsqueda dinámica por cliente/documento/número, visualización detallada y reimpresión de comprobantes.
3. **🧠 Machine Learning & Predicción (Druid):**
   - Panel de métricas en tiempo real ($R^2$, MAE, RMSE, número de muestras).
   - Simulador predictivo: selección de categoría de cliente, ciudad, producto y precio simulado para predecir unidades demandadas, subtotal y margen comercial.
   - Botón directo para cargar la recomendación del modelo a una factura real.
   - Visor integrado de gráficas de diagnóstico (Reales vs Predichos, Residuales, Coeficientes $\beta$).
   - Botón para re-entrenar el modelo con los hechos de Druid.
4. **👥 Clientes (Cassandra):** Vista tabular de clientes registrados y formulario modal para dar de alta nuevos clientes en Cassandra.
5. **📦 Productos (MongoDB):** Catálogo visual de productos con cálculo de márgenes y formulario modal para crear nuevos productos en MongoDB.
6. **🔄 Sincronización OLAP (Druid):** Disparador del proceso ETL hacia Druid y mapa de puertos/servicios de la infraestructura analítica.

---

## Endpoints Principales del Backend

- `GET /api/products` - Listar productos (MongoDB)
- `POST /api/products` - Crear producto (MongoDB)
- `GET /api/persons` - Listar personas (Cassandra)
- `POST /api/persons` - Registrar persona (Cassandra)
- `POST /api/invoices` - Emitir factura (MySQL)
- `GET /api/invoices` - Listar facturas (MySQL)
- `POST /api/druid/sync` - Ejecutar proceso ETL hacia Apache Druid
- `POST http://localhost:5000/api/ml/predict` - Inferencia predictiva de demanda (Python ML)


## Validación de emisión y resultados analíticos

- La emisión exige un cliente registrado, productos existentes con precio válido y cantidades positivas. Los errores de validación devuelven HTTP 400 con `message`. El nombre del cliente y los precios se obtienen del catálogo y del registro de personas.
- El servidor asigna fecha de emisión UTC y estado `EMITIDA`. Las fechas sin zona en la API se interpretan como UTC; recibos e historial las muestran en la zona del navegador. Los registros históricos no se migran automáticamente: conviene revisar su zona de origen antes de reenviarlos a Druid.
- `POST /api/druid/sync` devuelve HTTP 202 y `status: SUBMITTED` cuando se acepta el envío, con `hechosEnviados`. Esto no confirma la finalización de la tarea; puede consultarse en la consola de Druid. Sin hechos devuelve HTTP 200 / `NO_DATA`; un envío rechazado devuelve HTTP 502 / `ERROR`, y un fallo del proceso ETL devuelve HTTP 500 / `ERROR`. Los detalles técnicos quedan en el registro del servidor.
- La sincronización conserva el reemplazo del histórico; no activa carga incremental ni anexado de datos.
- El panel de ML muestra únicamente métricas recibidas del servicio y su fuente declarada. Sin conexión o sin entrenamiento muestra métricas pendientes; no calcula predicciones de respaldo con coeficientes fijos. El botón **Entrenar y Medir Ahora** solicita nuevas métricas. Python aún puede usar datos simulados cuando Druid no está disponible, y el panel identifica esa fuente.
- Al cargar una recomendación a la factura se usa el precio vigente del catálogo, aunque el escenario de predicción utilice un precio simulado.


### Orígenes permitidos del frontend

Los endpoints Java permiten solicitudes del navegador desde `http://localhost:3000` y `http://127.0.0.1:3000`. Si el frontend se publica en otra dirección, hay que configurar ese origen explícitamente en los controladores. Los estados y cuerpos de respuesta de la sincronización con Druid se conservan.
