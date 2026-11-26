package com.mycompany.veterinaria.servicios;

import static org.junit.jupiter.api.Assertions.*;

import com.mycompany.veterinaria.datos.InventarioRepositorio;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import com.mycompany.veterinaria.modelo.Producto;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import com.mycompany.veterinaria.seguridad.Rol;

class InventarioServiceTest {

    static class Memoria implements InventarioRepositorio {
        final List<Producto> productos = new ArrayList<>();

        @Override
        public List<Producto> listar(String filtro) {
            return productos;
        }

        @Override
        public Optional<Producto> buscarPorCodigo(String c) {
            return productos.stream().filter(p -> p.getCodigoQr().equals(c)).findFirst();
        }

        @Override
        public boolean existeCodigo(String c, int ignorar) {
            return productos.stream().anyMatch(p -> p.getCodigoQr().equals(c) && p.getId() != ignorar);
        }

        @Override
        public Producto registrar(Producto p) {
            p.setId(productos.size() + 1);
            productos.add(p);
            return p;
        }

        @Override
        public void actualizar(Producto p) {
            productos.replaceAll(x -> x.getId() == p.getId() ? p : x);
        }

        @Override
        public void sumarStock(int id, int n) {
            productos.stream().filter(p -> p.getId() == id).forEach(p -> p.setStock(p.getStock() + n));
        }

        @Override
        public void darDeBaja(int id) {
            productos.stream().filter(p -> p.getId() == id).forEach(p -> p.setEstado(Producto.BAJA));
        }
    }

    private Memoria repo;
    private InventarioService servicio;

    @BeforeEach
    void preparar() {
        repo = new Memoria();
        servicio = new InventarioService(repo);
    }

    @Test
    @DisplayName("P43 Registra un producto con precio, stock y caducidad")
    void registra() {
        Producto p = servicio.registrar(Rol.ALMACEN, "7501234567890", "Antiparasitario", "Farmacia", "$180",
                "11", "L-01", "31/12/2027");
        assertEquals(new BigDecimal("180.00"), p.getPrecio());
        assertEquals(LocalDate.of(2027, 12, 31), p.getCaducidad());
        assertTrue(p.isActivo());
    }

    @Test
    @DisplayName("P44 Valida obligatorios, números, fecha y código repetido")
    void valida() {
        servicio.registrar(Rol.ALMACEN, "A1", "Croquetas", "Alimento", "300", "5", "", "");
        assertThrows(IllegalArgumentException.class, () -> servicio.registrar(Rol.ALMACEN, "", "X", "Y", "1", "1", "", ""));
        assertThrows(IllegalArgumentException.class, () -> servicio.registrar(Rol.ALMACEN, "B", "X", "Y", "-1", "1", "", ""));
        assertThrows(IllegalArgumentException.class, () -> servicio.registrar(Rol.ALMACEN, "B", "X", "Y", "1", "1.5", "", ""));
        assertThrows(IllegalArgumentException.class, () -> servicio.registrar(Rol.ALMACEN, "B", "X", "Y", "1", "1", "", "31/02/2027"));
        assertThrows(IllegalArgumentException.class, () -> servicio.registrar(Rol.ALMACEN, "A1", "X", "Y", "1", "1", "", ""));
    }

    @Test
    @DisplayName("P45 Las entradas suman stock y deben ser mayores a 0")
    void entradas() {
        Producto p = servicio.registrar(Rol.ADMINISTRADOR, "A1", "Croquetas", "Alimento", "300", "5", "", "");
        servicio.registrarEntrada(Rol.ALMACEN, p.getId(), "10");
        assertEquals(15, repo.productos.get(0).getStock());
        assertThrows(IllegalArgumentException.class, () -> servicio.registrarEntrada(Rol.ALMACEN, p.getId(), "0"));
    }

    @Test
    @DisplayName("P46 Solo almacén y administrador modifican el inventario; la baja es lógica")
    void permisos() {
        assertThrows(IllegalStateException.class,
                () -> servicio.registrar(Rol.RECEPCIONISTA, "A1", "Croquetas", "Alimento", "300", "5", "", ""));
        Producto p = servicio.registrar(Rol.ALMACEN, "A1", "Croquetas", "Alimento", "300", "5", "", "");
        assertThrows(IllegalStateException.class, () -> servicio.darDeBaja(Rol.VETERINARIO, p.getId()));
        servicio.darDeBaja(Rol.ALMACEN, p.getId());
        assertEquals(Producto.BAJA, repo.productos.get(0).getEstado());
    }
}
