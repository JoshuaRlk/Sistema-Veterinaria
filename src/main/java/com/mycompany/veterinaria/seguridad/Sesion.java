package com.mycompany.veterinaria.seguridad;

/** Usuario que inició sesión. Lo usan los módulos para saber quién hace cada operación. */
public final class Sesion {

    private static Usuario actual;

    private Sesion() {
    }

    public static void iniciar(Usuario usuario) {
        actual = usuario;
    }

    public static void cerrar() {
        actual = null;
    }

    public static Usuario actual() {
        if (actual == null) {
            throw new IllegalStateException("No hay una sesión iniciada");
        }
        return actual;
    }

    public static boolean activa() {
        return actual != null;
    }
}
