package com.mycompany.veterinaria.ui;

import com.mycompany.veterinaria.datos.CobroDAO;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.GridLayout;
import java.awt.Insets;
import java.io.IOException;
import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.time.LocalDateTime;
import java.util.List;
import javax.swing.BorderFactory;
import javax.swing.DefaultComboBoxModel;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.ListSelectionModel;
import javax.swing.table.DefaultTableModel;
import com.mycompany.veterinaria.modelo.CitaClinica;
import com.mycompany.veterinaria.modelo.Concepto;
import com.mycompany.veterinaria.modelo.Dueno;
import com.mycompany.veterinaria.seguridad.Sesion;
import com.mycompany.veterinaria.servicios.CobroService;
import com.mycompany.veterinaria.servicios.Cuenta;

/** Pantalla 6: Inventario y cobro – cuenta del cliente (Figura 10 del PRY04). */
public class PanelCobros extends JPanel {

    private final CobroService servicio;
    private final Cuenta cuenta = new Cuenta();

    private final JTextField txtDueno = new JTextField(16);
    private final DefaultComboBoxModel<DuenoItem> modeloDuenos = new DefaultComboBoxModel<>();
    private final JComboBox<DuenoItem> cmbDuenos = new JComboBox<>(modeloDuenos);
    private final JTextField txtCodigo = new JTextField(14);
    private final JTextField txtCantidad = new JTextField("1", 4);
    private final DefaultComboBoxModel<CitaClinica> modeloServicios = new DefaultComboBoxModel<>();
    private final JComboBox<CitaClinica> cmbServicios = new JComboBox<>(modeloServicios);
    private final JTextField txtPrecioServicio = new JTextField(7);

    private final DefaultTableModel modelo = new DefaultTableModel(
            new String[]{"Concepto", "Cant.", "Precio unit.", "Subtotal", "Stock"}, 0) {
        @Override
        public boolean isCellEditable(int f, int c) {
            return false;
        }
    };
    private final JTable tabla = new JTable(modelo);

    private final JTextField txtDescuento = new JTextField("0.00", 8);
    private final JComboBox<String> cmbMetodo = new JComboBox<>(CobroService.METODOS.toArray(new String[0]));
    private final JLabel lblSubtotal = monto(14f);
    private final JLabel lblDescuento = monto(14f);
    private final JLabel lblTotal = monto(24f);
    private final JLabel lblMensaje = new JLabel(" ");
    private final JButton btnTicket = Estilo.boton("Emitir ticket", Estilo.AZUL);

    private String ultimoTicket;

    public PanelCobros() {
        this(new CobroService(new CobroDAO()));
    }

