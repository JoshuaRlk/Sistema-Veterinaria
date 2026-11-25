package com.mycompany.veterinaria.ui;

import com.mycompany.veterinaria.datos.MascotaDAO;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.GridLayout;
import java.awt.Insets;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.ListSelectionModel;
import javax.swing.table.DefaultTableModel;
import com.mycompany.veterinaria.modelo.Mascota;
import com.mycompany.veterinaria.seguridad.Rol;
import com.mycompany.veterinaria.seguridad.Sesion;
import com.mycompany.veterinaria.servicios.MascotaService;

/** Pantalla 3: Dueños y mascotas (Figura 7 del PRY04, CU-01). */
public class PanelDuenosMascotas extends JPanel {

    private final MascotaService servicio;

    private final JTextField txtDueno = new JTextField();
    private final JTextField txtTelefono = new JTextField();
    private final JTextField txtCorreo = new JTextField();
    private final JTextField txtNombre = new JTextField();
    private final JTextField txtEspecie = new JTextField();
    private final JTextField txtRaza = new JTextField();
    private final JTextField txtEdad = new JTextField();
    private final JTextField txtPeso = new JTextField();
    private final Map<String, JTextField> campos = new LinkedHashMap<>();
    private final Map<JTextField, javax.swing.border.Border> bordes = new java.util.HashMap<>();

    private final DefaultTableModel modelo = new DefaultTableModel(
            new String[]{"ID", "Expediente", "Mascota", "Especie", "Dueño", "Teléfono"}, 0) {
        @Override
        public boolean isCellEditable(int f, int c) {
            return false;
        }
    };
    private final JTable tabla = new JTable(modelo);
    private final List<Mascota> resultados = new ArrayList<>();

    private final JLabel lblId = valor();
    private final JLabel lblExpediente = valor();
    private final JLabel lblMensaje = new JLabel(" ");
    private final JButton btnEditar = Estilo.boton("Editar", Estilo.GRIS);
    private final JButton btnBaja = Estilo.boton("Dar de baja", Estilo.ROJO);

    private Mascota seleccionada;

    public PanelDuenosMascotas() {
        this(new MascotaService(new MascotaDAO()));
    }

