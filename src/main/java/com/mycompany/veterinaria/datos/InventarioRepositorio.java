package com.mycompany.veterinaria.datos;

import java.util.List;
import java.util.Optional;
import com.mycompany.veterinaria.modelo.Producto;

/** Acceso a datos del inventario. */
public interface InventarioRepositorio {

    /** Productos activos cuyo nombre, categoría o código contengan el texto (vacío = todos). */
    List<Producto> listar(String filtro);

    Optional<Producto> buscarPorCodigo(String codigoQr);

    boolean existeCodigo(String codigoQr, int ignorarId);

    Producto registrar(Producto p);

    void actualizar(Producto p);

    /** Entrada de mercancía: suma piezas al stock. */
    void sumarStock(int idProducto, int cantidad);

    /** Baja lógica: el producto queda en estado BAJA y ya no se vende. */
    void darDeBaja(int idProducto);
}
