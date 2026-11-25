package com.mycompany.veterinaria.seguridad;

/** Personal que inicia sesión (tabla usuario). */
public class Usuario {

    private final int id;
    private final String nombre;
    private final String usuario;
    private final Rol rol;

    public Usuario(int id, String nombre, String usuario, Rol rol) {
        this.id = id;
        this.nombre = nombre;
        this.usuario = usuario;
        this.rol = rol;
    }

    public int getId() { return id; }
    public String getNombre() { return nombre; }
    public String getUsuario() { return usuario; }
    public Rol getRol() { return rol; }
}
