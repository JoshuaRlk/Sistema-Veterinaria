package com.mycompany.veterinaria.modelo;

import java.math.BigDecimal;
import java.math.RoundingMode;

/** Renglón de la cuenta: un producto o un servicio (cita atendida). Se guarda en detalle_venta. */
public class Concepto {

    public enum Tipo { PRODUCTO, SERVICIO }

    private final Tipo tipo;
    private final int idReferencia; // id_producto o id_cita
    private final String descripcion;
    private int cantidad;
    private final BigDecimal precioUnitario;
    private final Integer stockDisponible; // null para servicios

    public Concepto(Tipo tipo, int idReferencia, String descripcion, int cantidad, BigDecimal precioUnitario,
            Integer stockDisponible) {
        this.tipo = tipo;
        this.idReferencia = idReferencia;
        this.descripcion = descripcion;
        this.cantidad = cantidad;
        this.precioUnitario = precioUnitario.setScale(2, RoundingMode.HALF_UP);
        this.stockDisponible = stockDisponible;
    }

    public BigDecimal getSubtotal() {
        return precioUnitario.multiply(BigDecimal.valueOf(cantidad)).setScale(2, RoundingMode.HALF_UP);
    }

    public Tipo getTipo() { return tipo; }
    public int getIdReferencia() { return idReferencia; }
    public String getDescripcion() { return descripcion; }
    public int getCantidad() { return cantidad; }
    public void setCantidad(int cantidad) { this.cantidad = cantidad; }
    public BigDecimal getPrecioUnitario() { return precioUnitario; }
    public Integer getStockDisponible() { return stockDisponible; }
}
