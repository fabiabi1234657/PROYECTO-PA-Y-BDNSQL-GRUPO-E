package com.facturacion.domain.model;

import java.time.LocalDateTime;
import java.util.List;

public class Invoice {
    private Long id;
    private String clienteDocumento;
    private String clienteNombre;
    private LocalDateTime fechaEmision;
    private List<InvoiceDetail> detalles;
    private Double subtotal;
    private Double iva; // 19%
    private Double total;
    private String estado; // EMITIDA, ANULADA

    public Invoice() {}

    public Invoice(String clienteDocumento, String clienteNombre, List<InvoiceDetail> detalles) {
        this.clienteDocumento = clienteDocumento;
        this.clienteNombre = clienteNombre;
        this.detalles = detalles;
        this.fechaEmision = LocalDateTime.now();
        this.estado = "EMITIDA";
        calcularTotales();
    }

    private void calcularTotales() {
        this.subtotal = detalles.stream()
            .mapToDouble(InvoiceDetail::getSubtotal)
            .sum();
        this.iva = this.subtotal * 0.19;
        this.total = this.subtotal + this.iva;
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getClienteDocumento() { return clienteDocumento; }
    public void setClienteDocumento(String clienteDocumento) { this.clienteDocumento = clienteDocumento; }
    public String getClienteNombre() { return clienteNombre; }
    public void setClienteNombre(String clienteNombre) { this.clienteNombre = clienteNombre; }
    public LocalDateTime getFechaEmision() { return fechaEmision; }
    public void setFechaEmision(LocalDateTime fechaEmision) { this.fechaEmision = fechaEmision; }
    public List<InvoiceDetail> getDetalles() { return detalles; }
    public void setDetalles(List<InvoiceDetail> detalles) { this.detalles = detalles; }
    public Double getSubtotal() { return subtotal; }
    public void setSubtotal(Double subtotal) { this.subtotal = subtotal; }
    public Double getIva() { return iva; }
    public void setIva(Double iva) { this.iva = iva; }
    public Double getTotal() { return total; }
    public void setTotal(Double total) { this.total = total; }
    public String getEstado() { return estado; }
    public void setEstado(String estado) { this.estado = estado; }
}
