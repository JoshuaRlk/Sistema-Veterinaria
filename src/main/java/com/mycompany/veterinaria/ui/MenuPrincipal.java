package com.mycompany.veterinaria.ui;

import java.awt.BorderLayout;
import java.awt.CardLayout;
import java.awt.Color;
import java.awt.FlowLayout;
import java.awt.Font;
import java.util.EnumMap;
import java.util.Map;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;
import com.mycompany.veterinaria.seguridad.Modulo;
import com.mycompany.veterinaria.seguridad.Rol;
import com.mycompany.veterinaria.seguridad.Sesion;

/**
 * Pantalla 2: menú principal con la barra superior del diseño final
 * (Inicio | Dueños y mascotas | Citas | Clínica | Inventario | Cobros | Cerrar sesión).
 * Solo se muestran los módulos que permite el rol del usuario (RF-A01).
 */
public class MenuPrincipal extends JFrame {

    private final CardLayout tarjetas = new CardLayout();
    private final JPanel contenido = new JPanel(tarjetas);
    private final Map<Modulo, JButton> botones = new EnumMap<>(Modulo.class);
    private final Map<Modulo, JComponent> paneles = new EnumMap<>(Modulo.class);
    private com.mycompany.veterinaria.modulos.modulocitas ventanaCitas;

    public MenuPrincipal() {
        super("Lima - Sistema de Gestión Veterinaria");
        Rol rol = Sesion.actual().getRol();

        JPanel barra = new JPanel(new BorderLayout());
        barra.setBackground(Estilo.AZUL_OSCURO);
        barra.setBorder(BorderFactory.createEmptyBorder(6, 16, 6, 16));
        JLabel logo = new JLabel("LIMA   ");
        logo.setForeground(Color.WHITE);
        logo.setFont(logo.getFont().deriveFont(Font.BOLD, 22f));

        JPanel menu = new JPanel(new FlowLayout(FlowLayout.LEFT, 6, 0));
        menu.setOpaque(false);
        for (Modulo m : Modulo.values()) {
            if (!rol.puedeAbrir(m)) {
                continue;
            }
            JButton b = new JButton(m.getTitulo());
            b.setForeground(Color.WHITE);
            b.setBackground(Estilo.AZUL_OSCURO);
            b.setOpaque(true);
            b.setFocusPainted(false);
            b.setBorder(BorderFactory.createEmptyBorder(6, 12, 6, 12));
            b.setFont(b.getFont().deriveFont(14f));
            b.addActionListener(e -> abrir(m));
            botones.put(m, b);
            menu.add(b);
        }

        JLabel usuario = new JLabel(Sesion.actual().getNombre() + " (" + rol + ")   ");
        usuario.setForeground(new Color(0xDD, 0xE3, 0xEE));
        JButton salir = new JButton("Cerrar sesión");
        salir.setForeground(Color.WHITE);
        salir.setBackground(Estilo.AZUL_OSCURO);
        salir.setOpaque(true);
        salir.setBorder(BorderFactory.createEmptyBorder(6, 12, 6, 12));
        salir.addActionListener(e -> cerrarSesion());
        JPanel derecha = new JPanel(new FlowLayout(FlowLayout.RIGHT, 0, 0));
        derecha.setOpaque(false);
        derecha.add(usuario);
        derecha.add(salir);

        barra.add(logo, BorderLayout.WEST);
        barra.add(menu, BorderLayout.CENTER);
        barra.add(derecha, BorderLayout.EAST);

        setLayout(new BorderLayout());
        add(barra, BorderLayout.NORTH);
        add(contenido, BorderLayout.CENTER);

        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setSize(1150, 720);
        setLocationRelativeTo(null);
        abrir(Modulo.INICIO);
    }

    private void abrir(Modulo m) {
        if (m == Modulo.CITAS) {
            abrirCitas();
            return;
        }
        if (!paneles.containsKey(m)) {
            JComponent panel = crearPanel(m);
            paneles.put(m, panel);
            contenido.add(panel, m.name());
        } else if (m == Modulo.INICIO) {
            ((PanelInicio) paneles.get(m)).refrescar();
        } else if (m == Modulo.INVENTARIO) {
            ((PanelInventario) paneles.get(m)).refrescar();
        }
        tarjetas.show(contenido, m.name());
        botones.forEach((modulo, b) -> b.setBackground(modulo == m ? Estilo.AZUL : Estilo.AZUL_OSCURO));
    }

    /**
     * Crea el panel de cada módulo la primera vez que se abre.
     */
    private JComponent crearPanel(Modulo m) {
        switch (m) {
            case INICIO:
                return new PanelInicio();
            case MASCOTAS:
                return new PanelDuenosMascotas();
            case CLINICA:
                return new PanelClinica();
            case INVENTARIO:
                return new PanelInventario();
            case COBROS:
                return new PanelCobros();
            default:
                throw new IllegalArgumentException("Módulo sin panel: " + m);
        }
    }

    /** El módulo de citas ya existe como ventana propia (modulocitas); se abre sin modificar su diseño. */
    private void abrirCitas() {
        try {
            if (ventanaCitas == null || !ventanaCitas.isDisplayable()) {
                ventanaCitas = new com.mycompany.veterinaria.modulos.modulocitas();
                ventanaCitas.setDefaultCloseOperation(DISPOSE_ON_CLOSE);
                ventanaCitas.setLocationRelativeTo(this);
            }
            ventanaCitas.setVisible(true);
            ventanaCitas.toFront();
        } catch (IllegalStateException ex) {
            JOptionPane.showMessageDialog(this, ex.getMessage(), "Error de conexión", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void cerrarSesion() {
        if (ventanaCitas != null) {
            ventanaCitas.dispose();
        }
        Sesion.cerrar();
        dispose();
        SwingUtilities.invokeLater(() -> new LoginFrame().setVisible(true));
    }
}
