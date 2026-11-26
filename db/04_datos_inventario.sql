-- Productos de ejemplo para probar Inventario y Cobros.
-- Ejecutar DESPUÉS de 03_login_menu.sql. Se puede correr varias veces (no duplica por el código QR).
USE veterinaria_lima;

INSERT IGNORE INTO producto (codigo_qr, nombre, categoria, precio, stock, lote, fecha_caducidad) VALUES
('7501234567890', 'Antiparasitario 10 kg', 'Farmacia', 180.00, 11, 'AP-2611', '2027-06-30'),
('7501234500011', 'Croquetas adulto 4 kg', 'Alimento', 420.00, 25, 'CR-1102', '2027-03-15'),
('7501234500028', 'Shampoo dermatológico', 'Estética', 160.00, 4, 'SH-0907', '2027-01-20'),
('7501234500035', 'Vacuna antirrábica', 'Farmacia', 250.00, 8, 'VR-2610', DATE_ADD(CURDATE(), INTERVAL 20 DAY)),
('7501234500042', 'Collar antipulgas', 'Accesorios', 95.00, 0, 'CO-0101', NULL);
