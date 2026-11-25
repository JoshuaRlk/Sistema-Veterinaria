package com.mycompany.veterinaria.seguridad;

import java.util.EnumSet;
import java.util.Set;

/**
 * Roles del sistema (columna usuario.rol) y los módulos que puede abrir cada uno,
 * según la sección 1.2.3 (características de los usuarios) del PRY04.
 */
public enum Rol {
    ADMINISTRADOR(EnumSet.allOf(Modulo.class)),
    VETERINARIO(EnumSet.of(Modulo.INICIO, Modulo.MASCOTAS, Modulo.CITAS, Modulo.CLINICA)),
    TECNICO(EnumSet.of(Modulo.INICIO, Modulo.MASCOTAS, Modulo.CITAS, Modulo.CLINICA)),
    RECEPCIONISTA(EnumSet.of(Modulo.INICIO, Modulo.MASCOTAS, Modulo.CITAS, Modulo.COBROS)),
    ALMACEN(EnumSet.of(Modulo.INICIO, Modulo.INVENTARIO));

    private final Set<Modulo> modulos;

    Rol(Set<Modulo> modulos) {
        this.modulos = modulos;
    }

    public boolean puedeAbrir(Modulo modulo) {
        return modulos.contains(modulo);
    }

    /** RF-M08: solo administrador y veterinario editan o dan de baja mascotas. */
    public boolean puedeEditarMascotas() {
        return this == ADMINISTRADOR || this == VETERINARIO;
    }
}
