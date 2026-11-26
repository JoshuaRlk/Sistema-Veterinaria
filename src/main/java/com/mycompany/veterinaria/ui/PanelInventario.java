package com.mycompany.veterinaria.ui;

import com.mycompany.veterinaria.datos.InventarioDAO;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.FlowLayout;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.GridLayout;
import java.awt.Insets;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.ListSelectionModel;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import com.mycompany.veterinaria.modelo.Producto;
import com.mycompany.veterinaria.seguridad.Sesion;
import com.mycompany.veterinaria.servicios.InventarioService;

/** Pantalla de Inventario: productos, entradas de mercancía y alertas de stock y caducidad. */
public class PanelInventario extends JPanel {

    private static final DateTimeFormatter FECHA = DateTimeFormatter.ofPattern("dd/MM/yyyy");

    private final InventarioService servicio;

    private final JTextField txtCodigo = new JTextField();
    private final JTextField txtNombre = new JTextField();
    private final JTextField txtCategoria = new JTextField();
    private final JTextField txtPrecio = new JTextField();
    private final JTextField txtStock = new JTextField();
    private final JTextField txtLote = new JTextField();
    private final JTextField txtCaducidad = new JTextField();
    private final JTextField txtFiltro = new JTextField(18);
    private final JLabel lblMensaje = new JLabel(" ");

    private final DefaultTableModel modelo = new DefaultTableModel(
            new String[]{"Código", "Producto", "Categoría", "Precio", "Stock", "Lote", "Caducidad", "Aviso"}, 0) {
        @Override
        public boolean isCellEditable(int f, int c) {
            return false;
        }
    };
    private final JTable tabla = new JTable(modelo);
    private final List<Producto> productos = new ArrayList<>();
    private Producto seleccionado;

    private final JButton btnGuardar = Estilo.boton("Guardar", Estilo.VERDE);
    private final JButton btnEditar = Estilo.boton("Editar", Estilo.GRIS);
    private final JButton btnEntrada = Estilo.boton("Registrar entrada", Estilo.AZUL);
    private final JButton btnBaja = Estilo.boton("Dar de baja", Estilo.ROJO);

    public PanelInventario() {
        this(new InventarioService(new InventarioDAO()));
    }

