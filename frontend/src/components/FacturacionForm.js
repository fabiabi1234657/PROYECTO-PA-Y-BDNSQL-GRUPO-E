import React, { useState, useEffect } from 'react';
import axios from 'axios';
import './FacturacionForm.css';

const API = 'http://localhost:8080/api';

function FacturacionForm({ onFacturaEmitida }) {
  const [clientes, setClientes] = useState([]);
  const [productos, setProductos] = useState([]);
  const [clienteSeleccionado, setClienteSeleccionado] = useState(null);
  const [detalles, setDetalles] = useState([]);
  const [filtroProducto, setFiltroProducto] = useState('');
  const [cargandoDatos, setCargandoDatos] = useState(true);
  const [emitiendo, setEmitiendo] = useState(false);
  const [notificacion, setNotificacion] = useState(null); // { type: 'error'|'warning', message: '' }

  useEffect(() => {
    let activo = true;

    async function cargarInicial() {
      try {
        const [respClientes, respProductos] = await Promise.all([
          axios.get(`${API}/persons/categoria/CLIENTE`),
          axios.get(`${API}/products`)
        ]);

        if (activo) {
          setClientes(Array.isArray(respClientes.data) ? respClientes.data : []);
          setProductos(Array.isArray(respProductos.data) ? respProductos.data : []);
        }
      } catch (err) {
        if (activo) {
          setNotificacion({
            type: 'error',
            message: 'No fue posible conectar con el servidor. Verifique que los servicios esten activos.'
          });
        }
      } finally {
        if (activo) {
          setCargandoDatos(false);
        }
      }
    }

    cargarInicial();
    return () => { activo = false; };
  }, []);

  const agregarProducto = (producto) => {
    setNotificacion(null);
    setDetalles((prev) => {
      const index = prev.findIndex((d) => d.productoId === producto.id);
      if (index >= 0) {
        const itemActual = prev[index];
        const nuevaCant = itemActual.cantidad + 1;
        const copia = [...prev];
        copia[index] = {
          ...itemActual,
          cantidad: nuevaCant,
          subtotal: Math.round(nuevaCant * itemActual.precioUnitario * 100) / 100
        };
        return copia;
      }
      return [
        ...prev,
        {
          productoId: producto.id,
          productoNombre: producto.nombre,
          cantidad: 1,
          precioUnitario: producto.precioVenta,
          subtotal: Math.round(producto.precioVenta * 100) / 100
        }
      ];
    });
  };

  const modificarCantidad = (productoId, delta) => {
    setNotificacion(null);
    setDetalles((prev) => {
      return prev
        .map((d) => {
          if (d.productoId === productoId) {
            const nuevaCantidad = d.cantidad + delta;
            if (nuevaCantidad <= 0) return null;
            return {
              ...d,
              cantidad: nuevaCantidad,
              subtotal: Math.round(nuevaCantidad * d.precioUnitario * 100) / 100
            };
          }
          return d;
        })
        .filter(Boolean);
    });
  };

  const eliminarDetalle = (productoId) => {
    setNotificacion(null);
    setDetalles((prev) => prev.filter((d) => d.productoId !== productoId));
  };

  const subtotal = Math.round(detalles.reduce((acc, d) => acc + d.subtotal, 0) * 100) / 100;
  const iva = Math.round(subtotal * 0.19 * 100) / 100;
  const total = Math.round((subtotal + iva) * 100) / 100;

  const emitirFactura = async (e) => {
    e.preventDefault();

    if (!clienteSeleccionado) {
      setNotificacion({
        type: 'warning',
        message: 'Debe seleccionar un cliente antes de emitir la factura.'
      });
      return;
    }

    if (detalles.length === 0) {
      setNotificacion({
        type: 'warning',
        message: 'Debe agregar al menos un producto a la factura.'
      });
      return;
    }

    setEmitiendo(true);
    setNotificacion(null);

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
    } catch (err) {
      const serverMsg = err.response?.data?.message || err.message;
      setNotificacion({
        type: 'error',
        message: `Error al emitir factura: ${serverMsg}`
      });
    } finally {
      setEmitiendo(false);
    }
  };

  const productosFiltrados = productos.filter((p) => {
    const termino = filtroProducto.toLowerCase().trim();
    if (!termino) return true;
    return (
      p.nombre?.toLowerCase().includes(termino) ||
      p.descripcion?.toLowerCase().includes(termino)
    );
  });

  const formatearCOP = (valor) =>
    (valor || 0).toLocaleString('es-CO', {
      style: 'currency',
      currency: 'COP',
      minimumFractionDigits: 0,
      maximumFractionDigits: 0
    });

  return (
    <div className="billing-layout">
      {notificacion && (
        <div
          role="alert"
          className={`feedback-banner ${notificacion.type === 'error' ? 'banner-error' : 'banner-warning'}`}
        >
          <span className="banner-text">{notificacion.message}</span>
          <button
            type="button"
            className="banner-close"
            onClick={() => setNotificacion(null)}
            aria-label="Cerrar aviso"
          >
            &times;
          </button>
        </div>
      )}

      {/* Columna Izquierda: Cliente y Catalogo */}
      <div className="catalog-column">
        {/* Seccion Cliente */}
        <section className="surface-card">
          <div className="card-header">
            <h2 className="card-title">Datos del Cliente</h2>
            <span className="source-tag">Cassandra</span>
          </div>

          <div className="field-group">
            <label htmlFor="cliente-select" className="field-label">
              Seleccionar cliente registrado
            </label>
            <select
              id="cliente-select"
              className="select-control"
              value={clienteSeleccionado ? clienteSeleccionado.documento : ''}
              onChange={(e) => {
                const c = clientes.find((item) => item.documento === e.target.value);
                setClienteSeleccionado(c || null);
                setNotificacion(null);
              }}
              disabled={cargandoDatos}
            >
              <option value="">-- Seleccione un cliente --</option>
              {clientes.map((c) => (
                <option key={c.documento} value={c.documento}>
                  {c.nombre} &middot; Doc: {c.documento} ({c.ciudad})
                </option>
              ))}
            </select>
          </div>

          {clienteSeleccionado && (
            <div className="client-summary">
              <div className="summary-item">
                <span className="summary-label">Documento</span>
                <span className="summary-value tabular-nums">{clienteSeleccionado.documento}</span>
              </div>
              <div className="summary-item">
                <span className="summary-label">Correo</span>
                <span className="summary-value">{clienteSeleccionado.correo}</span>
              </div>
              <div className="summary-item">
                <span className="summary-label">Telefono</span>
                <span className="summary-value tabular-nums">{clienteSeleccionado.telefono}</span>
              </div>
              <div className="summary-item">
                <span className="summary-label">Ciudad</span>
                <span className="summary-value">{clienteSeleccionado.ciudad}</span>
              </div>
            </div>
          )}
        </section>

        {/* Seccion Catalogo de Productos */}
        <section className="surface-card">
          <div className="card-header">
            <div>
              <h2 className="card-title">Catalogo de Productos</h2>
              <span className="card-subtitle">
                {productosFiltrados.length} disponibles
              </span>
            </div>
            <span className="source-tag">MongoDB</span>
          </div>

          <div className="search-bar">
            <input
              type="text"
              className="search-input"
              placeholder="Buscar por nombre o descripcion..."
              value={filtroProducto}
              onChange={(e) => setFiltroProducto(e.target.value)}
              aria-label="Buscar producto"
            />
          </div>

          <div className="product-list" role="list">
            {cargandoDatos ? (
              <div className="skeleton-container" aria-busy="true">
                <div className="skeleton-item" />
                <div className="skeleton-item" />
                <div className="skeleton-item" />
              </div>
            ) : productosFiltrados.length === 0 ? (
              <div className="empty-catalog">
                <p>No se encontraron productos disponibles.</p>
              </div>
            ) : (
              productosFiltrados.map((p) => (
                <div key={p.id} className="product-row" role="listitem">
                  <div className="product-main">
                    <div className="product-name-row">
                      <strong className="product-name">{p.nombre}</strong>
                      <span className="product-price tabular-nums">
                        {formatearCOP(p.precioVenta)}
                      </span>
                    </div>
                    {p.descripcion && (
                      <p className="product-desc">{p.descripcion}</p>
                    )}
                    {p.proveedor?.nombre && (
                      <span className="product-supplier">
                        Proveedor: {p.proveedor.nombre}
                      </span>
                    )}
                  </div>
                  <button
                    type="button"
                    className="btn-add"
                    onClick={() => agregarProducto(p)}
                    aria-label={`Agregar ${p.nombre} a la factura`}
                  >
                    Agregar
                  </button>
                </div>
              ))
            )}
          </div>
        </section>
      </div>

      {/* Columna Derecha: Resumen de Factura */}
      <div className="invoice-column">
        <section className="surface-card invoice-summary-card">
          <div className="card-header">
            <h2 className="card-title">Detalle de Factura</h2>
            <span className="source-tag">MySQL</span>
          </div>

          {detalles.length === 0 ? (
            <div className="empty-cart-state">
              <p className="empty-title">Factura vacia</p>
              <p className="empty-hint">Seleccione productos del catalogo para agregarlos a la orden.</p>
            </div>
          ) : (
            <div className="table-wrapper">
              <table className="items-table">
                <thead>
                  <tr>
                    <th scope="col">Producto</th>
                    <th scope="col" className="text-center">Cant.</th>
                    <th scope="col" className="text-right">Unitario</th>
                    <th scope="col" className="text-right">Subtotal</th>
                    <th scope="col" className="text-center">Accion</th>
                  </tr>
                </thead>
                <tbody>
                  {detalles.map((d) => (
                    <tr key={d.productoId}>
                      <td className="item-name">{d.productoNombre}</td>
                      <td className="text-center">
                        <div className="stepper" role="group" aria-label={`Cantidad para ${d.productoNombre}`}>
                          <button
                            type="button"
                            className="stepper-btn"
                            onClick={() => modificarCantidad(d.productoId, -1)}
                            aria-label={`Reducir cantidad de ${d.productoNombre}`}
                          >
                            -
                          </button>
                          <span className="stepper-value tabular-nums">{d.cantidad}</span>
                          <button
                            type="button"
                            className="stepper-btn"
                            onClick={() => modificarCantidad(d.productoId, 1)}
                            aria-label={`Aumentar cantidad de ${d.productoNombre}`}
                          >
                            +
                          </button>
                        </div>
                      </td>
                      <td className="text-right tabular-nums">
                        {formatearCOP(d.precioUnitario)}
                      </td>
                      <td className="text-right tabular-nums font-semibold">
                        {formatearCOP(d.subtotal)}
                      </td>
                      <td className="text-center">
                        <button
                          type="button"
                          className="btn-remove"
                          onClick={() => eliminarDetalle(d.productoId)}
                          aria-label={`Eliminar ${d.productoNombre}`}
                          title="Eliminar producto"
                        >
                          &times;
                        </button>
                      </td>
                    </tr>
                  ))}
                </tbody>
              </table>
            </div>
          )}

          <div className="totals-breakdown">
            <div className="totals-row">
              <span>Subtotal</span>
              <span className="tabular-nums">{formatearCOP(subtotal)}</span>
            </div>
            <div className="totals-row">
              <span>IVA (19%)</span>
              <span className="tabular-nums">{formatearCOP(iva)}</span>
            </div>
            <div className="totals-row total-highlight">
              <span>Total a pagar</span>
              <span className="tabular-nums">{formatearCOP(total)}</span>
            </div>
          </div>

          <button
            type="button"
            className="btn-submit"
            onClick={emitirFactura}
            disabled={emitiendo || detalles.length === 0 || !clienteSeleccionado}
          >
            {emitiendo ? 'Guardando factura...' : 'Emitir Factura'}
          </button>
        </section>
      </div>
    </div>
  );
}

export default FacturacionForm;
