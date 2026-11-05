package com.facturacion.infrastructure.adapter.mysql;

import jakarta.persistence.*;
import java.time.LocalDateTime;
import java.util.List;

@Entity
@Table(name = "facturas")
public class InvoiceEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "cliente_documento")
    private String clienteDocumento;

    @Column(name = "cliente_nombre")
    private String clienteNombre;

    @Column(name = "fecha_emision")
    private LocalDateTime fechaEmision;

    private Double subtotal;
    private Double iva;
    private Double total;
    private String estado;

    @OneToMany(mappedBy = "factura", cascade = CascadeType.ALL, fetch = FetchType.EAGER)
    private List<InvoiceDetailEntity> detalles;

    public InvoiceEntity() {}

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getClienteDocumento() { return clienteDocumento; }
    public void setClienteDocumento(String clienteDocumento) { this.clienteDocumento = clienteDocumento; }
    public String getClienteNombre() { return clienteNombre; }
    public void setClienteNombre(String clienteNombre) { this.clienteNombre = clienteNombre; }
    public LocalDateTime getFechaEmision() { return fechaEmision; }
    public void setFechaEmision(LocalDateTime fechaEmision) { this.fechaEmision = fechaEmision; }
    public Double getSubtotal() { return subtotal; }
    public void setSubtotal(Double subtotal) { this.subtotal = subtotal; }
    public Double getIva() { return iva; }
    public void setIva(Double iva) { this.iva = iva; }
    public Double getTotal() { return total; }
    public void setTotal(Double total) { this.total = total; }
    public String getEstado() { return estado; }
    public void setEstado(String estado) { this.estado = estado; }
    public List<InvoiceDetailEntity> getDetalles() { return detalles; }
    public void setDetalles(List<InvoiceDetailEntity> detalles) { this.detalles = detalles; }
}
