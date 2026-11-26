package com.mycompany.veterinaria.datos;

import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import com.mycompany.veterinaria.modelo.Producto;

/** Inventario en MySQL: tabla producto y vista vw_inventario_alertas. */
public class InventarioDAO implements InventarioRepositorio {

    private static final String SELECT = "SELECT id_producto, codigo_qr, nombre, categoria, precio, stock, lote, "
            + "fecha_caducidad, estado, alerta_inventario FROM vw_inventario_alertas ";

    @Override
    public List<Producto> listar(String filtro) {
        String sql = SELECT + "WHERE estado = 'ACTIVO'";
        boolean conFiltro = filtro != null && !filtro.isBlank();
        if (conFiltro) {
            sql += " AND (nombre LIKE ? OR categoria LIKE ? OR codigo_qr LIKE ?)";
        }
        sql += " ORDER BY nombre";
        List<Producto> lista = new ArrayList<>();
        try (Connection cn = ConexionBD.obtener();
                PreparedStatement ps = cn.prepareStatement(sql)) {
            if (conFiltro) {
                String f = "%" + filtro.trim() + "%";
                ps.setString(1, f);
                ps.setString(2, f);
                ps.setString(3, f);
            }
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    lista.add(leer(rs));
                }
            }
        } catch (SQLException ex) {
            throw error("consultar el inventario", ex);
        }
        return lista;
    }

    @Override
    public Optional<Producto> buscarPorCodigo(String codigoQr) {
        try (Connection cn = ConexionBD.obtener();
                PreparedStatement ps = cn.prepareStatement(SELECT + "WHERE codigo_qr = ? AND estado = 'ACTIVO'")) {
            ps.setString(1, codigoQr);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? Optional.of(leer(rs)) : Optional.empty();
            }
        } catch (SQLException ex) {
            throw error("buscar el producto", ex);
        }
    }

    @Override
    public boolean existeCodigo(String codigoQr, int ignorarId) {
        try (Connection cn = ConexionBD.obtener();
                PreparedStatement ps = cn.prepareStatement(
                        "SELECT 1 FROM producto WHERE codigo_qr = ? AND id_producto <> ?")) {
            ps.setString(1, codigoQr);
            ps.setInt(2, ignorarId);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next();
            }
        } catch (SQLException ex) {
            throw error("revisar el código", ex);
        }
    }

    @Override
    public Producto registrar(Producto p) {
        try (Connection cn = ConexionBD.obtener();
                PreparedStatement ps = cn.prepareStatement(
                        "INSERT INTO producto (codigo_qr, nombre, categoria, precio, stock, lote, fecha_caducidad) "
                        + "VALUES (?, ?, ?, ?, ?, ?, ?)", Statement.RETURN_GENERATED_KEYS)) {
            llenar(ps, p);
            ps.executeUpdate();
            try (ResultSet llaves = ps.getGeneratedKeys()) {
                llaves.next();
                p.setId(llaves.getInt(1));
            }
            return p;
        } catch (SQLException ex) {
            throw error("registrar el producto", ex);
        }
    }

    @Override
    public void actualizar(Producto p) {
        try (Connection cn = ConexionBD.obtener();
                PreparedStatement ps = cn.prepareStatement(
                        "UPDATE producto SET codigo_qr = ?, nombre = ?, categoria = ?, precio = ?, stock = ?, lote = ?, "
                        + "fecha_caducidad = ? WHERE id_producto = ?")) {
            llenar(ps, p);
            ps.setInt(8, p.getId());
            ps.executeUpdate();
        } catch (SQLException ex) {
            throw error("actualizar el producto", ex);
        }
    }

    @Override
    public void sumarStock(int idProducto, int cantidad) {
        try (Connection cn = ConexionBD.obtener();
                PreparedStatement ps = cn.prepareStatement(
                        "UPDATE producto SET stock = stock + ? WHERE id_producto = ? AND estado = 'ACTIVO'")) {
            ps.setInt(1, cantidad);
            ps.setInt(2, idProducto);
            if (ps.executeUpdate() != 1) {
                throw new IllegalStateException("El producto no existe o está dado de baja.");
            }
        } catch (SQLException ex) {
            throw error("registrar la entrada", ex);
        }
    }

    @Override
    public void darDeBaja(int idProducto) {
        try (Connection cn = ConexionBD.obtener();
                PreparedStatement ps = cn.prepareStatement("UPDATE producto SET estado = 'BAJA' WHERE id_producto = ?")) {
            ps.setInt(1, idProducto);
            ps.executeUpdate();
        } catch (SQLException ex) {
            throw error("dar de baja el producto", ex);
        }
    }

    private static void llenar(PreparedStatement ps, Producto p) throws SQLException {
        ps.setString(1, p.getCodigoQr());
        ps.setString(2, p.getNombre());
        ps.setString(3, p.getCategoria());
        ps.setBigDecimal(4, p.getPrecio());
        ps.setInt(5, p.getStock());
        ps.setString(6, p.getLote());
        ps.setDate(7, p.getCaducidad() == null ? null : Date.valueOf(p.getCaducidad()));
    }

    static Producto leer(ResultSet rs) throws SQLException {
        Date cad = rs.getDate("fecha_caducidad");
        return new Producto(rs.getInt("id_producto"), rs.getString("codigo_qr"), rs.getString("nombre"),
                rs.getString("categoria"), rs.getBigDecimal("precio"), rs.getInt("stock"), rs.getString("lote"),
                cad == null ? null : cad.toLocalDate(), rs.getString("estado"), rs.getString("alerta_inventario"));
    }

    private static IllegalStateException error(String accion, SQLException ex) {
        return new IllegalStateException("No se pudo " + accion + " en la base de datos: " + ex.getMessage(), ex);
    }
}
