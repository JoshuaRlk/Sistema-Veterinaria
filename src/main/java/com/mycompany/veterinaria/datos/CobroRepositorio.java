package com.mycompany.veterinaria.datos;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import com.mycompany.veterinaria.modelo.CitaClinica;
import com.mycompany.veterinaria.modelo.Concepto;
import com.mycompany.veterinaria.modelo.Dueno;
import com.mycompany.veterinaria.modelo.Producto;

/** Acceso a datos de Cobros. */
public interface CobroRepositorio {

    List<Dueno> buscarDuenos(String texto);

    /** Citas ATENDIDAS de las mascotas del dueño que aún no se cobran. */
    List<CitaClinica> serviciosPorCobrar(int idDueno);

    Optional<Producto> productoPorCodigo(String codigo);

    /**
     * Registra la venta y su detalle, descuenta el stock y marca las citas como COBRADAS en una sola transacción.
     * Si algún producto ya no tiene stock suficiente, no se guarda nada.
     * @return id de la venta
     */
    int registrarVenta(int idDueno, int idUsuario, BigDecimal descuento, BigDecimal total, String metodoPago,
            List<Concepto> conceptos);
}
