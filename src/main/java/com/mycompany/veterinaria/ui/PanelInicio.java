package com.mycompany.veterinaria.ui;

import com.mycompany.veterinaria.datos.ConexionBD;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.GridLayout;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import javax.swing.BorderFactory;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.table.DefaultTableModel;
import com.mycompany.veterinaria.seguridad.Sesion;

/** Pantalla 2 (Inicio): resumen de citas del día y avisos de inventario. */
public class PanelInicio extends JPanel {

    private final DefaultTableModel modeloCitas = tabla("Hora", "Mascota", "Dueño", "Servicio", "Encargado", "Estado");
    private final DefaultTableModel modeloAlertas = tabla("Producto", "Stock", "Caducidad", "Aviso");
    private final JLabel lblError = new JLabel(" ");

    public PanelInicio() {
        super(new BorderLayout(0, 12));
        setBackground(Color.WHITE);
        setBorder(BorderFactory.createEmptyBorder(16, 24, 16, 24));

        JPanel encabezado = new JPanel(new BorderLayout());
        encabezado.setOpaque(false);
        encabezado.add(Estilo.titulo("Hola, " + Sesion.actual().getNombre()), BorderLayout.NORTH);
        encabezado.add(lblError, BorderLayout.SOUTH);

        JPanel citas = new JPanel(new BorderLayout());
        Estilo.tarjeta(citas, "Citas de hoy");
        citas.add(new JScrollPane(new JTable(modeloCitas)));

        JPanel alertas = new JPanel(new BorderLayout());
        Estilo.tarjeta(alertas, "Avisos de inventario");
        alertas.add(new JScrollPane(new JTable(modeloAlertas)));

        JPanel cuerpo = new JPanel(new GridLayout(1, 2, 16, 0));
        cuerpo.setOpaque(false);
        cuerpo.add(citas);
        cuerpo.add(alertas);

        add(encabezado, BorderLayout.NORTH);
        add(cuerpo, BorderLayout.CENTER);
        refrescar();
    }

    public final void refrescar() {
        modeloCitas.setRowCount(0);
        modeloAlertas.setRowCount(0);
        try (Connection cn = ConexionBD.obtener()) {
            try (PreparedStatement ps = cn.prepareStatement(
                    "SELECT hora, mascota, dueno, tipo_servicio, personal_asignado, estado_cita "
                    + "FROM vw_agenda_citas WHERE fecha = CURDATE() ORDER BY hora");
                    ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    modeloCitas.addRow(new Object[]{rs.getTime(1).toLocalTime(), rs.getString(2), rs.getString(3),
                        rs.getString(4), rs.getString(5), rs.getString(6)});
                }
            }
            try (PreparedStatement ps = cn.prepareStatement(
                    "SELECT nombre, stock, fecha_caducidad, alerta_inventario FROM vw_inventario_alertas "
                    + "WHERE alerta_inventario NOT IN ('NORMAL','INACTIVO') ORDER BY stock");
                    ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    modeloAlertas.addRow(new Object[]{rs.getString(1), rs.getInt(2), rs.getDate(3), rs.getString(4)});
                }
            }
            Estilo.mensaje(lblError, null, true);
        } catch (SQLException ex) {
            Estilo.mensaje(lblError, "No se pudo consultar el resumen: " + ex.getMessage(), false);
        }
    }

    private static DefaultTableModel tabla(String... columnas) {
        return new DefaultTableModel(columnas, 0) {
            @Override
            public boolean isCellEditable(int fila, int columna) {
                return false;
            }
        };
    }
}
