package com.mycompany.veterinaria.servicios;

import com.mycompany.veterinaria.datos.ClinicaRepositorio;
import java.time.Clock;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.time.format.ResolverStyle;
import java.util.ArrayList;
import java.util.List;
import com.mycompany.veterinaria.modelo.Antecedentes;
import com.mycompany.veterinaria.modelo.CitaClinica;
import com.mycompany.veterinaria.modelo.RegistroClinico;
import com.mycompany.veterinaria.modelo.Tratamiento;
import com.mycompany.veterinaria.seguridad.Rol;

/** Reglas del expediente clínico (CU de consulta médica, Figura 9 del PRY04). */
public class ClinicaService {

    private static final DateTimeFormatter FECHA = DateTimeFormatter.ofPattern("d/M/uuuu")
            .withResolverStyle(ResolverStyle.STRICT);

    private final ClinicaRepositorio repositorio;
    private final Clock reloj;

    public ClinicaService(ClinicaRepositorio repositorio) {
        this(repositorio, Clock.systemDefaultZone());
    }

    public ClinicaService(ClinicaRepositorio repositorio, Clock reloj) {
        this.repositorio = repositorio;
        this.reloj = reloj;
    }

    public Antecedentes antecedentes(int idMascota) {
        return repositorio.antecedentes(idMascota);
    }

    public List<CitaClinica> citasPorAtender(int idMascota) {
        return repositorio.citasPorAtender(idMascota);
    }

    public List<RegistroClinico> historial(int idMascota) {
        return repositorio.historial(idMascota);
    }

    /** Solo el veterinario (o el administrador) registra diagnósticos y recetas. */
    public static boolean puedeDiagnosticar(Rol rol) {
        return rol == Rol.VETERINARIO || rol == Rol.ADMINISTRADOR;
    }

    public void actualizarAntecedentes(Rol rol, int idMascota, String vacunas, String alergias) {
        exigirPermiso(rol);
        repositorio.actualizarAntecedentes(idMascota, limpio(vacunas), limpio(alergias));
    }

    /**
     * Valida y guarda la consulta: diagnóstico obligatorio; si hay medicamento, la dosis es obligatoria;
     * la fecha de revisión (dd/mm/aaaa) es opcional pero no puede ser pasada.
     */
    public int registrarConsulta(Rol rol, int idVeterinario, CitaClinica cita, String sintomas, String diagnostico,
            String medicamento, String dosis, String indicaciones, String fechaRevision) {
        exigirPermiso(rol);
        if (cita == null) {
            throw new IllegalArgumentException("Selecciona la cita que se está atendiendo.");
        }
        if (vacio(diagnostico)) {
            throw new IllegalArgumentException("El diagnóstico es obligatorio.");
        }
        LocalDate revision = null;
        if (!vacio(fechaRevision)) {
            try {
                revision = LocalDate.parse(fechaRevision.trim(), FECHA);
            } catch (DateTimeParseException ex) {
                throw new IllegalArgumentException("La fecha de revisión debe tener el formato dd/mm/aaaa.");
            }
            if (revision.isBefore(LocalDate.now(reloj))) {
                throw new IllegalArgumentException("La fecha de revisión no puede ser una fecha pasada.");
            }
        }
        List<Tratamiento> receta = new ArrayList<>();
        if (!vacio(medicamento)) {
            if (vacio(dosis)) {
                throw new IllegalArgumentException("Escribe la dosis del medicamento.");
            }
            receta.add(new Tratamiento(medicamento.trim(), dosis.trim(), limpio(indicaciones), revision));
        } else if (!vacio(dosis) || !vacio(indicaciones) || revision != null) {
            throw new IllegalArgumentException("La dosis, las indicaciones y la fecha de revisión van con un medicamento.");
        }
        return repositorio.registrarConsulta(cita.getId(), idVeterinario, limpio(sintomas), diagnostico.trim(), receta);
    }

    private static void exigirPermiso(Rol rol) {
        if (!puedeDiagnosticar(rol)) {
            throw new IllegalStateException("Solo el veterinario o el administrador pueden registrar consultas.");
        }
    }

    private static String limpio(String s) {
        return vacio(s) ? null : s.trim();
    }

    private static boolean vacio(String s) {
        return s == null || s.isBlank();
    }
}
