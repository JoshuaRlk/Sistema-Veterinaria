package com.mycompany.veterinaria.modelo;

import java.time.LocalDate;
import java.time.LocalDateTime;

/** Renglón del historial clínico (vista vw_historial_clinico_citas). */
public class RegistroClinico {

    private final LocalDateTime fecha;
    private final String servicio;
    private final String sintomas;
    private final String diagnostico;
    private final String veterinario;
    private final String medicamento;
    private final String dosis;
    private final LocalDate fechaRevision;

    public RegistroClinico(LocalDateTime fecha, String servicio, String sintomas, String diagnostico,
            String veterinario, String medicamento, String dosis, LocalDate fechaRevision) {
        this.fecha = fecha;
        this.servicio = servicio;
        this.sintomas = sintomas;
        this.diagnostico = diagnostico;
        this.veterinario = veterinario;
        this.medicamento = medicamento;
        this.dosis = dosis;
        this.fechaRevision = fechaRevision;
    }

    public LocalDateTime getFecha() { return fecha; }
    public String getServicio() { return servicio; }
    public String getSintomas() { return sintomas; }
    public String getDiagnostico() { return diagnostico; }
    public String getVeterinario() { return veterinario; }
    public String getMedicamento() { return medicamento; }
    public String getDosis() { return dosis; }
    public LocalDate getFechaRevision() { return fechaRevision; }
}
