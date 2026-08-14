package com.kiiwik.pos.controller;

import com.kiiwik.pos.model.DetalleVenta;
import com.kiiwik.pos.model.Producto;
import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.ResourceBundle;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.HBox;

public class PuntoDeVentaController implements Initializable {

    @FXML private ScrollPane scrollCatalogo;
    @FXML private javafx.scene.layout.FlowPane pnlCatalogo;
    @FXML private ComboBox<String> cbCategoria;
    @FXML private ComboBox<String> cbMaterial;

    @FXML private TableView<DetalleVenta> tblCarrito;
    @FXML private TableColumn<DetalleVenta, String> colProducto;
    @FXML private TableColumn<DetalleVenta, Void> colCantidad; 
    @FXML private TableColumn<DetalleVenta, Double> colPrecio;
    @FXML private TableColumn<DetalleVenta, Double> colSubtotal;

    @FXML private Label lblTotal;
    @FXML private RadioButton rbEfectivo;
    @FXML private RadioButton rbTransferencia;
    @FXML private Button btnLimpiar;
    @FXML private Button btnConfirmarCobro;

    private final ObservableList<DetalleVenta> listaCarrito = FXCollections.observableArrayList();
    private final List<Producto> listaProductosBase = new ArrayList<>();
    private double totalPagar = 0.0;

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        configurarTabla();
        configurarFiltros();       // 1. Configuramos los ComboBoxes con materiales y categorías reales
        cargarCatalogoPrueba();    // 2. Cargamos los 36 productos de tu BD
    }

    private void configurarFiltros() {
        if (cbCategoria != null) {
            cbCategoria.setItems(FXCollections.observableArrayList(
                "Todas", "Anillo", "Arete", "Brazalete", "Cadena", "Dije", "Pulsera", "Tobillera"
            ));
            cbCategoria.setValue("Todas");
            cbCategoria.setOnAction(e -> aplicarFiltroCatalogo());
        }

        if (cbMaterial != null) {
            cbMaterial.setItems(FXCollections.observableArrayList(
                "Todos", "Acero inoxidable", "Laminado", "Plata", "Rodio"
            ));
            cbMaterial.setValue("Todos");
            cbMaterial.setOnAction(e -> aplicarFiltroCatalogo());
        }
    }

    private void aplicarFiltroCatalogo() {
        String catSel = (cbCategoria != null) ? cbCategoria.getValue() : "Todas";
        String matSel = (cbMaterial != null) ? cbMaterial.getValue() : "Todos";

        if (pnlCatalogo == null) return;

        pnlCatalogo.getChildren().clear();

        for (Producto p : listaProductosBase) {
            // Normalización para "Dije" vs "Dijes"
            boolean coincideCategoria = catSel == null || catSel.equals("Todas") 
                    || p.getCategoria().equalsIgnoreCase(catSel)
                    || (catSel.equalsIgnoreCase("Dije") && p.getCategoria().equalsIgnoreCase("Dijes"))
                    || (catSel.equalsIgnoreCase("Dijes") && p.getCategoria().equalsIgnoreCase("Dije"));

            boolean coincideMaterial = matSel == null || matSel.equals("Todos") 
                    || p.getMaterial().equalsIgnoreCase(matSel);

            if (coincideCategoria && coincideMaterial) {
                renderizarCardProducto(p);
            }
        }
    }

    private void renderizarCardProducto(Producto p) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/kiiwik/pos/views/ItemCard.fxml"));
            Parent card = loader.load();
            ItemCardController cardCtrl = loader.getController();
            cardCtrl.setData(p, this);
            pnlCatalogo.getChildren().add(card);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void configurarTabla() {
        colProducto.setCellValueFactory(new PropertyValueFactory<>("nombreProducto"));
        colPrecio.setCellValueFactory(new PropertyValueFactory<>("precioUnitario"));
        colSubtotal.setCellValueFactory(new PropertyValueFactory<>("subtotal"));

        colCantidad.setCellFactory(param -> new TableCell<>() {
            private final Button btnRestar = new Button("-");
            private final Button btnSumar = new Button("+");
            private final Label lblCant = new Label();
            private final HBox container = new HBox(5, btnRestar, lblCant, btnSumar);

            {
                container.setAlignment(Pos.CENTER);
                btnRestar.setStyle("-fx-font-weight: bold; -fx-min-width: 25px;");
                btnSumar.setStyle("-fx-font-weight: bold; -fx-min-width: 25px;");

                btnRestar.setOnAction(e -> {
                    DetalleVenta item = getTableView().getItems().get(getIndex());
                    if (item.getCantidad() > 1) {
                        item.setCantidad(item.getCantidad() - 1);
                        item.setSubtotal(item.getCantidad() * item.getPrecioUnitario());
                    } else {
                        listaCarrito.remove(item);
                    }
                    tblCarrito.refresh();
                    actualizarTotal();
                });

                btnSumar.setOnAction(e -> {
                    DetalleVenta item = getTableView().getItems().get(getIndex());
                    item.setCantidad(item.getCantidad() + 1);
                    item.setSubtotal(item.getCantidad() * item.getPrecioUnitario());
                    tblCarrito.refresh();
                    actualizarTotal();
                });
            }

            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                if (empty) {
                    setGraphic(null);
                } else {
                    DetalleVenta detalle = getTableView().getItems().get(getIndex());
                    lblCant.setText(String.valueOf(detalle.getCantidad()));
                    setGraphic(container);
                }
            }
        });

        tblCarrito.setItems(listaCarrito);
    }

    public void agregarAlCarrito(Producto prod) {
        boolean existe = false;
        for (DetalleVenta item : listaCarrito) {
            if (item.getProductoId() == prod.getId()) {
                item.setCantidad(item.getCantidad() + 1);
                item.setSubtotal(item.getCantidad() * item.getPrecioUnitario());
                existe = true;
                break;
            }
        }

        if (!existe) {
            DetalleVenta nuevoDetalle = new DetalleVenta(
                0, 
                prod.getId(), 
                prod.getNombre(), 
                1, 
                prod.getPrecio(), 
                prod.getPrecio()
            );
            listaCarrito.add(nuevoDetalle);
        }

        tblCarrito.refresh();
        actualizarTotal();
    }

    private void actualizarTotal() {
        totalPagar = 0.0;
        for (DetalleVenta item : listaCarrito) {
            totalPagar += item.getSubtotal();
        }
        lblTotal.setText(String.format("%.2f MXN", totalPagar));
    }

    @FXML
    private void handleLimpiarCarrito(ActionEvent event) {
        listaCarrito.clear();
        actualizarTotal();
    }

    @FXML
    private void handleAbrirModalCobro(ActionEvent event) {
        handleConfirmarCobro(event);
    }

    @FXML
    private void handleConfirmarCobro(ActionEvent event) {
        if (listaCarrito.isEmpty()) {
            mostrarAlerta("Carrito Vacío", "Agrega al menos un producto antes de cobrar.");
            return;
        }
        abrirModalCobro();
    }

    private void abrirModalCobro() {
        Dialog<Boolean> dialog = new Dialog<>();
        dialog.setTitle("Confirmar Cobro");
        dialog.setHeaderText("Monto Total: " + String.format("$%.2f MXN", totalPagar));

        ButtonType btnFinalizarType = new ButtonType("Finalizar Venta", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(btnFinalizarType, ButtonType.CANCEL);

        javafx.scene.layout.GridPane grid = new javafx.scene.layout.GridPane();
        grid.setHgap(10);
        grid.setVgap(10);

        TextField txtEfectivo = new TextField();
        txtEfectivo.setPromptText("0.00");
        Label lblCambioCalculado = new Label("$0.00 MXN");

        grid.add(new Label("Efectivo Recibido:"), 0, 0);
        grid.add(txtEfectivo, 1, 0);
        grid.add(new Label("Cambio:"), 0, 1);
        grid.add(lblCambioCalculado, 1, 1);

        Node btnFinalizar = dialog.getDialogPane().lookupButton(btnFinalizarType);
        btnFinalizar.setDisable(true);

        txtEfectivo.textProperty().addListener((obs, oldVal, newVal) -> {
            try {
                double efectivo = Double.parseDouble(newVal);
                double cambio = efectivo - totalPagar;
                if (cambio >= 0) {
                    lblCambioCalculado.setText(String.format("$%.2f MXN", cambio));
                    btnFinalizar.setDisable(false);
                } else {
                    lblCambioCalculado.setText("Efectivo insuficiente");
                    btnFinalizar.setDisable(true);
                }
            } catch (NumberFormatException e) {
                lblCambioCalculado.setText("$0.00 MXN");
                btnFinalizar.setDisable(true);
            }
        });

        dialog.getDialogPane().setContent(grid);

        dialog.showAndWait().ifPresent(response -> {
            mostrarAlerta("Venta Exitosa", "Venta procesada con éxito.");
            handleLimpiarCarrito(null);
        });
    }

    private void cargarCatalogoPrueba() {
        listaProductosBase.clear();
        
        // 36 productos reales cargados desde tu base de datos
        listaProductosBase.add(new Producto(1, "Aretes de acero inoxidable", "Arete", "Acero inoxidable", 55.00, 6));
        listaProductosBase.add(new Producto(2, "Aretes de Laminado", "Arete", "Laminado", 65.00, 80));
        listaProductosBase.add(new Producto(3, "Aretes de perla", "Arete", "Laminado", 130.00, 3));
        listaProductosBase.add(new Producto(4, "Aretes con zirconia", "Arete", "Plata", 150.00, 10));
        listaProductosBase.add(new Producto(5, "Aretes de gota", "Arete", "Plata", 70.00, 2));
        listaProductosBase.add(new Producto(6, "Aretes de corazón", "Arete", "Laminado", 120.00, 1));
        listaProductosBase.add(new Producto(7, "Aretes de flor", "Arete", "Rodio", 150.00, 2));
        listaProductosBase.add(new Producto(8, "Aretes de mariposa", "Arete", "Rodio", 95.00, 5));
        listaProductosBase.add(new Producto(9, "Arracadas lisas", "Arete", "Laminado", 95.00, 2));
        listaProductosBase.add(new Producto(10, "Arracadas con piedras", "Arete", "Laminado", 120.00, 1));
        listaProductosBase.add(new Producto(11, "Arracadas gruesas", "Arete", "Laminado", 120.00, 3));
        listaProductosBase.add(new Producto(12, "Anillos ajustables", "Anillo", "Rodio", 65.00, 16));
        listaProductosBase.add(new Producto(13, "Anillos con zirconia", "Anillo", "Rodio", 85.00, 10));
        listaProductosBase.add(new Producto(14, "Anillos Anti-estrès", "Anillo", "Laminado", 150.00, 5));
        listaProductosBase.add(new Producto(15, "Anillos tipo compromiso", "Anillo", "Laminado", 130.00, 6));
        listaProductosBase.add(new Producto(16, "collares gargatilla", "Cadena", "Laminado", 140.00, 2));
        listaProductosBase.add(new Producto(17, "Collares con dije", "Cadena", "Laminado", 210.00, 7));
        listaProductosBase.add(new Producto(18, "Cadenas gruesas", "Cadena", "Rodio", 250.00, 7));
        listaProductosBase.add(new Producto(19, "Cadenas delgadas", "Cadena", "Rodio", 140.00, 5));
        listaProductosBase.add(new Producto(20, "Pulseras de acero inoxidable", "Pulsera", "Acero inoxidable", 75.00, 6));
        listaProductosBase.add(new Producto(21, "Pulseras de eslabón", "Pulsera", "Rodio", 110.00, 7));
        listaProductosBase.add(new Producto(22, "Pulseras con dijes", "Pulsera", "Laminado", 110.00, 10));
        listaProductosBase.add(new Producto(23, "Tobilleras colgante", "Tobillera", "Laminado", 95.00, 6));
        listaProductosBase.add(new Producto(24, "Dijes variados", "Dijes", "Laminado", 65.00, 8));
        listaProductosBase.add(new Producto(25, "Broqueles", "Arete", "Plata", 150.00, 10));
        listaProductosBase.add(new Producto(26, "Ear cuffs", "Arete", "Laminado", 95.00, 2));
        listaProductosBase.add(new Producto(27, "Sets de aretes", "Arete", "Laminado", 120.00, 3));
        listaProductosBase.add(new Producto(28, "Juegos de collar y aretes", "Arete", "Laminado", 210.00, 5));
        listaProductosBase.add(new Producto(29, "Brazaletes", "Brazalete", "Laminado", 120.00, 3));
        listaProductosBase.add(new Producto(30, "Jugui", "Arete", "Rodio", 55.00, 60));
        listaProductosBase.add(new Producto(31, "Cadenas Pl", "Cadena", "Plata", 450.00, 4));
        listaProductosBase.add(new Producto(32, "Pulsera placa", "Pulsera", "Plata", 385.00, 3));
        listaProductosBase.add(new Producto(33, "Anillos Pl", "Anillo", "Plata", 275.00, 7));
        listaProductosBase.add(new Producto(34, "Arete largo", "Arete", "Laminado", 120.00, 7));
        listaProductosBase.add(new Producto(35, "Dije Pl", "Dije", "Plata", 100.00, 8));
        listaProductosBase.add(new Producto(36, "Esclava Hombre", "Pulsera", "Plata", 600.00, 4));

        aplicarFiltroCatalogo();
    }

    private void mostrarAlerta(String titulo, String msg) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(titulo);
        alert.setHeaderText(null);
        alert.setContentText(msg);
        alert.showAndWait();
    }

    @FXML private void handleIrInventario(ActionEvent event) throws IOException {
        cambiarVista(event, "/com/kiiwik/pos/views/Inventario.fxml");
    }

    @FXML private void handleIrReportes(ActionEvent event) throws IOException {
        cambiarVista(event, "/com/kiiwik/pos/views/Reportes.fxml");
    }

    private void cambiarVista(ActionEvent event, String fxmlPath) throws IOException {
        Parent root = FXMLLoader.load(getClass().getResource(fxmlPath));
        Scene scene = ((Node) event.getSource()).getScene();
        scene.setRoot(root);
    }
}