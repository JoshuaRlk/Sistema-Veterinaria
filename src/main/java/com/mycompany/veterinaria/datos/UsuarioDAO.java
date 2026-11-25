package com.mycompany.veterinaria.datos;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Optional;
import com.mycompany.veterinaria.seguridad.Contrasenas;
import com.mycompany.veterinaria.seguridad.Rol;
import com.mycompany.veterinaria.seguridad.Usuario;

/** Acceso a la tabla usuario para el inicio de sesión. */
public class UsuarioDAO {

    private static final String SQL_BUSCAR =
            "SELECT id_usuario, nombre, usuario, contrasena_hash, rol FROM usuario WHERE usuario = ? AND activo = 1";

    /** Regresa el usuario si existe, está activo y la contraseña coincide. */
    public Optional<Usuario> autenticar(String usuario, String contrasena) {
        try (Connection cn = ConexionBD.obtener();
                PreparedStatement ps = cn.prepareStatement(SQL_BUSCAR)) {
            ps.setString(1, usuario);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next() && Contrasenas.coincide(contrasena, rs.getString("contrasena_hash"))) {
                    return Optional.of(new Usuario(rs.getInt("id_usuario"), rs.getString("nombre"),
                            rs.getString("usuario"), Rol.valueOf(rs.getString("rol"))));
                }
                return Optional.empty();
            }
        } catch (SQLException ex) {
            throw new IllegalStateException("No se pudo conectar con la base de datos: " + ex.getMessage(), ex);
        }
    }
}
