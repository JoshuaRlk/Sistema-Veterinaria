package com.mycompany.veterinaria.datos;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import com.mycompany.veterinaria.modelo.Dueno;
import com.mycompany.veterinaria.modelo.Mascota;

/** Dueños y mascotas en MySQL (tablas dueno y mascota de veterinaria_lima). */
public class MascotaDAO implements MascotaRepositorio {

    private static final String SELECT =
            "SELECT m.id_mascota, m.num_expediente, m.nombre, m.especie, m.raza, m.edad, m.peso, m.estado, "
            + "d.id_dueno, d.nombre AS dueno, d.telefono, d.correo "
            + "FROM mascota m INNER JOIN dueno d ON m.id_dueno = d.id_dueno ";

    @Override
    public List<Mascota> buscar(String dueno, String telefono, String mascota) {
        StringBuilder sql = new StringBuilder(SELECT).append("WHERE m.estado = 'ACTIVA'");
        List<String> params = new ArrayList<>();
        if (!vacio(dueno)) {
            sql.append(" AND d.nombre LIKE ?");
            params.add("%" + dueno.trim() + "%");
        }
        if (!vacio(telefono)) {
            sql.append(" AND d.telefono LIKE ?");
            params.add("%" + telefono.trim() + "%");
        }
        if (!vacio(mascota)) {
            sql.append(" AND (m.nombre LIKE ? OR m.num_expediente LIKE ?)");
            params.add("%" + mascota.trim() + "%");
            params.add("%" + mascota.trim() + "%");
        }
        sql.append(" ORDER BY d.nombre, m.nombre LIMIT 200");
        List<Mascota> lista = new ArrayList<>();
        try (Connection cn = ConexionBD.obtener();
                PreparedStatement ps = cn.prepareStatement(sql.toString())) {
            for (int i = 0; i < params.size(); i++) {
                ps.setString(i + 1, params.get(i));
            }
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    lista.add(leer(rs));
                }
            }
        } catch (SQLException ex) {
            throw error("buscar mascotas", ex);
        }
        return lista;
    }

    @Override
    public boolean existeMascota(String nombreDueno, String telefono, String nombreMascota) {
        String sql = "SELECT 1 FROM mascota m INNER JOIN dueno d ON m.id_dueno = d.id_dueno "
                + "WHERE d.nombre = ? AND d.telefono = ? AND m.nombre = ?";
        try (Connection cn = ConexionBD.obtener();
                PreparedStatement ps = cn.prepareStatement(sql)) {
            ps.setString(1, nombreDueno);
            ps.setString(2, telefono);
            ps.setString(3, nombreMascota);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next();
            }
        } catch (SQLException ex) {
            throw error("revisar duplicados", ex);
        }
    }

    @Override
    public Mascota registrar(Mascota m) {
        try (Connection cn = ConexionBD.obtener()) {
            cn.setAutoCommit(false);
            try {
                int idDueno = buscarOInsertarDueno(cn, m.getDueno());
                m.getDueno().setId(idDueno);

                // El expediente depende del id, así que primero se inserta con un valor temporal único.
                String temporal = "TMP" + UUID.randomUUID().toString().replace("-", "").substring(0, 16);
                int idMascota;
                try (PreparedStatement ps = cn.prepareStatement(
                        "INSERT INTO mascota (id_dueno, num_expediente, nombre, especie, raza, edad, peso) "
                        + "VALUES (?, ?, ?, ?, ?, ?, ?)", Statement.RETURN_GENERATED_KEYS)) {
                    ps.setInt(1, idDueno);
                    ps.setString(2, temporal);
                    ps.setString(3, m.getNombre());
                    ps.setString(4, m.getEspecie());
                    ps.setString(5, m.getRaza());
                    ps.setBigDecimal(6, m.getEdad());
                    ps.setBigDecimal(7, m.getPeso());
                    ps.executeUpdate();
                    try (ResultSet llaves = ps.getGeneratedKeys()) {
                        llaves.next();
                        idMascota = llaves.getInt(1);
                    }
                }
                String expediente = Mascota.formatoExpediente(idMascota);
                try (PreparedStatement ps = cn.prepareStatement(
                        "UPDATE mascota SET num_expediente = ? WHERE id_mascota = ?")) {
                    ps.setString(1, expediente);
                    ps.setInt(2, idMascota);
                    ps.executeUpdate();
                }
                cn.commit();
                m.setId(idMascota);
                m.setExpediente(expediente);
                m.setEstado(Mascota.ACTIVA);
                return m;
            } catch (SQLException | RuntimeException ex) {
                cn.rollback();
                throw ex;
            }
        } catch (SQLException ex) {
            throw error("registrar la mascota", ex);
        }
    }

    @Override
    public void actualizar(Mascota m) {
        try (Connection cn = ConexionBD.obtener()) {
            cn.setAutoCommit(false);
            try (PreparedStatement pd = cn.prepareStatement(
                    "UPDATE dueno SET nombre = ?, telefono = ?, correo = ? WHERE id_dueno = ?");
                    PreparedStatement pm = cn.prepareStatement(
                    "UPDATE mascota SET nombre = ?, especie = ?, raza = ?, edad = ?, peso = ? WHERE id_mascota = ?")) {
                pd.setString(1, m.getDueno().getNombre());
                pd.setString(2, m.getDueno().getTelefono());
                pd.setString(3, m.getDueno().getCorreo());
                pd.setInt(4, m.getDueno().getId());
                pd.executeUpdate();
                pm.setString(1, m.getNombre());
                pm.setString(2, m.getEspecie());
                pm.setString(3, m.getRaza());
                pm.setBigDecimal(4, m.getEdad());
                pm.setBigDecimal(5, m.getPeso());
                pm.setInt(6, m.getId());
                pm.executeUpdate();
                cn.commit();
            } catch (SQLException ex) {
                cn.rollback();
                throw ex;
            }
        } catch (SQLException ex) {
            throw error("actualizar la mascota", ex);
        }
    }

    @Override
    public void darDeBaja(int idMascota) {
        try (Connection cn = ConexionBD.obtener();
                PreparedStatement ps = cn.prepareStatement("UPDATE mascota SET estado = 'BAJA' WHERE id_mascota = ?")) {
            ps.setInt(1, idMascota);
            ps.executeUpdate();
        } catch (SQLException ex) {
            throw error("dar de baja la mascota", ex);
        }
    }

    private static int buscarOInsertarDueno(Connection cn, Dueno d) throws SQLException {
        try (PreparedStatement ps = cn.prepareStatement("SELECT id_dueno FROM dueno WHERE nombre = ? AND telefono = ?")) {
            ps.setString(1, d.getNombre());
            ps.setString(2, d.getTelefono());
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt(1);
                }
            }
        }
        try (PreparedStatement ps = cn.prepareStatement(
                "INSERT INTO dueno (nombre, telefono, correo) VALUES (?, ?, ?)", Statement.RETURN_GENERATED_KEYS)) {
            ps.setString(1, d.getNombre());
            ps.setString(2, d.getTelefono());
            ps.setString(3, d.getCorreo());
            ps.executeUpdate();
            try (ResultSet llaves = ps.getGeneratedKeys()) {
                llaves.next();
                return llaves.getInt(1);
            }
        }
    }

    private static Mascota leer(ResultSet rs) throws SQLException {
        Dueno d = new Dueno(rs.getInt("id_dueno"), rs.getString("dueno"), rs.getString("telefono"), rs.getString("correo"));
        return new Mascota(rs.getInt("id_mascota"), rs.getString("num_expediente"), d, rs.getString("nombre"),
                rs.getString("especie"), rs.getString("raza"), rs.getBigDecimal("edad"), rs.getBigDecimal("peso"),
                rs.getString("estado"));
    }

    private static boolean vacio(String s) {
        return s == null || s.isBlank();
    }

    private static IllegalStateException error(String accion, SQLException ex) {
        return new IllegalStateException("No se pudo " + accion + " en la base de datos: " + ex.getMessage(), ex);
    }
}
