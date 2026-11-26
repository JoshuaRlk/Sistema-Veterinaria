package com.mycompany.veterinaria.servicios;

import static org.junit.jupiter.api.Assertions.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalTime;
import com.mycompany.veterinaria.modelo.CitaClinica;
import com.mycompany.veterinaria.modelo.Producto;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class CuentaTest {

    static Producto antiparasitario(int stock) {
        return new Producto(7, "7501234567890", "Antiparasitario 10 kg", "Farmacia", new BigDecimal("180.00"),
                stock, "L-01", null, Producto.ACTIVO, "NORMAL");
    }

    static CitaClinica consulta() {
        return new CitaClinica(2, LocalDate.of(2026, 11, 20), LocalTime.of(10, 0), "Consulta general", "Firulais",
                "ATENDIDA");
    }

    @Test
    @DisplayName("P34 Calcula subtotal y total como en la Figura 10 ($350 + $180 = $530)")
    void totales() {
        Cuenta c = new Cuenta();
        c.agregarServicio(consulta(), new BigDecimal("350"));
        c.agregarProducto(antiparasitario(11), 1);
        assertEquals(new BigDecimal("530.00"), c.subtotal());
        assertEquals(new BigDecimal("480.00"), c.total(new BigDecimal("50")));
    }

    @Test
    @DisplayName("P35 El mismo producto se acumula en un solo renglón")
    void acumula() {
        Cuenta c = new Cuenta();
        c.agregarProducto(antiparasitario(11), 2);
        c.agregarProducto(antiparasitario(11), 3);
        assertEquals(1, c.getConceptos().size());
        assertEquals(5, c.getConceptos().get(0).getCantidad());
        assertEquals(new BigDecimal("900.00"), c.subtotal());
    }

    @Test
    @DisplayName("P36 No deja vender más de lo que hay en stock, ni en dos partes")
    void stock() {
        Cuenta c = new Cuenta();
        assertThrows(IllegalArgumentException.class, () -> c.agregarProducto(antiparasitario(3), 4));
        c.agregarProducto(antiparasitario(3), 2);
        assertThrows(IllegalArgumentException.class, () -> c.agregarProducto(antiparasitario(3), 2));
        assertEquals(2, c.getConceptos().get(0).getCantidad());
    }

    @Test
    @DisplayName("P37 Rechaza cantidad 0, producto dado de baja y servicio repetido")
    void invalidos() {
        Cuenta c = new Cuenta();
        assertThrows(IllegalArgumentException.class, () -> c.agregarProducto(antiparasitario(5), 0));
        Producto baja = new Producto(8, "X", "Viejo", "F", BigDecimal.ONE, 9, null, null, Producto.BAJA, null);
        assertThrows(IllegalArgumentException.class, () -> c.agregarProducto(baja, 1));
        c.agregarServicio(consulta(), new BigDecimal("350"));
        assertThrows(IllegalArgumentException.class, () -> c.agregarServicio(consulta(), new BigDecimal("350")));
    }

    @Test
    @DisplayName("P38 El descuento no puede ser negativo ni mayor que el subtotal")
    void descuento() {
        Cuenta c = new Cuenta();
        c.agregarProducto(antiparasitario(5), 1);
        assertThrows(IllegalArgumentException.class, () -> c.total(new BigDecimal("-1")));
        assertThrows(IllegalArgumentException.class, () -> c.total(new BigDecimal("180.01")));
        assertEquals(new BigDecimal("0.00"), c.total(new BigDecimal("180")));
    }
}
