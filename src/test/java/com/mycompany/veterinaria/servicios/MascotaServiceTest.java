package com.mycompany.veterinaria.servicios;

import static org.junit.jupiter.api.Assertions.*;

import com.mycompany.veterinaria.datos.MascotaRepositorio;
import java.util.ArrayList;
import java.util.List;
import com.mycompany.veterinaria.modelo.Mascota;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import com.mycompany.veterinaria.seguridad.Rol;

/** Reglas de dueños y mascotas, con un repositorio en memoria (sin MySQL). */
class MascotaServiceTest {

    /** Repositorio falso en memoria. */
    static class Memoria implements MascotaRepositorio {
        final List<Mascota> mascotas = new ArrayList<>();
        int siguiente = 128;

        @Override
        public List<Mascota> buscar(String dueno, String telefono, String mascota) {
            List<Mascota> r = new ArrayList<>();
            for (Mascota m : mascotas) {
                if (m.isActiva() && (dueno == null || dueno.isBlank() || m.getDueno().getNombre().contains(dueno.trim()))
                        && (mascota == null || mascota.isBlank() || m.getNombre().contains(mascota.trim()))) {
                    r.add(m);
                }
            }
            return r;
        }

        @Override
        public boolean existeMascota(String d, String t, String n) {
            return mascotas.stream().anyMatch(m -> m.getDueno().getNombre().equals(d)
                    && m.getDueno().getTelefono().equals(t) && m.getNombre().equals(n));
        }

        @Override
        public Mascota registrar(Mascota m) {
            m.setId(siguiente++);
            m.setExpediente(Mascota.formatoExpediente(m.getId()));
            mascotas.add(m);
            return m;
        }

        @Override
        public void actualizar(Mascota m) {
            mascotas.removeIf(x -> x.getId() == m.getId());
            mascotas.add(m);
        }

        @Override
        public void darDeBaja(int id) {
            mascotas.stream().filter(m -> m.getId() == id).forEach(m -> m.setEstado(Mascota.BAJA));
        }
    }

    private Memoria repo;
    private MascotaService servicio;

    @BeforeEach
    void preparar() {
        repo = new Memoria();
        servicio = new MascotaService(repo);
    }

    private Mascota registrarLuna() {
        return servicio.registrar("María López Hernández", "228 555 0142", "maria@correo.com",
                "Luna", "Perro", "Labrador", "3", "24");
    }

    @Test
    @DisplayName("P26 Registra y asigna ID único y expediente (RF-M04)")
    void registra() {
        Mascota m = registrarLuna();
        assertEquals("M-000128", Mascota.formatoId(m.getId()));
        assertEquals("EXP-000128", m.getExpediente());
        assertEquals("2285550142", m.getDueno().getTelefono(), "El teléfono se guarda sin espacios");
        assertTrue(m.isActiva());
    }

    @Test
    @DisplayName("P27 Campos obligatorios vacíos se reportan todos juntos (RF-M05)")
    void camposObligatorios() {
        MascotaService.CamposInvalidos ex = assertThrows(MascotaService.CamposInvalidos.class,
                () -> servicio.registrar("", " ", null, "Luna", "", "Labrador", "", "24"));
        assertEquals(List.of("dueño", "teléfono", "especie", "edad"), ex.getCampos());
    }

    @Test
    @DisplayName("P28 Valida teléfono, correo, edad y peso")
    void validaFormatos() {
        assertThrows(MascotaService.CamposInvalidos.class, () -> servicio.registrar("Ana", "abc", null, "Rex", "Perro", "Pug", "2", "8"));
        assertThrows(MascotaService.CamposInvalidos.class, () -> servicio.registrar("Ana", "2281112233", "sin-arroba", "Rex", "Perro", "Pug", "2", "8"));
        assertThrows(MascotaService.CamposInvalidos.class, () -> servicio.registrar("Ana", "2281112233", null, "Rex", "Perro", "Pug", "-1", "8"));
        assertThrows(MascotaService.CamposInvalidos.class, () -> servicio.registrar("Ana", "2281112233", null, "Rex", "Perro", "Pug", "2", "0"));
        assertThrows(MascotaService.CamposInvalidos.class, () -> servicio.registrar("Ana", "2281112233", null, "Rex", "Perro", "Pug", "dos", "8"));
    }

    @Test
    @DisplayName("P29 Acepta '3 años' y '24,5 kg' como edad y peso")
    void aceptaUnidades() {
        Mascota m = servicio.registrar("Ana", "2281112233", null, "Rex", "Perro", "Pug", "3 años", "24,5 kg");
        assertEquals(0, m.getEdad().compareTo(new java.math.BigDecimal("3")));
        assertEquals(0, m.getPeso().compareTo(new java.math.BigDecimal("24.5")));
    }

    @Test
    @DisplayName("P30 Detecta duplicados del mismo dueño y permite varias mascotas por dueño (RF-M06)")
    void duplicados() {
        registrarLuna();
        assertThrows(IllegalArgumentException.class, this::registrarLuna);
        Mascota otra = servicio.registrar("María López Hernández", "2285550142", null, "Max", "Gato", "Siamés", "1", "4");
        assertEquals("EXP-000129", otra.getExpediente());
    }

    @Test
    @DisplayName("P31 Buscar sin ningún dato lanza error; con datos encuentra coincidencias")
    void buscar() {
        registrarLuna();
        assertThrows(IllegalArgumentException.class, () -> servicio.buscar("", " ", null));
        assertEquals(1, servicio.buscar("María", "", "").size());
        assertEquals(0, servicio.buscar("", "", "Firulais").size());
    }

    @Test
    @DisplayName("P32 Recepcionista no puede editar ni dar de baja; veterinario sí (RF-M08)")
    void permisos() {
        Mascota m = registrarLuna();
        assertThrows(IllegalStateException.class, () -> servicio.editar(Rol.RECEPCIONISTA, m.getId(), 0,
                "María López Hernández", "2285550142", null, "Luna", "Perro", "Labrador", "4", "25"));
        assertThrows(IllegalStateException.class, () -> servicio.darDeBaja(Rol.RECEPCIONISTA, m.getId()));
        Mascota editada = servicio.editar(Rol.VETERINARIO, m.getId(), 0,
                "María López Hernández", "2285550142", null, "Luna", "Perro", "Labrador", "4", "25");
        assertEquals(0, editada.getEdad().compareTo(new java.math.BigDecimal("4")));
    }

    @Test
    @DisplayName("P33 La baja es lógica: deja de aparecer en búsquedas pero no se borra")
    void bajaLogica() {
        Mascota m = registrarLuna();
        servicio.darDeBaja(Rol.ADMINISTRADOR, m.getId());
        assertEquals(0, servicio.buscar("María", "", "").size());
        assertEquals(1, repo.mascotas.size());
        assertEquals(Mascota.BAJA, repo.mascotas.get(0).getEstado());
    }
}
