CREATE DATABASE IF NOT EXISTS kiiwik_db;
USE kiiwik_db;

-- 1. Tabla productos
CREATE TABLE IF NOT EXISTS productos (
    id_productos INT AUTO_INCREMENT PRIMARY KEY,
    nombre VARCHAR(60) NOT NULL,
    categoria VARCHAR(30) NOT NULL,
    material VARCHAR(30) NOT NULL,
    precio_venta DECIMAL(10,2) NOT NULL,
    stock INT NOT NULL,
    activo TINYINT(1) DEFAULT 1
);

-- 2. Tabla ventas
CREATE TABLE IF NOT EXISTS ventas (
    id_venta INT AUTO_INCREMENT PRIMARY KEY,
    fecha DATETIME DEFAULT CURRENT_TIMESTAMP,
    metodo_pago VARCHAR(20) NOT NULL,
    total DECIMAL(10,2) NOT NULL
);

-- 3. Tabla detalle_ventas
CREATE TABLE IF NOT EXISTS detalle_ventas (
    id_detalle INT AUTO_INCREMENT PRIMARY KEY,
    cantidad INT NOT NULL,
    precio_unitario DECIMAL(10,2) NOT NULL,
    ventas_id_venta INT NOT NULL,
    productos_id_productos INT NOT NULL,
    FOREIGN KEY (ventas_id_venta) REFERENCES ventas(id_venta),
    FOREIGN KEY (productos_id_productos) REFERENCES productos(id_productos)
);

-- Insertar productos de prueba
INSERT INTO productos (nombre, categoria, material, precio_venta, stock, activo) VALUES
('Anillo Solitario con Circonia', 'Anillo', 'Plata .925', 380.00, 12, 1),
('Cadena Tejido Veneciano 45cm', 'Cadena', 'Plata .925', 450.00, 8, 1),
('Pulsera de Eslabones', 'Pulsera', 'Rodio', 290.00, 15, 1),
('Aretes Broquel de Mariposa', 'Aretes', 'Acero Inoxidable', 150.00, 20, 1),
('Dije de Cruz Calada', 'Dije', 'Rodio', 210.00, 6, 1);
