package com.mycompany.veterinaria.servicios;

import com.mycompany.veterinaria.datos.InventarioRepositorio;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.time.format.ResolverStyle;
import java.util.List;
import com.mycompany.veterinaria.modelo.Producto;
import com.mycompany.veterinaria.seguridad.Rol;

/** Reglas del inventario: alta, edición, entradas de mercancía y baja lógica. */
public class InventarioService {

    private static final DateTimeFormatter FECHA = DateTimeFormatter.ofPattern("d/M/uuuu")
            .withResolverStyle(ResolverStyle.STRICT);

    private final InventarioRepositorio repositorio;

    public InventarioService(InventarioRepositorio repositorio) {
        this.repositorio = repositorio;
    }

    /** Solo almacén y administrador modifican el inventario. */
    public static boolean puedeModificar(Rol rol) {
        return rol == Rol.ALMACEN || rol == Rol.ADMINISTRADOR;
    }

    public List<Producto> listar(String filtro) {
        return repositorio.listar(filtro);
    }

    public Producto registrar(Rol rol, String codigo, String nombre, String categoria, String precio, String stock,
            String lote, String caducidad) {
        exigirPermiso(rol);
        Producto p = construir(0, codigo, nombre, categoria, precio, stock, lote, caducidad);
        if (repositorio.existeCodigo(p.getCodigoQr(), 0)) {
            throw new IllegalArgumentException("Ya existe un producto con el código " + p.getCodigoQr() + ".");
        }
        return repositorio.registrar(p);
    }

    public Producto editar(Rol rol, int id, String codigo, String nombre, String categoria, String precio, String stock,
            String lote, String caducidad) {
        exigirPermiso(rol);
        Producto p = construir(id, codigo, nombre, categoria, precio, stock, lote, caducidad);
        if (repositorio.existeCodigo(p.getCodigoQr(), id)) {
            throw new IllegalArgumentException("Ya existe otro producto con el código " + p.getCodigoQr() + ".");
        }
        repositorio.actualizar(p);
        return p;
    }

    public void registrarEntrada(Rol rol, int idProducto, String cantidad) {
        exigirPermiso(rol);
        int n = entero(cantidad, "cantidad");
        if (n <= 0) {
            throw new IllegalArgumentException("La cantidad de la entrada debe ser mayor a 0.");
        }
        repositorio.sumarStock(idProducto, n);
    }

    public void darDeBaja(Rol rol, int idProducto) {
        exigirPermiso(rol);
        repositorio.darDeBaja(idProducto);
    }

    private static Producto construir(int id, String codigo, String nombre, String categoria, String precio,
            String stock, String lote, String caducidad) {
        if (vacio(codigo) || vacio(nombre) || vacio(categoria) || vacio(precio) || vacio(stock)) {
            throw new IllegalArgumentException("Código, nombre, categoría, precio y stock son obligatorios.");
        }
        BigDecimal pr;
        try {
            pr = new BigDecimal(precio.trim().replace("$", "").replace(",", "")).setScale(2, RoundingMode.HALF_UP);
        } catch (NumberFormatException ex) {
            throw new IllegalArgumentException("El precio debe ser un número, por ejemplo 180.00");
        }
        if (pr.signum() < 0) {
            throw new IllegalArgumentException("El precio no puede ser negativo.");
        }
        int st = entero(stock, "stock");
        if (st < 0) {
            throw new IllegalArgumentException("El stock no puede ser negativo.");
        }
        LocalDate cad = null;
        if (!vacio(caducidad)) {
            try {
                cad = LocalDate.parse(caducidad.trim(), FECHA);
            } catch (DateTimeParseException ex) {
                throw new IllegalArgumentException("La caducidad debe tener el formato dd/mm/aaaa.");
            }
        }
        return new Producto(id, codigo.trim(), nombre.trim(), categoria.trim(), pr, st,
                vacio(lote) ? null : lote.trim(), cad, Producto.ACTIVO, null);
    }

    private static int entero(String texto, String campo) {
        try {
            return Integer.parseInt(texto.trim());
        } catch (NumberFormatException | NullPointerException ex) {
            throw new IllegalArgumentException("El campo " + campo + " debe ser un número entero.");
        }
    }

    private static void exigirPermiso(Rol rol) {
        if (!puedeModificar(rol)) {
            throw new IllegalStateException("Solo almacén o administrador pueden modificar el inventario.");
        }
    }

    private static boolean vacio(String s) {
        return s == null || s.isBlank();
    }
}
