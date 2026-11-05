import React from 'react';
import './ReciboView.css';

function ReciboView({ factura, onNuevaFactura }) {
  const formatMoney = (n) =>
    n?.toLocaleString('es-CO', { style: 'currency', currency: 'COP', minimumFractionDigits: 0 });

  const formatDate = (s) =>
    new Date(s).toLocaleString('es-CO', {
      day: '2-digit', month: 'long', year: 'numeric',
      hour: '2-digit', minute: '2-digit'
    });

  return (
    <div className="recibo-wrapper">
      <div className="recibo">
        <div className="recibo-header">
          <div>
            <h2>FACTURA DE VENTA</h2>
            <p className="recibo-num">No. {String(factura.id).padStart(6, '0')}</p>
          </div>
          <div className="recibo-estado">{factura.estado}</div>
        </div>

        <div className="recibo-divider" />

        <div className="recibo-section">
          <h3>Cliente</h3>
          <div className="recibo-row"><span>Nombre:</span><span>{factura.clienteNombre}</span></div>
          <div className="recibo-row"><span>Documento:</span><span>{factura.clienteDocumento}</span></div>
          <div className="recibo-row"><span>Fecha:</span><span>{formatDate(factura.fechaEmision)}</span></div>
        </div>

        <div className="recibo-divider" />

        <div className="recibo-section">
          <h3>Productos</h3>
          <table className="recibo-table">
            <thead>
              <tr>
                <th>Descripcion</th>
                <th>Cant.</th>
                <th>Valor Unit.</th>
                <th>Subtotal</th>
              </tr>
            </thead>
            <tbody>
              {factura.detalles?.map((d, i) => (
                <tr key={i}>
                  <td>{d.productoNombre}</td>
                  <td>{d.cantidad}</td>
                  <td>{formatMoney(d.precioUnitario)}</td>
                  <td>{formatMoney(d.subtotal)}</td>
                </tr>
              ))}
            </tbody>
          </table>
        </div>

        <div className="recibo-divider" />

        <div className="recibo-totales">
          <div className="recibo-row"><span>Subtotal:</span><span>{formatMoney(factura.subtotal)}</span></div>
          <div className="recibo-row"><span>IVA (19%):</span><span>{formatMoney(factura.iva)}</span></div>
          <div className="recibo-row recibo-total"><span>TOTAL:</span><span>{formatMoney(factura.total)}</span></div>
        </div>

        <div className="recibo-footer">
          <p>Documento generado por el sistema de facturacion</p>
        </div>

        <button className="btn-nueva" onClick={onNuevaFactura}>
          Nueva Factura
        </button>
      </div>
    </div>
  );
}

export default ReciboView;
