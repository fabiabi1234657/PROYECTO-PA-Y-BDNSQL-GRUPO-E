"""
Script Principal de Ejecución y Demostración de Machine Learning.
Permite entrenar el modelo con datos de Apache Druid, evaluar su desempeño y predecir en vivo.
"""

import sys
import os
from druid_client import obtener_datos
from modelo_regresion import ModeloPrediccionDemanda


def imprimir_separador(titulo=""):
    print("\n" + "=" * 75)
    if titulo:
        print(f"  {titulo.upper()}")
        print("=" * 75)


def ejecutar_demostracion():
    print("""
    ╔═══════════════════════════════════════════════════════════════════════╗
    ║       PROYECTO BDNSQL & PA - MODELO DE MACHINE LEARNING               ║
    ║   Regresión Lineal Múltiple sobre Hechos OLAP de Apache Druid         ║
    ╚═══════════════════════════════════════════════════════════════════════╝
    """)

    # 1. Extracción de Datos desde Apache Druid
    imprimir_separador("Paso 1: Extracción de Hechos OLAP desde Apache Druid")
    df_hechos, origen = obtener_datos(preferir_druid=True)
    print(f"-> Origen de datos: {origen}")
    print(f"-> Filas obtenidas: {len(df_hechos)}")
    print(f"-> Columnas disponibles: {list(df_hechos.columns)}")
    print("\nVista previa de los primeros 3 registros:")
    print(df_hechos[["facturaId", "clienteCategoria", "clienteCiudad", "productoNombre", "cantidad", "precioUnitario", "subtotal"]].head(3).to_string(index=False))

    # 2. Entrenamiento del Modelo de Regresión Lineal Múltiple
    imprimir_separador("Paso 2: Entrenamiento del Modelo de Regresión Lineal Múltiple")
    modelo = ModeloPrediccionDemanda()
    metricas = modelo.entrenar(df_hechos, origen=origen)

    print("Métricas de Rendimiento del Modelo:")
    print(f"  * Coeficiente de Determinación R² (Test): {metricas['r2_test']:.4f} (Explica el {metricas['r2_test']*100:.1f}% de la varianza)")
    print(f"  * Coeficiente de Determinación R² (Train): {metricas['r2_train']:.4f}")
    print(f"  * Error Absoluto Medio (MAE):            {metricas['mae']:.2f} unidades")
    print(f"  * Raíz del Error Cuadrático Medio (RMSE): {metricas['rmse']:.2f} unidades")
    print(f"  * Total de Muestras:                     {metricas['total_muestras']} (Train: {metricas['muestras_train']}, Test: {metricas['muestras_test']})")

    # 3. Explicación Matemática de la Ecuación Aprendida
    imprimir_separador("Paso 3: Ecuación Matemática y Coeficientes Aprendidos")
    print(modelo.obtener_resumen_ecuacion())

    # 4. Generación de Gráficos de Evaluación
    imprimir_separador("Paso 4: Generación de Gráficos de Diagnóstico")
    ruta_grafica = os.path.join(os.path.dirname(__file__), "graficas", "evaluacion_modelo.png")
    modelo.generar_graficas(ruta_grafica)
    print(f"-> Gráficos guardados exitosamente en: {ruta_grafica}")

    # 5. Demostración de Predicciones en Vivo (Casos de Estudio de Negocio)
    imprimir_separador("Paso 5: Demostración de Predicciones de Negocio en Vivo")

    casos_prueba = [
        {
            "descripcion": "Caso A: Cliente MAYORISTA en Mocoa comprando Arroz Diana 1kg",
            "categoria": "MAYORISTA",
            "ciudad": "Mocoa",
            "producto": "Arroz Diana 1kg",
            "precio": 3910.0,
            "costo": 3800.0
        },
        {
            "descripcion": "Caso B: Cliente MINORISTA en Pasto comprando Café Juan Valdez 250g",
            "categoria": "MINORISTA",
            "ciudad": "Pasto",
            "producto": "Cafe Juan Valdez 250g",
            "precio": 18500.0,
            "costo": 14000.0
        },
        {
            "descripcion": "Caso C: Cliente DISTRIBUIDOR en Bogotá comprando Aceite Premier 1L",
            "categoria": "DISTRIBUIDOR",
            "ciudad": "Bogota",
            "producto": "Aceite Premier 1L",
            "precio": 8400.0,
            "costo": 8500.0
        },
        {
            "descripcion": "Caso D: Cliente CORPORATIVO en Cali comprando Azúcar Incauca 1kg",
            "categoria": "CORPORATIVO",
            "ciudad": "Cali",
            "producto": "Azucar Incauca 1kg",
            "precio": 3960.0,
            "costo": 3500.0
        }
    ]

    for c in casos_prueba:
        resultado = modelo.predecir(
            cliente_categoria=c["categoria"],
            cliente_ciudad=c["ciudad"],
            producto_nombre=c["producto"],
            precio_unitario=c["precio"],
            costo_unitario=c["costo"]
        )
        print(f"\n{c['descripcion']}:")
        print(f"   - Cantidad predicha: {resultado['cantidad_predicha']} unidades (Exacta: {resultado['cantidad_exacta_modelo']})")
        print(f"   - Subtotal estimado: ${resultado['subtotal_estimado']:,.2f} COP")
        print(f"   - Ganancia estimada: ${resultado['ganancia_estimada']:,.2f} COP")

    imprimir_separador("Demostración Finalizada con Éxito")
    print("El modelo cumple con los requisitos para trabajar de forma 100% local,")
    print("sin sobrecargar la memoria RAM, e interpretando los patrones leídos desde Apache Druid.")


if __name__ == "__main__":
    ejecutar_demostracion()
