package com.mycompany.veterinaria.servicios;

import static org.junit.jupiter.api.Assertions.*;

import com.mycompany.veterinaria.datos.CobroRepositorio;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import com.mycompany.veterinaria.modelo.CitaClinica;
import com.mycompany.veterinaria.modelo.Concepto;
import com.mycompany.veterinaria.modelo.Dueno;
import com.mycompany.veterinaria.modelo.Producto;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import com.mycompany.veterinaria.seguridad.Rol;

class CobroServiceTest {

    static class Falso implements CobroRepositorio {
        int ventas;
        BigDecimal ultimoTotal;
        List<Concepto> ultimosConceptos;

        @Override
        public List<Dueno> buscarDuenos(String texto) {
            return List.of(new Dueno(1, "Pedro Hernandez", "2281234567", null));
        }

        @Override
        public List<CitaClinica> serviciosPorCobrar(int idDueno) {
            return List.of(CuentaTest.consulta());
        }

        @Override
        public Optional<Producto> productoPorCodigo(String codigo) {
            return "7501234567890".equals(codigo) ? Optional.of(CuentaTest.antiparasitario(11)) : Optional.empty();
        }

        @Override
        public int registrarVenta(int idDueno, int idUsuario, BigDecimal descuento, BigDecimal total, String metodo,
                List<Concepto> conceptos) {
            ventas++;
            ultimoTotal = total;
            ultimosConceptos = new ArrayList<>(conceptos);
            return 15;
        }
    }

    private Falso repo;
    private CobroService servicio;
    private final Dueno pedro = new Dueno(1, "Pedro Hernandez", "2281234567", null);

    @BeforeEach
    void preparar() {
        repo = new Falso();
        servicio = new CobroService(repo);
    }

    @Test
    @DisplayName("P39 Agrega un producto por código y avisa si el código no existe")
    void porCodigo() {
        Cuenta c = new Cuenta();
        servicio.agregarProducto(c, " 7501234567890 ", "2");
        assertEquals(new BigDecimal("360.00"), c.subtotal());
        assertThrows(IllegalArgumentException.class, () -> servicio.agregarProducto(c, "000", "1"));
        assertThrows(IllegalArgumentException.class, () -> servicio.agregarProducto(c, "7501234567890", "dos"));
    }

    @Test
    @DisplayName("P40 Cobra con descuento y manda el total correcto a la base de datos")
    void cobra() {
        Cuenta c = new Cuenta();
        servicio.agregarServicio(c, CuentaTest.consulta(), "$350");
        servicio.agregarProducto(c, "7501234567890", "1");
        int id = servicio.cobrar(Rol.RECEPCIONISTA, 3, pedro, c, "30", "TARJETA");
        assertEquals(15, id);
        assertEquals(new BigDecimal("500.00"), repo.ultimoTotal);
        assertEquals(2, repo.ultimosConceptos.size());
    }

    @Test
    @DisplayName("P41 No cobra sin permiso, sin dueño, con cuenta vacía o método inválido")
    void validaciones() {
        Cuenta vacia = new Cuenta();
        Cuenta llena = new Cuenta();
        servicio.agregarProducto(llena, "7501234567890", "1");
        assertThrows(IllegalStateException.class, () -> servicio.cobrar(Rol.VETERINARIO, 1, pedro, llena, "0", "EFECTIVO"));
        assertThrows(IllegalArgumentException.class, () -> servicio.cobrar(Rol.ADMINISTRADOR, 1, null, llena, "0", "EFECTIVO"));
        assertThrows(IllegalArgumentException.class, () -> servicio.cobrar(Rol.ADMINISTRADOR, 1, pedro, vacia, "0", "EFECTIVO"));
        assertThrows(IllegalArgumentException.class, () -> servicio.cobrar(Rol.ADMINISTRADOR, 1, pedro, llena, "0", "VALES"));
        assertThrows(IllegalArgumentException.class, () -> servicio.cobrar(Rol.ADMINISTRADOR, 1, pedro, llena, "abc", "EFECTIVO"));
        assertEquals(0, repo.ventas);
    }

    @Test
    @DisplayName("P42 El ticket trae folio, cliente, conceptos y total")
    void ticket() {
        Cuenta c = new Cuenta();
        servicio.agregarServicio(c, CuentaTest.consulta(), "350");
        String t = CobroService.ticket(15, LocalDateTime.of(2026, 11, 23, 18, 30), pedro, "Recepción", c,
                new BigDecimal("50"), "EFECTIVO");
        assertTrue(t.contains("V-000015"));
        assertTrue(t.contains("Pedro Hernandez"));
        assertTrue(t.contains("Consulta general"));
        assertTrue(t.contains("TOTAL:     $300.00"));
    }
}
