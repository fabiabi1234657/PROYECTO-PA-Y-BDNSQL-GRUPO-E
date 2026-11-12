"""
Modelo de Machine Learning: Regresión Lineal Múltiple.
Aprende patrones de compra y demanda a partir de hechos OLAP extraídos de Apache Druid.

Objetivo:
Predecir la cantidad demandada (unidades que comprará el cliente) y el subtotal proyectado
en función del tipo de cliente (categoría), ubicación (ciudad), tipo de producto y precio.
"""

import os
import json
import numpy as np
import pandas as pd
import matplotlib.pyplot as plt
from sklearn.model_selection import train_test_split
from sklearn.linear_model import LinearRegression
from sklearn.preprocessing import OneHotEncoder
from sklearn.compose import ColumnTransformer
from sklearn.pipeline import Pipeline
from sklearn.metrics import r2_score, mean_absolute_error, mean_squared_error


class ModeloPrediccionDemanda:
    """
    Pipeline de Regresión Lineal Múltiple para predecir la cantidad demandada.
    """

    def __init__(self):
        self.pipeline = None
        self.feature_names = []
        self.coeficientes_df = None
        self.intercepto = 0.0
        self.metricas = {}
        self.X_test = None
        self.y_test = None
        self.y_pred = None
        self.origen_datos = ""

    def entrenar(self, df: pd.DataFrame, origen: str = "Desconocido") -> dict:
        """
        Entrena el modelo de Regresión Lineal Múltiple con el DataFrame de hechos de Druid.
        """
        self.origen_datos = origen

        # Variables predictoras (X) y variable objetivo (y)
        columnas_categoricas = ["clienteCategoria", "clienteCiudad", "productoNombre"]
        columnas_numericas = ["precioUnitario", "costoUnitario"]
        features = columnas_categoricas + columnas_numericas
        target = "cantidad"

        # Limpieza básica
        df_clean = df.dropna(subset=features + [target]).copy()

        X = df_clean[features]
        y = df_clean[target]

        # Partición en entrenamiento (80%) y prueba (20%)
        X_train, X_test, y_train, y_test = train_test_split(
            X, y, test_size=0.20, random_state=42
        )

        # Preprocesamiento: One-Hot Encoding para categorías con drop='first' para evitar multicolinealidad
        preprocessor = ColumnTransformer(
            transformers=[
                ("cat", OneHotEncoder(drop="first", sparse_output=False, handle_unknown="ignore"), columnas_categoricas),
                ("num", "passthrough", columnas_numericas)
            ]
        )

        # Regresión Lineal Múltiple
        regresor = LinearRegression()

        # Construcción del Pipeline
        self.pipeline = Pipeline(steps=[
            ("preprocessor", preprocessor),
            ("regressor", regresor)
        ])

        # Ajuste del modelo
        self.pipeline.fit(X_train, y_train)

        # Predicciones de evaluación
        y_train_pred = self.pipeline.predict(X_train)
        y_test_pred = self.pipeline.predict(X_test)

        self.X_test = X_test
        self.y_test = y_test
        self.y_pred = y_test_pred

        # Cálculo de métricas
        r2_train = r2_score(y_train, y_train_pred)
        r2_test = r2_score(y_test, y_test_pred)
        mae = mean_absolute_error(y_test, y_test_pred)
        mse = mean_squared_error(y_test, y_test_pred)
        rmse = np.sqrt(mse)

        self.metricas = {
            "origen_datos": self.origen_datos,
            "total_muestras": len(df_clean),
            "muestras_train": len(X_train),
            "muestras_test": len(X_test),
            "r2_train": round(float(r2_train), 4),
            "r2_test": round(float(r2_test), 4),
            "mae": round(float(mae), 4),
            "mse": round(float(mse), 4),
            "rmse": round(float(rmse), 4)
        }

        # Extraer nombres de las variables codificadas y coeficientes
        ohe = self.pipeline.named_steps["preprocessor"].named_transformers_["cat"]
        cat_feature_names = list(ohe.get_feature_names_out(columnas_categoricas))
        all_features = cat_feature_names + columnas_numericas
        self.feature_names = all_features

        reg_model = self.pipeline.named_steps["regressor"]
        self.intercepto = float(reg_model.intercept_)
        coefs = reg_model.coef_

        self.coeficientes_df = pd.DataFrame({
            "Variable": all_features,
            "Coeficiente_Beta": coefs,
            "Impacto_Absoluto": np.abs(coefs)
        }).sort_values(by="Impacto_Absoluto", ascending=False)

        return self.metricas

    def obtener_resumen_ecuacion(self) -> str:
        """Retorna una representación matemática legible de la ecuación de regresión múltiple."""
        if self.coeficientes_df is None:
            return "El modelo no ha sido entrenado."

        lineas = [
            "=" * 70,
            "  ECUACIÓN DE REGRESIÓN LINEAL MÚLTIPLE APRENDIDA (DESDE DRUID)",
            "=" * 70,
            f"  Variable Objetivo (Y): Cantidad de Unidades Demandadas",
            f"  Intercepto base (β₀): {self.intercepto:.4f}",
            "",
            "  Fórmula general:",
            "  Cantidad = β₀ + (β₁ · X₁) + (β₂ · X₂) + ... + (βₙ · Xₙ)",
            "",
            "  Top 10 Variables con Mayor Impacto (Coeficientes β):",
            "-" * 70
        ]

        for _, row in self.coeficientes_df.head(10).iterrows():
            signo = "+" if row["Coeficiente_Beta"] >= 0 else "-"
            lineas.append(f"  {signo} {abs(row['Coeficiente_Beta']):10.4f}  *  [{row['Variable']}]")

        lineas.append("=" * 70)
        return "\n".join(lineas)

    def predecir(self, cliente_categoria: str, cliente_ciudad: str,
                 producto_nombre: str, precio_unitario: float, costo_unitario: float) -> dict:
        """
        Realiza una predicción para un escenario de negocio nuevo.
        """
        if self.pipeline is None:
            raise ValueError("El modelo debe ser entrenado antes de realizar predicciones.")

        input_df = pd.DataFrame([{
            "clienteCategoria": cliente_categoria,
            "clienteCiudad": cliente_ciudad,
            "productoNombre": producto_nombre,
            "precioUnitario": float(precio_unitario),
            "costoUnitario": float(costo_unitario)
        }])

        pred_qty = float(self.pipeline.predict(input_df)[0])
        # Una cantidad no puede ser negativa en compras reales
        pred_qty_ajustada = max(1, round(pred_qty))
        subtotal_estimado = round(pred_qty_ajustada * precio_unitario, 2)
        ganancia_estimada = round((precio_unitario - costo_unitario) * pred_qty_ajustada, 2)

        return {
            "clienteCategoria": cliente_categoria,
            "clienteCiudad": cliente_ciudad,
            "productoNombre": producto_nombre,
            "precioUnitario": precio_unitario,
            "costoUnitario": costo_unitario,
            "cantidad_exacta_modelo": round(pred_qty, 2),
            "cantidad_predicha": pred_qty_ajustada,
            "subtotal_estimado": subtotal_estimado,
            "ganancia_estimada": ganancia_estimada
        }

    def generar_graficas(self, ruta_guardado: str = "ml/graficas/evaluacion_modelo.png") -> str:
        """
        Genera gráficos visuales de evaluación del modelo y los guarda como archivo PNG.
        """
        if self.y_test is None or self.y_pred is None:
            raise ValueError("El modelo no tiene predicciones para graficar.")

        os.makedirs(os.path.dirname(ruta_guardado), exist_ok=True)

        fig, (ax1, ax2, ax3) = plt.subplots(1, 3, figsize=(18, 5))
        plt.style.use("seaborn-v0_8-whitegrid" if "seaborn-v0_8-whitegrid" in plt.style.available else "default")

        # 1. Gráfica Reales vs Predichos
        ax1.scatter(self.y_test, self.y_pred, color="#1f77b4", alpha=0.6, edgecolors="none")
        min_val = min(self.y_test.min(), self.y_pred.min())
        max_val = max(self.y_test.max(), self.y_pred.max())
        ax1.plot([min_val, max_val], [min_val, max_val], color="#d62728", linestyle="--", linewidth=2, label="Ajuste Ideal (y = x)")
        ax1.set_title(f"Valores Reales vs Predichos\n(R² = {self.metricas['r2_test']})", fontsize=12, fontweight="bold")
        ax1.set_xlabel("Cantidad Real")
        ax1.set_ylabel("Cantidad Predicha")
        ax1.legend()

        # 2. Distribución de Residuales (Errores)
        residuales = self.y_test - self.y_pred
        ax2.hist(residuales, bins=20, color="#2ca02c", edgecolor="black", alpha=0.7)
        ax2.axvline(0, color="red", linestyle="--", linewidth=1.5)
        ax2.set_title(f"Distribución de Residuales\n(MAE = {self.metricas['mae']:.2f}, RMSE = {self.metricas['rmse']:.2f})", fontsize=12, fontweight="bold")
        ax2.set_xlabel("Error Residual (Real - Predicho)")
        ax2.set_ylabel("Frecuencia")

        # 3. Coeficientes más influyentes (Top 8)
        top_coefs = self.coeficientes_df.head(8).sort_values(by="Impacto_Absoluto", ascending=True)
        colores = ["#2ca02c" if c >= 0 else "#d62728" for c in top_coefs["Coeficiente_Beta"]]
        ax3.barh(top_coefs["Variable"], top_coefs["Coeficiente_Beta"], color=colores)
        ax3.axvline(0, color="black", linestyle="-", linewidth=0.8)
        ax3.set_title("Coeficientes β de Mayor Impacto\n(Verde: Incrementa / Rojo: Reduce)", fontsize=12, fontweight="bold")
        ax3.set_xlabel("Valor del Coeficiente β")

        plt.tight_layout()
        plt.savefig(ruta_guardado, dpi=300)
        plt.close()

        return ruta_guardado
