package com.mycompany.veterinaria.seguridad;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class RolTest {

    @Test
    @DisplayName("P23 El administrador abre todos los módulos")
    void administrador() {
        for (Modulo m : Modulo.values()) {
            assertTrue(Rol.ADMINISTRADOR.puedeAbrir(m));
        }
    }

    @Test
    @DisplayName("P24 Cada rol ve solo sus módulos (RF-A01)")
    void permisosPorRol() {
        assertTrue(Rol.RECEPCIONISTA.puedeAbrir(Modulo.COBROS));
        assertFalse(Rol.RECEPCIONISTA.puedeAbrir(Modulo.INVENTARIO));
        assertTrue(Rol.ALMACEN.puedeAbrir(Modulo.INVENTARIO));
        assertFalse(Rol.ALMACEN.puedeAbrir(Modulo.MASCOTAS));
        assertTrue(Rol.VETERINARIO.puedeAbrir(Modulo.CLINICA));
        assertFalse(Rol.VETERINARIO.puedeAbrir(Modulo.COBROS));
    }

    @Test
    @DisplayName("P25 Solo administrador y veterinario editan mascotas (RF-M08)")
    void editarMascotas() {
        assertTrue(Rol.ADMINISTRADOR.puedeEditarMascotas());
        assertTrue(Rol.VETERINARIO.puedeEditarMascotas());
        assertFalse(Rol.RECEPCIONISTA.puedeEditarMascotas());
        assertFalse(Rol.TECNICO.puedeEditarMascotas());
        assertFalse(Rol.ALMACEN.puedeEditarMascotas());
    }
}
