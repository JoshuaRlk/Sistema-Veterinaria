package com.mycompany.veterinaria.modelo;

import java.math.BigDecimal;
import java.time.LocalDate;

/** Producto del inventario (tabla producto). */
public class Producto {

    public static final String ACTIVO = "ACTIVO";
    public static final String BAJA = "BAJA";

    private int id;
    private final String codigoQr;
    private final String nombre;
    private final String categoria;
    private final BigDecimal precio;
    private int stock;
    private final String lote;
    private final LocalDate caducidad;
    private String estado;
    private String alerta;

    public Producto(int id, String codigoQr, String nombre, String categoria, BigDecimal precio, int stock,
            String lote, LocalDate caducidad, String estado, String alerta) {
        this.id = id;
        this.codigoQr = codigoQr;
        this.nombre = nombre;
        this.categoria = categoria;
        this.precio = precio;
        this.stock = stock;
        this.lote = lote;
        this.caducidad = caducidad;
        this.estado = estado;
        this.alerta = alerta;
    }

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }
    public String getCodigoQr() { return codigoQr; }
    public String getNombre() { return nombre; }
    public String getCategoria() { return categoria; }
    public BigDecimal getPrecio() { return precio; }
    public int getStock() { return stock; }
    public void setStock(int stock) { this.stock = stock; }
    public String getLote() { return lote; }
    public LocalDate getCaducidad() { return caducidad; }
    public String getEstado() { return estado; }
    public void setEstado(String estado) { this.estado = estado; }
    /** Aviso calculado por la vista vw_inventario_alertas (NORMAL, STOCK_BAJO, CADUCADO, etc.). */
    public String getAlerta() { return alerta; }
    public boolean isActivo() { return ACTIVO.equals(estado); }
}
