-- =====================================================================
-- Sistema de Gestión Veterinaria "Lima" - Hito 1 (TAR05)
-- Script de creación de la base de datos (MySQL 8.0.16 o superior)
-- Equipo Lima - ITSX - Ingeniería de Software
-- =====================================================================
DROP DATABASE IF EXISTS veterinaria_lima;
CREATE DATABASE veterinaria_lima CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
USE veterinaria_lima;

-- 1) usuario: personal que inicia sesión (tabla de apoyo de seguridad)
CREATE TABLE usuario (
  id_usuario      INT NOT NULL AUTO_INCREMENT,
  nombre          VARCHAR(120) NOT NULL,
  usuario         VARCHAR(60)  NOT NULL,
  contrasena_hash VARCHAR(255) NOT NULL,
  rol             VARCHAR(20)  NOT NULL,
  activo          TINYINT(1)   NOT NULL DEFAULT 1,
  PRIMARY KEY (id_usuario),
  UNIQUE KEY uq_usuario_usuario (usuario),
  CONSTRAINT ck_usuario_rol CHECK (rol IN ('ADMINISTRADOR','VETERINARIO','TECNICO','RECEPCIONISTA','ALMACEN'))
) ENGINE=InnoDB;

-- 2) dueno
CREATE TABLE dueno (
  id_dueno INT NOT NULL AUTO_INCREMENT,
  nombre   VARCHAR(120) NOT NULL,
  telefono VARCHAR(20)  NOT NULL,
  correo   VARCHAR(120) NULL,
  PRIMARY KEY (id_dueno),
  UNIQUE KEY uq_dueno_nombre_tel (nombre, telefono)
) ENGINE=InnoDB;

-- 3) mascota (incluye el expediente clínico, relación 1 a 1)
CREATE TABLE mascota (
  id_mascota       INT NOT NULL AUTO_INCREMENT,
  id_dueno         INT NOT NULL,
  num_expediente   VARCHAR(20)  NOT NULL,
  nombre           VARCHAR(80)  NOT NULL,
  especie          VARCHAR(40)  NOT NULL,
  raza             VARCHAR(60)  NOT NULL,
  edad             DECIMAL(5,2) NOT NULL,
  peso             DECIMAL(6,2) NOT NULL,
  foto             VARCHAR(255) NULL,
  historial_medico TEXT NULL,
  vacunas          TEXT NULL,
  alergias         TEXT NULL,
  fecha_creacion   DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  estado           VARCHAR(15)  NOT NULL DEFAULT 'ACTIVA',
  PRIMARY KEY (id_mascota),
  UNIQUE KEY uq_mascota_expediente (num_expediente),
  UNIQUE KEY uq_mascota_dueno_nombre (id_dueno, nombre),
  CONSTRAINT fk_mascota_dueno FOREIGN KEY (id_dueno) REFERENCES dueno (id_dueno) ON DELETE RESTRICT ON UPDATE RESTRICT,
  CONSTRAINT ck_mascota_edad CHECK (edad >= 0),
  CONSTRAINT ck_mascota_peso CHECK (peso > 0),
  CONSTRAINT ck_mascota_estado CHECK (estado IN ('ACTIVA','BAJA'))
) ENGINE=InnoDB;

-- 4) cita (objeto principal: su estado sigue el diagrama de estados 3.2)
CREATE TABLE cita (
  id_cita       INT NOT NULL AUTO_INCREMENT,
  id_mascota    INT NOT NULL,
  id_usuario    INT NOT NULL,
  tipo_servicio VARCHAR(60) NOT NULL,
  fecha         DATE NOT NULL,
  hora          TIME NOT NULL,
  estado        VARCHAR(15) NOT NULL DEFAULT 'PROGRAMADA',
  PRIMARY KEY (id_cita),
  KEY idx_cita_fecha_hora (fecha, hora),
  CONSTRAINT fk_cita_mascota FOREIGN KEY (id_mascota) REFERENCES mascota (id_mascota) ON DELETE RESTRICT ON UPDATE RESTRICT,
  CONSTRAINT fk_cita_usuario FOREIGN KEY (id_usuario) REFERENCES usuario (id_usuario) ON DELETE RESTRICT ON UPDATE RESTRICT,
  CONSTRAINT ck_cita_estado CHECK (estado IN ('PROGRAMADA','EN_ESPERA','EN_ATENCION','ATENDIDA','COBRADA','CANCELADA','NO_ASISTIO'))
) ENGINE=InnoDB;

-- 5) diagnostico
CREATE TABLE diagnostico (
  id_diagnostico INT NOT NULL AUTO_INCREMENT,
  id_cita        INT NOT NULL,
  id_veterinario INT NOT NULL,
  fecha          DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  sintomas       TEXT NULL,
  diagnostico    TEXT NOT NULL,
  estado         VARCHAR(15) NOT NULL DEFAULT 'ABIERTO',
  PRIMARY KEY (id_diagnostico),
  CONSTRAINT fk_diag_cita FOREIGN KEY (id_cita) REFERENCES cita (id_cita) ON DELETE RESTRICT ON UPDATE RESTRICT,
  CONSTRAINT fk_diag_vet FOREIGN KEY (id_veterinario) REFERENCES usuario (id_usuario) ON DELETE RESTRICT ON UPDATE RESTRICT,
  CONSTRAINT ck_diag_estado CHECK (estado IN ('ABIERTO','CERRADO'))
) ENGINE=InnoDB;

