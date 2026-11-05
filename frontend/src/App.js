import React, { useState } from 'react';
import FacturacionForm from './components/FacturacionForm';
import ReciboView from './components/ReciboView';
import './App.css';

function App() {
  const [facturaEmitida, setFacturaEmitida] = useState(null);

  return (
    <div className="app-container">
      <header className="app-header">
        <h1>Sistema de Facturacion</h1>
        <p>Grupo E - Programacion Avanzada y BD NoSQL</p>
      </header>

      <main className="app-main">
        {!facturaEmitida ? (
          <FacturacionForm onFacturaEmitida={setFacturaEmitida} />
        ) : (
          <ReciboView factura={facturaEmitida} onNuevaFactura={() => setFacturaEmitida(null)} />
        )}
      </main>
    </div>
  );
}

export default App;
