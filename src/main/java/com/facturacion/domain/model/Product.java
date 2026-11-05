package com.facturacion.domain.model;

import java.util.List;

public class Product {
    private String id;
    private String nombre;
    private String descripcion;
    private Double costo;
    private Double precioVenta;
    private Supplier proveedor;
    private List<String> imageUrls;

    public Product() {}

    public Product(String id, String nombre, String descripcion, Double costo,
                   Double precioVenta, Supplier proveedor, List<String> imageUrls) {
        this.id = id;
        this.nombre = nombre;
        this.descripcion = descripcion;
        this.costo = costo;
        this.precioVenta = precioVenta;
        this.proveedor = proveedor;
        this.imageUrls = imageUrls;
    }

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }
    public String getNombre() { return nombre; }
    public void setNombre(String nombre) { this.nombre = nombre; }
    public String getDescripcion() { return descripcion; }
    public void setDescripcion(String descripcion) { this.descripcion = descripcion; }
    public Double getCosto() { return costo; }
    public void setCosto(Double costo) { this.costo = costo; }
    public Double getPrecioVenta() { return precioVenta; }
    public void setPrecioVenta(Double precioVenta) { this.precioVenta = precioVenta; }
    public Supplier getProveedor() { return proveedor; }
    public void setProveedor(Supplier proveedor) { this.proveedor = proveedor; }
    public List<String> getImageUrls() { return imageUrls; }
    public void setImageUrls(List<String> imageUrls) { this.imageUrls = imageUrls; }
}
