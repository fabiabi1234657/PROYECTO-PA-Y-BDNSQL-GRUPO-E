import React, { useState } from 'react';
import axios from 'axios';
import './SincronizacionDruid.css';

const API = 'http://localhost:8080/api';

function SincronizacionDruid() {
  const [sincronizando, setSincronizando] = useState(false);
  const [resultado, setResultado] = useState(null);
  const [error, setError] = useState(null);

  const ejecutarSincronizacion = async () => {
    setSincronizando(true);
    setError(null);
    setResultado(null);

    try {
      const resp = await axios.post(`${API}/druid/sync`);
      setResultado(resp.data);
    } catch (err) {
      const msg = err.response?.data?.message || err.message;
      setError(`No se pudo completar el proceso ETL hacia Apache Druid: ${msg}. Asegúrese de que el clúster de Druid esté activo.`);
    } finally {
      setSincronizando(false);
    }
  };

  const formatMoney = (n) =>
    (n || 0).toLocaleString('es-CO', {
      style: 'currency',
      currency: 'COP',
      minimumFractionDigits: 0,
      maximumFractionDigits: 0
    });

  return (
    <div className="druid-sync-container">
      {/* Tarjeta de Control ETL */}
      <div className="surface-card druid-hero-card">
        <div className="druid-hero-flex">
          <div>
            <div className="title-row">
              <h2 className="card-title">Respaldo Analítico y Sincronización OLAP</h2>
              <span className="source-tag">Apache Druid 31.0</span>
            </div>
            <p className="card-subtitle">
              Extrae las facturas transaccionales de MySQL, las enriquece con datos de clientes (Cassandra)
              y productos (MongoDB), y consolida los hechos analíticos en Druid para el entrenamiento de Machine Learning.
            </p>
          </div>
          <button
            type="button"
            className="btn-sync-now"
            onClick={ejecutarSincronizacion}
            disabled={sincronizando}
          >
            {sincronizando ? 'Ejecutando Proceso ETL...' : 'Sincronizar con Druid Ahora'}
          </button>
        </div>

        {error && (
          <div className="feedback-banner banner-error">
            <span>{error}</span>
          </div>
        )}

        {resultado && (
          <div className="feedback-banner banner-exito">
            <div>
              <strong>{resultado.status === 'NO_DATA' ? 'Sin datos para enviar' : 'Ingesta enviada a Druid'}</strong>
              <p>
                {resultado.mensaje}. Hechos enviados: <strong>{resultado.hechosEnviados}</strong>.
                Monto total de ganancias calculado: <strong>{formatMoney(resultado.gananciaTotal)}</strong>.
              </p>
            </div>
          </div>
        )}
      </div>

      {/* Grid de Arquitectura y Puertos de Druid */}
      <div className="druid-arch-grid">
        <div className="surface-card arch-card">
          <span className="arch-badge">Servicio</span>
          <h3 className="arch-title">Druid Router & Consola Web</h3>
          <p className="arch-desc">Punto de entrada unificado para consultas SQL y panel de administración.</p>
          <div className="arch-meta">
            <span>Puerto: <strong>8888</strong></span>
            <a
              href="http://localhost:8888"
              target="_blank"
              rel="noopener noreferrer"
              className="arch-link"
            >
              Abrir Consola Druid &rarr;
            </a>
          </div>
        </div>

        <div className="surface-card arch-card">
          <span className="arch-badge">Servicio</span>
          <h3 className="arch-title">Druid Broker</h3>
          <p className="arch-desc">Recibe consultas SQL desde el microservicio de Machine Learning y las distribuye.</p>
          <div className="arch-meta">
            <span>Puerto: <strong>8082</strong></span>
            <span className="arch-endpoint">Endpoint: <code>/druid/v2/sql</code></span>
          </div>
        </div>

        <div className="surface-card arch-card">
          <span className="arch-badge">Servicio</span>
          <h3 className="arch-title">Druid Coordinator & Indexer</h3>
          <p className="arch-desc">Gestiona segmentos de datos y coordina tareas paralelas de indexación.</p>
          <div className="arch-meta">
            <span>Puerto: <strong>8081</strong></span>
            <span className="arch-endpoint">Datasource: <strong>hechos_ventas</strong></span>
          </div>
        </div>
      </div>

      {/* Pipeline ETL Visual */}
      <div className="surface-card">
        <div className="card-header">
          <h3 className="card-title">Flujo ETL Implementado (GenerarHechosService)</h3>
          <span className="source-tag">Spring Boot 3.4</span>
        </div>
        <div className="etl-steps-list">
          <div className="etl-step-item">
            <span className="step-num">1</span>
            <div className="step-content">
              <strong>Extracción Relacional</strong>
              <p>Lectura de facturas emitidas y sus ítems desde MySQL (tablas <code>invoices</code> e <code>invoice_details</code>).</p>
            </div>
          </div>
          <div className="etl-step-item">
            <span className="step-num">2</span>
            <div className="step-content">
              <strong>Enriquecimiento Políglota (Lookup en Caché)</strong>
              <p>Cruce de cada cliente en Apache Cassandra (categoría, ciudad) y de cada producto en MongoDB (costo, proveedor).</p>
            </div>
          </div>
          <div className="etl-step-item">
            <span className="step-num">3</span>
            <div className="step-content">
              <strong>Cálculo de Métricas OLAP</strong>
              <p>Cálculo de subtotal, costo total y margen de ganancia por detalle: <code>(Precio - Costo) &times; Cantidad</code>.</p>
            </div>
          </div>
          <div className="etl-step-item">
            <span className="step-num">4</span>
            <div className="step-content">
              <strong>Ingesta Paralela en Apache Druid</strong>
              <p>Envío de la especificación <code>index_parallel</code> hacia la API del Indexer de Druid para disponibilidad analítica inmediata.</p>
            </div>
          </div>
        </div>
      </div>
    </div>
  );
}

export default SincronizacionDruid;
