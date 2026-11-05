package com.facturacion.domain.model;

public class Person {
    private String documento; // primary key in Cassandra
    private String nombre;
    private String correo;
    private String telefono;
    private String ciudad;
    private String categoria; // CLIENTE, EMPLEADO, PROVEEDOR

    public Person() {}

    public Person(String documento, String nombre, String correo,
                  String telefono, String ciudad, String categoria) {
        this.documento = documento;
        this.nombre = nombre;
        this.correo = correo;
        this.telefono = telefono;
        this.ciudad = ciudad;
        this.categoria = categoria;
    }

    public String getDocumento() { return documento; }
    public void setDocumento(String documento) { this.documento = documento; }
    public String getNombre() { return nombre; }
    public void setNombre(String nombre) { this.nombre = nombre; }
    public String getCorreo() { return correo; }
    public void setCorreo(String correo) { this.correo = correo; }
    public String getTelefono() { return telefono; }
    public void setTelefono(String telefono) { this.telefono = telefono; }
    public String getCiudad() { return ciudad; }
    public void setCiudad(String ciudad) { this.ciudad = ciudad; }
    public String getCategoria() { return categoria; }
    public void setCategoria(String categoria) { this.categoria = categoria; }
}
