CREATE DATABASE IF NOT EXISTS kiiwik_db;
USE kiiwik_db;

-- 1. Eliminar y recrear las tablas para reiniciar estructurado desde cero
DROP TABLE IF EXISTS detalle_ventas;
DROP TABLE IF EXISTS ventas;
DROP TABLE IF EXISTS productos;

-- 2. Creación de tablas
CREATE TABLE productos (
    id_productos INT AUTO_INCREMENT PRIMARY KEY,
    nombre VARCHAR(60) NOT NULL,
    categoria VARCHAR(30) NOT NULL,
    material VARCHAR(30) NOT NULL,
    precio_venta DECIMAL(10,2) NOT NULL,
    stock INT NOT NULL,
    activo TINYINT(1) DEFAULT 1
);

CREATE TABLE ventas (
    id_venta INT AUTO_INCREMENT PRIMARY KEY,
    fecha DATETIME DEFAULT CURRENT_TIMESTAMP,
    metodo_pago VARCHAR(20) NOT NULL,
    total DECIMAL(10,2) NOT NULL
);

CREATE TABLE detalle_ventas (
    id_detalle INT AUTO_INCREMENT PRIMARY KEY,
    cantidad INT NOT NULL,
    precio_unitario DECIMAL(10,2) NOT NULL,
    ventas_id_venta INT NOT NULL,
    productos_id_productos INT NOT NULL,
    FOREIGN KEY (ventas_id_venta) REFERENCES ventas(id_venta),
    FOREIGN KEY (productos_id_productos) REFERENCES productos(id_productos)
);

-- 3. Cargar los 36 productos del inventario real
INSERT INTO productos (nombre, categoria, material, precio_venta, stock, activo) VALUES
('Aretes de acero inoxidable', 'Arete', 'Acero inoxidable', 55.00, 6, 1),
('Aretes de Laminado', 'Arete', 'Laminado', 65.00, 80, 1),
('Aretes de perla', 'Arete', 'Laminado', 130.00, 3, 1),
('Aretes con zirconia', 'Arete', 'Plata', 150.00, 10, 1),
('Aretes de gota', 'Arete', 'Plata', 70.00, 2, 1),
('Aretes de corazón', 'Arete', 'Laminado', 120.00, 1, 1),
('Aretes de flor', 'Arete', 'Rodio', 150.00, 2, 1),
('Aretes de mariposa', 'Arete', 'Rodio', 95.00, 5, 1),
('Arracadas lisas', 'Arete', 'Laminado', 95.00, 2, 1),
('Arracadas con piedras', 'Arete', 'Laminado', 120.00, 1, 1),
('Arracadas gruesas', 'Arete', 'Laminado', 120.00, 3, 1),
('Anillos ajustables', 'Anillo', 'Rodio', 65.00, 16, 1),
('Anillos con zirconia', 'Anillo', 'Rodio', 85.00, 10, 1),
('Anillos Anti-estrès', 'Anillo', 'Laminado', 150.00, 5, 1),
('Anillos tipo compromiso', 'Anillo', 'Laminado', 130.00, 6, 1),
('collares gargatilla', 'Cadena', 'Laminado', 140.00, 2, 1),
('Collares con dije', 'Cadena', 'Laminado', 210.00, 7, 1),
('Cadenas gruesas', 'Cadena', 'Rodio', 250.00, 7, 1),
('Cadenas delgadas', 'Cadena', 'Rodio', 140.00, 5, 1),
('Pulseras de acero inoxidable', 'Pulsera', 'Acero inoxidable', 75.00, 6, 1),
('Pulseras de eslabón', 'Pulsera', 'Rodio', 110.00, 7, 1),
('Pulseras con dijes', 'Pulsera', 'Laminado', 110.00, 10, 1),
('Tobilleras colgante', 'Tobillera', 'Laminado', 95.00, 6, 1),
('Dijes variados', 'Dijes', 'Laminado', 65.00, 8, 1),
('Broqueles', 'Arete', 'Plata', 150.00, 10, 1),
('Ear cuffs', 'Arete', 'Laminado', 95.00, 2, 1),
('Sets de aretes', 'Arete', 'Laminado', 120.00, 3, 1),
('Juegos de collar y aretes', 'Arete', 'Laminado', 210.00, 5, 1),
('Brazaletes', 'Brazalete', 'Laminado', 120.00, 3, 1),
('Jugui', 'Arete', 'Rodio', 55.00, 60, 1),
('Cadenas Pl', 'Cadena', 'Plata', 450.00, 4, 1),
('Pulsera placa', 'Pulsera', 'Plata', 385.00, 3, 1),
('Anillos Pl', 'Anillo', 'Plata', 275.00, 7, 1),
('Arete largo', 'Arete', 'Laminado', 120.00, 7, 1),
('Dije Pl', 'Dije', 'Plata', 100.00, 8, 1),
('Esclava Hombre', 'Pulsera', 'Plata', 600.00, 4, 1);

-- 4. Verificación
SELECT * FROM productos;