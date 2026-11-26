package com.mycompany.veterinaria.datos;

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import com.mycompany.veterinaria.modelo.CitaClinica;
import com.mycompany.veterinaria.modelo.Concepto;
import com.mycompany.veterinaria.modelo.Dueno;
import com.mycompany.veterinaria.modelo.Producto;

/** Cobros en MySQL: tablas venta, detalle_venta, producto y cita. */
public class CobroDAO implements CobroRepositorio {

    private final InventarioDAO inventario = new InventarioDAO();

    @Override
    public List<Dueno> buscarDuenos(String texto) {
        List<Dueno> lista = new ArrayList<>();
        try (Connection cn = ConexionBD.obtener();
                PreparedStatement ps = cn.prepareStatement(
                        "SELECT id_dueno, nombre, telefono, correo FROM dueno WHERE nombre LIKE ? OR telefono LIKE ? "
                        + "ORDER BY nombre LIMIT 50")) {
            String f = "%" + (texto == null ? "" : texto.trim()) + "%";
            ps.setString(1, f);
            ps.setString(2, f);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    lista.add(new Dueno(rs.getInt(1), rs.getString(2), rs.getString(3), rs.getString(4)));
                }
            }
        } catch (SQLException ex) {
            throw error("buscar dueños", ex);
        }
        return lista;
    }

    @Override
    public List<CitaClinica> serviciosPorCobrar(int idDueno) {
        List<CitaClinica> lista = new ArrayList<>();
        String sql = "SELECT c.id_cita, c.fecha, c.hora, c.tipo_servicio, m.nombre, c.estado FROM cita c "
                + "INNER JOIN mascota m ON c.id_mascota = m.id_mascota "
                + "WHERE m.id_dueno = ? AND c.estado = 'ATENDIDA' ORDER BY c.fecha, c.hora";
        try (Connection cn = ConexionBD.obtener();
                PreparedStatement ps = cn.prepareStatement(sql)) {
            ps.setInt(1, idDueno);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    lista.add(new CitaClinica(rs.getInt(1), rs.getDate(2).toLocalDate(), rs.getTime(3).toLocalTime(),
                            rs.getString(4), rs.getString(5), rs.getString(6)));
                }
            }
        } catch (SQLException ex) {
            throw error("consultar los servicios por cobrar", ex);
        }
        return lista;
    }

    @Override
    public Optional<Producto> productoPorCodigo(String codigo) {
        return inventario.buscarPorCodigo(codigo);
    }

    @Override
    public int registrarVenta(int idDueno, int idUsuario, BigDecimal descuento, BigDecimal total, String metodoPago,
            List<Concepto> conceptos) {
        try (Connection cn = ConexionBD.obtener()) {
            cn.setAutoCommit(false);
            try {
                // 1) Bloquear y revisar el stock de cada producto (evita vender lo que otro cajero ya vendió).
                try (PreparedStatement ps = cn.prepareStatement(
                        "SELECT stock, nombre FROM producto WHERE id_producto = ? AND estado = 'ACTIVO' FOR UPDATE")) {
                    for (Concepto c : conceptos) {
                        if (c.getTipo() != Concepto.Tipo.PRODUCTO) {
                            continue;
                        }
                        ps.setInt(1, c.getIdReferencia());
                        try (ResultSet rs = ps.executeQuery()) {
                            if (!rs.next()) {
                                throw new IllegalStateException("Un producto de la cuenta ya no está disponible.");
                            }
                            if (rs.getInt(1) < c.getCantidad()) {
                                throw new IllegalStateException("Stock insuficiente de " + rs.getString(2)
                                        + ": quedan " + rs.getInt(1) + ".");
                            }
                        }
                    }
                }
                // 2) Venta
                int idVenta;
                try (PreparedStatement ps = cn.prepareStatement(
                        "INSERT INTO venta (id_dueno, id_usuario, descuento, total, metodo_pago) VALUES (?, ?, ?, ?, ?)",
                        Statement.RETURN_GENERATED_KEYS)) {
                    ps.setInt(1, idDueno);
                    ps.setInt(2, idUsuario);
                    ps.setBigDecimal(3, descuento);
                    ps.setBigDecimal(4, total);
                    ps.setString(5, metodoPago);
                    ps.executeUpdate();
                    try (ResultSet llaves = ps.getGeneratedKeys()) {
                        llaves.next();
                        idVenta = llaves.getInt(1);
                    }
                }
                // 3) Detalle, stock y citas
                try (PreparedStatement det = cn.prepareStatement(
                        "INSERT INTO detalle_venta (id_venta, id_producto, id_cita, cantidad, precio_unitario, subtotal) "
                        + "VALUES (?, ?, ?, ?, ?, ?)");
                        PreparedStatement stock = cn.prepareStatement(
                                "UPDATE producto SET stock = stock - ? WHERE id_producto = ?");
                        PreparedStatement cita = cn.prepareStatement(
                                "UPDATE cita SET estado = 'COBRADA' WHERE id_cita = ? AND estado = 'ATENDIDA'")) {
                    for (Concepto c : conceptos) {
                        boolean producto = c.getTipo() == Concepto.Tipo.PRODUCTO;
                        det.setInt(1, idVenta);
                        det.setObject(2, producto ? c.getIdReferencia() : null);
                        det.setObject(3, producto ? null : c.getIdReferencia());
                        det.setBigDecimal(4, BigDecimal.valueOf(c.getCantidad()));
                        det.setBigDecimal(5, c.getPrecioUnitario());
                        det.setBigDecimal(6, c.getSubtotal());
                        det.executeUpdate();
                        if (producto) {
                            stock.setInt(1, c.getCantidad());
                            stock.setInt(2, c.getIdReferencia());
                            stock.executeUpdate();
                        } else {
                            cita.setInt(1, c.getIdReferencia());
                            if (cita.executeUpdate() != 1) {
                                throw new IllegalStateException("Una de las citas ya fue cobrada o no está atendida.");
                            }
                        }
                    }
                }
                cn.commit();
                return idVenta;
            } catch (SQLException | RuntimeException ex) {
                cn.rollback();
                throw ex;
            }
        } catch (SQLException ex) {
            throw error("registrar la venta", ex);
        }
    }

    private static IllegalStateException error(String accion, SQLException ex) {
        return new IllegalStateException("No se pudo " + accion + " en la base de datos: " + ex.getMessage(), ex);
    }
}
