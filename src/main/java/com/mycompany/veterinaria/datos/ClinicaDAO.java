package com.mycompany.veterinaria.datos;

import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import com.mycompany.veterinaria.modelo.Antecedentes;
import com.mycompany.veterinaria.modelo.CitaClinica;
import com.mycompany.veterinaria.modelo.RegistroClinico;
import com.mycompany.veterinaria.modelo.Tratamiento;

/** Expediente clínico en MySQL: tablas mascota, cita, diagnostico, tratamiento y vista vw_historial_clinico_citas. */
public class ClinicaDAO implements ClinicaRepositorio {

    @Override
    public Antecedentes antecedentes(int idMascota) {
        try (Connection cn = ConexionBD.obtener();
                PreparedStatement ps = cn.prepareStatement(
                        "SELECT vacunas, alergias, historial_medico FROM mascota WHERE id_mascota = ?")) {
            ps.setInt(1, idMascota);
            try (ResultSet rs = ps.executeQuery()) {
                if (!rs.next()) {
                    throw new IllegalArgumentException("No existe la mascota con id " + idMascota);
                }
                return new Antecedentes(rs.getString(1), rs.getString(2), rs.getString(3));
            }
        } catch (SQLException ex) {
            throw error("consultar el expediente", ex);
        }
    }

    @Override
    public void actualizarAntecedentes(int idMascota, String vacunas, String alergias) {
        try (Connection cn = ConexionBD.obtener();
                PreparedStatement ps = cn.prepareStatement(
                        "UPDATE mascota SET vacunas = ?, alergias = ? WHERE id_mascota = ?")) {
            ps.setString(1, vacunas);
            ps.setString(2, alergias);
            ps.setInt(3, idMascota);
            ps.executeUpdate();
        } catch (SQLException ex) {
            throw error("actualizar vacunas y alergias", ex);
        }
    }

    @Override
    public List<CitaClinica> citasPorAtender(int idMascota) {
        List<CitaClinica> lista = new ArrayList<>();
        String sql = "SELECT c.id_cita, c.fecha, c.hora, c.tipo_servicio, m.nombre, c.estado FROM cita c "
                + "INNER JOIN mascota m ON c.id_mascota = m.id_mascota "
                + "WHERE c.id_mascota = ? AND c.estado IN ('PROGRAMADA','EN_ESPERA','EN_ATENCION') "
                + "ORDER BY c.fecha, c.hora";
        try (Connection cn = ConexionBD.obtener();
                PreparedStatement ps = cn.prepareStatement(sql)) {
            ps.setInt(1, idMascota);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    lista.add(new CitaClinica(rs.getInt(1), rs.getDate(2).toLocalDate(), rs.getTime(3).toLocalTime(),
                            rs.getString(4), rs.getString(5), rs.getString(6)));
                }
            }
        } catch (SQLException ex) {
            throw error("consultar las citas", ex);
        }
        return lista;
    }

    @Override
    public List<RegistroClinico> historial(int idMascota) {
        List<RegistroClinico> lista = new ArrayList<>();
        String sql = "SELECT fecha_diagnostico, tipo_servicio, sintomas, diagnostico, veterinario, medicamento, "
                + "dosis, fecha_revision FROM vw_historial_clinico_citas "
                + "WHERE id_mascota = ? AND id_diagnostico IS NOT NULL ORDER BY fecha_diagnostico DESC";
        try (Connection cn = ConexionBD.obtener();
                PreparedStatement ps = cn.prepareStatement(sql)) {
            ps.setInt(1, idMascota);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    Date revision = rs.getDate(8);
                    lista.add(new RegistroClinico(rs.getTimestamp(1).toLocalDateTime(), rs.getString(2),
                            rs.getString(3), rs.getString(4), rs.getString(5), rs.getString(6), rs.getString(7),
                            revision == null ? null : revision.toLocalDate()));
                }
            }
        } catch (SQLException ex) {
            throw error("consultar el historial", ex);
        }
        return lista;
    }

    @Override
    public int registrarConsulta(int idCita, int idVeterinario, String sintomas, String diagnostico,
            List<Tratamiento> tratamientos) {
        try (Connection cn = ConexionBD.obtener()) {
            cn.setAutoCommit(false);
            try {
                int idDiagnostico;
                try (PreparedStatement ps = cn.prepareStatement(
                        "INSERT INTO diagnostico (id_cita, id_veterinario, sintomas, diagnostico, estado) "
                        + "VALUES (?, ?, ?, ?, 'CERRADO')", Statement.RETURN_GENERATED_KEYS)) {
                    ps.setInt(1, idCita);
                    ps.setInt(2, idVeterinario);
                    ps.setString(3, sintomas);
                    ps.setString(4, diagnostico);
                    ps.executeUpdate();
                    try (ResultSet llaves = ps.getGeneratedKeys()) {
                        llaves.next();
                        idDiagnostico = llaves.getInt(1);
                    }
                }
                try (PreparedStatement ps = cn.prepareStatement(
                        "INSERT INTO tratamiento (id_diagnostico, medicamento, dosis, indicaciones, fecha_revision) "
                        + "VALUES (?, ?, ?, ?, ?)")) {
                    for (Tratamiento t : tratamientos) {
                        ps.setInt(1, idDiagnostico);
                        ps.setString(2, t.getMedicamento());
                        ps.setString(3, t.getDosis());
                        ps.setString(4, t.getIndicaciones());
                        ps.setDate(5, t.getFechaRevision() == null ? null : Date.valueOf(t.getFechaRevision()));
                        ps.addBatch();
                    }
                    ps.executeBatch();
                }
                try (PreparedStatement ps = cn.prepareStatement(
                        "UPDATE cita SET estado = 'ATENDIDA' WHERE id_cita = ? "
                        + "AND estado IN ('PROGRAMADA','EN_ESPERA','EN_ATENCION')")) {
                    ps.setInt(1, idCita);
                    if (ps.executeUpdate() != 1) {
                        throw new IllegalStateException("La cita ya no está pendiente de atender (¿la cancelaron?).");
                    }
                }
                cn.commit();
                return idDiagnostico;
            } catch (SQLException | RuntimeException ex) {
                cn.rollback();
                throw ex;
            }
        } catch (SQLException ex) {
            throw error("guardar la consulta", ex);
        }
    }

    private static IllegalStateException error(String accion, SQLException ex) {
        return new IllegalStateException("No se pudo " + accion + " en la base de datos: " + ex.getMessage(), ex);
    }
}
