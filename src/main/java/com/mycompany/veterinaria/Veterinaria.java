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

        // Pantalla 1 del PRY04: el sistema inicia en el login y después abre el menú principal.
        java.awt.EventQueue.invokeLater(() -> new com.mycompany.veterinaria.ui.LoginFrame().setVisible(true));
    }
}
