package com.mycompany.veterinaria.servicios;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import com.mycompany.veterinaria.modelo.CitaClinica;
import com.mycompany.veterinaria.modelo.Concepto;
import com.mycompany.veterinaria.modelo.Producto;

/** Cuenta que se está armando en Cobros: productos y servicios, subtotal, descuento y total. */
public class Cuenta {

    private final List<Concepto> conceptos = new ArrayList<>();

    /** Agrega un producto; si ya estaba, suma la cantidad. Nunca permite vender más de lo que hay en stock. */
    public void agregarProducto(Producto p, int cantidad) {
        if (p == null || !p.isActivo()) {
            throw new IllegalArgumentException("El producto no existe o está dado de baja.");
        }
        if (cantidad <= 0) {
            throw new IllegalArgumentException("La cantidad debe ser mayor a 0.");
        }
        for (Concepto c : conceptos) {
            if (c.getTipo() == Concepto.Tipo.PRODUCTO && c.getIdReferencia() == p.getId()) {
                validarStock(p, c.getCantidad() + cantidad);
                c.setCantidad(c.getCantidad() + cantidad);
                return;
            }
        }
        validarStock(p, cantidad);
        conceptos.add(new Concepto(Concepto.Tipo.PRODUCTO, p.getId(), "Producto: " + p.getNombre(), cantidad,
                p.getPrecio(), p.getStock()));
    }

    /** Agrega el servicio de una cita atendida con su precio. Una cita solo se cobra una vez. */
    public void agregarServicio(CitaClinica cita, BigDecimal precio) {
        if (cita == null) {
            throw new IllegalArgumentException("Selecciona el servicio a cobrar.");
        }
        if (precio == null || precio.signum() < 0) {
            throw new IllegalArgumentException("El precio del servicio no puede ser negativo.");
        }
        for (Concepto c : conceptos) {
            if (c.getTipo() == Concepto.Tipo.SERVICIO && c.getIdReferencia() == cita.getId()) {
                throw new IllegalArgumentException("Ese servicio ya está en la cuenta.");
            }
        }
        conceptos.add(new Concepto(Concepto.Tipo.SERVICIO, cita.getId(),
                "Servicio: " + cita.getServicio() + " de " + cita.getMascota() + " (cita " + cita.getFecha() + ")",
                1, precio, null));
    }

    public void quitar(int indice) {
        conceptos.remove(indice);
    }

    public void vaciar() {
        conceptos.clear();
    }

    public List<Concepto> getConceptos() {
        return Collections.unmodifiableList(conceptos);
    }

    public boolean isVacia() {
        return conceptos.isEmpty();
    }

    public BigDecimal subtotal() {
        BigDecimal s = BigDecimal.ZERO;
        for (Concepto c : conceptos) {
            s = s.add(c.getSubtotal());
        }
        return s.setScale(2, RoundingMode.HALF_UP);
    }

    /** Total con descuento; el descuento no puede ser negativo ni mayor que el subtotal. */
    public BigDecimal total(BigDecimal descuento) {
        BigDecimal d = descuento == null ? BigDecimal.ZERO : descuento.setScale(2, RoundingMode.HALF_UP);
        if (d.signum() < 0) {
            throw new IllegalArgumentException("El descuento no puede ser negativo.");
        }
        if (d.compareTo(subtotal()) > 0) {
            throw new IllegalArgumentException("El descuento no puede ser mayor que el subtotal.");
        }
        return subtotal().subtract(d);
    }

    private static void validarStock(Producto p, int cantidadTotal) {
        if (cantidadTotal > p.getStock()) {
            throw new IllegalArgumentException("Stock insuficiente de " + p.getNombre() + ": hay " + p.getStock()
                    + " y se piden " + cantidadTotal + ".");
        }
    }
}
