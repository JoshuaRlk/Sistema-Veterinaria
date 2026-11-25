package com.mycompany.veterinaria.servicios;

import com.mycompany.veterinaria.datos.MascotaRepositorio;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import com.mycompany.veterinaria.modelo.Dueno;
import com.mycompany.veterinaria.modelo.Mascota;
import com.mycompany.veterinaria.seguridad.Rol;

/**
 * Reglas de negocio de dueños y mascotas (RF-M04, RF-M05, RF-M06, RF-M08 del PRY04).
 */
public class MascotaService {

    /** Error de validación que trae los campos vacíos para remarcarlos en la pantalla (RF-M05). */
    public static class CamposInvalidos extends IllegalArgumentException {
        private final List<String> campos;

        public CamposInvalidos(String mensaje, List<String> campos) {
            super(mensaje);
            this.campos = campos;
        }

        public List<String> getCampos() {
            return campos;
        }
    }

    private final MascotaRepositorio repositorio;

    public MascotaService(MascotaRepositorio repositorio) {
        this.repositorio = repositorio;
    }

    public List<Mascota> buscar(String dueno, String telefono, String mascota) {
        if (vacio(dueno) && vacio(telefono) && vacio(mascota)) {
            throw new IllegalArgumentException("Escribe al menos un dato para buscar (dueño, teléfono o mascota).");
        }
        return repositorio.buscar(dueno, telefono, mascota);
    }

    /** Valida y registra. Regresa la mascota con su ID y expediente asignados (RF-M04). */
    public Mascota registrar(String nombreDueno, String telefono, String correo, String nombre, String especie,
            String raza, String edad, String peso) {
        Mascota m = construir(0, 0, nombreDueno, telefono, correo, nombre, especie, raza, edad, peso);
        if (repositorio.existeMascota(m.getDueno().getNombre(), m.getDueno().getTelefono(), m.getNombre())) {
            throw new IllegalArgumentException("Posible duplicado: " + m.getDueno().getNombre()
                    + " ya tiene registrada una mascota llamada " + m.getNombre() + ". Búscala antes de registrar.");
        }
        return repositorio.registrar(m);
    }

    /** Edita una mascota existente; solo administrador y veterinario (RF-M08). */
    public Mascota editar(Rol rol, int idMascota, int idDueno, String nombreDueno, String telefono, String correo,
            String nombre, String especie, String raza, String edad, String peso) {
        exigirPermiso(rol, "editar");
        Mascota m = construir(idMascota, idDueno, nombreDueno, telefono, correo, nombre, especie, raza, edad, peso);
        repositorio.actualizar(m);
        return m;
    }

    /** Baja lógica; solo administrador y veterinario (RF-M08). */
    public void darDeBaja(Rol rol, int idMascota) {
        exigirPermiso(rol, "dar de baja");
        repositorio.darDeBaja(idMascota);
    }

    private static void exigirPermiso(Rol rol, String accion) {
        if (rol == null || !rol.puedeEditarMascotas()) {
            throw new IllegalStateException("Solo el administrador o el veterinario pueden " + accion + " mascotas.");
        }
    }

    private static Mascota construir(int idMascota, int idDueno, String nombreDueno, String telefono, String correo,
            String nombre, String especie, String raza, String edad, String peso) {
        List<String> vacios = new ArrayList<>();
        revisar(vacios, "dueño", nombreDueno);
        revisar(vacios, "teléfono", telefono);
        revisar(vacios, "nombre", nombre);
        revisar(vacios, "especie", especie);
        revisar(vacios, "raza", raza);
        revisar(vacios, "edad", edad);
        revisar(vacios, "peso", peso);
        if (!vacios.isEmpty()) {
            throw new CamposInvalidos("Faltan datos obligatorios: " + String.join(", ", vacios) + ".", vacios);
        }
        String tel = telefono.trim().replaceAll("[\\s-]", "");
        if (!tel.matches("\\d{7,15}")) {
            throw new CamposInvalidos("El teléfono debe tener solo números (7 a 15 dígitos).", List.of("teléfono"));
        }
        BigDecimal e = numero(edad, "edad");
        BigDecimal p = numero(peso, "peso");
        if (e.signum() < 0 || e.compareTo(new BigDecimal("50")) > 0) {
            throw new CamposInvalidos("La edad debe estar entre 0 y 50 años.", List.of("edad"));
        }
        if (p.signum() <= 0 || p.compareTo(new BigDecimal("500")) > 0) {
            throw new CamposInvalidos("El peso debe ser mayor a 0 y menor a 500 kg.", List.of("peso"));
        }
        String mail = vacio(correo) ? null : correo.trim();
        if (mail != null && !mail.matches("[^@\\s]+@[^@\\s]+\\.[^@\\s]+")) {
            throw new CamposInvalidos("El correo no es válido.", List.of("correo"));
        }
        Dueno d = new Dueno(idDueno, nombreDueno.trim(), tel, mail);
        return new Mascota(idMascota, null, d, nombre.trim(), especie.trim(), raza.trim(), e, p, Mascota.ACTIVA);
    }

    private static BigDecimal numero(String texto, String campo) {
        try {
            return new BigDecimal(texto.trim().replace(",", ".").replaceAll("(?i)\\s*(años|año|kg)$", ""));
        } catch (NumberFormatException ex) {
            throw new CamposInvalidos("El campo " + campo + " debe ser un número.", List.of(campo));
        }
    }

    private static void revisar(List<String> vacios, String campo, String valor) {
        if (vacio(valor)) {
            vacios.add(campo);
        }
    }

    private static boolean vacio(String s) {
        return s == null || s.isBlank();
    }
}
