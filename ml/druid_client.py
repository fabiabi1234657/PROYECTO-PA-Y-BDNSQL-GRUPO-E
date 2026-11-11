"""
Módulo de conexión y extracción de datos desde Apache Druid (o simulación de hechos OLAP).
Permite consultar la tabla 'hechos_ventas' almacenada en Druid mediante su API SQL (Broker/Router).
"""

import os
import json
import logging
import random
import requests
import pandas as pd
import numpy as np

logging.basicConfig(level=logging.INFO, format="%(asctime)s [%(levelname)s] %(message)s")
logger = logging.getLogger("DruidClient")

# URL por defecto del router/broker de Apache Druid
DRUID_SQL_URL = os.getenv("DRUID_SQL_URL", "http://localhost:8888/druid/v2/sql")
DATASOURCE_NAME = os.getenv("DRUID_DATASOURCE", "hechos_ventas")


def consultar_hechos_druid(druid_url: str = DRUID_SQL_URL, datasource: str = DATASOURCE_NAME) -> pd.DataFrame:
    """
    Ejecuta una consulta SQL en Apache Druid para extraer los hechos de ventas.
    Retorna un pandas DataFrame. Si la conexión falla o no hay datos, retorna un DataFrame vacío.
    """
    query_payload = {
        "query": f"""
            SELECT 
                "__time" AS fecha,
                facturaId,
                clienteDocumento,
                clienteNombre,
                clienteCiudad,
                clienteCategoria,
                productoId,
                productoNombre,
                proveedorNombre,
                cantidad,
                precioUnitario,
                costoUnitario,
                subtotal,
                ganancia
            FROM "{datasource}"
            LIMIT 50000
        """
    }

    headers = {"Content-Type": "application/json"}
    logger.info(f"Intentando conectar a Apache Druid en: {druid_url}")

    try:
        response = requests.post(druid_url, json=query_payload, headers=headers, timeout=5)
        if response.status_code == 200:
            data = response.json()
            if isinstance(data, list) and len(data) > 0:
                df = pd.DataFrame(data)
                logger.info(f"Éxito: Se obtuvieron {len(df)} registros directamente desde Druid ('{datasource}').")
                return normalizar_dataframe(df)
            else:
                logger.warning(f"Druid respondió pero la tabla '{datasource}' está vacía o sin datos.")
        else:
            logger.warning(f"Druid respondió con código {response.status_code}: {response.text[:200]}")
    except requests.exceptions.RequestException as e:
        logger.warning(f"No fue posible conectar con Druid ({e.__class__.__name__}). Es normal si los contenedores no están activos.")

    return pd.DataFrame()


def normalizar_dataframe(df: pd.DataFrame) -> pd.DataFrame:
    """Convierte los tipos de datos numéricos adecuadamente."""
    numeric_cols = ["cantidad", "precioUnitario", "costoUnitario", "subtotal", "ganancia", "facturaId"]
    for col in numeric_cols:
        if col in df.columns:
            df[col] = pd.to_numeric(df[col], errors="coerce").fillna(0)
    return df