    public PanelCobros(CobroService servicio) {
        super(new BorderLayout(0, 12));
        this.servicio = servicio;
        setBackground(Color.WHITE);
        setBorder(BorderFactory.createEmptyBorder(16, 24, 16, 24));
        cmbDuenos.setPrototypeDisplayValue(new DuenoItem(null, "Nombre de un dueño bastante largo · 2280000000"));
        cmbServicios.setPrototypeDisplayValue(new CitaClinica(0, java.time.LocalDate.now(), java.time.LocalTime.NOON,
                "Consulta general larga", "Mascota", "ATENDIDA"));

        // ---- Fila 1: dueño ----
        JButton btnBuscar = Estilo.boton("Buscar", Estilo.GRIS);
        JPanel fDueno = fila();
        fDueno.add(new JLabel("Dueño:"));
        fDueno.add(txtDueno);
        fDueno.add(btnBuscar);
        fDueno.add(cmbDuenos);

        // ---- Fila 2: productos ----
        JButton btnProducto = Estilo.boton("Agregar concepto", Estilo.AZUL);
        JPanel fProducto = fila();
        fProducto.add(new JLabel("Código QR / producto:"));
        fProducto.add(txtCodigo);
        fProducto.add(new JLabel("Cantidad:"));
        fProducto.add(txtCantidad);
        fProducto.add(btnProducto);

        // ---- Fila 3: servicios ----
        JButton btnServicio = Estilo.boton("Agregar servicio", Estilo.AZUL);
        JPanel fServicio = fila();
        fServicio.add(new JLabel("Servicio atendido:"));
        fServicio.add(cmbServicios);
        fServicio.add(new JLabel("Precio $"));
        fServicio.add(txtPrecioServicio);
        fServicio.add(btnServicio);

        JPanel captura = new JPanel(new GridLayout(3, 1, 0, 6));
        captura.setOpaque(false);
        captura.add(fDueno);
        captura.add(fProducto);
        captura.add(fServicio);

        JPanel arriba = new JPanel(new BorderLayout(0, 8));
        arriba.setOpaque(false);
        arriba.add(Estilo.titulo("Inventario y cobro"), BorderLayout.NORTH);
        arriba.add(captura, BorderLayout.CENTER);

        // ---- Conceptos ----
        JPanel pConceptos = new JPanel(new BorderLayout(0, 6));
        Estilo.tarjeta(pConceptos, "Conceptos de la cuenta");
        tabla.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        tabla.getColumnModel().getColumn(0).setPreferredWidth(380);
        pConceptos.add(new JScrollPane(tabla), BorderLayout.CENTER);
        JButton btnQuitar = Estilo.boton("Quitar concepto", Estilo.GRIS);
        JPanel fQuitar = fila();
        fQuitar.add(btnQuitar);
        pConceptos.add(fQuitar, BorderLayout.SOUTH);

        // ---- Abajo: pago y totales ----
        JPanel pago = new JPanel(new GridBagLayout());
        pago.setOpaque(false);
        GridBagConstraints g = new GridBagConstraints();
        g.insets = new Insets(4, 4, 4, 12);
        g.anchor = GridBagConstraints.WEST;
        g.gridx = 0; g.gridy = 0; pago.add(new JLabel("Descuento $"), g);
        g.gridx = 1; pago.add(new JLabel("Método de pago"), g);
        g.gridx = 0; g.gridy = 1; pago.add(txtDescuento, g);
        g.gridx = 1; pago.add(cmbMetodo, g);
        JButton btnCalcular = Estilo.boton("Calcular total", Estilo.GRIS);
        JButton btnCobrar = Estilo.boton("Cobrar", Estilo.VERDE);
        JPanel botones = fila();
        botones.add(btnCalcular);
        botones.add(btnCobrar);
        botones.add(btnTicket);
        g.gridx = 0; g.gridy = 2; g.gridwidth = 2; g.insets = new Insets(14, 4, 4, 4);
        pago.add(botones, g);
        g.gridy = 3; g.insets = new Insets(8, 4, 4, 4); g.fill = GridBagConstraints.HORIZONTAL;
        pago.add(lblMensaje, g);

        JPanel totales = new JPanel(new GridLayout(3, 2, 8, 6));
        Estilo.tarjeta(totales, "Total");
        totales.add(new JLabel("Subtotal"));
        totales.add(lblSubtotal);
        totales.add(new JLabel("Descuento"));
        totales.add(lblDescuento);
        JLabel tt = new JLabel("TOTAL");
        tt.setFont(tt.getFont().deriveFont(Font.BOLD, 20f));
        tt.setForeground(Estilo.AZUL_OSCURO);
        totales.add(tt);
        totales.add(lblTotal);

        JPanel abajo = new JPanel(new GridLayout(1, 2, 16, 0));
        abajo.setOpaque(false);
        abajo.add(pago);
        abajo.add(totales);

        add(arriba, BorderLayout.NORTH);
        add(pConceptos, BorderLayout.CENTER);
        add(abajo, BorderLayout.SOUTH);

        boolean puede = Sesion.activa() && CobroService.puedeCobrar(Sesion.actual().getRol());
        btnCobrar.setVisible(puede);
        btnTicket.setEnabled(false);

        btnBuscar.addActionListener(e -> buscarDueno());
        txtDueno.addActionListener(e -> buscarDueno());
        cmbDuenos.addActionListener(e -> cargarServicios());
        btnProducto.addActionListener(e -> agregarProducto());
        txtCodigo.addActionListener(e -> agregarProducto()); // un lector de código de barras manda Enter
        btnServicio.addActionListener(e -> agregarServicio());
        btnQuitar.addActionListener(e -> quitar());
        btnCalcular.addActionListener(e -> calcular());
        btnCobrar.addActionListener(e -> cobrar());
        btnTicket.addActionListener(e -> mostrarTicket());
        refrescarTabla();
    }

