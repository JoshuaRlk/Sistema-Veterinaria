package datos;

import static org.junit.jupiter.api.Assertions.assertEquals;

import modelo.Cita;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

/** Prueba la conversión de estados entre la clase Cita y la columna cita.estado de MySQL. */
class CitaDAOTest {

    @Test
    @DisplayName("P15 Los estados de la aplicación se guardan con los valores del CHECK de MySQL")
    void estadosHaciaBD() {
        assertEquals("PROGRAMADA", CitaDAO.estadoHaciaBD(Cita.PROGRAMADA));
        assertEquals("EN_ESPERA", CitaDAO.estadoHaciaBD(Cita.EN_CLINICA));
        assertEquals("CANCELADA", CitaDAO.estadoHaciaBD(Cita.CANCELADA));
    }

    @Test
    @DisplayName("P16 Los estados leídos de MySQL regresan a los de la aplicación (ida y vuelta)")
    void estadosDesdeBD() {
        for (String estado : new String[]{Cita.PROGRAMADA, Cita.EN_CLINICA, Cita.CANCELADA}) {
            assertEquals(estado, CitaDAO.estadoDesdeBD(CitaDAO.estadoHaciaBD(estado)));
        }
    }
}
