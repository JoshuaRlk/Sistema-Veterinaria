package com.mycompany.veterinaria.ui;

import com.mycompany.veterinaria.datos.ClinicaDAO;
import com.mycompany.veterinaria.datos.MascotaDAO;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.GridLayout;
import java.awt.Insets;
import java.time.format.DateTimeFormatter;
import java.util.List;
import javax.swing.BorderFactory;
import javax.swing.DefaultComboBoxModel;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.table.DefaultTableModel;
import com.mycompany.veterinaria.modelo.Antecedentes;
import com.mycompany.veterinaria.modelo.CitaClinica;
import com.mycompany.veterinaria.modelo.Mascota;
import com.mycompany.veterinaria.modelo.RegistroClinico;
import com.mycompany.veterinaria.seguridad.Rol;
import com.mycompany.veterinaria.seguridad.Sesion;
import com.mycompany.veterinaria.servicios.ClinicaService;
import com.mycompany.veterinaria.servicios.MascotaService;

/** Pantalla 5: Expediente clínico (Figura 9 del PRY04). */
public class PanelClinica extends JPanel {

    private static final DateTimeFormatter FECHA = DateTimeFormatter.ofPattern("dd/MM/yyyy");
    private static final DateTimeFormatter FECHA_HORA = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");

    private final ClinicaService clinica;
    private final MascotaService mascotas;

    private final JTextField txtBuscar = new JTextField(22);
    private final DefaultComboBoxModel<MascotaItem> modeloResultados = new DefaultComboBoxModel<>();
    private final JComboBox<MascotaItem> cmbResultados = new JComboBox<>(modeloResultados);

    private final JLabel lblNombre = new JLabel("Selecciona una mascota");
    private final JLabel lblDatos = new JLabel(" ");
    private final JLabel lblId = new JLabel(" ");
    private final JLabel lblExpediente = new JLabel(" ");
    private final JLabel lblDueno = new JLabel(" ");

    private final JTextField txtVacunas = new JTextField();
    private final JTextField txtAlergias = new JTextField();
    private final DefaultTableModel modeloHistorial = new DefaultTableModel(
            new String[]{"Fecha", "Servicio", "Diagnóstico", "Medicamento", "Dosis", "Revisión", "Veterinario"}, 0) {
        @Override
        public boolean isCellEditable(int f, int c) {
            return false;
        }
    };

    private final DefaultComboBoxModel<CitaClinica> modeloCitas = new DefaultComboBoxModel<>();
    private final JComboBox<CitaClinica> cmbCita = new JComboBox<>(modeloCitas);
    private final JTextField txtSintomas = new JTextField();
    private final JTextField txtDiagnostico = new JTextField();
    private final JTextField txtMedicamento = new JTextField();
    private final JTextField txtDosis = new JTextField();
    private final JTextField txtIndicaciones = new JTextField();
    private final JTextField txtRevision = new JTextField();
    private final JLabel lblMensaje = new JLabel(" ");
    private final JButton btnGuardarConsulta = Estilo.boton("Guardar consulta", Estilo.VERDE);
    private final JButton btnAntecedentes = Estilo.boton("Guardar vacunas y alergias", Estilo.AZUL_OSCURO);

    private Mascota actual;

    public PanelClinica() {
        this(new ClinicaService(new ClinicaDAO()), new MascotaService(new MascotaDAO()));
    }

