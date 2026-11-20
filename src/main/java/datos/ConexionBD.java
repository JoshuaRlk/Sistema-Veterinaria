package datos;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

/**
 * Conexión JDBC a la base de datos veterinaria_lima.
 * Usa el usuario de la aplicación vet_app, que solo tiene permisos sobre veterinaria_lima
 * (se crea en db/datos_prueba.sql). Se puede cambiar sin recompilar con
 * -Ddb.user=... -Ddb.password=... (NetBeans: Properties > Run > VM Options).
 */
public final class ConexionBD {

    private static final String URL = System.getProperty("db.url",
            "jdbc:mysql://localhost:3306/veterinaria_lima?serverTimezone=America/Mexico_City");
    private static final String USUARIO = System.getProperty("db.user", "vet_app");
    private static final String CONTRASENA = System.getProperty("db.password", "VetLima2026");

    private ConexionBD() {
    }

    public static Connection obtener() throws SQLException {
        return DriverManager.getConnection(URL, USUARIO, CONTRASENA);
    }
}
