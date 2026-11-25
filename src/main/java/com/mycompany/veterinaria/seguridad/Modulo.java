package com.mycompany.veterinaria.seguridad;

/** Módulos de la barra superior (Pantalla 2 del prototipo, PRY04). */
public enum Modulo {
    INICIO("Inicio"),
    MASCOTAS("Dueños y mascotas"),
    CITAS("Citas"),
    CLINICA("Clínica"),
    INVENTARIO("Inventario"),
    COBROS("Cobros");

    private final String titulo;

    Modulo(String titulo) {
        this.titulo = titulo;
    }

    public String getTitulo() {
        return titulo;
    }
}
