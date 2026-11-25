package com.mycompany.veterinaria.modelo;

import java.time.LocalDate;

/** Un renglón de la receta médica (tabla tratamiento). */
public class Tratamiento {

    private final String medicamento;
    private final String dosis;
    private final String indicaciones;
    private final LocalDate fechaRevision;

    public Tratamiento(String medicamento, String dosis, String indicaciones, LocalDate fechaRevision) {
        this.medicamento = medicamento;
        this.dosis = dosis;
        this.indicaciones = indicaciones;
        this.fechaRevision = fechaRevision;
    }

    public String getMedicamento() { return medicamento; }
    public String getDosis() { return dosis; }
    public String getIndicaciones() { return indicaciones; }
    public LocalDate getFechaRevision() { return fechaRevision; }
}
