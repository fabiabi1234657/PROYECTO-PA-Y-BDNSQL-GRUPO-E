import React, { useState } from 'react';
import FacturacionForm from './components/FacturacionForm';
import ReciboView from './components/ReciboView';
import HistorialFacturas from './components/HistorialFacturas';
import MachineLearningView from './components/MachineLearningView';
import GestionClientes from './components/GestionClientes';
import GestionProductos from './components/GestionProductos';
import SincronizacionDruid from './components/SincronizacionDruid';
import './App.css';

function App() {
  const [tabActiva, setTabActiva] = useState('facturacion');
  const [facturaEmitida, setFacturaEmitida] = useState(null);
  const [prediccionCargada, setPrediccionCargada] = useState(null);

  const handleCargarPrediccionAFactura = (pred) => {
    setPrediccionCargada(pred);
    setFacturaEmitida(null);
    setTabActiva('facturacion');
  };

  const handleVerReciboDesdeHistorial = (factura) => {
    setFacturaEmitida(factura);
  };

  const handleNuevaFactura = () => {
    setFacturaEmitida(null);
    setPrediccionCargada(null);
    setTabActiva('facturacion');
  };

  return (
    <div className="app-container">
      {/* Encabezado Principal */}
      <header className="app-header">
        <div className="header-content">
          <div className="brand-group">
            <div className="brand-badge">PROYECTO</div>
            <div>
              <h1 className="brand-title">Facturación & Analítica Inteligente</h1>
              <p className="brand-subtitle">
                Arquitectura Hexagonal &middot; Persistencia Políglota &middot; Machine Learning con Apache Druid
              </p>
            </div>
          </div>
          <div className="header-meta">
            <span className="cluster-badge polyglot-badge">
              Cassandra &middot; MongoDB &middot; MySQL &middot; Druid &middot; Scikit-Learn
            </span>
          </div>
        </div>

        {/* Barra de Pestañas de Navegación */}
        <nav className="tab-navigation" aria-label="Navegación principal del sistema">
          <button
            type="button"
            className={`tab-btn ${tabActiva === 'facturacion' && !facturaEmitida ? 'active' : ''}`}
            onClick={() => {
              setFacturaEmitida(null);
              setTabActiva('facturacion');
            }}
          >
            <span className="tab-icon">🧾</span>
            <span className="tab-label">Facturar</span>
          </button>

          <button
            type="button"
            className={`tab-btn ${tabActiva === 'historial' || (tabActiva === 'facturacion' && facturaEmitida) ? 'active' : ''}`}
            onClick={() => setTabActiva('historial')}
          >
            <span className="tab-icon">📜</span>
            <span className="tab-label">Historial Facturas</span>
            <span className="tab-db-tag db-mysql">MySQL</span>
          </button>

          <button
            type="button"
            className={`tab-btn tab-highlight ${tabActiva === 'ml' ? 'active' : ''}`}
            onClick={() => {
              setFacturaEmitida(null);
              setTabActiva('ml');
            }}
          >
            <span className="tab-icon">🧠</span>
            <span className="tab-label">Machine Learning & Predicción</span>
            <span className="tab-db-tag db-druid">Druid OLAP</span>
          </button>

          <button
            type="button"
            className={`tab-btn ${tabActiva === 'clientes' ? 'active' : ''}`}
            onClick={() => {
              setFacturaEmitida(null);
              setTabActiva('clientes');
            }}
          >
            <span className="tab-icon">👥</span>
            <span className="tab-label">Clientes</span>
            <span className="tab-db-tag db-cassandra">Cassandra</span>
          </button>

          <button
            type="button"
            className={`tab-btn ${tabActiva === 'productos' ? 'active' : ''}`}
            onClick={() => {
              setFacturaEmitida(null);
              setTabActiva('productos');
            }}
          >
            <span className="tab-icon">📦</span>
            <span className="tab-label">Productos</span>
            <span className="tab-db-tag db-mongo">MongoDB</span>
          </button>

          <button
            type="button"
            className={`tab-btn ${tabActiva === 'druid' ? 'active' : ''}`}
            onClick={() => {
              setFacturaEmitida(null);
              setTabActiva('druid');
            }}
          >
            <span className="tab-icon">🔄</span>
            <span className="tab-label">Sincronización OLAP</span>
            <span className="tab-db-tag db-druid">Druid ETL</span>
          </button>
        </nav>
      </header>

      {/* Contenido Principal */}
      <main className="app-main" id="main-content">
        {facturaEmitida ? (
          <ReciboView factura={facturaEmitida} onNuevaFactura={handleNuevaFactura} />
        ) : (
          <>
            {tabActiva === 'facturacion' && (
              <FacturacionForm
                onFacturaEmitida={setFacturaEmitida}
                prediccionInicial={prediccionCargada}
                onIrAML={() => setTabActiva('ml')}
              />
            )}

            {tabActiva === 'historial' && (
              <HistorialFacturas onVerFactura={handleVerReciboDesdeHistorial} />
            )}

            {tabActiva === 'ml' && (
              <MachineLearningView onCargarAFactura={handleCargarPrediccionAFactura} />
            )}

            {tabActiva === 'clientes' && <GestionClientes />}

            {tabActiva === 'productos' && <GestionProductos />}

            {tabActiva === 'druid' && <SincronizacionDruid />}
          </>
        )}
      </main>

      {/* Pie de Página */}
      <footer className="app-footer">
        <div className="footer-content">
          <p>
            <strong>Proyecto PA y BDNSQL &middot; Grupo E</strong> &mdash; Instituto Tecnológico del Putumayo (ITP)
          </p>
          <p className="footer-sub">
            Arquitectura Hexagonal &middot; Polyglot Persistence: Cassandra, MongoDB, MySQL, Apache Druid 31.0 &middot; Linear Regression ML
          </p>
        </div>
      </footer>
    </div>
  );
}

export default App;
