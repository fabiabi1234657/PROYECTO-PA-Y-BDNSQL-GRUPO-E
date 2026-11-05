package com.facturacion.infrastructure.adapter.mongodb;

import com.facturacion.domain.model.Supplier;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import java.util.List;

@Document(collection = "products")
public class ProductDocument {

    @Id
    private String id;
    private String nombre;
    private String descripcion;
    private Double costo;
    private Double precioVenta;
    private Supplier proveedor;
    private List<String> imageUrls;

    public ProductDocument() {}

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
