package com.mycompany.veterinaria.datos;

import static org.junit.jupiter.api.Assertions.*;

import java.nio.file.Path;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import com.mycompany.veterinaria.modelo.Cita;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

class RepositorioCitasTest {

    @TempDir
    Path carpeta;

    @Test
    @DisplayName("P18 Guarda y vuelve a leer las citas sin perder datos")
    void guardaYLee() {
        RepositorioCitas repo = new RepositorioCitas(carpeta.resolve("citas.csv"));
        Cita original = new Cita(7, LocalDate.of(2026, 12, 1), LocalTime.of(9, 30), "Firulais",
                "Pedro; Hernandez", "EXP-0001", "Baño", "Bañador", Cita.PROGRAMADA);
        repo.guardar(List.of(original));

        List<Cita> leidas = repo.cargar();
        assertEquals(1, leidas.size());
        Cita c = leidas.get(0);
        assertEquals(7, c.getId());
        assertEquals(LocalTime.of(9, 30), c.getHora());
        assertEquals("Pedro, Hernandez", c.getDueno(), "El ; se cambia para no romper el archivo");
        assertEquals("EXP-0001", c.getExpediente());
    }

    @Test
    @DisplayName("P19 Si el archivo no existe regresa una lista vacía")
    void archivoInexistente() {
        assertTrue(new RepositorioCitas(carpeta.resolve("no-existe.csv")).cargar().isEmpty());
    }
}
