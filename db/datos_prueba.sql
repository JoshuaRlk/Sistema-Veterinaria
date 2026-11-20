-- Datos mínimos para probar el flujo principal (Happy Path) del módulo de citas.
-- Ejecutar DESPUÉS de veterinaria_schema.sql
-- Los nombres de usuario coinciden con el combo "Encargado" de la ventana de citas.
USE veterinaria_lima;

-- Usuario de MySQL que usa la aplicación (solo para desarrollo; permisos limitados a esta base)
CREATE USER IF NOT EXISTS 'vet_app'@'localhost' IDENTIFIED BY 'VetLima2026';
GRANT SELECT, INSERT, UPDATE ON veterinaria_lima.* TO 'vet_app'@'localhost';

INSERT INTO usuario (nombre, usuario, contrasena_hash, rol) VALUES
('Veterinario', 'veterinario', 'pendiente_bcrypt', 'VETERINARIO'),
('Bañador',     'banador',     'pendiente_bcrypt', 'TECNICO'),
('Estilista',   'estilista',   'pendiente_bcrypt', 'TECNICO');

INSERT INTO dueno (nombre, telefono, correo) VALUES
('Pedro Hernandez', '2281234567', 'pedro@correo.com'),
('Maria Lopez',     '2287654321', NULL);

INSERT INTO mascota (id_dueno, num_expediente, nombre, especie, raza, edad, peso) VALUES
(1, 'EXP-0001', 'Firulais', 'Perro', 'Mestizo', 3, 12.5),
(2, 'EXP-0002', 'Michi',    'Gato',  'Siames',  2, 4.2);
