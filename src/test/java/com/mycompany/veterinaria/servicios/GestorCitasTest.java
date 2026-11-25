package com.mycompany.veterinaria.servicios;

import static org.junit.jupiter.api.Assertions.*;

import com.mycompany.veterinaria.datos.RepositorioCitas;
import java.nio.file.Path;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import com.mycompany.veterinaria.modelo.Cita;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

/**
 * Pruebas de las reglas de negocio de GestorCitas.
 * Se usa un RepositorioCitas sobre un archivo temporal para no depender de MySQL.
 */
class GestorCitasTest {

    @TempDir
    Path carpeta;

    private GestorCitas gestor;
    private final LocalDate futuro = LocalDate.now().plusDays(7);

    @BeforeEach
    void preparar() {
        gestor = new GestorCitas(new RepositorioCitas(carpeta.resolve("citas.csv")));
    }

    private Cita registrar(String responsable, LocalDate fecha, LocalTime hora) {
        return gestor.registrar("Firulais", "Pedro Hernandez", "", "Consulta general", responsable, fecha, hora);
    }

    @Test
    @DisplayName("P01 Registra una cita válida en estado Programada")
    void registraCitaValida() {
        Cita c = registrar("Veterinario", futuro, LocalTime.of(10, 0));
        assertEquals(1, c.getId());
        assertEquals(Cita.PROGRAMADA, c.getEstado());
        assertEquals(1, gestor.citasDelDia(futuro).size());
    }

    @Test
    @DisplayName("P02 La agenda tiene 18 horarios de 09:00 a 17:30")
    void horariosDelDia() {
        List<LocalTime> horarios = gestor.horariosDelDia();
        assertEquals(18, horarios.size());
        assertEquals(LocalTime.of(9, 0), horarios.get(0));
        assertEquals(LocalTime.of(17, 30), horarios.get(17));
    }

    @Test
    @DisplayName("P03 Rechaza una fecha que ya pasó")
    void rechazaFechaPasada() {
        assertThrows(IllegalArgumentException.class,
                () -> registrar("Veterinario", LocalDate.now().minusDays(1), LocalTime.of(10, 0)));
    }

    @ParameterizedTest(name = "P04 Rechaza la hora {0}")
    @ValueSource(strings = {"08:30", "18:00", "10:15", "20:00"})
    void rechazaHoraFueraDeHorario(String hora) {
        assertThrows(IllegalArgumentException.class,
                () -> registrar("Veterinario", futuro, LocalTime.parse(hora)));
    }

    @Test
    @DisplayName("P05 Frontera: acepta 09:00 y 17:30")
    void aceptaHorariosFrontera() {
        assertDoesNotThrow(() -> registrar("Veterinario", futuro, LocalTime.of(9, 0)));
        assertDoesNotThrow(() -> registrar("Veterinario", futuro, LocalTime.of(17, 30)));
    }

    @Test
    @DisplayName("P06 Rechaza dos citas del mismo encargado a la misma hora")
    void rechazaHorarioOcupado() {
        registrar("Veterinario", futuro, LocalTime.of(10, 0));
        assertThrows(IllegalArgumentException.class,
                () -> registrar("Veterinario", futuro, LocalTime.of(10, 0)));
    }

    @Test
    @DisplayName("P07 Permite la misma hora con otro encargado")
    void permiteOtroEncargado() {
        registrar("Veterinario", futuro, LocalTime.of(10, 0));
        assertDoesNotThrow(() -> registrar("Bañador", futuro, LocalTime.of(10, 0)));
    }

    @Test
    @DisplayName("P08 Rechaza datos obligatorios vacíos")
    void rechazaDatosVacios() {
        LocalTime h = LocalTime.of(11, 0);
        assertThrows(IllegalArgumentException.class,
                () -> gestor.registrar("", "Pedro", "", "Consulta", "Veterinario", futuro, h));
        assertThrows(IllegalArgumentException.class,
                () -> gestor.registrar("Firulais", "  ", "", "Consulta", "Veterinario", futuro, h));
        assertThrows(IllegalArgumentException.class,
                () -> gestor.registrar("Firulais", "Pedro", "", null, "Veterinario", futuro, h));
        assertThrows(IllegalArgumentException.class,
                () -> gestor.registrar("Firulais", "Pedro", "", "Consulta", "", futuro, h));
        assertThrows(IllegalArgumentException.class,
                () -> gestor.registrar("Firulais", "Pedro", "", "Consulta", "Veterinario", null, h));
    }

    @Test
    @DisplayName("P09 Reprogramar cambia fecha y hora")
    void reprograma() {
        Cita c = registrar("Veterinario", futuro, LocalTime.of(10, 0));
        gestor.reprogramar(c.getId(), futuro.plusDays(1), LocalTime.of(12, 30));
        assertEquals(futuro.plusDays(1), c.getFecha());
        assertEquals(LocalTime.of(12, 30), c.getHora());
    }

    @Test
    @DisplayName("P10 No se puede reprogramar encima de otra cita del mismo encargado")
    void reprogramarAHorarioOcupado() {
        registrar("Veterinario", futuro, LocalTime.of(10, 0));
        Cita segunda = registrar("Veterinario", futuro, LocalTime.of(11, 0));
        assertThrows(IllegalArgumentException.class,
                () -> gestor.reprogramar(segunda.getId(), futuro, LocalTime.of(10, 0)));
        assertEquals(LocalTime.of(11, 0), segunda.getHora());
    }

    @Test
    @DisplayName("P11 Marcar llegada cambia el estado y ya no se puede reprogramar")
    void marcaLlegada() {
        Cita c = registrar("Veterinario", futuro, LocalTime.of(10, 0));
        gestor.marcarLlegada(c.getId());
        assertEquals(Cita.EN_CLINICA, c.getEstado());
        assertThrows(IllegalStateException.class,
                () -> gestor.reprogramar(c.getId(), futuro, LocalTime.of(12, 0)));
        assertThrows(IllegalStateException.class, () -> gestor.marcarLlegada(c.getId()));
    }

    @Test
    @DisplayName("P12 Cancelar libera el horario y no se puede cancelar dos veces")
    void cancela() {
        Cita c = registrar("Veterinario", futuro, LocalTime.of(10, 0));
        gestor.cancelar(c.getId());
        assertEquals(Cita.CANCELADA, c.getEstado());
        assertNull(gestor.citaEnHorario(futuro, LocalTime.of(10, 0), "Veterinario"));
        assertDoesNotThrow(() -> registrar("Veterinario", futuro, LocalTime.of(10, 0)));
        assertThrows(IllegalStateException.class, () -> gestor.cancelar(c.getId()));
    }

    @Test
    @DisplayName("P13 Buscar una cita que no existe lanza excepción")
    void buscarInexistente() {
        assertThrows(IllegalArgumentException.class, () -> gestor.buscar(99));
    }

    @Test
    @DisplayName("P14 Las citas guardadas se recuperan al volver a abrir el gestor")
    void persisteEntreSesiones() {
        registrar("Veterinario", futuro, LocalTime.of(10, 0));
        registrar("Estilista", futuro, LocalTime.of(10, 0));
        GestorCitas reabierto = new GestorCitas(new RepositorioCitas(carpeta.resolve("citas.csv")));
        assertEquals(2, reabierto.citasDelDia(futuro).size());
        Cita nueva = reabierto.registrar("Michi", "Maria Lopez", "", "Vacuna", "Veterinario", futuro, LocalTime.of(11, 0));
        assertEquals(3, nueva.getId(), "El id continúa después del mayor guardado");
    }
}
