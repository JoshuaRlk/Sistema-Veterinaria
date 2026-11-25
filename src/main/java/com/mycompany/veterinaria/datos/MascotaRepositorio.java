package com.mycompany.veterinaria.datos;

import java.util.List;
import com.mycompany.veterinaria.modelo.Dueno;
import com.mycompany.veterinaria.modelo.Mascota;

/** Contrato de acceso a datos de dueños y mascotas (MySQL en la aplicación, memoria en las pruebas). */
public interface MascotaRepositorio {

    /** Busca mascotas activas por coincidencia en dueño, teléfono, mascota o expediente. Los vacíos se ignoran. */
    List<Mascota> buscar(String dueno, String telefono, String mascota);

    /** true si el dueño (nombre + teléfono) ya tiene una mascota con ese nombre (RF-M06). */
    boolean existeMascota(String nombreDueno, String telefono, String nombreMascota);

    /** Guarda dueño (si no existía) y mascota; asigna id y expediente. */
    Mascota registrar(Mascota mascota);

    /** Actualiza los datos de la mascota y de su dueño. */
    void actualizar(Mascota mascota);

    /** Baja lógica: la mascota queda en estado BAJA y conserva su historial. */
    void darDeBaja(int idMascota);
}
