import React, { useState } from 'react';
import FacturacionForm from './components/FacturacionForm';
import ReciboView from './components/ReciboView';
import './App.css';

function App() {
  const [facturaEmitida, setFacturaEmitida] = useState(null);

  return (
    <div className="app-container">
      <header className="app-header">
        <div className="header-content">
          <div className="brand-group">
            <div className="brand-badge">SISTEMA</div>
            <div>
              <h1 className="brand-title">Facturacion Electronica</h1>
              <p className="brand-subtitle">Arquitectura Hexagonal &middot; Persistencia Poliglota</p>
            </div>
          </div>
          <div className="header-meta">
            <span className="cluster-badge">MySQL &middot; Mongo &middot; Cassandra</span>
          </div>
        </div>
      </header>

      <main className="app-main" id="main-content">
        {!facturaEmitida ? (
          <FacturacionForm onFacturaEmitida={setFacturaEmitida} />
        ) : (
          <ReciboView
            factura={facturaEmitida}
            onNuevaFactura={() => setFacturaEmitida(null)}
          />
        )}
      </main>

      <footer className="app-footer">
        <p>Proyecto PA y BDNSQL &middot; Grupo E</p>
      </footer>
    </div>
  );
}

export default App;
