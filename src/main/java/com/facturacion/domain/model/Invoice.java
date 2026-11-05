package com.facturacion.domain.model;

import java.time.LocalDateTime;
import java.util.Collections;
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

    public void calcularTotales() {
        List<InvoiceDetail> items = detalles != null ? detalles : Collections.emptyList();
        double sum = 0.0;
        for (InvoiceDetail d : items) {
            int cant = d.getCantidad() != null ? d.getCantidad() : 0;
            double precio = d.getPrecioUnitario() != null ? d.getPrecioUnitario() : 0.0;
            double lineSub = round2(cant * precio);
            d.setSubtotal(lineSub);
            sum += lineSub;
        }
        this.subtotal = round2(sum);
        this.iva = round2(this.subtotal * 0.19);
        this.total = round2(this.subtotal + this.iva);
        if (this.fechaEmision == null) {
            this.fechaEmision = LocalDateTime.now();
        }
        if (this.estado == null || this.estado.isBlank()) {
            this.estado = "EMITIDA";
        }
    }

    private static double round2(double value) {
        return Math.round(value * 100.0) / 100.0;
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
