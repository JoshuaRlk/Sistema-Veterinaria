package com.mycompany.veterinaria.servicios;

import com.mycompany.veterinaria.datos.CobroRepositorio;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Set;
import com.mycompany.veterinaria.modelo.CitaClinica;
import com.mycompany.veterinaria.modelo.Concepto;
import com.mycompany.veterinaria.modelo.Dueno;
import com.mycompany.veterinaria.modelo.Producto;
import com.mycompany.veterinaria.seguridad.Rol;

/** Reglas de Cobros (Figura 10 del PRY04): armar la cuenta, cobrar y emitir el ticket. */
public class CobroService {

    public static final List<String> METODOS = List.of("EFECTIVO", "TARJETA", "TRANSFERENCIA", "MIXTO");
    private static final Set<Rol> PUEDEN_COBRAR = Set.of(Rol.RECEPCIONISTA, Rol.ADMINISTRADOR);

    private final CobroRepositorio repositorio;

    public CobroService(CobroRepositorio repositorio) {
        this.repositorio = repositorio;
    }

    public static boolean puedeCobrar(Rol rol) {
        return PUEDEN_COBRAR.contains(rol);
    }

    public List<Dueno> buscarDuenos(String texto) {
        if (texto == null || texto.isBlank()) {
            throw new IllegalArgumentException("Escribe el nombre o teléfono del dueño.");
        }
        return repositorio.buscarDuenos(texto);
    }

    public List<CitaClinica> serviciosPorCobrar(int idDueno) {
        return repositorio.serviciosPorCobrar(idDueno);
    }

    /** Busca el producto por código y lo agrega a la cuenta. */
    public Producto agregarProducto(Cuenta cuenta, String codigo, String cantidad) {
        if (codigo == null || codigo.isBlank()) {
            throw new IllegalArgumentException("Escribe o escanea el código del producto.");
        }
        int n = entero(cantidad);
        Producto p = repositorio.productoPorCodigo(codigo.trim())
                .orElseThrow(() -> new IllegalArgumentException("No hay un producto activo con el código " + codigo.trim() + "."));
        cuenta.agregarProducto(p, n);
        return p;
    }

    public void agregarServicio(Cuenta cuenta, CitaClinica cita, String precio) {
        cuenta.agregarServicio(cita, dinero(precio, "precio del servicio"));
    }

    public BigDecimal total(Cuenta cuenta, String descuento) {
        return cuenta.total(dinero(descuento, "descuento"));
    }

    /** Valida y registra el cobro. Regresa el id de la venta. */
    public int cobrar(Rol rol, int idUsuario, Dueno dueno, Cuenta cuenta, String descuento, String metodo) {
        if (!puedeCobrar(rol)) {
            throw new IllegalStateException("Solo recepción o administrador pueden cobrar.");
        }
        if (dueno == null) {
            throw new IllegalArgumentException("Selecciona al dueño que paga.");
        }
        if (cuenta.isVacia()) {
            throw new IllegalArgumentException("La cuenta no tiene conceptos.");
        }
        if (!METODOS.contains(metodo)) {
            throw new IllegalArgumentException("Selecciona un método de pago válido.");
        }
        BigDecimal d = dinero(descuento, "descuento");
        BigDecimal total = cuenta.total(d);
        return repositorio.registrarVenta(dueno.getId(), idUsuario, d, total, metodo, cuenta.getConceptos());
    }

    /** Texto del ticket de venta. */
    public static String ticket(int idVenta, LocalDateTime fecha, Dueno dueno, String atendio, Cuenta cuenta,
            BigDecimal descuento, String metodo) {
        StringBuilder t = new StringBuilder();
        t.append("        VETERINARIA LIMA\n");
        t.append("================================\n");
        t.append(String.format("Ticket: V-%06d%n", idVenta));
        t.append("Fecha:  ").append(fecha.format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm"))).append('\n');
        t.append("Cliente: ").append(dueno.getNombre()).append('\n');
        t.append("Atendió: ").append(atendio).append('\n');
        t.append("--------------------------------\n");
        for (Concepto c : cuenta.getConceptos()) {
            t.append(c.getDescripcion()).append('\n');
            t.append(String.format("   %d x $%s = $%s%n", c.getCantidad(), c.getPrecioUnitario().toPlainString(),
                    c.getSubtotal().toPlainString()));
        }
        BigDecimal d = descuento == null ? BigDecimal.ZERO : descuento.setScale(2, RoundingMode.HALF_UP);
        t.append("--------------------------------\n");
        t.append(String.format("Subtotal:  $%s%n", cuenta.subtotal().toPlainString()));
        t.append(String.format("Descuento: $%s%n", d.toPlainString()));
        t.append(String.format("TOTAL:     $%s%n", cuenta.total(d).toPlainString()));
        t.append("Pago: ").append(metodo).append('\n');
        t.append("================================\n");
        t.append("     ¡Gracias por su visita!\n");
        return t.toString();
    }

    static BigDecimal dinero(String texto, String campo) {
        if (texto == null || texto.isBlank()) {
            return BigDecimal.ZERO.setScale(2);
        }
        try {
            return new BigDecimal(texto.trim().replace("$", "").replace(",", "")).setScale(2, RoundingMode.HALF_UP);
        } catch (NumberFormatException ex) {
            throw new IllegalArgumentException("El " + campo + " debe ser una cantidad, por ejemplo 350.00");
        }
    }

    private static int entero(String texto) {
        try {
            return Integer.parseInt(texto == null ? "" : texto.trim());
        } catch (NumberFormatException ex) {
            throw new IllegalArgumentException("La cantidad debe ser un número entero.");
        }
    }
}
