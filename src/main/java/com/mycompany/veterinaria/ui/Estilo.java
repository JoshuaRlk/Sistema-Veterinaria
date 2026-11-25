package com.mycompany.veterinaria.ui;

import java.awt.Color;
import java.awt.Font;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.border.Border;

/** Colores y componentes del diseño final de las interfaces (PRY04, sección 3.4). */
public final class Estilo {

    public static final Color AZUL_OSCURO = new Color(0x1F, 0x38, 0x64);
    public static final Color AZUL = new Color(0x2E, 0x52, 0x96);
    public static final Color VERDE = new Color(0x2E, 0x7D, 0x32);
    public static final Color GRIS = new Color(0x7A, 0x7A, 0x7A);
    public static final Color ROJO = new Color(0xC6, 0x28, 0x28);
    public static final Color FONDO_TARJETA = new Color(0xF1, 0xF1, 0xF1);
    public static final Color VERDE_CLARO = new Color(0xE8, 0xF5, 0xE9);
    public static final Color ROJO_CLARO = new Color(0xFD, 0xEC, 0xEA);

    private Estilo() {
    }

    public static JButton boton(String texto, Color color) {
        JButton b = new JButton(texto);
        b.setBackground(color);
        b.setForeground(Color.WHITE);
        b.setFont(b.getFont().deriveFont(Font.BOLD, 13f));
        b.setFocusPainted(false);
        b.setOpaque(true);
        b.setBorder(BorderFactory.createEmptyBorder(8, 16, 8, 16));
        return b;
    }

    public static JLabel titulo(String texto) {
        JLabel l = new JLabel(texto);
        l.setFont(l.getFont().deriveFont(Font.BOLD, 22f));
        l.setForeground(AZUL);
        return l;
    }

    /** Recuadro gris con título, como los paneles "Datos del dueño", "Datos de la mascota", etc. */
    public static void tarjeta(JComponent c, String titulo) {
        Border linea = BorderFactory.createLineBorder(new Color(0x99, 0x99, 0x99), 1, true);
        Border conTitulo = BorderFactory.createTitledBorder(linea, titulo,
                javax.swing.border.TitledBorder.LEFT, javax.swing.border.TitledBorder.TOP,
                c.getFont().deriveFont(Font.BOLD, 14f), AZUL_OSCURO);
        c.setBorder(BorderFactory.createCompoundBorder(conTitulo, BorderFactory.createEmptyBorder(10, 8, 8, 8)));
        c.setBackground(FONDO_TARJETA);
        c.setOpaque(true);
    }

    /** Mensaje verde de éxito o rojo de error, como "Registro guardado exitosamente". */
    public static void mensaje(JLabel etiqueta, String texto, boolean exito) {
        // En HTML el texto largo baja de renglón en lugar de cortarse con "...".
        etiqueta.setText(texto == null ? " " : "<html>" + texto.replace("&", "&amp;").replace("<", "&lt;") + "</html>");
        etiqueta.setOpaque(texto != null);
        etiqueta.setForeground(exito ? VERDE : ROJO);
        etiqueta.setBackground(exito ? VERDE_CLARO : ROJO_CLARO);
        etiqueta.setFont(etiqueta.getFont().deriveFont(Font.BOLD, 13f));
        etiqueta.setBorder(texto == null ? BorderFactory.createEmptyBorder(6, 10, 6, 10)
                : BorderFactory.createCompoundBorder(
                        BorderFactory.createLineBorder(exito ? VERDE : ROJO, 1, true),
                        BorderFactory.createEmptyBorder(6, 10, 6, 10)));
    }
}
