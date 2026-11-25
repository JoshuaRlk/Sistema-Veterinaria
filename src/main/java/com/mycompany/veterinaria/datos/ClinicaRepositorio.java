package com.mycompany.veterinaria.datos;

import java.util.List;
import com.mycompany.veterinaria.modelo.Antecedentes;
import com.mycompany.veterinaria.modelo.CitaClinica;
import com.mycompany.veterinaria.modelo.RegistroClinico;
import com.mycompany.veterinaria.modelo.Tratamiento;

/** Acceso a datos del expediente clínico. */
public interface ClinicaRepositorio {

    Antecedentes antecedentes(int idMascota);

    void actualizarAntecedentes(int idMascota, String vacunas, String alergias);

    /** Citas de la mascota que todavía se pueden atender (PROGRAMADA, EN_ESPERA o EN_ATENCION). */
    List<CitaClinica> citasPorAtender(int idMascota);

    List<RegistroClinico> historial(int idMascota);

    /**
     * Guarda el diagnóstico con su receta y deja la cita en ATENDIDA, todo en una transacción.
     * @return id del diagnóstico
     */
    int registrarConsulta(int idCita, int idVeterinario, String sintomas, String diagnostico,
            List<Tratamiento> tratamientos);
}
