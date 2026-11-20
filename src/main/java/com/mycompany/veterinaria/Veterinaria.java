/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 */

package com.mycompany.veterinaria;

/**
 * Punto de entrada del sistema de la veterinaria.
 *
 * @author narut
 */
public class Veterinaria {

    public static void main(String[] args) {
        try {
            for (javax.swing.UIManager.LookAndFeelInfo info : javax.swing.UIManager.getInstalledLookAndFeels()) {
                if ("Nimbus".equals(info.getName())) {
                    javax.swing.UIManager.setLookAndFeel(info.getClassName());
                    break;
                }
            }
        } catch (ReflectiveOperationException | javax.swing.UnsupportedLookAndFeelException ex) {
            System.err.println("No se pudo aplicar el estilo Nimbus: " + ex.getMessage());
        }

        // Por ahora el sistema abre directamente el módulo de citas (Avance 1).
        java.awt.EventQueue.invokeLater(() -> {
            try {
                new modulos.modulocitas().setVisible(true);
            } catch (IllegalStateException ex) {
                // Por ejemplo, MySQL apagado o contraseña incorrecta.
                javax.swing.JOptionPane.showMessageDialog(null, ex.getMessage(),
                        "Error de conexión", javax.swing.JOptionPane.ERROR_MESSAGE);
            }
        });
    }
}
