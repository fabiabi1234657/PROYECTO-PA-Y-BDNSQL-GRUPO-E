import React, { useState, useEffect } from 'react';
import axios from 'axios';
import './FacturacionForm.css';

const API = 'http://localhost:8080/api';

function FacturacionForm({ onFacturaEmitida }) {
  const [clientes, setClientes] = useState([]);
  const [productos, setProductos] = useState([]);
  const [clienteSeleccionado, setClienteSeleccionado] = useState(null);
  const [detalles, setDetalles] = useState([]);
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState(null);

  useEffect(() => {
    axios.get(`${API}/persons/categoria/CLIENTE`)
      .then(r => setClientes(r.data))
      .catch(() => setError('No se pudieron cargar los clientes'));

    axios.get(`${API}/products`)
      .then(r => setProductos(r.data))
      .catch(() => setError('No se pudieron cargar los productos'));
  }, []);

  const agregarProducto = (producto) => {
    const existe = detalles.find(d => d.productoId === producto.id);
    if (existe) {
      setDetalles(detalles.map(d =>
        d.productoId === producto.id
          ? { ...d, cantidad: d.cantidad + 1, subtotal: (d.cantidad + 1) * d.precioUnitario }
          : d
      ));
    } else {
      setDetalles([...detalles, {
        productoId: producto.id,
        productoNombre: producto.nombre,
        cantidad: 1,
        precioUnitario: producto.precioVenta,
        subtotal: producto.precioVenta
      }]);
    }
  };

  const cambiarCantidad = (productoId, cantidad) => {
    if (cantidad <= 0) {
      setDetalles(detalles.filter(d => d.productoId !== productoId));
    } else {
      setDetalles(detalles.map(d =>
        d.productoId === productoId
          ? { ...d, cantidad, subtotal: cantidad * d.precioUnitario }
          : d
      ));
    }
  };

  const subtotal = detalles.reduce((acc, d) => acc + d.subtotal, 0);
  const iva = subtotal * 0.19;
  const total = subtotal + iva;

  const emitirFactura = async () => {
    if (!clienteSeleccionado) { alert('Selecciona un cliente'); return; }
    if (detalles.length === 0) { alert('Agrega al menos un producto'); return; }

    setLoading(true);
    setError(null);
    try {
      const payload = {
        clienteDocumento: clienteSeleccionado.documento,
        clienteNombre: clienteSeleccionado.nombre,
        fechaEmision: new Date().toISOString().slice(0, 19),
        detalles,
        subtotal,
        iva,
        total,
        estado: 'EMITIDA'
      };
      const response = await axios.post(`${API}/invoices`, payload);
      onFacturaEmitida(response.data);
    } catch (e) {
      setError('Error al emitir la factura: ' + (e.response?.data?.message || e.message));
    } finally {
      setLoading(false);
    }
  };

  return (
    <div className="form-grid">
      {error && <div className="error-banner">{error}</div>}

      <div className="panel">
        <section className="card">
          <h2>Cliente</h2>
          <select
            className="select-input"
            onChange={e => {
              const c = clientes.find(c => c.documento === e.target.value);
              setClienteSeleccionado(c || null);
            }}
            defaultValue=""
          >
            <option value="" disabled>Seleccione un cliente</option>
            {clientes.map(c => (
              <option key={c.documento} value={c.documento}>
                {c.nombre} - {c.documento} ({c.ciudad})
              </option>
            ))}
          </select>
          {clienteSeleccionado && (
            <div className="cliente-info">
              <span>Correo: {clienteSeleccionado.correo}</span>
              <span>Tel: {clienteSeleccionado.telefono}</span>
            </div>
          )}
        </section>

        <section className="card">
          <h2>Catalogo de Productos</h2>
          <div className="productos-grid">
            {productos.map(p => (
              <div key={p.id} className="producto-card">
                {p.imageUrls && p.imageUrls[0] && (
                  <img src={p.imageUrls[0]} alt={p.nombre} className="producto-img" />
                )}
                <div className="producto-info">
                  <strong>{p.nombre}</strong>
                  <span className="precio">${p.precioVenta?.toLocaleString('es-CO')}</span>
                  <span className="desc">{p.descripcion}</span>
                </div>
                <button className="btn-agregar" onClick={() => agregarProducto(p)}>
                  Agregar
                </button>
              </div>
            ))}
          </div>
        </section>
      </div>

      <div className="panel">
        <section className="card factura-panel">
          <h2>Detalle de Factura</h2>

          {detalles.length === 0 ? (
            <p className="empty-msg">Agrega productos del catalogo</p>
          ) : (
            <table className="detalle-table">
              <thead>
                <tr>
                  <th>Producto</th>
                  <th>Cant.</th>
                  <th>P. Unit.</th>
                  <th>Subtotal</th>
                </tr>
              </thead>
              <tbody>
                {detalles.map(d => (
                  <tr key={d.productoId}>
                    <td>{d.productoNombre}</td>
                    <td>
                      <div className="qty-ctrl">
                        <button onClick={() => cambiarCantidad(d.productoId, d.cantidad - 1)}>-</button>
                        <span>{d.cantidad}</span>
                        <button onClick={() => cambiarCantidad(d.productoId, d.cantidad + 1)}>+</button>
                      </div>
                    </td>
                    <td>${d.precioUnitario?.toLocaleString('es-CO')}</td>
                    <td>${d.subtotal?.toLocaleString('es-CO')}</td>
                  </tr>
                ))}
              </tbody>
            </table>
          )}

          <div className="totales">
            <div className="totales-row"><span>Subtotal:</span><span>${subtotal.toLocaleString('es-CO', { minimumFractionDigits: 2 })}</span></div>
            <div className="totales-row"><span>IVA (19%):</span><span>${iva.toLocaleString('es-CO', { minimumFractionDigits: 2 })}</span></div>
            <div className="totales-row total"><span>TOTAL:</span><span>${total.toLocaleString('es-CO', { minimumFractionDigits: 2 })}</span></div>
          </div>

          <button
            className="btn-emitir"
            onClick={emitirFactura}
            disabled={loading || detalles.length === 0 || !clienteSeleccionado}
          >
            {loading ? 'Emitiendo...' : 'Emitir Factura'}
          </button>
        </section>
      </div>
    </div>
  );
}

export default FacturacionForm;
