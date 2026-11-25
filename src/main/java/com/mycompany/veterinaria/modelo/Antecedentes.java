package com.mycompany.veterinaria.modelo;

/** Datos médicos generales del expediente (columnas vacunas, alergias e historial_medico de mascota). */
public class Antecedentes {

    private final String vacunas;
    private final String alergias;
    private final String historialMedico;

    public Antecedentes(String vacunas, String alergias, String historialMedico) {
        this.vacunas = vacunas;
        this.alergias = alergias;
        this.historialMedico = historialMedico;
    }

    public String getVacunas() { return vacunas; }
    public String getAlergias() { return alergias; }
    public String getHistorialMedico() { return historialMedico; }
}