    public PanelClinica(ClinicaService clinica, MascotaService mascotas) {
        super(new BorderLayout(0, 12));
        this.clinica = clinica;
        this.mascotas = mascotas;
        setBackground(Color.WHITE);
        setBorder(BorderFactory.createEmptyBorder(16, 24, 16, 24));

        // ---- Búsqueda ----
        JButton btnBuscar = Estilo.boton("Buscar", Estilo.GRIS);
        JPanel buscar = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 0));
        buscar.setOpaque(false);
        buscar.add(new JLabel("Mascota o expediente:"));
        buscar.add(txtBuscar);
        buscar.add(btnBuscar);
        buscar.add(cmbResultados);
        cmbResultados.setPrototypeDisplayValue(new MascotaItem(null, "EXP-000000 · Nombre largo de mascota (Dueño)"));

        JPanel arriba = new JPanel(new BorderLayout(0, 8));
        arriba.setOpaque(false);
        arriba.add(Estilo.titulo("Expediente clínico"), BorderLayout.NORTH);
        arriba.add(buscar, BorderLayout.CENTER);

        // ---- Encabezado de la mascota ----
        JPanel encabezado = new JPanel(new GridLayout(2, 3, 12, 4));
        Estilo.tarjeta(encabezado, "Mascota");
        lblNombre.setFont(lblNombre.getFont().deriveFont(Font.BOLD, 20f));
        lblNombre.setForeground(Estilo.AZUL_OSCURO);
        lblId.setFont(lblId.getFont().deriveFont(Font.BOLD, 14f));
        lblExpediente.setFont(lblExpediente.getFont().deriveFont(Font.BOLD, 14f));
        encabezado.add(lblNombre);
        encabezado.add(lblId);
        encabezado.add(lblDueno);
        encabezado.add(lblDatos);
        encabezado.add(lblExpediente);
        encabezado.add(new JLabel(" "));

        // ---- Historial ----
        JPanel antecedentes = new JPanel(new GridBagLayout());
        antecedentes.setOpaque(false);
        GridBagConstraints g = gbc(0, 0);
        antecedentes.add(new JLabel("Vacunas"), g);
        antecedentes.add(txtVacunas, gbc(1, 0));
        antecedentes.add(new JLabel("Alergias"), gbc(0, 1));
        antecedentes.add(txtAlergias, gbc(1, 1));
        GridBagConstraints gb = gbc(1, 2);
        gb.fill = GridBagConstraints.NONE;
        gb.anchor = GridBagConstraints.WEST;
        antecedentes.add(btnAntecedentes, gb);

        JPanel historial = new JPanel(new BorderLayout(0, 8));
        Estilo.tarjeta(historial, "Historial médico");
        historial.add(antecedentes, BorderLayout.NORTH);
        JTable tabla = new JTable(modeloHistorial);
        historial.add(new JScrollPane(tabla), BorderLayout.CENTER);

        // ---- Nuevo diagnóstico ----
        JPanel nuevo = new JPanel(new GridBagLayout());
        Estilo.tarjeta(nuevo, "Nuevo diagnóstico");
        int fila = 0;
        fila = campo(nuevo, "Cita que se atiende *", cmbCita, fila);
        fila = campo(nuevo, "Síntomas", txtSintomas, fila);
        fila = campo(nuevo, "Diagnóstico *", txtDiagnostico, fila);
        fila = campo(nuevo, "Tratamiento / medicamento", txtMedicamento, fila);
        JPanel dosisRevision = new JPanel(new GridLayout(2, 2, 8, 2));
        dosisRevision.setOpaque(false);
        dosisRevision.add(new JLabel("Dosis"));
        dosisRevision.add(new JLabel("Fecha de revisión (dd/mm/aaaa)"));
        dosisRevision.add(txtDosis);
        dosisRevision.add(txtRevision);
        GridBagConstraints gd = gbc(0, fila++);
        gd.gridwidth = 2;
        nuevo.add(dosisRevision, gd);
        fila = campo(nuevo, "Indicaciones", txtIndicaciones, fila);
        GridBagConstraints gg = gbc(0, fila++);
        gg.gridwidth = 2;
        gg.insets = new Insets(12, 4, 4, 4);
        JPanel botones = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
        botones.setOpaque(false);
        botones.add(btnGuardarConsulta);
        nuevo.add(botones, gg);
        gg = gbc(0, fila++);
        gg.gridwidth = 2;
        nuevo.add(lblMensaje, gg);
        gg = gbc(0, fila);
        gg.gridwidth = 2;
        gg.weighty = 1;
        JPanel relleno = new JPanel();
        relleno.setOpaque(false);
        nuevo.add(relleno, gg);

        JPanel cuerpo = new JPanel(new GridLayout(1, 2, 16, 0));
        cuerpo.setOpaque(false);
        cuerpo.add(historial);
        cuerpo.add(nuevo);

        JPanel centro = new JPanel(new BorderLayout(0, 12));
        centro.setOpaque(false);
        centro.add(encabezado, BorderLayout.NORTH);
        centro.add(cuerpo, BorderLayout.CENTER);

        add(arriba, BorderLayout.NORTH);
        add(centro, BorderLayout.CENTER);

        boolean puede = Sesion.activa() && ClinicaService.puedeDiagnosticar(Sesion.actual().getRol());
        btnGuardarConsulta.setVisible(puede);
        btnAntecedentes.setVisible(puede);
        if (!puede) {
            Estilo.mensaje(lblMensaje, "Solo consulta: los diagnósticos los registra el veterinario.", true);
        }

        btnBuscar.addActionListener(e -> buscar());
        txtBuscar.addActionListener(e -> buscar());
        cmbResultados.addActionListener(e -> {
            MascotaItem item = (MascotaItem) cmbResultados.getSelectedItem();
            if (item != null && item.mascota != null) {
                cargar(item.mascota);
            }
        });
        btnAntecedentes.addActionListener(e -> guardarAntecedentes());
        btnGuardarConsulta.addActionListener(e -> guardarConsulta());
        habilitar(false);
    }

    private void buscar() {
        try {
            List<Mascota> encontradas = mascotas.buscar("", "", txtBuscar.getText());
            modeloResultados.removeAllElements();
            for (Mascota m : encontradas) {
                modeloResultados.addElement(new MascotaItem(m, m.getExpediente() + " · " + m.getNombre()
                        + " (" + m.getDueno().getNombre() + ")"));
            }
            if (encontradas.isEmpty()) {
                Estilo.mensaje(lblMensaje, "No se encontró ninguna mascota activa con ese nombre o expediente.", false);
            }
        } catch (RuntimeException ex) {
            Estilo.mensaje(lblMensaje, ex.getMessage(), false);
        }
    }

    private void cargar(Mascota m) {
        actual = m;
        lblNombre.setText(m.getNombre());
        lblDatos.setText(m.getEspecie() + " · " + m.getRaza() + " · " + m.getEdad().stripTrailingZeros().toPlainString()
                + " años · " + m.getPeso().stripTrailingZeros().toPlainString() + " kg");
        lblId.setText("ID: " + Mascota.formatoId(m.getId()));
        lblExpediente.setText("Expediente: " + m.getExpediente());
        lblDueno.setText("Dueño: " + m.getDueno().getNombre());
        try {
            Antecedentes a = clinica.antecedentes(m.getId());
            txtVacunas.setText(a.getVacunas() == null ? "" : a.getVacunas());
            txtAlergias.setText(a.getAlergias() == null ? "" : a.getAlergias());
            refrescarHistorialYCitas();
            habilitar(true);
            Estilo.mensaje(lblMensaje, null, true);
        } catch (RuntimeException ex) {
            Estilo.mensaje(lblMensaje, ex.getMessage(), false);
        }
    }

    private void refrescarHistorialYCitas() {
        modeloHistorial.setRowCount(0);
        for (RegistroClinico r : clinica.historial(actual.getId())) {
            modeloHistorial.addRow(new Object[]{r.getFecha().format(FECHA_HORA), r.getServicio(), r.getDiagnostico(),
                r.getMedicamento(), r.getDosis(), r.getFechaRevision() == null ? "" : r.getFechaRevision().format(FECHA),
                r.getVeterinario()});
        }
        modeloCitas.removeAllElements();
        for (CitaClinica c : clinica.citasPorAtender(actual.getId())) {
            modeloCitas.addElement(c);
        }
    }

    private void guardarAntecedentes() {
        if (actual == null) {
            return;
        }
        try {
            clinica.actualizarAntecedentes(rol(), actual.getId(), txtVacunas.getText(), txtAlergias.getText());
            Estilo.mensaje(lblMensaje, "Vacunas y alergias actualizadas", true);
        } catch (RuntimeException ex) {
            Estilo.mensaje(lblMensaje, ex.getMessage(), false);
        }
    }

    private void guardarConsulta() {
        if (actual == null) {
            Estilo.mensaje(lblMensaje, "Primero busca y selecciona una mascota.", false);
            return;
        }
        try {
            clinica.registrarConsulta(rol(), Sesion.actual().getId(), (CitaClinica) cmbCita.getSelectedItem(),
                    txtSintomas.getText(), txtDiagnostico.getText(), txtMedicamento.getText(), txtDosis.getText(),
                    txtIndicaciones.getText(), txtRevision.getText());
            for (JTextField t : new JTextField[]{txtSintomas, txtDiagnostico, txtMedicamento, txtDosis, txtIndicaciones, txtRevision}) {
                t.setText("");
            }
            refrescarHistorialYCitas();
            Estilo.mensaje(lblMensaje, "Diagnóstico registrado correctamente", true);
        } catch (RuntimeException ex) {
            Estilo.mensaje(lblMensaje, ex.getMessage(), false);
        }
    }

    private void habilitar(boolean si) {
        for (JComponent c : new JComponent[]{txtVacunas, txtAlergias, cmbCita, txtSintomas, txtDiagnostico,
            txtMedicamento, txtDosis, txtIndicaciones, txtRevision, btnGuardarConsulta, btnAntecedentes}) {
            c.setEnabled(si);
        }
    }

    private static Rol rol() {
        return Sesion.actual().getRol();
    }

    private static GridBagConstraints gbc(int x, int y) {
        GridBagConstraints g = new GridBagConstraints();
        g.gridx = x;
        g.gridy = y;
        g.weightx = x == 0 ? 0 : 1;
        g.fill = GridBagConstraints.HORIZONTAL;
        g.anchor = GridBagConstraints.NORTHWEST;
        g.insets = new Insets(4, 4, 4, 4);
        return g;
    }

    private static int campo(JPanel panel, String etiqueta, JComponent campo, int fila) {
        GridBagConstraints g = gbc(0, fila);
        g.gridwidth = 2;
        if (fila == 0) {
            g.insets = new Insets(14, 4, 4, 4); // deja espacio para el título del recuadro
        }
        panel.add(new JLabel(etiqueta), g);
        g = gbc(0, fila + 1);
        g.gridwidth = 2;
        g.weightx = 1;
        panel.add(campo, g);
        return fila + 2;
    }

    /** Elemento del combo de resultados. */
    private static final class MascotaItem {
        final Mascota mascota;
        final String texto;

        MascotaItem(Mascota mascota, String texto) {
            this.mascota = mascota;
            this.texto = texto;
        }

        @Override
        public String toString() {
            return texto;
        }
    }
}
