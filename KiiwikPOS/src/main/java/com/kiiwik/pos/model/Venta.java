package com.kiiwik.pos.model;

public class Venta {

    private int idVenta;
    private String fecha;
    private String metodoPago;
    private double total;

    // Constructor vacío
    public Venta() {
    }

    // Constructor con parámetros
    public Venta(int idVenta, String fecha, String metodoPago, double total) {
        this.idVenta = idVenta;
        this.fecha = fecha;
        this.metodoPago = metodoPago;
        this.total = total;
    }

    // Getters y Setters
    public int getIdVenta() {
        return idVenta;
    }

    public void setIdVenta(int idVenta) {
        this.idVenta = idVenta;
    }

    public String getFecha() {
        return fecha;
    }

    public void setFecha(String fecha) {
        this.fecha = fecha;
    }

    public String getMetodoPago() {
        return metodoPago;
    }

    public void setMetodoPago(String metodoPago) {
        this.metodoPago = metodoPago;
    }

    public double getTotal() {
        return total;
    }

    public void setTotal(double total) {
        this.total = total;
    }
}