def generar_dataset_simulado(n_filas: int = 500, random_seed: int = 42) -> pd.DataFrame:
    """
    Genera un dataset sintético idéntico al esquema de 'VentaFact' en Druid.
    Este generador introduce patrones estadísticos reales para que el modelo de Machine Learning
    (Regresión Lineal Múltiple) aprenda relaciones de demanda por categoría de cliente y producto:
    - Clientes MAYORISTAS / DISTRIBUIDORES compran en grandes volúmenes.
    - Clientes MINORISTAS / OCASIONALES compran pocas unidades.
    - El precio y costo unitario afectan la cantidad (elasticidad precio de la demanda).
    """
    random.seed(random_seed)
    np.random.seed(random_seed)

    categorias_cliente = {
        "MAYORISTA": {"base_qty": 35, "std_qty": 8, "dcto": 0.15},
        "DISTRIBUIDOR": {"base_qty": 60, "std_qty": 12, "dcto": 0.20},
        "CORPORATIVO": {"base_qty": 20, "std_qty": 5, "dcto": 0.10},
        "MINORISTA": {"base_qty": 3, "std_qty": 1.2, "dcto": 0.0},
        "OCASIONAL": {"base_qty": 1.8, "std_qty": 0.8, "dcto": 0.0}
    }

    ciudades = ["Mocoa", "Pasto", "Bogota", "Cali", "Medellin", "Sibundoy", "Puerto Asis"]

    catalogo_productos = [
        {"id": "PROD-001", "nombre": "Arroz Diana 1kg", "costo": 3800.0, "precio": 4600.0, "proveedor": "Molinos Roa"},
        {"id": "PROD-002", "nombre": "Aceite Premier 1L", "costo": 8500.0, "precio": 10500.0, "proveedor": "Team Foods"},
        {"id": "PROD-003", "nombre": "Leche Alqueria 1L", "costo": 3200.0, "precio": 4100.0, "proveedor": "Alqueria S.A."},
        {"id": "PROD-004", "nombre": "Cafe Juan Valdez 250g", "costo": 14000.0, "precio": 18500.0, "proveedor": "Procafecol"},
        {"id": "PROD-005", "nombre": "Azucar Incauca 1kg", "costo": 3500.0, "precio": 4400.0, "proveedor": "Incauca"},
        {"id": "PROD-006", "nombre": "Atun Van Camps 160g", "costo": 5200.0, "precio": 6900.0, "proveedor": "Van Camps"},
        {"id": "PROD-007", "nombre": "Detergente Fab 1kg", "costo": 7000.0, "precio": 9200.0, "proveedor": "Colgate-Palmolive"},
        {"id": "PROD-008", "nombre": "Pasta Doria 500g", "costo": 2600.0, "precio": 3400.0, "proveedor": "Pastas Doria"}
    ]

    registros = []
    for i in range(1, n_filas + 1):
        cat = random.choice(list(categorias_cliente.keys()))
        perfil = categorias_cliente[cat]
        prod = random.choice(catalogo_productos)
        ciudad = random.choice(ciudades)

        # Elasticidad precio: a mayor precio relativo, ligera reducción de cantidad
        factor_precio = 1.0 - (prod["precio"] / 30000.0) * 0.1
        # Cantidad simulada con distribución normal + ruido estocástico
        cantidad = max(1, int(round(np.random.normal(perfil["base_qty"] * factor_precio, perfil["std_qty"]))))

        precio_unitario = round(prod["precio"] * (1.0 - perfil["dcto"]), 2)
        costo_unitario = prod["costo"]
        subtotal = round(cantidad * precio_unitario, 2)
        ganancia = round((precio_unitario - costo_unitario) * cantidad, 2)

        registros.append({
            "facturaId": 1000 + (i // 3),
            "clienteDocumento": f"CC-{10000000 + (i % 50)}",
            "clienteNombre": f"Cliente-{i % 50 + 1}",
            "clienteCiudad": ciudad,
            "clienteCategoria": cat,
            "productoId": prod["id"],
            "productoNombre": prod["nombre"],
            "proveedorNombre": prod["proveedor"],
            "cantidad": cantidad,
            "precioUnitario": precio_unitario,
            "costoUnitario": costo_unitario,
            "subtotal": subtotal,
            "ganancia": ganancia
        })

    logger.info(f"Dataset de hechos simulado generado: {n_filas} registros alineados al modelo OLAP de Druid.")
    return pd.DataFrame(registros)


def obtener_datos(preferir_druid: bool = True) -> tuple[pd.DataFrame, str]:
    """
    Obtiene los datos desde Druid si están disponibles; en caso contrario,
    genera y entrega los datos simulados con el mismo esquema.
    Retorna (df, origen).
    """
    if preferir_druid:
        df = consultar_hechos_druid()
        if not df.empty and len(df) >= 10:
            return df, "Apache Druid (En vivo)"

    logger.info("Usando dataset de hechos OLAP simulado para entrenamiento local.")
    df_sim = generar_dataset_simulado(n_filas=600)
    return df_sim, "Simulación Hechos OLAP (Respaldo Druid)"
