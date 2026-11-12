import React, { useState, useEffect } from 'react';
import axios from 'axios';
import './GestionClientes.css';

const API = 'http://localhost:8080/api';

function GestionClientes() {
  const [clientes, setClientes] = useState([]);
  const [cargando, setCargando] = useState(true);
  const [filtroCategoria, setFiltroCategoria] = useState('TODOS');
  const [mostrarModal, setMostrarModal] = useState(false);
  const [guardando, setGuardando] = useState(false);
  const [notificacion, setNotificacion] = useState(null);

  const [nuevoCliente, setNuevoCliente] = useState({
    documento: '',
    nombre: '',
    correo: '',
    telefono: '',
    ciudad: 'Mocoa',
    categoria: 'CLIENTE',
    tipoCliente: 'MAYORISTA'
  });

  const cargarClientes = async () => {
    setCargando(true);
    setNotificacion(null);
    try {
      const resp = await axios.get(`${API}/persons`);
      setClientes(Array.isArray(resp.data) ? resp.data : []);
    } catch (err) {
      setNotificacion({
        tipo: 'error',
        mensaje: 'No fue posible conectar con Cassandra. Verifique que el servicio esté activo.'
      });
    } finally {
      setCargando(false);
    }
  };

  useEffect(() => {
    cargarClientes();
  }, []);

  const handleSubmit = async (e) => {
    e.preventDefault();
    if (!nuevoCliente.documento || !nuevoCliente.nombre) {
      setNotificacion({ tipo: 'warning', mensaje: 'Documento y nombre son obligatorios.' });
      return;
    }

    setGuardando(true);
    try {
      await axios.post(`${API}/persons`, {
        ...nuevoCliente,
        categoria: nuevoCliente.tipoCliente || 'CLIENTE'
      });
      setNotificacion({ tipo: 'exito', mensaje: 'Cliente registrado exitosamente en Cassandra.' });
      setMostrarModal(false);
      setNuevoCliente({
        documento: '',
        nombre: '',
        correo: '',
        telefono: '',
        ciudad: 'Mocoa',
        categoria: 'CLIENTE',
        tipoCliente: 'MAYORISTA'
      });
      cargarClientes();
    } catch (err) {
      setNotificacion({ tipo: 'error', mensaje: 'Error al registrar cliente en Cassandra.' });
    } finally {
      setGuardando(false);
    }
  };

  const clientesFiltrados = clientes.filter((c) => {
    if (filtroCategoria === 'TODOS') return true;
    return c.categoria?.toUpperCase() === filtroCategoria.toUpperCase();
  });

  return (
    <div className="clients-container">
      <div className="surface-card clients-header-card">
        <div className="header-flex">
          <div>
            <div className="title-row">
              <h2 className="card-title">Gestión de Clientes y Personas</h2>
              <span className="source-tag">Apache Cassandra</span>
            </div>
            <p className="card-subtitle">
              Almacenamiento no relacional tabular de alta disponibilidad para el registro masivo de clientes.
            </p>
          </div>
          <div className="header-actions">
            <button
              type="button"
              className="btn-primary"
              onClick={() => setMostrarModal(true)}
            >
              + Nuevo Cliente
            </button>
            <button type="button" className="btn-refresh" onClick={cargarClientes} disabled={cargando}>
              Actualizar
            </button>
          </div>
        </div>

        {/* Filtros por Categoría */}
        <div className="category-filter-bar">
          <span className="filter-label">Filtrar por Categoría:</span>
          {['TODOS', 'CLIENTE', 'MAYORISTA', 'DISTRIBUIDOR', 'MINORISTA', 'CORPORATIVO'].map((cat) => (
            <button
              key={cat}
              type="button"
              className={`filter-btn ${filtroCategoria === cat ? 'active' : ''}`}
              onClick={() => setFiltroCategoria(cat)}
            >
              {cat}
            </button>
          ))}
        </div>
      </div>

      {notificacion && (
        <div className={`feedback-banner banner-${notificacion.tipo}`}>
          <span>{notificacion.mensaje}</span>
          <button type="button" className="banner-close" onClick={() => setNotificacion(null)}>&times;</button>
        </div>
      )}

      {/* Tabla de Clientes en Cassandra */}
      <div className="surface-card table-card">
        {cargando ? (
          <div className="skeleton-container" aria-busy="true">
            <div className="skeleton-item" />
            <div className="skeleton-item" />
          </div>
        ) : clientesFiltrados.length === 0 ? (
          <div className="empty-state">
            <p className="empty-title">No hay clientes con este filtro en Cassandra</p>
            <p className="empty-hint">Agregue un nuevo cliente con el botón superior.</p>
          </div>
        ) : (
          <div className="table-responsive">
            <table className="clients-table">
              <thead>
                <tr>
                  <th>Documento (Key)</th>
                  <th>Nombre Completo</th>
                  <th>Ciudad</th>
                  <th>Correo</th>
                  <th>Teléfono</th>
                  <th className="text-center">Categoría</th>
                </tr>
              </thead>
              <tbody>
                {clientesFiltrados.map((c) => (
                  <tr key={c.documento}>
                    <td className="font-semibold tabular-nums">{c.documento}</td>
                    <td className="font-medium">{c.nombre}</td>
                    <td>{c.ciudad || 'No especificada'}</td>
                    <td>{c.correo || 'N/A'}</td>
                    <td className="tabular-nums">{c.telefono || 'N/A'}</td>
                    <td className="text-center">
                      <span className="client-cat-badge">
                        {c.categoria || 'CLIENTE'}
                      </span>
                    </td>
                  </tr>
                ))}
              </tbody>
            </table>
          </div>
        )}
      </div>

      {/* Modal para registrar nuevo cliente */}
      {mostrarModal && (
        <div className="modal-backdrop" onClick={() => setMostrarModal(false)}>
          <div className="modal-card" onClick={(e) => e.stopPropagation()}>
            <div className="modal-header">
              <h3 className="modal-title">Registrar Nuevo Cliente en Cassandra</h3>
              <button type="button" className="modal-close" onClick={() => setMostrarModal(false)}>&times;</button>
            </div>
            <form onSubmit={handleSubmit} className="modal-form">
              <div className="form-group">
                <label className="field-label">Documento / Cédula / NIT *</label>
                <input
                  type="text"
                  required
                  className="search-input"
                  placeholder="Ej: 1004567890"
                  value={nuevoCliente.documento}
                  onChange={(e) => setNuevoCliente({ ...nuevoCliente, documento: e.target.value })}
                />
              </div>

              <div className="form-group">
                <label className="field-label">Nombre Completo *</label>
                <input
                  type="text"
                  required
                  className="search-input"
                  placeholder="Ej: Juan Pérez"
                  value={nuevoCliente.nombre}
                  onChange={(e) => setNuevoCliente({ ...nuevoCliente, nombre: e.target.value })}
                />
              </div>

              <div className="form-grid-two">
                <div className="form-group">
                  <label className="field-label">Ciudad</label>
                  <select
                    className="select-control"
                    value={nuevoCliente.ciudad}
                    onChange={(e) => setNuevoCliente({ ...nuevoCliente, ciudad: e.target.value })}
                  >
                    <option value="Mocoa">Mocoa</option>
                    <option value="Pasto">Pasto</option>
                    <option value="Sibundoy">Sibundoy</option>
                    <option value="Puerto Asis">Puerto Asís</option>
                    <option value="Bogota">Bogotá</option>
                    <option value="Cali">Cali</option>
                    <option value="Medellin">Medellín</option>
                  </select>
                </div>

                <div className="form-group">
                  <label className="field-label">Categoría Comercial</label>
                  <select
                    className="select-control"
                    value={nuevoCliente.tipoCliente}
                    onChange={(e) => setNuevoCliente({ ...nuevoCliente, tipoCliente: e.target.value })}
                  >
                    <option value="MAYORISTA">MAYORISTA</option>
                    <option value="DISTRIBUIDOR">DISTRIBUIDOR</option>
                    <option value="CORPORATIVO">CORPORATIVO</option>
                    <option value="MINORISTA">MINORISTA</option>
                    <option value="OCASIONAL">OCASIONAL</option>
                  </select>
                </div>
              </div>

              <div className="form-grid-two">
                <div className="form-group">
                  <label className="field-label">Correo Electrónico</label>
                  <input
                    type="email"
                    className="search-input"
                    placeholder="correo@ejemplo.com"
                    value={nuevoCliente.correo}
                    onChange={(e) => setNuevoCliente({ ...nuevoCliente, correo: e.target.value })}
                  />
                </div>
                <div className="form-group">
                  <label className="field-label">Teléfono</label>
                  <input
                    type="text"
                    className="search-input"
                    placeholder="3101234567"
                    value={nuevoCliente.telefono}
                    onChange={(e) => setNuevoCliente({ ...nuevoCliente, telefono: e.target.value })}
                  />
                </div>
              </div>

              <div className="modal-actions">
                <button type="button" className="btn-secondary" onClick={() => setMostrarModal(false)}>
                  Cancelar
                </button>
                <button type="submit" className="btn-primary" disabled={guardando}>
                  {guardando ? 'Guardando...' : 'Guardar en Cassandra'}
                </button>
              </div>
            </form>
          </div>
        </div>
      )}
    </div>
  );
}

export default GestionClientes;
