package com.kiiwik.pos.controller;

import com.kiiwik.pos.model.Producto;
import java.net.URL;
import java.util.Locale;
import java.util.ResourceBundle;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.Label;

public class ItemCardController implements Initializable {

    @FXML private Label lblNombreProducto;
    @FXML private Label lblDetalleProducto;
    @FXML private Label lblPrecioProducto;
    @FXML private Button btnAgregar;

    @Override
    public void initialize(URL url, ResourceBundle rb) {
    }

    public void setData(Producto producto, PuntoDeVentaController mainController) {
        this.lblNombreProducto.setText(producto.getNombre());
        this.lblDetalleProducto.setText(producto.getMaterial());
        
        // Se fuerza el uso de punto decimal en el precio
        this.lblPrecioProducto.setText(String.format(Locale.US, "%.2f MXN", producto.getPrecio()));

        this.btnAgregar.setOnAction(e -> {
            mainController.agregarAlCarrito(producto);
        });
    }
}