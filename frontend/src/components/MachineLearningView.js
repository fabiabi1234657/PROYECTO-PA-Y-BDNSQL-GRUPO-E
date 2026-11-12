import React, { useState, useEffect } from 'react';
import axios from 'axios';
import './MachineLearningView.css';

const ML_API = process.env.REACT_APP_ML_API || 'http://127.0.0.1:5000/api/ml';
const BACKEND_API = 'http://localhost:8080/api';

function metricasMedidas(data) {
  const metricas = data?.metricas;
  return metricas && ['r2_test', 'mae', 'rmse', 'total_muestras'].every(
    (campo) => Number.isFinite(metricas[campo])
  ) && metricas.total_muestras > 0 ? metricas : null;
}

function MachineLearningView({ onCargarAFactura }) {
  const [productos, setProductos] = useState([]);
  const [categoriaCliente, setCategoriaCliente] = useState('MAYORISTA');
  const [ciudadCliente, setCiudadCliente] = useState('Mocoa');
  const [productoSeleccionado, setProductoSeleccionado] = useState(null);
  const [precioPersonalizado, setPrecioPersonalizado] = useState(4600);
  const [costoUnitario, setCostoUnitario] = useState(3800);

  const [prediccion, setPrediccion] = useState(null);
  const [metricas, setMetricas] = useState(null);
  const [servicioActivo, setServicioActivo] = useState(false);
  const [cargandoPrediccion, setCargandoPrediccion] = useState(false);
  const [reentrenando, setReentrenando] = useState(false);
  const [mensajeEstado, setMensajeEstado] = useState(null);
  const [verGrafica, setVerGrafica] = useState(false);

  useEffect(() => {
    // 1. Cargar productos desde MongoDB
    axios.get(`${BACKEND_API}/products`)
      .then((res) => {
        const prods = Array.isArray(res.data) ? res.data : [];
        setProductos(prods);
        if (prods.length > 0) {
          setProductoSeleccionado(prods[0]);
          setPrecioPersonalizado(prods[0].precioVenta || 4600);
          setCostoUnitario(prods[0].costo || 3800);
        }
      })
      .catch(() => {
        setProductos([]);
        setProductoSeleccionado(null);
        setMensajeEstado({ tipo: 'error', texto: 'No se pudo cargar el catálogo de productos.' });
      });

    // Verificar si el microservicio Flask ML está activo
    axios.get(`${ML_API}/status`, { timeout: 6000 })
      .then((res) => {
        setServicioActivo(true);
        setMetricas(metricasMedidas(res.data));
      })
      .catch(() => {
        setServicioActivo(false);
        setMetricas(null);
      });
  }, []);

  useEffect(() => {
    setPrediccion(null);
  }, [categoriaCliente, ciudadCliente, productoSeleccionado, precioPersonalizado, costoUnitario]);

  const handleProductoChange = (e) => {
    const prod = productos.find((p) => p.id === e.target.value);
    if (prod) {
      setProductoSeleccionado(prod);
      setPrecioPersonalizado(prod.precioVenta || 0);
      setCostoUnitario(prod.costo || 0);
    }
  };

  const calcularPrediccion = async () => {
    if (!productoSeleccionado) return;
    setCargandoPrediccion(true);
    setMensajeEstado(null);

    const payload = {
      clienteCategoria: categoriaCliente,
      clienteCiudad: ciudadCliente,
      productoNombre: productoSeleccionado.nombre,
      precioUnitario: Number(precioPersonalizado),
      costoUnitario: Number(costoUnitario)
    };

    // Intentar consultar al microservicio Flask ML
    try {
      const resp = await axios.post(`${ML_API}/predict`, payload, { timeout: 2500 });
      setPrediccion(resp.data);
      setServicioActivo(true);
    } catch (error) {
      setServicioActivo(false);
      setMetricas(null);
      setPrediccion(null);
      setMensajeEstado({
        tipo: 'error',
        texto: error.response?.data?.error || 'No se pudo obtener la predicción. Compruebe el servicio de ML.'
      });
    } finally {
      setCargandoPrediccion(false);
    }
  };

  const reentrenarModelo = async () => {
    setReentrenando(true);
    setMensajeEstado(null);
    setPrediccion(null);
    setMetricas(null);
    try {
      const resp = await axios.post(`${ML_API}/train`, {}, { timeout: 60000 });
      const medidas = metricasMedidas(resp.data);
      if (!medidas) throw new Error('El servicio no devolvió métricas válidas.');
      setMetricas(medidas);
      setMensajeEstado({
        tipo: 'exito',
        texto: `Modelo entrenado y medido. Fuente: ${resp.data.origen_datos || medidas.origen_datos || 'No informada'}.`
      });
      setServicioActivo(true);
    } catch (error) {
      setServicioActivo(false);
      setMetricas(null);
      setPrediccion(null);
      setMensajeEstado({
        tipo: 'error',
        texto: error.response?.data?.error || 'No se pudo completar el entrenamiento. Métricas no calculadas.'
      });
    } finally {
      setReentrenando(false);
    }
  };

  const formatMoney = (n) =>
    (n || 0).toLocaleString('es-CO', {
      style: 'currency',
      currency: 'COP',
      minimumFractionDigits: 0,
      maximumFractionDigits: 0
    });

  const enviarAFactura = () => {
    if (!prediccion || !productoSeleccionado) return;
    if (onCargarAFactura) {
      onCargarAFactura({
        producto: productoSeleccionado,
        cantidad: prediccion.cantidad_predicha,
        precioUnitario: Number(precioPersonalizado),
        categoriaCliente,
        ciudadCliente
      });
    }
  };

  return (
    <div className="ml-view-container">
      {/* Encabezado y Estado */}
      <div className="surface-card ml-hero-card">
        <div className="ml-hero-content">
          <div>
            <div className="ml-badge-group">
              <span className="source-tag">Apache Druid</span>
              <span className="source-tag ml-tag">Regresión Lineal Múltiple</span>
              <span className={`status-indicator ${servicioActivo ? 'status-online' : 'status-sim'}`}>
                {servicioActivo ? 'Servicio ML Online' : 'Servicio no conectado'}
              </span>
            </div>
            <h2 className="card-title">Módulo de Machine Learning & Análisis Predictivo</h2>
            <p className="card-subtitle">
              Predicción de volumen de demanda y subtotal en función de clientes, categorías y precios,
              utilizando los hechos OLAP sincronizados en Apache Druid.
            </p>
          </div>
          <div className="ml-hero-actions">
            <button
              type="button"
              className="btn-reentrenar"
              onClick={reentrenarModelo}
              disabled={reentrenando || cargandoPrediccion}
            >
              {reentrenando ? 'Reentrenando...' : 'Entrenar y Medir Ahora'}
            </button>
            <button
              type="button"
              className="btn-toggle-grafica"
              onClick={() => setVerGrafica(!verGrafica)}
              disabled={!servicioActivo || !metricas}
            >
              {verGrafica ? 'Ocultar Gráficas' : 'Ver Gráficas de Ajuste'}
            </button>
          </div>
        </div>

        {mensajeEstado && (
          <div className={`ml-banner banner-${mensajeEstado.tipo}`}>
            {mensajeEstado.texto}
          </div>
        )}

        {/* Tarjetas de Metricas */}
        {!metricas && <p role="status">Métricas no calculadas / Pendiente de entrenamiento.</p>}
        {metricas && <div className="ml-metrics-grid">
          <div className="ml-metric-card">
            <span className="ml-metric-title">Coeficiente R² (Test)</span>
            <strong className="ml-metric-num highlight">{metricas.r2_test}</strong>
            <span className="ml-metric-desc">Explica el {(metricas.r2_test * 100).toFixed(1)}% de la varianza</span>
          </div>
          <div className="ml-metric-card">
            <span className="ml-metric-title">Error Absoluto Medio (MAE)</span>
            <strong className="ml-metric-num">{metricas.mae} u.</strong>
            <span className="ml-metric-desc">Desviación promedio en cantidad</span>
          </div>
          <div className="ml-metric-card">
            <span className="ml-metric-title">Error Cuadrático (RMSE)</span>
            <strong className="ml-metric-num">{metricas.rmse} u.</strong>
            <span className="ml-metric-desc">Penalización de valores extremos</span>
          </div>
          <div className="ml-metric-card">
            <span className="ml-metric-title">Muestras Analizadas</span>
            <strong className="ml-metric-num">{metricas.total_muestras}</strong>
            <span className="ml-metric-desc">Fuente: {metricas.origen_datos}</span>
          </div>
        </div>}
      </div>

      {/* Seccion Gráfica de Diagnóstico Desplegable */}
      {verGrafica && servicioActivo && metricas && (
        <div className="surface-card ml-chart-container">
          <div className="card-header">
            <h3 className="card-title">Gráficos de Diagnóstico y Validación del Modelo</h3>
            <span className="source-tag">Matplotlib / Scikit-Learn</span>
          </div>
          <p className="card-subtitle">
            1) Dispersión Reales vs. Predichos (R² = {metricas.r2_test}) &bull; 2) Normalidad de Residuales &bull; 3) Coeficientes de mayor impacto.
          </p>
          <div className="chart-preview-box">
            <img
              src={`${ML_API}/grafica`}
              alt="Evaluación del Modelo de Machine Learning"
              className="chart-img"
              onError={(e) => {
                e.target.style.display = 'none';
                document.getElementById('chart-fallback').style.display = 'block';
              }}
            />
            <div id="chart-fallback" style={{ display: 'none' }} className="chart-fallback-info">
              <p><strong>Nota:</strong> Para visualizar la gráfica en vivo vía HTTP, ejecute en su terminal:</p>
              <code>python ml/api_service.py</code>
              <p>También puede consultar la imagen estática directamente en: <code>ml/graficas/evaluacion_modelo.png</code></p>
            </div>
          </div>
        </div>
      )}

      {/* Simulador Interactivo */}
      <div className="ml-simulator-layout">
        {/* Columna Izquierda: Parámetros del Escenario */}
        <div className="surface-card simulator-form-card">
          <div className="card-header">
            <h3 className="card-title">Simulador de Demanda en Tiempo Real</h3>
            <span className="source-tag">Regresión Múltiple</span>
          </div>
          <p className="card-subtitle">
            Configure los atributos del cliente y producto para calcular la demanda estimada por el modelo.
          </p>

          <div className="sim-field-group">
            <label className="field-label">Categoría del Cliente</label>
            <select
              className="select-control"
              value={categoriaCliente}
              onChange={(e) => setCategoriaCliente(e.target.value)}
            >
              <option value="DISTRIBUIDOR">DISTRIBUIDOR (Compra a gran escala)</option>
              <option value="MAYORISTA">MAYORISTA (Compra por volumen)</option>
              <option value="CORPORATIVO">CORPORATIVO (Empresas e instituciones)</option>
              <option value="MINORISTA">MINORISTA (Tiendas de barrio / detal)</option>
              <option value="OCASIONAL">OCASIONAL (Consumidor casual)</option>
            </select>
          </div>

          <div className="sim-field-group">
            <label className="field-label">Ciudad de Destino</label>
            <select
              className="select-control"
              value={ciudadCliente}
              onChange={(e) => setCiudadCliente(e.target.value)}
            >
              <option value="Mocoa">Mocoa (Putumayo)</option>
              <option value="Pasto">Pasto (Nariño)</option>
              <option value="Sibundoy">Sibundoy (Putumayo)</option>
              <option value="Puerto Asis">Puerto Asís (Putumayo)</option>
              <option value="Bogota">Bogotá D.C.</option>
              <option value="Cali">Cali (Valle)</option>
              <option value="Medellin">Medellín (Antioquia)</option>
            </select>
          </div>

          <div className="sim-field-group">
            <label className="field-label">Producto del Catálogo</label>
            <select
              className="select-control"
              value={productoSeleccionado ? productoSeleccionado.id : ''}
              onChange={handleProductoChange}
            >
              {productos.map((p) => (
                <option key={p.id} value={p.id}>
                  {p.nombre} &middot; Base: {formatMoney(p.precioVenta)}
                </option>
              ))}
            </select>
          </div>

          <div className="sim-grid-two">
            <div className="sim-field-group">
              <label className="field-label">Precio Unitario Simulado (COP)</label>
              <input
                type="number"
                className="search-input tabular-nums"
                value={precioPersonalizado}
                onChange={(e) => setPrecioPersonalizado(Number(e.target.value))}
                min="100"
                step="100"
              />
            </div>
            <div className="sim-field-group">
              <label className="field-label">Costo Unitario (COP)</label>
              <input
                type="number"
                className="search-input tabular-nums"
                value={costoUnitario}
                onChange={(e) => setCostoUnitario(Number(e.target.value))}
                min="100"
                step="100"
              />
            </div>
          </div>

          <button
            type="button"
            className="btn-submit btn-ml-predict"
            onClick={calcularPrediccion}
            disabled={cargandoPrediccion || reentrenando || !productoSeleccionado}
          >
            {cargandoPrediccion ? 'Calculando con Regresión...' : 'Calcular Predicción con Machine Learning'}
          </button>
        </div>

        {/* Columna Derecha: Resultado de la Predicción */}
        <div className="surface-card simulator-result-card">
          <div className="card-header">
            <h3 className="card-title">Resultado de la Predicción</h3>
            <span className="source-tag">Inferencia</span>
          </div>

          {!prediccion ? (
            <div className="empty-prediction-state">
              <div className="prediction-icon">📊</div>
              <p className="empty-title">Sin predicción calculada</p>
              <p className="empty-hint">
                Presione el botón "Calcular Predicción con Machine Learning" para evaluar este escenario con la ecuación de regresión.
              </p>
            </div>
          ) : (
            <div className="prediction-content">
              <div className="prediction-highlight-box">
                <span className="pred-label">Demanda Proyectada</span>
                <div className="pred-qty-row">
                  <span className="pred-large-num">{prediccion.cantidad_predicha}</span>
                  <span className="pred-unit">unidades</span>
                </div>
                <span className="pred-exact-val">
                  (Valor continuo exacto del modelo: {prediccion.cantidad_exacta_modelo})
                </span>
              </div>

              <div className="prediction-details-list">
                <div className="pred-detail-row">
                  <span>Subtotal proyectado:</span>
                  <strong className="tabular-nums">{formatMoney(prediccion.subtotal_estimado)}</strong>
                </div>
                <div className="pred-detail-row">
                  <span>Ganancia bruta estimada:</span>
                  <strong className="tabular-nums pred-profit">{formatMoney(prediccion.ganancia_estimada)}</strong>
                </div>
                <div className="pred-detail-row">
                  <span>Margen comercial:</span>
                  <strong className="tabular-nums">
                    {prediccion.subtotal_estimado > 0
                      ? `${((prediccion.ganancia_estimada / prediccion.subtotal_estimado) * 100).toFixed(1)}%`
                      : '0%'}
                  </strong>
                </div>
              </div>

              <button
                type="button"
                className="btn-apply-invoice"
                onClick={enviarAFactura}
              >
                Cargar esta Recomendación a la Factura
              </button>
            </div>
          )}
        </div>
      </div>
    </div>
  );
}

export default MachineLearningView;
