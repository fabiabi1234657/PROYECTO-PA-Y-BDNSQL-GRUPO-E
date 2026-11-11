"""
Microservicio API REST con Flask para el Modelo de Machine Learning.
Permite re-entrenar el modelo y consultar predicciones en tiempo real vía HTTP/JSON.
"""

from flask import Flask, request, jsonify, send_file
import os
from druid_client import obtener_datos
from modelo_regresion import ModeloPrediccionDemanda

app = Flask(__name__)

# Soporte CORS sin dependencias externas
@app.before_request
def handle_preflight():
    if request.method == "OPTIONS":
        res = app.make_default_options_response()
        res.headers['Access-Control-Allow-Origin'] = '*'
        res.headers['Access-Control-Allow-Headers'] = 'Content-Type,Authorization'
        res.headers['Access-Control-Allow-Methods'] = 'GET,PUT,POST,DELETE,OPTIONS'
        return res

@app.after_request
def add_cors_headers(response):
    response.headers['Access-Control-Allow-Origin'] = '*'
    response.headers['Access-Control-Allow-Headers'] = 'Content-Type,Authorization'
    response.headers['Access-Control-Allow-Methods'] = 'GET,PUT,POST,DELETE,OPTIONS'
    return response

# Instancia global del modelo en memoria (muy bajo consumo de RAM)
modelo_global = ModeloPrediccionDemanda()
origen_actual = "No entrenado"
ruta_grafica = os.path.join(os.path.dirname(__file__), "graficas", "evaluacion_modelo.png")


def inicializar():
    global origen_actual
    df, origen = obtener_datos(preferir_druid=True)
    modelo_global.entrenar(df, origen=origen)
    modelo_global.generar_graficas(ruta_grafica)
    origen_actual = origen


@app.route("/api/ml/status", methods=["GET"])
def get_status():
    """Retorna el estado actual del modelo y sus métricas."""
    return jsonify({
        "status": "ACTIVO",
        "modelo": "Regresion Lineal Multiple",
        "origen_datos": origen_actual,
        "metricas": modelo_global.metricas,
        "intercepto_beta0": modelo_global.intercepto
    })


@app.route("/api/ml/train", methods=["POST"])
def reentrenar():
    """Re-entrena el modelo consultando datos frescos de Druid."""
    global origen_actual
    df, origen = obtener_datos(preferir_druid=True)
    metricas = modelo_global.entrenar(df, origen=origen)
    modelo_global.generar_graficas(ruta_grafica)
    origen_actual = origen
    return jsonify({
        "mensaje": "Modelo reentrenado con exito",
        "origen_datos": origen,
        "metricas": metricas
    })


@app.route("/api/ml/predict", methods=["POST"])
def predecir():
    """
    Predice la cantidad demandada y el subtotal proyectado.
    JSON esperado:
    {
        "clienteCategoria": "MAYORISTA",
        "clienteCiudad": "Mocoa",
        "productoNombre": "Arroz Diana 1kg",
        "precioUnitario": 3910.0,
        "costoUnitario": 3800.0
    }
    """
    data = request.get_json(force=True)
    requeridos = ["clienteCategoria", "clienteCiudad", "productoNombre", "precioUnitario", "costoUnitario"]
    for campo in requeridos:
        if campo not in data:
            return jsonify({"error": f"Falta el campo obligatorio: {campo}"}), 400

    try:
        resultado = modelo_global.predecir(
            cliente_categoria=str(data["clienteCategoria"]),
            cliente_ciudad=str(data["clienteCiudad"]),
            producto_nombre=str(data["productoNombre"]),
            precio_unitario=float(data["precioUnitario"]),
            costo_unitario=float(data["costoUnitario"])
        )
        return jsonify(resultado)
    except Exception as e:
        return jsonify({"error": str(e)}), 500


@app.route("/api/ml/grafica", methods=["GET"])
def descargar_grafica():
    """Sirve la imagen con los gráficos de evaluación del modelo."""
    if os.path.exists(ruta_grafica):
        return send_file(ruta_grafica, mimetype="image/png")
    return jsonify({"error": "La gráfica aún no ha sido generada."}), 404


if __name__ == "__main__":
    print("[Flask] Inicializando y entrenando modelo localmente...")
    inicializar()
    print("[Flask] Servidor ML activo en http://localhost:5000")
    app.run(host="0.0.0.0", port=5000, debug=False)
