package com.kiiwik.pos.controller;

import com.kiiwik.pos.model.Producto;
import java.net.URL;
import java.util.ResourceBundle;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.geometry.Insets;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonBar;
import javafx.scene.control.ButtonType;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Dialog;
import javafx.scene.control.Label;
import javafx.scene.control.RadioButton;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.control.ToggleGroup;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.GridPane;
import javafx.stage.Stage;

public class PuntoDeVentaController implements Initializable {

    // Componentes FXML
    @FXML private ComboBox<String> cmbCategoria;
    @FXML private ComboBox<String> cmbMaterial;
    @FXML private TableView<Producto> tblCarrito;
    @FXML private TableColumn<Producto, String> colProducto;
    @FXML private TableColumn<Producto, Integer> colCantidad;
    @FXML private TableColumn<Producto, Double> colPrecio;
    @FXML private TableColumn<Producto, Double> colSubtotal;
    
    @FXML private Label lblTotal;
    @FXML private RadioButton rbEfectivo;
    @FXML private RadioButton rbTransferencia;
    @FXML private Button btnLimpiar;
    @FXML private Button btnCobrar;

    private ToggleGroup tgMetodoPago;
    private ObservableList<Producto> listaCarrito = FXCollections.observableArrayList();
    private double totalAcumulado = 0.0;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        // Configurar ToggleGroup en Java para evitar fallas en el FXML
        tgMetodoPago = new ToggleGroup();
        rbEfectivo.setToggleGroup(tgMetodoPago);
        rbTransferencia.setToggleGroup(tgMetodoPago);

        // Configurar columnas de la tabla del carrito
        colProducto.setCellValueFactory(new PropertyValueFactory<>("nombre"));
        colCantidad.setCellValueFactory(new PropertyValueFactory<>("stock"));
        colPrecio.setCellValueFactory(new PropertyValueFactory<>("precio"));
        colSubtotal.setCellValueFactory(new PropertyValueFactory<>("precio"));

        tblCarrito.setItems(listaCarrito);
    }

    // --- NAVEGACIÓN ENTRE VENTANAS ---
    @FXML
    private void handleIrInventario(ActionEvent event) {
        cargarVentana("/com/kiiwik/pos/views/Inventario.fxml", event);
    }

    @FXML
    private void handleIrReportes(ActionEvent event) {
        cargarVentana("/com/kiiwik/pos/views/Reportes.fxml", event);
    }

    private void cargarVentana(String rutaFXML, ActionEvent event) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource(rutaFXML));
            Parent root = loader.load();
            Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
            stage.setScene(new Scene(root));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // --- MODAL FLOTANTE DE COBRO ---
    @FXML
    private void handleAbrirModalCobro(ActionEvent event) {
        Dialog<ButtonType> dialog = new Dialog<>();
        dialog.setTitle("Confirmar Cobro");
        dialog.setHeaderText("Monto Total: $" + String.format("%.2f", totalAcumulado) + " MXN");

        ButtonType btnFinalizar = new ButtonType("✔ Finalizar Venta", ButtonBar.ButtonData.OK_DONE);
        ButtonType btnCancelar = new ButtonType("Cancelar", ButtonBar.ButtonData.CANCEL_CLOSE);
        dialog.getDialogPane().getButtonTypes().addAll(btnFinalizar, btnCancelar);

        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(10);
        grid.setPadding(new Insets(20, 150, 10, 10));

        TextField txtEfectivo = new TextField();
        txtEfectivo.setPromptText("0.00");
        Label lblCambio = new Label("$0.00 MXN");

        txtEfectivo.textProperty().addListener((observable, oldValue, newValue) -> {
            try {
                double efectivo = Double.parseDouble(newValue);
                double cambio = efectivo - totalAcumulado;
                lblCambio.setText(String.format("$%.2f MXN", Math.max(0, cambio)));
            } catch (NumberFormatException e) {
                lblCambio.setText("$0.00 MXN");
            }
        });

        grid.add(new Label("Efectivo Recibido:"), 0, 0);
        grid.add(txtEfectivo, 1, 0);
        grid.add(new Label("Cambio:"), 0, 1);
        grid.add(lblCambio, 1, 1);

        dialog.getDialogPane().setContent(grid);

        dialog.showAndWait().ifPresent(response -> {
            if (response == btnFinalizar) {
                System.out.println("Venta procesada con éxito.");
                limpiarCarrito();
            }
        });
    }

    @FXML
    private void handleLimpiarCarrito(ActionEvent event) {
        limpiarCarrito();
    }

    private void limpiarCarrito() {
        listaCarrito.clear();
        totalAcumulado = 0.0;
        lblTotal.setText("$0.00 MXN");
    }
}