package com.kiiwik.pos.dao;

import com.kiiwik.pos.model.DetalleVenta;
import com.kiiwik.pos.model.Venta;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class VentaDAO {

    public boolean registrarVenta(Venta venta, List<DetalleVenta> detalles) {
        String sqlVenta = "INSERT INTO ventas (metodo_pago, total) VALUES (?, ?)";
        String sqlDetalle = "INSERT INTO detalle_ventas (cantidad, precio_unitario, ventas_id_venta, productos_id_productos) VALUES (?, ?, ?, ?)";
        String sqlStock = "UPDATE productos SET stock = stock - ? WHERE id_productos = ?";

        Connection conn = null;
        try {
            conn = ConexionDB.obtenerConexion();
            conn.setAutoCommit(false); // Iniciar Transacción SQL

            // 1. Insertar la cabecera de la Venta
            int idVentaGenerado = -1;
            try (PreparedStatement psV = conn.prepareStatement(sqlVenta, Statement.RETURN_GENERATED_KEYS)) {
                psV.setString(1, venta.getMetodoPago());
                psV.setDouble(2, venta.getTotal());
                psV.executeUpdate();

                ResultSet rs = psV.getGeneratedKeys();
                if (rs.next()) {
                    idVentaGenerado = rs.getInt(1);
                }
            }

            if (idVentaGenerado == -1) {
                conn.rollback();
                return false;
            }

            // 2. Insertar Detalle de Venta y Descontar Stock
            try (PreparedStatement psD = conn.prepareStatement(sqlDetalle);
                 PreparedStatement psS = conn.prepareStatement(sqlStock)) {

                for (DetalleVenta d : detalles) {
                    // Detalle
                    psD.setInt(1, d.getCantidad());
                    psD.setDouble(2, d.getPrecioUnitario());
                    psD.setInt(3, idVentaGenerado);
                    psD.setInt(4, d.getProductoId());
                    psD.addBatch();

                    // Descuento de inventario
                    psS.setInt(1, d.getCantidad());
                    psS.setInt(2, d.getProductoId());
                    psS.addBatch();
                }

                psD.executeBatch();
                psS.executeBatch();
            }

            conn.commit(); // Todo correcto, guardamos permanentemente
            return true;

        } catch (SQLException e) {
            if (conn != null) {
                try { conn.rollback(); } catch (SQLException ex) { ex.printStackTrace(); }
            }
            System.err.println("Error en transacción de venta: " + e.getMessage());
            return false;
        } finally {
            if (conn != null) {
                try { conn.setAutoCommit(true); conn.close(); } catch (SQLException e) { e.printStackTrace(); }
            }
        }
    }

    public List<Venta> obtenerVentasPorFecha(String fechaInicio, String fechaFin) {
        List<Venta> lista = new ArrayList<>();
        String sql = "SELECT * FROM ventas WHERE DATE(fecha) BETWEEN ? AND ? ORDER BY id_venta DESC";

        try (Connection conn = ConexionDB.obtenerConexion();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, fechaInicio);
            ps.setString(2, fechaFin);

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    Venta v = new Venta(
                        rs.getInt("id_venta"),
                        rs.getString("fecha"),
                        rs.getString("metodo_pago"),
                        rs.getDouble("total")
                    );
                    lista.add(v);
                }
            }
        } catch (SQLException e) {
            System.err.println("Error al consultar reporte de ventas: " + e.getMessage());
        }
        return lista;
    }

    /**
     * Obtiene todas las ventas registradas en la base de datos
     */
    public List<Venta> obtenerTodas() {
        List<Venta> lista = new ArrayList<>();
        String sql = "SELECT * FROM ventas ORDER BY id_venta DESC";

        try (Connection conn = ConexionDB.obtenerConexion();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                Venta v = new Venta(
                    rs.getInt("id_venta"),
                    rs.getString("fecha"),
                    rs.getString("metodo_pago"),
                    rs.getDouble("total")
                );
                lista.add(v);
            }
        } catch (SQLException e) {
            System.err.println("Error al consultar todas las ventas: " + e.getMessage());
        }
        return lista;
    }

    /**
     * Obtiene los productos comprados en una venta específica relacionando detalle_ventas y productos
     */
    public List<String> obtenerDetallesTexto(int idVenta) {
        List<String> detalles = new ArrayList<>();
        String sql = "SELECT p.nombre, dv.cantidad, dv.precio_unitario " +
                     "FROM detalle_ventas dv " +
                     "JOIN productos p ON dv.productos_id_productos = p.id_productos " +
                     "WHERE dv.ventas_id_venta = ?";

        try (Connection conn = ConexionDB.obtenerConexion();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, idVenta);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    String linea = String.format("• %s  x%d  ($%.2f MXN c/u)",
                            rs.getString("nombre"),
                            rs.getInt("cantidad"),
                            rs.getDouble("precio_unitario"));
                    detalles.add(linea);
                }
            }
        } catch (SQLException e) {
            System.err.println("Error al obtener detalle de la venta: " + e.getMessage());
        }
        return detalles;
    }
}