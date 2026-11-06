package com.facturacion.domain.model;

import java.time.Instant;

/** Hecho OLAP de ventas (una fila por detalle de factura) para Apache Druid. */
public class VentaFact {
    private Instant timestamp;
    private Long facturaId;
    private String clienteDocumento;
    private String clienteNombre;
    private String clienteCiudad;
    private String clienteCategoria;
    private String productoId;
    private String productoNombre;
    private String proveedorNombre;
    private Integer cantidad;
    private Double precioUnitario;
    private Double costoUnitario;
    private Double subtotal;
    private Double ganancia;

    public VentaFact() {}

    public VentaFact(Instant timestamp, Long facturaId, String clienteDocumento, String clienteNombre,
                     String clienteCiudad, String clienteCategoria, String productoId, String productoNombre,
                     String proveedorNombre, Integer cantidad, Double precioUnitario, Double costoUnitario,
                     Double subtotal, Double ganancia) {
        this.timestamp = timestamp;
        this.facturaId = facturaId;
        this.clienteDocumento = clienteDocumento;
        this.clienteNombre = clienteNombre;
        this.clienteCiudad = clienteCiudad;
        this.clienteCategoria = clienteCategoria;
        this.productoId = productoId;
        this.productoNombre = productoNombre;
        this.proveedorNombre = proveedorNombre;
        this.cantidad = cantidad;
        this.precioUnitario = precioUnitario;
        this.costoUnitario = costoUnitario;
        this.subtotal = subtotal;
        this.ganancia = ganancia;
    }

    public Instant getTimestamp() { return timestamp; }
    public void setTimestamp(Instant timestamp) { this.timestamp = timestamp; }
    public Long getFacturaId() { return facturaId; }
    public void setFacturaId(Long facturaId) { this.facturaId = facturaId; }
    public String getClienteDocumento() { return clienteDocumento; }
    public void setClienteDocumento(String clienteDocumento) { this.clienteDocumento = clienteDocumento; }
    public String getClienteNombre() { return clienteNombre; }
    public void setClienteNombre(String clienteNombre) { this.clienteNombre = clienteNombre; }
    public String getClienteCiudad() { return clienteCiudad; }
    public void setClienteCiudad(String clienteCiudad) { this.clienteCiudad = clienteCiudad; }
    public String getClienteCategoria() { return clienteCategoria; }
    public void setClienteCategoria(String clienteCategoria) { this.clienteCategoria = clienteCategoria; }
    public String getProductoId() { return productoId; }
    public void setProductoId(String productoId) { this.productoId = productoId; }
    public String getProductoNombre() { return productoNombre; }
    public void setProductoNombre(String productoNombre) { this.productoNombre = productoNombre; }
    public String getProveedorNombre() { return proveedorNombre; }
    public void setProveedorNombre(String proveedorNombre) { this.proveedorNombre = proveedorNombre; }
    public Integer getCantidad() { return cantidad; }
    public void setCantidad(Integer cantidad) { this.cantidad = cantidad; }
    public Double getPrecioUnitario() { return precioUnitario; }
    public void setPrecioUnitario(Double precioUnitario) { this.precioUnitario = precioUnitario; }
    public Double getCostoUnitario() { return costoUnitario; }
    public void setCostoUnitario(Double costoUnitario) { this.costoUnitario = costoUnitario; }
    public Double getSubtotal() { return subtotal; }
    public void setSubtotal(Double subtotal) { this.subtotal = subtotal; }
    public Double getGanancia() { return ganancia; }
    public void setGanancia(Double ganancia) { this.ganancia = ganancia; }
}
