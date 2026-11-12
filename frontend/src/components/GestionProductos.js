import React, { useState, useEffect } from 'react';
import axios from 'axios';
import './GestionProductos.css';

const API = 'http://localhost:8080/api';

function GestionProductos() {
  const [productos, setProductos] = useState([]);
  const [cargando, setCargando] = useState(true);
  const [filtro, setFiltro] = useState('');
  const [mostrarModal, setMostrarModal] = useState(false);
  const [guardando, setGuardando] = useState(false);
  const [notificacion, setNotificacion] = useState(null);

  const [nuevoProducto, setNuevoProducto] = useState({
    nombre: '',
    descripcion: '',
    costo: '',
    precioVenta: '',
    proveedorNombre: 'Molinos Roa'
  });

  const cargarProductos = async () => {
    setCargando(true);
    setNotificacion(null);
    try {
      const resp = await axios.get(`${API}/products`);
      setProductos(Array.isArray(resp.data) ? resp.data : []);
    } catch (err) {
      setNotificacion({
        tipo: 'error',
        mensaje: 'No fue posible conectar con MongoDB. Verifique que el servicio esté activo.'
      });
    } finally {
      setCargando(false);
    }
  };

  useEffect(() => {
    cargarProductos();
  }, []);

  const formatMoney = (n) =>
    (n || 0).toLocaleString('es-CO', {
      style: 'currency',
      currency: 'COP',
      minimumFractionDigits: 0,
      maximumFractionDigits: 0
    });

  const handleSubmit = async (e) => {
    e.preventDefault();
    if (!nuevoProducto.nombre || !nuevoProducto.precioVenta) {
      setNotificacion({ tipo: 'warning', mensaje: 'Nombre y precio de venta son requeridos.' });
      return;
    }

    setGuardando(true);
    try {
      const payload = {
        nombre: nuevoProducto.nombre,
        descripcion: nuevoProducto.descripcion,
        costo: Number(nuevoProducto.costo) || 0,
        precioVenta: Number(nuevoProducto.precioVenta),
        proveedor: {
          nombre: nuevoProducto.proveedorNombre
        }
      };

      await axios.post(`${API}/products`, payload);
      setNotificacion({ tipo: 'exito', mensaje: 'Producto guardado exitosamente en MongoDB.' });
      setMostrarModal(false);
      setNuevoProducto({
        nombre: '',
        descripcion: '',
        costo: '',
        precioVenta: '',
        proveedorNombre: 'Molinos Roa'
      });
      cargarProductos();
    } catch (err) {
      setNotificacion({ tipo: 'error', mensaje: 'Error al registrar producto en MongoDB.' });
    } finally {
      setGuardando(false);
    }
  };

  const productosFiltrados = productos.filter((p) => {
    const q = filtro.toLowerCase().trim();
    if (!q) return true;
    return (
      p.nombre?.toLowerCase().includes(q) ||
      p.descripcion?.toLowerCase().includes(q) ||
      p.proveedor?.nombre?.toLowerCase().includes(q)
    );
  });

  return (
    <div className="products-container">
      <div className="surface-card products-header-card">
        <div className="header-flex">
          <div>
            <div className="title-row">
              <h2 className="card-title">Catálogo de Productos y Precios</h2>
              <span className="source-tag">MongoDB</span>
            </div>
            <p className="card-subtitle">
              Persistencia documental JSON orientada a catálogos dinámicos con esquemas flexibles y proveedores anidados.
            </p>
          </div>
          <div className="header-actions">
            <button
              type="button"
              className="btn-primary"
              onClick={() => setMostrarModal(true)}
            >
              + Nuevo Producto
            </button>
            <button type="button" className="btn-refresh" onClick={cargarProductos} disabled={cargando}>
              Actualizar
            </button>
          </div>
        </div>

        <div className="search-bar">
          <input
            type="text"
            className="search-input"
            placeholder="Buscar producto por nombre, descripción o proveedor..."
            value={filtro}
            onChange={(e) => setFiltro(e.target.value)}
          />
        </div>
      </div>

      {notificacion && (
        <div className={`feedback-banner banner-${notificacion.tipo}`}>
          <span>{notificacion.mensaje}</span>
          <button type="button" className="banner-close" onClick={() => setNotificacion(null)}>&times;</button>
        </div>
      )}

      {/* Grid de Productos */}
      <div className="products-grid">
        {cargando ? (
          <div className="skeleton-container" aria-busy="true">
            <div className="skeleton-item" />
            <div className="skeleton-item" />
          </div>
        ) : productosFiltrados.length === 0 ? (
          <div className="surface-card empty-state">
            <p className="empty-title">No hay productos disponibles</p>
            <p className="empty-hint">Agregue un nuevo producto con el botón superior.</p>
          </div>
        ) : (
          productosFiltrados.map((p) => {
            const margen = p.precioVenta && p.costo
              ? ((p.precioVenta - p.costo) / p.precioVenta) * 100
              : 0;

            return (
              <div key={p.id} className="surface-card product-card">
                <div className="product-card-top">
                  <span className="product-card-id">ID: {p.id?.slice(-6) || 'PROD'}</span>
                  <span className="product-card-price tabular-nums">{formatMoney(p.precioVenta)}</span>
                </div>
                <h3 className="product-card-name">{p.nombre}</h3>
                <p className="product-card-desc">{p.descripcion || 'Sin descripción detallada'}</p>

                <div className="product-card-financials">
                  <div className="fin-item">
                    <span className="fin-label">Costo Unitario:</span>
                    <span className="tabular-nums">{formatMoney(p.costo)}</span>
                  </div>
                  <div className="fin-item">
                    <span className="fin-label">Margen Bruto:</span>
                    <span className="fin-margin tabular-nums">{margen.toFixed(1)}%</span>
                  </div>
                </div>

                <div className="product-card-footer">
                  <span className="product-card-supplier">
                    Proveedor: {p.proveedor?.nombre || 'General'}
                  </span>
                </div>
              </div>
            );
          })
        )}
      </div>

      {/* Modal Nuevo Producto */}
      {mostrarModal && (
        <div className="modal-backdrop" onClick={() => setMostrarModal(false)}>
          <div className="modal-card" onClick={(e) => e.stopPropagation()}>
            <div className="modal-header">
              <h3 className="modal-title">Registrar Producto en MongoDB</h3>
              <button type="button" className="modal-close" onClick={() => setMostrarModal(false)}>&times;</button>
            </div>
            <form onSubmit={handleSubmit} className="modal-form">
              <div className="form-group">
                <label className="field-label">Nombre del Producto *</label>
                <input
                  type="text"
                  required
                  className="search-input"
                  placeholder="Ej: Panela Pulverizada 500g"
                  value={nuevoProducto.nombre}
                  onChange={(e) => setNuevoProducto({ ...nuevoProducto, nombre: e.target.value })}
                />
              </div>

              <div className="form-group">
                <label className="field-label">Descripción</label>
                <textarea
                  className="search-input"
                  rows="2"
                  placeholder="Descripción detallada del artículo..."
                  value={nuevoProducto.descripcion}
                  onChange={(e) => setNuevoProducto({ ...nuevoProducto, descripcion: e.target.value })}
                />
              </div>

              <div className="form-grid-two">
                <div className="form-group">
                  <label className="field-label">Precio de Venta (COP) *</label>
                  <input
                    type="number"
                    required
                    className="search-input tabular-nums"
                    placeholder="Ej: 5200"
                    value={nuevoProducto.precioVenta}
                    onChange={(e) => setNuevoProducto({ ...nuevoProducto, precioVenta: e.target.value })}
                  />
                </div>
                <div className="form-group">
                  <label className="field-label">Costo Unitario (COP)</label>
                  <input
                    type="number"
                    className="search-input tabular-nums"
                    placeholder="Ej: 4100"
                    value={nuevoProducto.costo}
                    onChange={(e) => setNuevoProducto({ ...nuevoProducto, costo: e.target.value })}
                  />
                </div>
              </div>

              <div className="form-group">
                <label className="field-label">Proveedor</label>
                <input
                  type="text"
                  className="search-input"
                  placeholder="Ej: Distribuciones Andinas S.A.S."
                  value={nuevoProducto.proveedorNombre}
                  onChange={(e) => setNuevoProducto({ ...nuevoProducto, proveedorNombre: e.target.value })}
                />
              </div>

              <div className="modal-actions">
                <button type="button" className="btn-secondary" onClick={() => setMostrarModal(false)}>
                  Cancelar
                </button>
                <button type="submit" className="btn-primary" disabled={guardando}>
                  {guardando ? 'Guardando...' : 'Guardar en MongoDB'}
                </button>
              </div>
            </form>
          </div>
        </div>
      )}
    </div>
  );
}

export default GestionProductos;
