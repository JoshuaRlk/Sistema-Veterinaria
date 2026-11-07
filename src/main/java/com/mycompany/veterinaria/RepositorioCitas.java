package datos;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;
import modelo.Cita;

/**
 * Guarda y lee las citas en un archivo de texto (citas.csv) para que
 * no se pierdan al cerrar el programa.
 */
public class RepositorioCitas {

    private static final String SEPARADOR = ";";
    private final Path archivo;

    public RepositorioCitas() {
        this(Paths.get(System.getProperty("user.dir"), "citas.csv"));
    }

    public RepositorioCitas(Path archivo) {
        this.archivo = archivo;
    }

    public List<Cita> cargar() {
        List<Cita> citas = new ArrayList<>();
        if (!Files.exists(archivo)) {
            return citas;
        }
        try {
            for (String linea : Files.readAllLines(archivo, StandardCharsets.UTF_8)) {
                if (linea.isBlank()) {
                    continue;
                }
                String[] p = linea.split(SEPARADOR, -1);
                if (p.length < 9) {
                    continue; // línea dañada, se ignora
                }
                citas.add(new Cita(
                        Integer.parseInt(p[0]),
                        LocalDate.parse(p[1]),
                        LocalTime.parse(p[2]),
                        p[3], p[4], p[5], p[6], p[7], p[8]));
            }
        } catch (IOException | RuntimeException ex) {
            System.err.println("No se pudo leer " + archivo + ": " + ex.getMessage());
        }
        return citas;
    }

    public void guardar(List<Cita> citas) {
        List<String> lineas = new ArrayList<>();
        for (Cita c : citas) {
            lineas.add(String.join(SEPARADOR,
                    String.valueOf(c.getId()),
                    c.getFecha().toString(),
                    c.getHora().toString(),
                    limpiar(c.getMascota()),
                    limpiar(c.getDueno()),
                    limpiar(c.getExpediente()),
                    limpiar(c.getServicio()),
                    limpiar(c.getResponsable()),
                    limpiar(c.getEstado())));
        }
        try {
            Files.write(archivo, lineas, StandardCharsets.UTF_8);
        } catch (IOException ex) {
            throw new IllegalStateException("No se pudieron guardar las citas: " + ex.getMessage(), ex);
        }
    }

    /** Evita que el texto del usuario rompa el formato del archivo. */
    private static String limpiar(String texto) {
        return texto == null ? "" : texto.replace(SEPARADOR, ",").replace("\n", " ").replace("\r", " ");
    }
}