    private void buscarDueno() {
        try {
            List<Dueno> duenos = servicio.buscarDuenos(txtDueno.getText());
            modeloDuenos.removeAllElements();
            for (Dueno d : duenos) {
                modeloDuenos.addElement(new DuenoItem(d, d.getNombre() + " · " + d.getTelefono()));
            }
            if (duenos.isEmpty()) {
                Estilo.mensaje(lblMensaje, "No se encontró ese dueño. Regístralo en Dueños y mascotas.", false);
            } else {
                Estilo.mensaje(lblMensaje, null, true);
            }
        } catch (RuntimeException ex) {
            Estilo.mensaje(lblMensaje, ex.getMessage(), false);
        }
    }

    private void cargarServicios() {
        modeloServicios.removeAllElements();
        Dueno d = duenoSeleccionado();
        if (d == null) {
            return;
        }
        try {
            for (CitaClinica c : servicio.serviciosPorCobrar(d.getId())) {
                modeloServicios.addElement(c);
            }
        } catch (RuntimeException ex) {
            Estilo.mensaje(lblMensaje, ex.getMessage(), false);
        }
    }

    private void agregarProducto() {
        try {
            servicio.agregarProducto(cuenta, txtCodigo.getText(), txtCantidad.getText());
            txtCodigo.setText("");
            txtCantidad.setText("1");
            txtCodigo.requestFocusInWindow();
            refrescarTabla();
            Estilo.mensaje(lblMensaje, null, true);
        } catch (RuntimeException ex) {
            Estilo.mensaje(lblMensaje, ex.getMessage(), false);
        }
    }

    private void agregarServicio() {
        try {
            servicio.agregarServicio(cuenta, (CitaClinica) cmbServicios.getSelectedItem(), txtPrecioServicio.getText());
            txtPrecioServicio.setText("");
            refrescarTabla();
            Estilo.mensaje(lblMensaje, null, true);
        } catch (RuntimeException ex) {
            Estilo.mensaje(lblMensaje, ex.getMessage(), false);
        }
    }

    private void quitar() {
        int fila = tabla.getSelectedRow();
        if (fila >= 0) {
            cuenta.quitar(fila);
            refrescarTabla();
        }
    }

    private boolean calcular() {
        try {
            BigDecimal d = new BigDecimal(normalizar(txtDescuento.getText()));
            BigDecimal total = servicio.total(cuenta, txtDescuento.getText());
            lblSubtotal.setText("$" + cuenta.subtotal().toPlainString());
            lblDescuento.setText("$" + d.setScale(2, java.math.RoundingMode.HALF_UP).toPlainString());
            lblTotal.setText("$" + total.toPlainString());
            return true;
        } catch (RuntimeException ex) {
            Estilo.mensaje(lblMensaje, ex.getMessage() == null ? "Revisa el descuento." : ex.getMessage(), false);
            return false;
        }
    }

