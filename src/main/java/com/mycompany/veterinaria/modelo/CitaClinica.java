package com.mycompany.veterinaria.modelo;

import java.time.LocalDate;
import java.time.LocalTime;

/** Cita vista desde Clínica o Cobros: la que se atiende o se cobra. */
public class CitaClinica {

    private final int id;
    private final LocalDate fecha;
    private final LocalTime hora;
    private final String servicio;
    private final String mascota;
    private final String estado;

    public CitaClinica(int id, LocalDate fecha, LocalTime hora, String servicio, String mascota, String estado) {
        this.id = id;
        this.fecha = fecha;
        this.hora = hora;
        this.servicio = servicio;
        this.mascota = mascota;
        this.estado = estado;
    }

    public int getId() { return id; }
    public LocalDate getFecha() { return fecha; }
    public LocalTime getHora() { return hora; }
    public String getServicio() { return servicio; }
    public String getMascota() { return mascota; }
    public String getEstado() { return estado; }

    @Override
    public String toString() {
        return fecha + " " + hora + " · " + servicio + " (" + estado + ")";
    }
}
