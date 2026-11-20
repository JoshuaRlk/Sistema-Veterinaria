package servicios;

import datos.RepositorioCitas;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import modelo.Cita;

/**
 * Reglas de negocio del módulo de citas: horarios, disponibilidad,
 * registro, reprogramación, llegada y cancelación.
 */
public class GestorCitas {

    public static final LocalTime HORA_APERTURA = LocalTime.of(9, 0);
    public static final LocalTime HORA_CIERRE = LocalTime.of(18, 0);
    public static final int MINUTOS_POR_CITA = 30;

    private final RepositorioCitas repositorio;
    private final List<Cita> citas;

    public GestorCitas(RepositorioCitas repositorio) {
        this.repositorio = repositorio;
        this.citas = repositorio.cargar();
    }

    /** Horarios en los que se pueden agendar citas (cada 30 minutos). */
    public List<LocalTime> horariosDelDia() {
        List<LocalTime> horarios = new ArrayList<>();
        for (LocalTime h = HORA_APERTURA; h.isBefore(HORA_CIERRE); h = h.plusMinutes(MINUTOS_POR_CITA)) {
            horarios.add(h);
        }
        return horarios;
    }

    public List<Cita> citasDelDia(LocalDate fecha) {
        List<Cita> resultado = new ArrayList<>();
        for (Cita c : citas) {
            if (c.getFecha().equals(fecha)) {
                resultado.add(c);
            }
        }
        resultado.sort(Comparator.comparing(Cita::getHora).thenComparing(Cita::getResponsable));
        return resultado;
    }

    /** Devuelve la cita activa del responsable en ese horario, o null si está libre. */
    public Cita citaEnHorario(LocalDate fecha, LocalTime hora, String responsable) {
        for (Cita c : citas) {
            if (c.ocupaHorario() && c.getFecha().equals(fecha) && c.getHora().equals(hora)
                    && c.getResponsable().equals(responsable)) {
                return c;
            }
        }
        return null;
    }

    public Cita registrar(String mascota, String dueno, String expediente, String servicio,
            String responsable, LocalDate fecha, LocalTime hora) {
        if (vacio(mascota) || vacio(dueno)) {
            throw new IllegalArgumentException("Escribe al menos el nombre de la mascota y el del dueño.");
        }
        if (vacio(servicio)) {
            throw new IllegalArgumentException("Escribe el tipo de servicio solicitado.");
        }
        if (vacio(responsable)) {
            throw new IllegalArgumentException("Selecciona un encargado.");
        }
        validarHorario(fecha, hora, responsable, -1);

        Cita nueva = new Cita(siguienteId(), fecha, hora, mascota.trim(), dueno.trim(),
                expediente == null ? "" : expediente.trim(), servicio.trim(), responsable, Cita.PROGRAMADA);
        citas.add(nueva);
        repositorio.guardar(citas);
        return nueva;
    }

    public void reprogramar(int id, LocalDate fecha, LocalTime hora) {
        Cita cita = buscar(id);
        if (!Cita.PROGRAMADA.equals(cita.getEstado())) {
            throw new IllegalStateException("Solo se pueden reprogramar citas en estado \"" + Cita.PROGRAMADA + "\".");
        }
        validarHorario(fecha, hora, cita.getResponsable(), id);
        cita.setFecha(fecha);
        cita.setHora(hora);
        repositorio.guardar(citas);
    }

    public void marcarLlegada(int id) {
        Cita cita = buscar(id);
        if (!Cita.PROGRAMADA.equals(cita.getEstado())) {
            throw new IllegalStateException("La cita está en estado \"" + cita.getEstado() + "\".");
        }
        cita.setEstado(Cita.EN_CLINICA);
        repositorio.guardar(citas);
    }

    public void cancelar(int id) {
        Cita cita = buscar(id);
        if (Cita.CANCELADA.equals(cita.getEstado())) {
            throw new IllegalStateException("La cita ya estaba cancelada.");
        }
        cita.setEstado(Cita.CANCELADA);
        repositorio.guardar(citas);
    }

    public Cita buscar(int id) {
        for (Cita c : citas) {
            if (c.getId() == id) {
                return c;
            }
        }
        throw new IllegalArgumentException("No existe la cita con id " + id);
    }

    private void validarHorario(LocalDate fecha, LocalTime hora, String responsable, int idIgnorar) {
        if (fecha == null || hora == null) {
            throw new IllegalArgumentException("Indica la fecha y la hora de la cita.");
        }
        if (!horariosDelDia().contains(hora)) {
            throw new IllegalArgumentException("La hora debe estar entre " + HORA_APERTURA + " y "
                    + HORA_CIERRE.minusMinutes(MINUTOS_POR_CITA) + ", en intervalos de "
                    + MINUTOS_POR_CITA + " minutos (ej. 09:00, 09:30).");
        }
        if (LocalDateTime.of(fecha, hora).isBefore(LocalDateTime.now())) {
            throw new IllegalArgumentException("No se pueden agendar citas en una fecha u hora que ya pasó.");
        }
        Cita ocupada = citaEnHorario(fecha, hora, responsable);
        if (ocupada != null && ocupada.getId() != idIgnorar) {
            throw new IllegalArgumentException("El encargado (" + responsable + ") ya tiene una cita a esa hora con "
                    + ocupada.getMascota() + ".");
        }
    }

    private int siguienteId() {
        int max = 0;
        for (Cita c : citas) {
            max = Math.max(max, c.getId());
        }
        return max + 1;
    }

    private static boolean vacio(String texto) {
        return texto == null || texto.isBlank();
    }
}