    private void cobrar() {
        if (!calcular()) {
            return;
        }
        Dueno d = duenoSeleccionado();
        String metodo = (String) cmbMetodo.getSelectedItem();
        int r = JOptionPane.showConfirmDialog(this, "¿Cobrar " + lblTotal.getText() + " con " + metodo + "?",
                "Confirmar cobro", JOptionPane.YES_NO_OPTION);
        if (r != JOptionPane.YES_OPTION) {
            return;
        }
        try {
            int idVenta = servicio.cobrar(Sesion.actual().getRol(), Sesion.actual().getId(), d, cuenta,
                    txtDescuento.getText(), metodo);
            ultimoTicket = CobroService.ticket(idVenta, LocalDateTime.now(), d, Sesion.actual().getNombre(), cuenta,
                    new BigDecimal(normalizar(txtDescuento.getText())), metodo);
            cuenta.vaciar();
            txtDescuento.setText("0.00");
            refrescarTabla();
            cargarServicios();
            btnTicket.setEnabled(true);
            Estilo.mensaje(lblMensaje, String.format("Venta V-%06d registrada. Inventario actualizado", idVenta), true);
        } catch (RuntimeException ex) {
            Estilo.mensaje(lblMensaje, ex.getMessage(), false);
        }
    }

    private void mostrarTicket() {
        if (ultimoTicket == null) {
            return;
        }
        JTextArea area = new JTextArea(ultimoTicket, 22, 34);
        area.setFont(new Font(Font.MONOSPACED, Font.PLAIN, 12));
        area.setEditable(false);
        Object[] opciones = {"Guardar .txt", "Cerrar"};
        int r = JOptionPane.showOptionDialog(this, new JScrollPane(area), "Ticket", JOptionPane.DEFAULT_OPTION,
                JOptionPane.PLAIN_MESSAGE, null, opciones, opciones[1]);
        if (r == 0) {
            JFileChooser fc = new JFileChooser();
            fc.setSelectedFile(new java.io.File("ticket.txt"));
            if (fc.showSaveDialog(this) == JFileChooser.APPROVE_OPTION) {
                try {
                    Files.writeString(fc.getSelectedFile().toPath(), ultimoTicket, StandardCharsets.UTF_8);
                    Estilo.mensaje(lblMensaje, "Ticket guardado en " + fc.getSelectedFile().getName(), true);
                } catch (IOException ex) {
                    Estilo.mensaje(lblMensaje, "No se pudo guardar el ticket: " + ex.getMessage(), false);
                }
            }
        }
    }

    private void refrescarTabla() {
        modelo.setRowCount(0);
        for (Concepto c : cuenta.getConceptos()) {
            modelo.addRow(new Object[]{c.getDescripcion(), c.getCantidad(), "$" + c.getPrecioUnitario().toPlainString(),
                "$" + c.getSubtotal().toPlainString(), c.getStockDisponible() == null ? "—" : c.getStockDisponible()});
        }
        lblSubtotal.setText("$" + cuenta.subtotal().toPlainString());
        lblDescuento.setText("$0.00");
        lblTotal.setText("$" + cuenta.subtotal().toPlainString());
    }

    private Dueno duenoSeleccionado() {
        DuenoItem item = (DuenoItem) cmbDuenos.getSelectedItem();
        return item == null ? null : item.dueno;
    }

    private static String normalizar(String s) {
        return s == null || s.isBlank() ? "0" : s.trim().replace("$", "").replace(",", "");
    }

    private static JPanel fila() {
        JPanel p = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 0));
        p.setOpaque(false);
        return p;
    }

    private static JLabel monto(float tam) {
        JLabel l = new JLabel("$0.00");
        l.setFont(l.getFont().deriveFont(Font.BOLD, tam));
        l.setForeground(Estilo.AZUL_OSCURO);
        return l;
    }

    private static final class DuenoItem {
        final Dueno dueno;
        final String texto;

        DuenoItem(Dueno dueno, String texto) {
            this.dueno = dueno;
            this.texto = texto;
        }

        @Override
        public String toString() {
            return texto;
        }
    }
}
