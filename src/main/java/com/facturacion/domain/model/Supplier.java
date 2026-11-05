package com.facturacion.domain.model;

public class Supplier {
    private String nombre;
    private String contacto;
    private String telefono;

    public Supplier() {}

    public Supplier(String nombre, String contacto, String telefono) {
        this.nombre = nombre;
        this.contacto = contacto;
        this.telefono = telefono;
    }

    public String getNombre() { return nombre; }
    public void setNombre(String nombre) { this.nombre = nombre; }
    public String getContacto() { return contacto; }
    public void setContacto(String contacto) { this.contacto = contacto; }
    public String getTelefono() { return telefono; }
    public void setTelefono(String telefono) { this.telefono = telefono; }
}