    public PanelDuenosMascotas(MascotaService servicio) {
        super(new BorderLayout(16, 12));
        this.servicio = servicio;
        setBackground(Color.WHITE);
        setBorder(BorderFactory.createEmptyBorder(16, 24, 16, 24));

        campos.put("dueño", txtDueno);
        campos.put("teléfono", txtTelefono);
        campos.put("correo", txtCorreo);
        campos.put("nombre", txtNombre);
        campos.put("especie", txtEspecie);
        campos.put("raza", txtRaza);
        campos.put("edad", txtEdad);
        campos.put("peso", txtPeso);
        campos.values().forEach(c -> bordes.put(c, c.getBorder()));

        // ---- Izquierda: formularios ----
        JPanel pDueno = new JPanel(new GridBagLayout());
        Estilo.tarjeta(pDueno, "Datos del dueño");
        agregar(pDueno, "Nombre del dueño *", txtDueno, 0, 0, 2);
        agregar(pDueno, "Teléfono *", txtTelefono, 0, 2, 1);
        agregar(pDueno, "Correo", txtCorreo, 1, 2, 1);

        JPanel pMascota = new JPanel(new GridBagLayout());
        Estilo.tarjeta(pMascota, "Datos de la mascota");
        agregar(pMascota, "Nombre *", txtNombre, 0, 0, 1);
        agregar(pMascota, "Especie *", txtEspecie, 1, 0, 1);
        agregar(pMascota, "Raza *", txtRaza, 0, 2, 1);
        JPanel edadPeso = new JPanel(new GridLayout(1, 2, 8, 0));
        edadPeso.setOpaque(false);
        edadPeso.add(conEtiqueta("Edad (años) *", txtEdad));
        edadPeso.add(conEtiqueta("Peso (kg) *", txtPeso));
        GridBagConstraints g = base(1, 2, 1);
        g.gridheight = 2;
        pMascota.add(edadPeso, g);

        JButton btnBuscar = Estilo.boton("Buscar", Estilo.GRIS);
        JButton btnNuevo = Estilo.boton("Nuevo", Estilo.AZUL_OSCURO);
        JButton btnGuardar = Estilo.boton("Guardar", Estilo.VERDE);
        JPanel acciones = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 0));
        acciones.setOpaque(false);
        acciones.add(btnBuscar);
        acciones.add(btnNuevo);
        acciones.add(btnGuardar);
        acciones.add(btnEditar);
        acciones.add(btnBaja);
        g = base(0, 4, 2);
        g.insets = new Insets(14, 4, 4, 4);
        pMascota.add(acciones, g);
        relleno(pMascota, 5);
        relleno(pDueno, 3);

        JPanel izquierda = new JPanel(new BorderLayout(0, 12));
        izquierda.setOpaque(false);
        izquierda.add(pDueno, BorderLayout.NORTH);
        izquierda.add(pMascota, BorderLayout.CENTER);

        // ---- Derecha: coincidencias y resultado ----
        JPanel pLista = new JPanel(new BorderLayout());
        Estilo.tarjeta(pLista, "Listado de coincidencias");
        tabla.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        tabla.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) {
                cargarSeleccion();
            }
        });
        pLista.add(new JScrollPane(tabla));

        JPanel pResultado = new JPanel(new GridLayout(2, 2, 8, 6));
        Estilo.tarjeta(pResultado, "Resultado del registro");
        pResultado.add(new JLabel("ID único de mascota:"));
        pResultado.add(lblId);
        pResultado.add(new JLabel("Expediente:"));
        pResultado.add(lblExpediente);

        JButton btnExpediente = Estilo.boton("Ver expediente", Estilo.AZUL);
        JPanel abajoDerecha = new JPanel(new BorderLayout(0, 10));
        abajoDerecha.setOpaque(false);
        abajoDerecha.add(pResultado, BorderLayout.NORTH);
        abajoDerecha.add(lblMensaje, BorderLayout.CENTER);
        JPanel filaExp = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
        filaExp.setOpaque(false);
        filaExp.add(btnExpediente);
        abajoDerecha.add(filaExp, BorderLayout.SOUTH);

        JPanel derecha = new JPanel(new BorderLayout(0, 12));
        derecha.setOpaque(false);
        derecha.add(pLista, BorderLayout.CENTER);
        derecha.add(abajoDerecha, BorderLayout.SOUTH);

        JPanel cuerpo = new JPanel(new GridLayout(1, 2, 16, 0));
        cuerpo.setOpaque(false);
        cuerpo.add(izquierda);
        cuerpo.add(derecha);

        add(Estilo.titulo("Dueños y mascotas"), BorderLayout.NORTH);
        add(cuerpo, BorderLayout.CENTER);

        boolean puedeEditar = Sesion.activa() && Sesion.actual().getRol().puedeEditarMascotas();
        btnEditar.setVisible(puedeEditar);
        btnBaja.setVisible(puedeEditar);

        btnBuscar.addActionListener(e -> buscar());
        btnNuevo.addActionListener(e -> nuevo());
        btnGuardar.addActionListener(e -> guardar());
        btnEditar.addActionListener(e -> editar());
        btnBaja.addActionListener(e -> darDeBaja());
        btnExpediente.addActionListener(e -> verExpediente());
        nuevo();
    }

    // ===================== Acciones =====================

    private void buscar() {
        limpiarMarcas();
        try {
            resultados.clear();
            resultados.addAll(servicio.buscar(txtDueno.getText(), txtTelefono.getText(), txtNombre.getText()));
            modelo.setRowCount(0);
            for (Mascota m : resultados) {
                modelo.addRow(new Object[]{Mascota.formatoId(m.getId()), m.getExpediente(), m.getNombre(),
                    m.getEspecie(), m.getDueno().getNombre(), m.getDueno().getTelefono()});
            }
            Estilo.mensaje(lblMensaje, resultados.isEmpty()
                    ? "Sin coincidencias: se puede registrar." : resultados.size() + " coincidencia(s).", true);
        } catch (RuntimeException ex) {
            Estilo.mensaje(lblMensaje, ex.getMessage(), false);
        }
    }

    private void nuevo() {
        seleccionada = null;
        campos.values().forEach(c -> c.setText(""));
        limpiarMarcas();
        tabla.clearSelection();
        lblId.setText("—");
        lblExpediente.setText("—");
        btnEditar.setEnabled(false);
        btnBaja.setEnabled(false);
        Estilo.mensaje(lblMensaje, null, true);
        txtDueno.requestFocusInWindow();
    }

    private void guardar() {
        if (seleccionada != null) {
            Estilo.mensaje(lblMensaje, "Es una mascota ya registrada: usa Editar, o Nuevo para registrar otra.", false);
            return;
        }
        limpiarMarcas();
        try {
            Mascota m = servicio.registrar(txtDueno.getText(), txtTelefono.getText(), txtCorreo.getText(),
                    txtNombre.getText(), txtEspecie.getText(), txtRaza.getText(), txtEdad.getText(), txtPeso.getText());
            seleccionada = m;
            lblId.setText(Mascota.formatoId(m.getId()));
            lblExpediente.setText(m.getExpediente());
            btnEditar.setEnabled(true);
            btnBaja.setEnabled(true);
            Estilo.mensaje(lblMensaje, "Registro guardado exitosamente", true);
        } catch (MascotaService.CamposInvalidos ex) {
            marcar(ex.getCampos());
            Estilo.mensaje(lblMensaje, ex.getMessage(), false);
        } catch (RuntimeException ex) {
            Estilo.mensaje(lblMensaje, ex.getMessage(), false);
        }
    }

    private void editar() {
        if (seleccionada == null) {
            Estilo.mensaje(lblMensaje, "Selecciona una mascota del listado.", false);
            return;
        }
        limpiarMarcas();
        try {
            Rol rol = Sesion.actual().getRol();
            Mascota m = servicio.editar(rol, seleccionada.getId(), seleccionada.getDueno().getId(),
                    txtDueno.getText(), txtTelefono.getText(), txtCorreo.getText(), txtNombre.getText(),
                    txtEspecie.getText(), txtRaza.getText(), txtEdad.getText(), txtPeso.getText());
            m.setExpediente(seleccionada.getExpediente());
            seleccionada = m;
            Estilo.mensaje(lblMensaje, "Cambios guardados correctamente", true);
        } catch (MascotaService.CamposInvalidos ex) {
            marcar(ex.getCampos());
            Estilo.mensaje(lblMensaje, ex.getMessage(), false);
        } catch (RuntimeException ex) {
            Estilo.mensaje(lblMensaje, ex.getMessage(), false);
        }
    }

    private void darDeBaja() {
        if (seleccionada == null) {
            Estilo.mensaje(lblMensaje, "Selecciona una mascota del listado.", false);
            return;
        }
        int r = JOptionPane.showConfirmDialog(this, "¿Dar de baja a " + seleccionada.getNombre()
                + "? Su historial se conserva.", "Dar de baja", JOptionPane.YES_NO_OPTION);
        if (r != JOptionPane.YES_OPTION) {
            return;
        }
        try {
            servicio.darDeBaja(Sesion.actual().getRol(), seleccionada.getId());
            String nombre = seleccionada.getNombre();
            nuevo();
            modelo.setRowCount(0);
            Estilo.mensaje(lblMensaje, nombre + " quedó dada de baja.", true);
        } catch (RuntimeException ex) {
            Estilo.mensaje(lblMensaje, ex.getMessage(), false);
        }
    }

    /** Por ahora muestra el resumen; cuando exista el módulo Clínica, este botón abrirá el expediente completo. */
    private void verExpediente() {
        if (seleccionada == null) {
            Estilo.mensaje(lblMensaje, "Selecciona o registra una mascota primero.", false);
            return;
        }
        Mascota m = seleccionada;
        JOptionPane.showMessageDialog(this,
                "Expediente: " + m.getExpediente() + "\nID: " + Mascota.formatoId(m.getId())
                + "\nMascota: " + m.getNombre() + " · " + m.getEspecie() + " · " + m.getRaza()
                + "\nEdad: " + m.getEdad().stripTrailingZeros().toPlainString() + " años · Peso: "
                + m.getPeso().stripTrailingZeros().toPlainString() + " kg"
                + "\nDueño: " + m.getDueno().getNombre() + " · " + m.getDueno().getTelefono(),
                "Expediente " + m.getExpediente(), JOptionPane.INFORMATION_MESSAGE);
    }

    private void cargarSeleccion() {
        int fila = tabla.getSelectedRow();
        if (fila < 0 || fila >= resultados.size()) {
            return;
        }
        Mascota m = resultados.get(fila);
        seleccionada = m;
        txtDueno.setText(m.getDueno().getNombre());
        txtTelefono.setText(m.getDueno().getTelefono());
        txtCorreo.setText(m.getDueno().getCorreo() == null ? "" : m.getDueno().getCorreo());
        txtNombre.setText(m.getNombre());
        txtEspecie.setText(m.getEspecie());
        txtRaza.setText(m.getRaza());
        txtEdad.setText(m.getEdad().stripTrailingZeros().toPlainString());
        txtPeso.setText(m.getPeso().stripTrailingZeros().toPlainString());
        lblId.setText(Mascota.formatoId(m.getId()));
        lblExpediente.setText(m.getExpediente());
        btnEditar.setEnabled(true);
        btnBaja.setEnabled(true);
        limpiarMarcas();
    }

    // ===================== Utilidades de la vista =====================

    private void marcar(List<String> nombres) {
        for (String n : nombres) {
            JTextField c = campos.get(n);
            if (c != null) {
                c.setBorder(BorderFactory.createLineBorder(Estilo.ROJO, 2));
            }
        }
    }

    private void limpiarMarcas() {
        campos.values().forEach(c -> c.setBorder(bordes.get(c)));
    }

    private static JLabel valor() {
        JLabel l = new JLabel("—");
        l.setFont(l.getFont().deriveFont(Font.BOLD, 16f));
        l.setForeground(Estilo.AZUL_OSCURO);
        return l;
    }

    private static JPanel conEtiqueta(String etiqueta, JTextField campo) {
        JPanel p = new JPanel(new BorderLayout(0, 2));
        p.setOpaque(false);
        p.add(new JLabel(etiqueta), BorderLayout.NORTH);
        p.add(campo, BorderLayout.CENTER);
        return p;
    }

    private static GridBagConstraints base(int x, int y, int ancho) {
        GridBagConstraints g = new GridBagConstraints();
        g.gridx = x;
        g.gridy = y;
        g.gridwidth = ancho;
        g.weightx = 1;
        g.fill = GridBagConstraints.HORIZONTAL;
        g.anchor = GridBagConstraints.NORTHWEST;
        g.insets = new Insets(4, 4, 4, 4);
        return g;
    }

    /** Empuja los campos hacia arriba para que no queden centrados con un hueco. */
    private static void relleno(JPanel panel, int fila) {
        GridBagConstraints g = base(0, fila, 2);
        g.weighty = 1;
        JPanel vacio = new JPanel();
        vacio.setOpaque(false);
        panel.add(vacio, g);
    }

    private static void agregar(JPanel panel, String etiqueta, JTextField campo, int x, int y, int ancho) {
        panel.add(conEtiqueta(etiqueta, campo), base(x, y, ancho));
    }
}
