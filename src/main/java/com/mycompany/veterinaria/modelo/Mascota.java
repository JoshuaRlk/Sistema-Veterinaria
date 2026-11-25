package com.mycompany.veterinaria.modelo;

import java.math.BigDecimal;

/** Mascota con su expediente clínico (tabla mascota, relación 1 a 1 con el expediente). */
public class Mascota {

    public static final String ACTIVA = "ACTIVA";
    public static final String BAJA = "BAJA";

    private int id;
    private String expediente;
    private final Dueno dueno;
    private final String nombre;
    private final String especie;
    private final String raza;
    private final BigDecimal edad;
    private final BigDecimal peso;
    private String estado;

    public Mascota(int id, String expediente, Dueno dueno, String nombre, String especie, String raza,
            BigDecimal edad, BigDecimal peso, String estado) {
        this.id = id;
        this.expediente = expediente;
        this.dueno = dueno;
        this.nombre = nombre;
        this.especie = especie;
        this.raza = raza;
        this.edad = edad;
        this.peso = peso;
        this.estado = estado;
    }

    /** ID único que se muestra al usuario, por ejemplo M-000128 (RF-M04). */
    public static String formatoId(int id) {
        return String.format("M-%06d", id);
    }

    /** Número de expediente, por ejemplo EXP-000128 (RF-M04). */
    public static String formatoExpediente(int id) {
        return String.format("EXP-%06d", id);
    }

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }
    public String getExpediente() { return expediente; }
    public void setExpediente(String expediente) { this.expediente = expediente; }
    public Dueno getDueno() { return dueno; }
    public String getNombre() { return nombre; }
    public String getEspecie() { return especie; }
    public String getRaza() { return raza; }
    public BigDecimal getEdad() { return edad; }
    public BigDecimal getPeso() { return peso; }
    public String getEstado() { return estado; }
    public void setEstado(String estado) { this.estado = estado; }
    public boolean isActiva() { return ACTIVA.equals(estado); }
}
