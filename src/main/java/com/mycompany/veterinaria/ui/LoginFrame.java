package com.mycompany.veterinaria.ui;

import com.mycompany.veterinaria.datos.UsuarioDAO;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.util.Optional;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JPasswordField;
import javax.swing.JTextField;
import javax.swing.SwingUtilities;
import com.mycompany.veterinaria.seguridad.Sesion;
import com.mycompany.veterinaria.seguridad.Usuario;

/** Pantalla 1: inicio de sesión (Figura 6 del PRY04). */
public class LoginFrame extends JFrame {

    private final UsuarioDAO usuarios;
    private final JTextField txtUsuario = new JTextField(18);
    private final JPasswordField txtContrasena = new JPasswordField(18);
    private final JLabel lblMensaje = new JLabel(" ");

    public LoginFrame() {
        this(new UsuarioDAO());
    }

    public LoginFrame(UsuarioDAO usuarios) {
        super("Lima - Inicio de sesión");
        this.usuarios = usuarios;

        JPanel barra = new JPanel(new BorderLayout());
        barra.setBackground(Estilo.AZUL_OSCURO);
        barra.setBorder(BorderFactory.createEmptyBorder(12, 20, 12, 20));
        JLabel logo = new JLabel("LIMA");
        logo.setForeground(Color.WHITE);
        logo.setFont(logo.getFont().deriveFont(Font.BOLD, 22f));
        barra.add(logo, BorderLayout.WEST);

        JPanel tarjeta = new JPanel(new GridBagLayout());
        Estilo.tarjeta(tarjeta, "Iniciar sesión");
        GridBagConstraints g = new GridBagConstraints();
        g.insets = new Insets(4, 4, 4, 4);
        g.fill = GridBagConstraints.HORIZONTAL;
        g.gridx = 0;
        g.gridwidth = 2;
        g.gridy = 0; tarjeta.add(new JLabel("Usuario"), g);
        g.gridy = 1; tarjeta.add(txtUsuario, g);
        g.gridy = 2; tarjeta.add(new JLabel("Contraseña"), g);
        g.gridy = 3; tarjeta.add(txtContrasena, g);
        JButton btnEntrar = Estilo.boton("Iniciar sesión", Estilo.AZUL_OSCURO);
        JButton btnLimpiar = Estilo.boton("Limpiar", Estilo.GRIS);
        g.gridwidth = 1;
        g.gridy = 4; g.gridx = 0; tarjeta.add(btnEntrar, g);
        g.gridx = 1; tarjeta.add(btnLimpiar, g);
        g.gridx = 0; g.gridy = 5; g.gridwidth = 2;
        tarjeta.add(lblMensaje, g);

        JPanel centro = new JPanel(new GridBagLayout());
        centro.setBackground(Color.WHITE);
        JLabel bienvenida = Estilo.titulo("Bienvenido a Lima");
        GridBagConstraints c = new GridBagConstraints();
        c.gridy = 0; c.insets = new Insets(0, 0, 16, 0);
        centro.add(bienvenida, c);
        c.gridy = 1;
        centro.add(tarjeta, c);

        setLayout(new BorderLayout());
        add(barra, BorderLayout.NORTH);
        add(centro, BorderLayout.CENTER);

        btnEntrar.addActionListener(e -> entrar());
        btnLimpiar.addActionListener(e -> {
            txtUsuario.setText("");
            txtContrasena.setText("");
            Estilo.mensaje(lblMensaje, null, true);
            txtUsuario.requestFocusInWindow();
        });
        getRootPane().setDefaultButton(btnEntrar);

        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setSize(760, 520);
        setLocationRelativeTo(null);
    }

    private void entrar() {
        String usuario = txtUsuario.getText().trim();
        String contrasena = new String(txtContrasena.getPassword());
        if (usuario.isEmpty() || contrasena.isEmpty()) {
            Estilo.mensaje(lblMensaje, "Escribe tu usuario y contraseña.", false);
            return;
        }
        try {
            Optional<Usuario> encontrado = usuarios.autenticar(usuario, contrasena);
            if (encontrado.isEmpty()) {
                Estilo.mensaje(lblMensaje, "Usuario o contraseña incorrectos.", false);
                txtContrasena.setText("");
                return;
            }
            Sesion.iniciar(encontrado.get());
            dispose();
            SwingUtilities.invokeLater(() -> new MenuPrincipal().setVisible(true));
        } catch (IllegalStateException ex) {
            Estilo.mensaje(lblMensaje, ex.getMessage(), false);
        }
    }
}
