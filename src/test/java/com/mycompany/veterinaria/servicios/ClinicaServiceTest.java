package com.mycompany.veterinaria.servicios;

import static org.junit.jupiter.api.Assertions.*;

import com.mycompany.veterinaria.datos.ClinicaRepositorio;
import java.time.Clock;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.List;
import com.mycompany.veterinaria.modelo.Antecedentes;
import com.mycompany.veterinaria.modelo.CitaClinica;
import com.mycompany.veterinaria.modelo.RegistroClinico;
import com.mycompany.veterinaria.modelo.Tratamiento;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import com.mycompany.veterinaria.seguridad.Rol;

class ClinicaServiceTest {

    static CitaClinica consulta() {
        return new CitaClinica(2, LocalDate.of(2026, 11, 20), java.time.LocalTime.of(10, 0), "Consulta general",
                "Firulais", "PROGRAMADA");
    }

    static class Falso implements ClinicaRepositorio {
        int consultas;
        String ultimoDiagnostico;
        List<Tratamiento> ultimaReceta;

        @Override
        public Antecedentes antecedentes(int id) {
            return new Antecedentes("Rabia", null, null);
        }

        @Override
        public void actualizarAntecedentes(int id, String v, String a) {
        }

        @Override
        public List<CitaClinica> citasPorAtender(int id) {
            return List.of(consulta());
        }

        @Override
        public List<RegistroClinico> historial(int id) {
            return List.of();
        }

        @Override
        public int registrarConsulta(int idCita, int idVet, String s, String d, List<Tratamiento> t) {
            consultas++;
            ultimoDiagnostico = d;
            ultimaReceta = t;
            return 9;
        }
    }

    private Falso repo;
    private ClinicaService servicio;
    private final CitaClinica cita = consulta();

    @BeforeEach
    void preparar() {
        repo = new Falso();
        ZoneId z = ZoneId.of("America/Mexico_City");
        servicio = new ClinicaService(repo, Clock.fixed(LocalDateTime.of(2026, 11, 23, 12, 0).atZone(z).toInstant(), z));
    }

    @Test
    @DisplayName("P47 Registra diagnóstico con receta y fecha de revisión (Figura 9)")
    void registra() {
        int id = servicio.registrarConsulta(Rol.VETERINARIO, 2, cita, "Picazón y sacude la cabeza", "Otitis externa",
                "Gotas óticas", "3 gotas cada 12 h", "por 7 días", "02/12/2026");
        assertEquals(9, id);
        assertEquals("Otitis externa", repo.ultimoDiagnostico);
        assertEquals(1, repo.ultimaReceta.size());
        assertEquals(LocalDate.of(2026, 12, 2), repo.ultimaReceta.get(0).getFechaRevision());
    }

    @Test
    @DisplayName("P48 Diagnóstico sin receta también es válido")
    void sinReceta() {
        servicio.registrarConsulta(Rol.ADMINISTRADOR, 1, cita, "", "Sano", "", "", "", "");
        assertTrue(repo.ultimaReceta.isEmpty());
    }

    @Test
    @DisplayName("P49 Valida cita, diagnóstico, dosis y fecha de revisión")
    void valida() {
        assertThrows(IllegalArgumentException.class,
                () -> servicio.registrarConsulta(Rol.VETERINARIO, 2, null, "", "Otitis", "", "", "", ""));
        assertThrows(IllegalArgumentException.class,
                () -> servicio.registrarConsulta(Rol.VETERINARIO, 2, cita, "x", " ", "", "", "", ""));
        assertThrows(IllegalArgumentException.class,
                () -> servicio.registrarConsulta(Rol.VETERINARIO, 2, cita, "", "Otitis", "Gotas", "", "", ""));
        assertThrows(IllegalArgumentException.class,
                () -> servicio.registrarConsulta(Rol.VETERINARIO, 2, cita, "", "Otitis", "", "3 gotas", "", ""));
        assertThrows(IllegalArgumentException.class,
                () -> servicio.registrarConsulta(Rol.VETERINARIO, 2, cita, "", "Otitis", "Gotas", "3", "", "01/11/2026"));
        assertThrows(IllegalArgumentException.class,
                () -> servicio.registrarConsulta(Rol.VETERINARIO, 2, cita, "", "Otitis", "Gotas", "3", "", "2026-12-02"));
        assertEquals(0, repo.consultas);
    }

    @Test
    @DisplayName("P50 Solo veterinario y administrador registran consultas")
    void permisos() {
        assertThrows(IllegalStateException.class,
                () -> servicio.registrarConsulta(Rol.TECNICO, 2, cita, "", "Otitis", "", "", "", ""));
        assertThrows(IllegalStateException.class,
                () -> servicio.actualizarAntecedentes(Rol.RECEPCIONISTA, 1, "Rabia", "Ninguna"));
        assertTrue(ClinicaService.puedeDiagnosticar(Rol.VETERINARIO));
        assertFalse(ClinicaService.puedeDiagnosticar(Rol.ALMACEN));
    }
}
