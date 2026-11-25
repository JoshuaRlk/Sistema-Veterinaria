-- =====================================================================
-- Login y menú principal (después del Hito 2)
-- Ejecutar DESPUÉS de veterinaria_schema.sql y datos_prueba.sql.
-- Se puede correr varias veces sin duplicar nada.
-- =====================================================================
USE veterinaria_lima;

-- Vistas de los módulos (por si la base se creó sin ellas).
-- En vw_inventario_alertas, CADUCADO se revisa antes que el stock: un producto caducado
-- con stock bajo debe avisar que está caducado.
-- 2)Módulo Clínico: Expediente, Diagnósticos y Recetas
CREATE OR REPLACE VIEW vw_historial_clinico_citas AS
SELECT
    m.id_mascota,
    m.num_expediente,
    m.nombre AS mascota,
    m.especie,
    m.raza,
    m.alergias,
    m.vacunas,
    m.historial_medico,
    c.id_cita,
    c.fecha AS fecha_cita,
    c.tipo_servicio,
    c.estado AS estado_cita,
    diag.id_diagnostico,
    diag.fecha AS fecha_diagnostico,
    diag.sintomas,
    diag.diagnostico,
    diag.estado AS estado_diagnostico,
    vet.nombre AS veterinario,
    trat.id_tratamiento,
    trat.medicamento,
    trat.dosis,
    trat.indicaciones,
    trat.fecha_revision
FROM mascota m
INNER JOIN cita c ON m.id_mascota = c.id_mascota
LEFT JOIN diagnostico diag ON c.id_cita = diag.id_cita
LEFT JOIN usuario vet ON diag.id_veterinario = vet.id_usuario
LEFT JOIN tratamiento trat ON diag.id_diagnostico = trat.id_diagnostico;

-- 3) Módulo de Inventario y Alertas
CREATE OR REPLACE VIEW vw_inventario_alertas AS
SELECT
    p.id_producto,
    p.codigo_qr,
    p.nombre,
    p.categoria,
    p.lote,
    p.fecha_caducidad,
    p.precio,
    p.stock,
    p.estado,
    (p.precio * p.stock) AS valor_inventario,
    CASE
        WHEN p.estado = 'BAJA' THEN 'INACTIVO'
        WHEN p.fecha_caducidad IS NOT NULL AND p.fecha_caducidad <= CURDATE() THEN 'CADUCADO'
        WHEN p.stock = 0 THEN 'SIN_STOCK'
        WHEN p.stock <= 5 THEN 'STOCK_BAJO'
        WHEN p.fecha_caducidad IS NOT NULL AND p.fecha_caducidad <= DATE_ADD(CURDATE(), INTERVAL 30 DAY) THEN 'POR_CADUCAR'
        ELSE 'NORMAL'
    END AS alerta_inventario
FROM producto p;

-- 4) Módulo de Ventas y Cobros
CREATE OR REPLACE VIEW vw_resumen_ventas_detalle AS
SELECT
    v.id_venta,
    v.fecha AS fecha_venta,
    v.metodo_pago,
    v.descuento,
    v.total AS total_venta,
    d.id_dueno,
    d.nombre AS dueno,
    u.id_usuario,
    u.nombre AS atendido_por,
    dv.id_detalle,
    CASE
        WHEN dv.id_producto IS NOT NULL THEN 'PRODUCTO'
        WHEN dv.id_cita IS NOT NULL THEN 'SERVICIO_CITA'
    END AS tipo_concepto,
    COALESCE(p.nombre, c.tipo_servicio) AS concepto,
    p.codigo_qr,
    c.id_cita,
    dv.cantidad,
    dv.precio_unitario,
    dv.subtotal
FROM venta v
INNER JOIN dueno d ON v.id_dueno = d.id_dueno
INNER JOIN usuario u ON v.id_usuario = u.id_usuario
INNER JOIN detalle_venta dv ON v.id_venta = dv.id_venta
LEFT JOIN producto p ON dv.id_producto = p.id_producto
LEFT JOIN cita c ON dv.id_cita = c.id_cita;

-- Usuarios de PRUEBA con contraseña cifrada (PBKDF2). Cambiarlas antes de usar el sistema en serio.
--   usuario       contraseña      rol
--   admin         Admin2026       ADMINISTRADOR
--   veterinario   Vet2026         VETERINARIO
--   recepcion     Recepcion2026   RECEPCIONISTA
--   almacen       Almacen2026     ALMACEN
INSERT INTO usuario (nombre, usuario, contrasena_hash, rol) VALUES
('Administrador General', 'admin', 'pbkdf2$65536$T1M79q2b86Q/0H3Amg3cAg==$4o8gLHO80K8L2RnBqjGxSy6vhUnrS/CRQgBaJZAl3RA=', 'ADMINISTRADOR'),
('Veterinario', 'veterinario', 'pbkdf2$65536$mFmSklZDptvYHcRiZWJeeA==$z3C+aGhDWIyZcSUMVLF6/GaQUHXHPq5vizWp9vzXFAM=', 'VETERINARIO'),
('Recepción', 'recepcion', 'pbkdf2$65536$ZLBMCPC602XBNdNu3ymMfw==$dr2tqFFkCCbv55/OZj+elHXGYSw0bAe393elrD0oIFY=', 'RECEPCIONISTA'),
('Almacén', 'almacen', 'pbkdf2$65536$a+ar/G4eeoYGQe2pwWG8Fg==$JkkHTb2+oHwMXc9DK5nvHrR3DRQuV752EvuGyeFACek=', 'ALMACEN')
ON DUPLICATE KEY UPDATE contrasena_hash = VALUES(contrasena_hash), rol = VALUES(rol), activo = 1;
