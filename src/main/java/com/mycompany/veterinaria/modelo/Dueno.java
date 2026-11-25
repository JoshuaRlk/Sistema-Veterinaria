package com.mycompany.veterinaria.modelo;

/** Dueño de una o varias mascotas (tabla dueno). */
public class Dueno {

    private int id;
    private final String nombre;
    private final String telefono;
    private final String correo;

    public Dueno(int id, String nombre, String telefono, String correo) {
        this.id = id;
        this.nombre = nombre;
        this.telefono = telefono;
        this.correo = correo;
    }

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }
    public String getNombre() { return nombre; }
    public String getTelefono() { return telefono; }
    public String getCorreo() { return correo; }
}
