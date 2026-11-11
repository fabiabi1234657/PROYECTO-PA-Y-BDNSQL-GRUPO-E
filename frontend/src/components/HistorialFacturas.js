import React, { useState, useEffect } from 'react';
import axios from 'axios';
import './HistorialFacturas.css';

const API = 'http://localhost:8080/api';

function HistorialFacturas({ onVerFactura }) {
  const [facturas, setFacturas] = useState([]);
  const [cargando, setCargando] = useState(true);
  const [error, setError] = useState(null);
  const [filtro, setFiltro] = useState('');

  const cargarFacturas = async () => {
    setCargando(true);
    setError(null);
    try {
      const resp = await axios.get(`${API}/invoices`);
      const data = Array.isArray(resp.data) ? resp.data : [];
      // Ordenar por ID descendente
      setFacturas(data.sort((a, b) => (b.id || 0) - (a.id || 0)));
    } catch (err) {
      setError('No fue posible cargar el historial de facturas desde MySQL.');
    } finally {
      setCargando(false);
    }
  };

  useEffect(() => {
    cargarFacturas();
  }, []);

  const formatMoney = (n) =>
    (n || 0).toLocaleString('es-CO', {
      style: 'currency',
      currency: 'COP',
      minimumFractionDigits: 0,
      maximumFractionDigits: 0
    });

  const formatDate = (s) => {
    if (!s) return 'N/A';
    try {
      return new Date(/(?:Z|[+-]\d{2}:\d{2})$/i.test(s) ? s : `${s}Z`).toLocaleString('es-CO', {
        day: '2-digit',
        month: 'short',
        year: 'numeric',
        hour: '2-digit',
        minute: '2-digit'
      });
    } catch {
      return s;
    }
  };

  const facturasFiltradas = facturas.filter((f) => {
    const q = filtro.toLowerCase().trim();
    if (!q) return true;
    return (
      String(f.id).includes(q) ||
      f.clienteNombre?.toLowerCase().includes(q) ||
      f.clienteDocumento?.toLowerCase().includes(q) ||
      f.estado?.toLowerCase().includes(q)
    );
  });

  const totalVendido = facturasFiltradas.reduce((acc, f) => acc + (f.total || 0), 0);

  return (
    <div className="history-container">
      <div className="history-header-card surface-card">
        <div className="header-flex">
          <div>
            <div className="title-row">
              <h2 className="card-title">Historial de Facturas Emitidas</h2>
              <span className="source-tag">MySQL</span>
            </div>
            <p className="card-subtitle">
              Registro transaccional de facturas y detalles persistidos en MySQL.
            </p>
          </div>
          <button type="button" className="btn-refresh" onClick={cargarFacturas} disabled={cargando}>
            {cargando ? 'Cargando...' : 'Actualizar'}
          </button>
        </div>

        {/* Metricas rapidas */}
        <div className="metrics-bar">
          <div className="metric-box">
            <span className="metric-label">Total Facturas</span>
            <strong className="metric-value">{facturasFiltradas.length}</strong>
          </div>
          <div className="metric-box">
            <span className="metric-label">Monto Consolidado</span>
            <strong className="metric-value highlight">{formatMoney(totalVendido)}</strong>
          </div>
        </div>

        {/* Buscador */}
        <div className="search-bar">
          <input
            type="text"
            className="search-input"
            placeholder="Buscar por No. Factura, nombre de cliente o documento..."
            value={filtro}
            onChange={(e) => setFiltro(e.target.value)}
          />
        </div>
      </div>

      {error && (
        <div className="feedback-banner banner-error">
          <span>{error}</span>
        </div>
      )}

      {/* Tabla de Facturas */}
      <div className="surface-card table-card">
        {cargando ? (
          <div className="skeleton-container" aria-busy="true">
            <div className="skeleton-item" />
            <div className="skeleton-item" />
            <div className="skeleton-item" />
          </div>
        ) : facturasFiltradas.length === 0 ? (
          <div className="empty-state">
            <p className="empty-title">No hay facturas registradas</p>
            <p className="empty-hint">Emita una nueva factura en la pestaña de facturación para verla reflejada aquí.</p>
          </div>
        ) : (
          <div className="table-responsive">
            <table className="history-table">
              <thead>
                <tr>
                  <th>No. Factura</th>
                  <th>Fecha de Emisión</th>
                  <th>Cliente</th>
                  <th>Documento</th>
                  <th className="text-center">Ítems</th>
                  <th className="text-right">Subtotal</th>
                  <th className="text-right">IVA (19%)</th>
                  <th className="text-right">Total</th>
                  <th className="text-center">Estado</th>
                  <th className="text-center">Acción</th>
                </tr>
              </thead>
              <tbody>
                {facturasFiltradas.map((f) => (
                  <tr key={f.id}>
                    <td className="font-semibold tabular-nums">#{String(f.id).padStart(5, '0')}</td>
                    <td>{formatDate(f.fechaEmision)}</td>
                    <td className="font-medium">{f.clienteNombre}</td>
                    <td className="tabular-nums">{f.clienteDocumento}</td>
                    <td className="text-center tabular-nums">{f.detalles?.length || 0}</td>
                    <td className="text-right tabular-nums">{formatMoney(f.subtotal)}</td>
                    <td className="text-right tabular-nums">{formatMoney(f.iva)}</td>
                    <td className="text-right tabular-nums font-semibold">{formatMoney(f.total)}</td>
                    <td className="text-center">
                      <span className={`status-pill status-${f.estado?.toLowerCase() || 'emitida'}`}>
                        {f.estado || 'EMITIDA'}
                      </span>
                    </td>
                    <td className="text-center">
                      <button
                        type="button"
                        className="btn-view"
                        onClick={() => onVerFactura(f)}
                        title="Ver e imprimir comprobante"
                      >
                        Ver Recibo
                      </button>
                    </td>
                  </tr>
                ))}
              </tbody>
            </table>
          </div>
        )}
      </div>
    </div>
  );
}

export default HistorialFacturas;
