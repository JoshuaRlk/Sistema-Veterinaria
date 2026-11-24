package com.mycompany.veterinaria.datos;

import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Time;
import java.util.ArrayList;
import java.util.List;
import com.mycompany.veterinaria.modelo.Cita;

/**
 * Sustituye al archivo citas.csv: guarda y lee las citas en la tabla cita de veterinaria_lima.
 * Extiende RepositorioCitas para que GestorCitas y la ventana no cambien.
 *
 * Correspondencia (ver sección 4.5 del TAR05):
 *  - mascota/dueño/expediente -> cita.id_mascota (se busca en mascota y dueno)
 *  - responsable             -> cita.id_usuario (se busca por usuario.nombre)
 *  - servicio                -> cita.tipo_servicio
 *  - estado                  -> Programada = PROGRAMADA, Mascota en clínica = EN_ESPERA, Cancelada = CANCELADA,
 *                               Atendida = ATENDIDA (la marca Clínica), Cobrada = COBRADA (la marca Cobros)
 */
public class CitaDAO extends RepositorioCitas {

    /** Estados que ponen los módulos de Clínica y Cobros; el módulo de citas solo los muestra. */
    public static final String ATENDIDA = "Atendida";
    public static final String COBRADA = "Cobrada";

    private static final String SQL_LISTAR =
            "SELECT id_cita, fecha, hora, mascota, dueno, num_expediente, tipo_servicio, "
            + "personal_asignado, estado_cita FROM vw_agenda_citas ORDER BY fecha, hora";
    private static final String SQL_MASCOTA_POR_EXPEDIENTE =
            "SELECT id_mascota FROM mascota WHERE num_expediente = ? AND estado = 'ACTIVA'";
    private static final String SQL_MASCOTA_POR_NOMBRE =
            "SELECT m.id_mascota FROM mascota m INNER JOIN dueno d ON m.id_dueno = d.id_dueno "
            + "WHERE m.nombre = ? AND d.nombre = ? AND m.estado = 'ACTIVA'";
    private static final String SQL_USUARIO =
            "SELECT id_usuario FROM usuario WHERE nombre = ? AND activo = 1";
    private static final String SQL_GUARDAR =
            "INSERT INTO cita (id_cita, id_mascota, id_usuario, tipo_servicio, fecha, hora, estado) "
            + "VALUES (?, ?, ?, ?, ?, ?, ?) "
            + "ON DUPLICATE KEY UPDATE fecha = VALUES(fecha), hora = VALUES(hora), "
            // Una cita ATENDIDA o COBRADA ya no regresa a un estado anterior aunque la agenda la reenvíe.
            + "estado = IF(estado IN ('ATENDIDA','COBRADA'), estado, VALUES(estado))";

    @Override
    public List<Cita> cargar() {
        List<Cita> citas = new ArrayList<>();
        try (Connection cn = ConexionBD.obtener();
                PreparedStatement ps = cn.prepareStatement(SQL_LISTAR);
                ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                citas.add(new Cita(
                        rs.getInt("id_cita"),
                        rs.getDate("fecha").toLocalDate(),
                        rs.getTime("hora").toLocalTime(),
                        rs.getString("mascota"),
                        rs.getString("dueno"),
                        rs.getString("num_expediente"),
                        rs.getString("tipo_servicio"),
                        rs.getString("personal_asignado"),
                        estadoDesdeBD(rs.getString("estado_cita"))));
            }
        } catch (SQLException ex) {
            throw new IllegalStateException("No se pudo conectar con la base de datos: " + ex.getMessage(), ex);
        }
        return citas;
    }

    /**
     * GestorCitas manda la lista completa después de cada cambio. Se inserta lo nuevo y se actualiza
     * fecha, hora y estado de lo que ya existe, todo en una sola transacción.
     */
    @Override
    public void guardar(List<Cita> citas) {
        try (Connection cn = ConexionBD.obtener()) {
            cn.setAutoCommit(false);
            try (PreparedStatement ps = cn.prepareStatement(SQL_GUARDAR)) {
                for (Cita c : citas) {
                    ps.setInt(1, c.getId());
                    ps.setInt(2, idMascota(cn, c));
                    ps.setInt(3, buscarId(cn, SQL_USUARIO,
                            "No existe el encargado \"" + c.getResponsable() + "\" en la tabla usuario.",
                            c.getResponsable()));
                    ps.setString(4, c.getServicio());
                    ps.setDate(5, Date.valueOf(c.getFecha()));
                    ps.setTime(6, Time.valueOf(c.getHora()));
                    ps.setString(7, estadoHaciaBD(c.getEstado()));
                    ps.addBatch();
                }
                ps.executeBatch();
                cn.commit();
            } catch (SQLException | RuntimeException ex) {
                cn.rollback();
                throw ex;
            }
        } catch (SQLException ex) {
            throw new IllegalStateException("No se pudieron guardar las citas en la base de datos: " + ex.getMessage(), ex);
        }
    }

    private static int idMascota(Connection cn, Cita c) throws SQLException {
        if (c.getExpediente() != null && !c.getExpediente().isBlank()) {
            return buscarId(cn, SQL_MASCOTA_POR_EXPEDIENTE,
                    "No existe una mascota activa con el expediente " + c.getExpediente() + ".",
                    c.getExpediente());
        }
        return buscarId(cn, SQL_MASCOTA_POR_NOMBRE,
                "No existe la mascota \"" + c.getMascota() + "\" del dueño \"" + c.getDueno()
                + "\". Primero hay que registrarla.",
                c.getMascota(), c.getDueno());
    }

    private static int buscarId(Connection cn, String sql, String mensaje, String... parametros) throws SQLException {
        try (PreparedStatement ps = cn.prepareStatement(sql)) {
            for (int i = 0; i < parametros.length; i++) {
                ps.setString(i + 1, parametros[i]);
            }
            try (ResultSet rs = ps.executeQuery()) {
                if (!rs.next()) {
                    throw new IllegalArgumentException(mensaje);
                }
                return rs.getInt(1);
            }
        }
    }

    static String estadoHaciaBD(String estado) {
        if (Cita.EN_CLINICA.equals(estado)) {
            return "EN_ESPERA";
        }
        if (ATENDIDA.equals(estado)) {
            return "ATENDIDA";
        }
        if (COBRADA.equals(estado)) {
            return "COBRADA";
        }
        if (Cita.CANCELADA.equals(estado)) {
            return "CANCELADA";
        }
        return "PROGRAMADA";
    }

    static String estadoDesdeBD(String estado) {
        if ("EN_ESPERA".equals(estado) || "EN_ATENCION".equals(estado)) {
            return Cita.EN_CLINICA;
        }
        if ("ATENDIDA".equals(estado)) {
            return ATENDIDA;
        }
        if ("COBRADA".equals(estado)) {
            return COBRADA;
        }
        if ("CANCELADA".equals(estado)) {
            return Cita.CANCELADA;
        }
        return Cita.PROGRAMADA;
    }
}
