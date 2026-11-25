package com.mycompany.veterinaria.seguridad;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class ContrasenasTest {

    @Test
    @DisplayName("P20 La contraseña cifrada no se guarda en texto plano y se puede verificar")
    void cifraYVerifica() {
        String guardado = Contrasenas.cifrar("Admin2026");
        assertFalse(guardado.contains("Admin2026"));
        assertTrue(guardado.startsWith("pbkdf2$"));
        assertTrue(Contrasenas.coincide("Admin2026", guardado));
        assertFalse(Contrasenas.coincide("admin2026", guardado));
    }

    @Test
    @DisplayName("P21 La misma contraseña genera hashes distintos (sal aleatoria)")
    void salAleatoria() {
        assertFalse(Contrasenas.cifrar("Vet2026").equals(Contrasenas.cifrar("Vet2026")));
    }

    @Test
    @DisplayName("P22 Un hash inválido o pendiente nunca deja entrar")
    void hashInvalido() {
        assertFalse(Contrasenas.coincide("x", "pendiente_bcrypt"));
        assertFalse(Contrasenas.coincide("x", null));
        assertFalse(Contrasenas.coincide(null, Contrasenas.cifrar("x")));
        assertFalse(Contrasenas.coincide("x", "pbkdf2$1$no-es-base64$tampoco"));
    }
}
