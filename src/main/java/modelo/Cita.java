package modelo;

import java.time.LocalDate;
import java.time.LocalTime;

/**
 * Representa una cita de la veterinaria.
 */
public class Cita {

    public static final String PROGRAMADA = "Programada";
    public static final String EN_CLINICA = "Mascota en clínica";
    public static final String CANCELADA = "Cancelada";

    private int id;
    private LocalDate fecha;
    private LocalTime hora;
    private String mascota;
    private String dueno;
    private String expediente;
    private String servicio;
    private String responsable;
    private String estado;

    public Cita(int id, LocalDate fecha, LocalTime hora, String mascota, String dueno,
            String expediente, String servicio, String responsable, String estado) {
        this.id = id;
        this.fecha = fecha;
        this.hora = hora;
        this.mascota = mascota;
        this.dueno = dueno;
        this.expediente = expediente;
        this.servicio = servicio;
        this.responsable = responsable;
        this.estado = estado;
    }

    /** Una cita cancelada ya no ocupa su horario. */
    public boolean ocupaHorario() {
        return !CANCELADA.equals(estado);
    }

    public int getId() { return id; }

    public LocalDate getFecha() { return fecha; }
    public void setFecha(LocalDate fecha) { this.fecha = fecha; }

    public LocalTime getHora() { return hora; }
    public void setHora(LocalTime hora) { this.hora = hora; }

    public String getMascota() { return mascota; }
    public String getDueno() { return dueno; }
    public String getExpediente() { return expediente; }
    public String getServicio() { return servicio; }
    public String getResponsable() { return responsable; }

    public String getEstado() { return estado; }
    public void setEstado(String estado) { this.estado = estado; }
}
