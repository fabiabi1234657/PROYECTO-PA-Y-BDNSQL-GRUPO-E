import React from 'react';
import './ReciboView.css';

function ReciboView({ factura, onNuevaFactura }) {
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
      return new Date(s).toLocaleString('es-CO', {
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

  const handlePrint = () => {
    window.print();
  };

  return (
    <div className="receipt-container">
      <div className="receipt-card" id="printable-receipt">
        {/* Encabezado */}
        <div className="receipt-header">
          <div className="receipt-brand">
            <span className="receipt-type">COMPROBANTE DE VENTA</span>
            <h2 className="receipt-number tabular-nums">
              No. {String(factura.id || 1).padStart(6, '0')}
            </h2>
          </div>
          <div className="receipt-status-badge">
            {factura.estado || 'EMITIDA'}
          </div>
        </div>

        <div className="receipt-divider" />

        {/* Metadatos del cliente y emision */}
        <div className="receipt-grid">
          <div className="receipt-meta-block">
            <span className="meta-label">Cliente</span>
            <strong className="meta-value">{factura.clienteNombre || 'Consumidor Final'}</strong>
          </div>
          <div className="receipt-meta-block">
            <span className="meta-label">Documento</span>
            <span className="meta-value tabular-nums">{factura.clienteDocumento || 'N/A'}</span>
          </div>
          <div className="receipt-meta-block">
            <span className="meta-label">Fecha y hora</span>
            <span className="meta-value">{formatDate(factura.fechaEmision)}</span>
          </div>
          <div className="receipt-meta-block">
            <span className="meta-label">Metodo de pago</span>
            <span className="meta-value">Contado / Efectivo</span>
          </div>
        </div>

        <div className="receipt-divider" />

        {/* Tabla de Items */}
        <div className="receipt-table-wrapper">
          <table className="receipt-table">
            <thead>
              <tr>
                <th scope="col">Descripcion</th>
                <th scope="col" className="text-center">Cant.</th>
                <th scope="col" className="text-right">Unitario</th>
                <th scope="col" className="text-right">Subtotal</th>
              </tr>
            </thead>
            <tbody>
              {factura.detalles?.map((d, index) => (
                <tr key={index}>
                  <td className="receipt-item-title">{d.productoNombre}</td>
                  <td className="text-center tabular-nums">{d.cantidad}</td>
                  <td className="text-right tabular-nums">{formatMoney(d.precioUnitario)}</td>
                  <td className="text-right tabular-nums font-semibold">{formatMoney(d.subtotal)}</td>
                </tr>
              ))}
            </tbody>
          </table>
        </div>

        <div className="receipt-divider" />

        {/* Totales */}
        <div className="receipt-totals">
          <div className="receipt-total-line">
            <span>Subtotal</span>
            <span className="tabular-nums">{formatMoney(factura.subtotal)}</span>
          </div>
          <div className="receipt-total-line">
            <span>IVA (19%)</span>
            <span className="tabular-nums">{formatMoney(factura.iva)}</span>
          </div>
          <div className="receipt-total-line receipt-grand-total">
            <span>Total Pagado</span>
            <span className="tabular-nums">{formatMoney(factura.total)}</span>
          </div>
        </div>

        {/* Pie de comprobante */}
        <div className="receipt-footer">
          <p className="footer-legend">Documento valido como soporte de transaccion comercial</p>
          <span className="footer-system">Facturacion IT &middot; Arquitectura Hexagonal</span>
        </div>
      </div>

      {/* Botones de accion */}
      <div className="receipt-actions no-print">
        <button
          type="button"
          className="btn-action btn-secondary"
          onClick={handlePrint}
        >
          Imprimir comprobante
        </button>
        <button
          type="button"
          className="btn-action btn-primary"
          onClick={onNuevaFactura}
        >
          Nueva Factura
        </button>
      </div>
    </div>
  );
}

export default ReciboView;
