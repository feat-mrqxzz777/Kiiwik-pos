package com.kiiwik.pos.dao;

import com.kiiwik.pos.model.Producto;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class ProductoDAO {

    public List<Producto> obtenerTodos() {
        List<Producto> lista = new ArrayList<>();
        String sql = "SELECT * FROM productos WHERE activo = 1";

        try (Connection conn = ConexionDB.obtenerConexion();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                Producto p = new Producto(
                    rs.getInt("id_productos"),
                    rs.getString("nombre"),
                    rs.getString("categoria"),
                    rs.getString("material"),
                    rs.getDouble("precio_venta"),
                    rs.getInt("stock")
                );
                lista.add(p);
            }
        } catch (SQLException e) {
            System.err.println("Error al consultar productos: " + e.getMessage());
        }
        return lista;
    }

    public boolean guardar(Producto p) {
        String sql = "INSERT INTO productos (nombre, categoria, material, precio_venta, stock, activo) VALUES (?, ?, ?, ?, ?, 1)";

        try (Connection conn = ConexionDB.obtenerConexion();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, p.getNombre());
            ps.setString(2, p.getCategoria());
            ps.setString(3, p.getMaterial());
            ps.setDouble(4, p.getPrecio());
            ps.setInt(5, p.getStock());

            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("Error al guardar producto: " + e.getMessage());
            return false;
        }
    }

    public boolean actualizar(Producto p) {
        String sql = "UPDATE productos SET nombre = ?, categoria = ?, material = ?, precio_venta = ?, stock = ? WHERE id_productos = ?";

        try (Connection conn = ConexionDB.obtenerConexion();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, p.getNombre());
            ps.setString(2, p.getCategoria());
            ps.setString(3, p.getMaterial());
            ps.setDouble(4, p.getPrecio());
            ps.setInt(5, p.getStock());
            ps.setInt(6, p.getId());

            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("Error al actualizar producto: " + e.getMessage());
            return false;
        }
    }

    public boolean eliminarLogico(int idProducto) {
        String sql = "UPDATE productos SET activo = 0 WHERE id_productos = ?";

        try (Connection conn = ConexionDB.obtenerConexion();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, idProducto);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("Error al eliminar producto: " + e.getMessage());
            return false;
        }
    }
}