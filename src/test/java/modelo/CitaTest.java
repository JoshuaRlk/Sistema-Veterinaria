package modelo;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.time.LocalDate;
import java.time.LocalTime;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class CitaTest {

    @Test
    @DisplayName("P17 Solo una cita cancelada deja de ocupar su horario")
    void ocupaHorario() {
        Cita c = new Cita(1, LocalDate.now(), LocalTime.of(9, 0), "Firulais", "Pedro", "", "Vacuna",
                "Veterinario", Cita.PROGRAMADA);
        assertTrue(c.ocupaHorario());
        c.setEstado(Cita.EN_CLINICA);
        assertTrue(c.ocupaHorario());
        c.setEstado(Cita.CANCELADA);
        assertFalse(c.ocupaHorario());
    }
}