    public PanelInventario(InventarioService servicio) {
        super(new BorderLayout(16, 12));
        this.servicio = servicio;
        setBackground(Color.WHITE);
        setBorder(BorderFactory.createEmptyBorder(16, 24, 16, 24));

        // ---- Formulario ----
        JPanel form = new JPanel(new GridBagLayout());
        Estilo.tarjeta(form, "Datos del producto");
        int fila = 0;
        fila = campo(form, "Código QR / código de barras *", txtCodigo, fila);
        fila = campo(form, "Nombre *", txtNombre, fila);
        fila = campo(form, "Categoría *", txtCategoria, fila);
        JPanel numeros = new JPanel(new GridLayout(2, 2, 8, 2));
        numeros.setOpaque(false);
        numeros.add(new JLabel("Precio *"));
        numeros.add(new JLabel("Stock *"));
        numeros.add(txtPrecio);
        numeros.add(txtStock);
        GridBagConstraints g = gbc(fila++);
        form.add(numeros, g);
        fila = campo(form, "Lote", txtLote, fila);
        fila = campo(form, "Caducidad (dd/mm/aaaa)", txtCaducidad, fila);

        JButton btnNuevo = Estilo.boton("Nuevo", Estilo.AZUL_OSCURO);
        JPanel botones = new JPanel(new GridLayout(3, 2, 8, 8));
        botones.setOpaque(false);
        botones.add(btnNuevo);
        botones.add(btnGuardar);
        botones.add(btnEditar);
        botones.add(btnEntrada);
        botones.add(btnBaja);
        g = gbc(fila++);
        g.insets = new Insets(14, 4, 4, 4);
        form.add(botones, g);
        g = gbc(fila++);
        form.add(lblMensaje, g);
        g = gbc(fila);
        g.weighty = 1;
        JPanel relleno = new JPanel();
        relleno.setOpaque(false);
        form.add(relleno, g);

        // ---- Lista ----
        JButton btnBuscar = Estilo.boton("Buscar", Estilo.GRIS);
        JPanel buscar = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 0));
        buscar.setOpaque(false);
        buscar.add(new JLabel("Buscar:"));
        buscar.add(txtFiltro);
        buscar.add(btnBuscar);

        JPanel lista = new JPanel(new BorderLayout(0, 8));
        Estilo.tarjeta(lista, "Productos activos");
        tabla.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        tabla.getColumnModel().getColumn(7).setCellRenderer(new AvisoRenderer());
        tabla.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) {
                cargarSeleccion();
            }
        });
        lista.add(buscar, BorderLayout.NORTH);
        lista.add(new JScrollPane(tabla), BorderLayout.CENTER);

        JPanel izquierda = new JPanel(new BorderLayout());
        izquierda.setOpaque(false);
        izquierda.add(form);
        izquierda.setPreferredSize(new java.awt.Dimension(380, 10));

        add(Estilo.titulo("Inventario"), BorderLayout.NORTH);
        add(izquierda, BorderLayout.WEST);
        add(lista, BorderLayout.CENTER);

        boolean puede = Sesion.activa() && InventarioService.puedeModificar(Sesion.actual().getRol());
        for (JButton b : new JButton[]{btnNuevo, btnGuardar, btnEditar, btnEntrada, btnBaja}) {
            b.setVisible(puede);
        }

        btnNuevo.addActionListener(e -> nuevo());
        btnGuardar.addActionListener(e -> guardar());
        btnEditar.addActionListener(e -> editar());
        btnEntrada.addActionListener(e -> entrada());
        btnBaja.addActionListener(e -> baja());
        btnBuscar.addActionListener(e -> refrescar());
        txtFiltro.addActionListener(e -> refrescar());
        nuevo();
        refrescar();
    }

    public final void refrescar() {
        try {
            productos.clear();
            productos.addAll(servicio.listar(txtFiltro.getText()));
            modelo.setRowCount(0);
            for (Producto p : productos) {
                modelo.addRow(new Object[]{p.getCodigoQr(), p.getNombre(), p.getCategoria(), "$" + p.getPrecio(),
                    p.getStock(), p.getLote() == null ? "" : p.getLote(),
                    p.getCaducidad() == null ? "" : p.getCaducidad().format(FECHA), p.getAlerta()});
            }
        } catch (RuntimeException ex) {
            Estilo.mensaje(lblMensaje, ex.getMessage(), false);
        }
    }

    private void nuevo() {
        seleccionado = null;
        for (JTextField t : campos()) {
            t.setText("");
        }
        tabla.clearSelection();
        btnGuardar.setEnabled(true);
        btnEditar.setEnabled(false);
        btnEntrada.setEnabled(false);
        btnBaja.setEnabled(false);
        Estilo.mensaje(lblMensaje, null, true);
    }

    private void guardar() {
        try {
            Producto p = servicio.registrar(Sesion.actual().getRol(), txtCodigo.getText(), txtNombre.getText(),
                    txtCategoria.getText(), txtPrecio.getText(), txtStock.getText(), txtLote.getText(),
                    txtCaducidad.getText());
            refrescar();
            nuevo();
            Estilo.mensaje(lblMensaje, "Producto " + p.getNombre() + " registrado", true);
        } catch (RuntimeException ex) {
            Estilo.mensaje(lblMensaje, ex.getMessage(), false);
        }
    }

    private void editar() {
        if (seleccionado == null) {
            return;
        }
        try {
            servicio.editar(Sesion.actual().getRol(), seleccionado.getId(), txtCodigo.getText(), txtNombre.getText(),
                    txtCategoria.getText(), txtPrecio.getText(), txtStock.getText(), txtLote.getText(),
                    txtCaducidad.getText());
            refrescar();
            Estilo.mensaje(lblMensaje, "Cambios guardados", true);
        } catch (RuntimeException ex) {
            Estilo.mensaje(lblMensaje, ex.getMessage(), false);
        }
    }

    private void entrada() {
        if (seleccionado == null) {
            return;
        }
        String cantidad = JOptionPane.showInputDialog(this, "¿Cuántas piezas de " + seleccionado.getNombre()
                + " entraron?", "Entrada de mercancía", JOptionPane.QUESTION_MESSAGE);
        if (cantidad == null) {
            return;
        }
        try {
            servicio.registrarEntrada(Sesion.actual().getRol(), seleccionado.getId(), cantidad);
            String nombre = seleccionado.getNombre();
            refrescar();
            nuevo();
            Estilo.mensaje(lblMensaje, "Entrada registrada: +" + cantidad.trim() + " de " + nombre, true);
        } catch (RuntimeException ex) {
            Estilo.mensaje(lblMensaje, ex.getMessage(), false);
        }
    }

    private void baja() {
        if (seleccionado == null) {
            return;
        }
        int r = JOptionPane.showConfirmDialog(this, "¿Dar de baja " + seleccionado.getNombre()
                + "? Ya no aparecerá en ventas.", "Dar de baja", JOptionPane.YES_NO_OPTION);
        if (r != JOptionPane.YES_OPTION) {
            return;
        }
        try {
            servicio.darDeBaja(Sesion.actual().getRol(), seleccionado.getId());
            refrescar();
            nuevo();
            Estilo.mensaje(lblMensaje, "Producto dado de baja", true);
        } catch (RuntimeException ex) {
            Estilo.mensaje(lblMensaje, ex.getMessage(), false);
        }
    }

    private void cargarSeleccion() {
        int fila = tabla.getSelectedRow();
        if (fila < 0 || fila >= productos.size()) {
            return;
        }
        Producto p = productos.get(fila);
        seleccionado = p;
        txtCodigo.setText(p.getCodigoQr());
        txtNombre.setText(p.getNombre());
        txtCategoria.setText(p.getCategoria());
        txtPrecio.setText(p.getPrecio().toPlainString());
        txtStock.setText(String.valueOf(p.getStock()));
        txtLote.setText(p.getLote() == null ? "" : p.getLote());
        txtCaducidad.setText(p.getCaducidad() == null ? "" : p.getCaducidad().format(FECHA));
        btnGuardar.setEnabled(false);
        btnEditar.setEnabled(true);
        btnEntrada.setEnabled(true);
        btnBaja.setEnabled(true);
        Estilo.mensaje(lblMensaje, null, true);
    }

    private JTextField[] campos() {
        return new JTextField[]{txtCodigo, txtNombre, txtCategoria, txtPrecio, txtStock, txtLote, txtCaducidad};
    }

    private static GridBagConstraints gbc(int y) {
        GridBagConstraints g = new GridBagConstraints();
        g.gridx = 0;
        g.gridy = y;
        g.weightx = 1;
        g.fill = GridBagConstraints.HORIZONTAL;
        g.anchor = GridBagConstraints.NORTHWEST;
        g.insets = new Insets(3, 4, 3, 4);
        return g;
    }

    private static int campo(JPanel panel, String etiqueta, JComponent campo, int fila) {
        panel.add(new JLabel(etiqueta), gbc(fila));
        panel.add(campo, gbc(fila + 1));
        return fila + 2;
    }

    /** Pinta el aviso en rojo (sin stock / caducado) o naranja (stock bajo / por caducar). */
    private static final class AvisoRenderer extends DefaultTableCellRenderer {
        @Override
        public Component getTableCellRendererComponent(JTable t, Object valor, boolean sel, boolean foco, int f, int c) {
            Component comp = super.getTableCellRendererComponent(t, valor, sel, foco, f, c);
            String v = valor == null ? "" : valor.toString();
            if (!sel) {
                if (v.equals("SIN_STOCK") || v.equals("CADUCADO")) {
                    comp.setForeground(Estilo.ROJO);
                } else if (v.equals("STOCK_BAJO") || v.equals("POR_CADUCAR")) {
                    comp.setForeground(new Color(0xE6, 0x7E, 0x00));
                } else {
                    comp.setForeground(Estilo.VERDE);
                }
            }
            return comp;
        }
    }
}