-- 6) tratamiento (renglones de la receta médica)
CREATE TABLE tratamiento (
  id_tratamiento INT NOT NULL AUTO_INCREMENT,
  id_diagnostico INT NOT NULL,
  medicamento    VARCHAR(120) NOT NULL,
  dosis          VARCHAR(80)  NOT NULL,
  indicaciones   TEXT NULL,
  fecha_revision DATE NULL,
  PRIMARY KEY (id_tratamiento),
  CONSTRAINT fk_trat_diag FOREIGN KEY (id_diagnostico) REFERENCES diagnostico (id_diagnostico) ON DELETE RESTRICT ON UPDATE RESTRICT
) ENGINE=InnoDB;

-- 7) producto (inventario)
CREATE TABLE producto (
  id_producto    INT NOT NULL AUTO_INCREMENT,
  codigo_qr      VARCHAR(120) NOT NULL,
  nombre         VARCHAR(120) NOT NULL,
  categoria      VARCHAR(60)  NOT NULL,
  precio         DECIMAL(10,2) NOT NULL,
  stock          INT NOT NULL DEFAULT 0,
  lote           VARCHAR(60) NULL,
  fecha_caducidad DATE NULL,
  estado         VARCHAR(15) NOT NULL DEFAULT 'ACTIVO',
  PRIMARY KEY (id_producto),
  UNIQUE KEY uq_producto_qr (codigo_qr),
  CONSTRAINT ck_producto_precio CHECK (precio >= 0),
  CONSTRAINT ck_producto_stock CHECK (stock >= 0),
  CONSTRAINT ck_producto_estado CHECK (estado IN ('ACTIVO','BAJA'))
) ENGINE=InnoDB;

-- 8) venta (cobro)
CREATE TABLE venta (
  id_venta    INT NOT NULL AUTO_INCREMENT,
  id_dueno    INT NOT NULL,
  id_usuario  INT NOT NULL,
  fecha       DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  descuento   DECIMAL(10,2) NOT NULL DEFAULT 0,
  total       DECIMAL(10,2) NOT NULL,
  metodo_pago VARCHAR(20) NOT NULL,
  PRIMARY KEY (id_venta),
  CONSTRAINT fk_venta_dueno FOREIGN KEY (id_dueno) REFERENCES dueno (id_dueno) ON DELETE RESTRICT ON UPDATE RESTRICT,
  CONSTRAINT fk_venta_usuario FOREIGN KEY (id_usuario) REFERENCES usuario (id_usuario) ON DELETE RESTRICT ON UPDATE RESTRICT,
  CONSTRAINT ck_venta_desc CHECK (descuento >= 0),
  CONSTRAINT ck_venta_total CHECK (total >= 0),
  CONSTRAINT ck_venta_pago CHECK (metodo_pago IN ('EFECTIVO','TARJETA','TRANSFERENCIA','MIXTO'))
) ENGINE=InnoDB;

-- 9) detalle_venta: cada renglón cobra un producto O un servicio (cita)
CREATE TABLE detalle_venta (
  id_detalle      INT NOT NULL AUTO_INCREMENT,
  id_venta        INT NOT NULL,
  id_producto     INT NULL,
  id_cita         INT NULL,
  cantidad        DECIMAL(10,2) NOT NULL,
  precio_unitario DECIMAL(10,2) NOT NULL,
  subtotal        DECIMAL(10,2) NOT NULL,
  PRIMARY KEY (id_detalle),
  CONSTRAINT fk_det_venta FOREIGN KEY (id_venta) REFERENCES venta (id_venta) ON DELETE RESTRICT ON UPDATE RESTRICT,
  CONSTRAINT fk_det_producto FOREIGN KEY (id_producto) REFERENCES producto (id_producto) ON DELETE RESTRICT ON UPDATE RESTRICT,
  CONSTRAINT fk_det_cita FOREIGN KEY (id_cita) REFERENCES cita (id_cita) ON DELETE RESTRICT ON UPDATE RESTRICT,
  CONSTRAINT ck_det_cantidad CHECK (cantidad > 0),
  CONSTRAINT ck_det_subtotal CHECK (subtotal >= 0),
  CONSTRAINT ck_det_concepto CHECK ((id_producto IS NOT NULL AND id_cita IS NULL) OR (id_producto IS NULL AND id_cita IS NOT NULL))
) ENGINE=InnoDB;

-- 1) Módulo de Citas y Agenda
CREATE OR REPLACE VIEW vw_agenda_citas AS
SELECT
    c.id_cita,
    c.fecha,
    c.hora,
    c.tipo_servicio,
    c.estado AS estado_cita,
    m.id_mascota,
    m.num_expediente,
    m.nombre AS mascota,
    m.especie,
    m.raza,
    d.id_dueno,
    d.nombre AS dueno,
    d.telefono AS telefono_dueno,
    u.id_usuario AS id_responsable,
    u.nombre AS personal_asignado,
    u.rol AS rol_responsable
FROM cita c
INNER JOIN mascota m ON c.id_mascota = m.id_mascota
INNER JOIN dueno d ON m.id_dueno = d.id_dueno
INNER JOIN usuario u ON c.id_usuario = u.id_usuario;

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
        WHEN p.stock = 0 THEN 'SIN_STOCK'
        WHEN p.stock <= 5 THEN 'STOCK_BAJO'
        WHEN p.fecha_caducidad IS NOT NULL AND p.fecha_caducidad <= CURDATE() THEN 'CADUCADO'
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
