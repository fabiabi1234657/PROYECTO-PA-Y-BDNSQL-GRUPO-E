package com.facturacion.infrastructure.adapter.mysql;

import jakarta.persistence.*;

@Entity
@Table(name = "detalle_factura")
public class InvoiceDetailEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "factura_id")
    private InvoiceEntity factura;

    @Column(name = "producto_id")
    private String productoId;

    @Column(name = "producto_nombre")
    private String productoNombre;

    private Integer cantidad;

    @Column(name = "precio_unitario")
    private Double precioUnitario;

    private Double subtotal;

    public InvoiceDetailEntity() {}

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public InvoiceEntity getFactura() { return factura; }
    public void setFactura(InvoiceEntity factura) { this.factura = factura; }
    public String getProductoId() { return productoId; }
    public void setProductoId(String productoId) { this.productoId = productoId; }
    public String getProductoNombre() { return productoNombre; }
    public void setProductoNombre(String productoNombre) { this.productoNombre = productoNombre; }
    public Integer getCantidad() { return cantidad; }
    public void setCantidad(Integer cantidad) { this.cantidad = cantidad; }
    public Double getPrecioUnitario() { return precioUnitario; }
    public void setPrecioUnitario(Double precioUnitario) { this.precioUnitario = precioUnitario; }
    public Double getSubtotal() { return subtotal; }
    public void setSubtotal(Double subtotal) { this.subtotal = subtotal; }
}
