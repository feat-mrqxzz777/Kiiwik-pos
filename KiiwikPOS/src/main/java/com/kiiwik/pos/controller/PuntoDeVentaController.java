package com.kiiwik.pos.controller;

import com.kiiwik.pos.dao.ProductoDAO;
import com.kiiwik.pos.dao.VentaDAO;
import com.kiiwik.pos.model.DetalleVenta;
import com.kiiwik.pos.model.Producto;
import com.kiiwik.pos.model.Venta;
import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
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

    // Instancias de DAOs para MySQL
    private final ProductoDAO productoDAO = new ProductoDAO();
    private final VentaDAO ventaDAO = new VentaDAO();

    private final ObservableList<DetalleVenta> listaCarrito = FXCollections.observableArrayList();
    private final List<Producto> listaProductosBase = new ArrayList<>();
    private double totalPagar = 0.0;

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        configurarTabla();
        configurarFiltros();
        cargarCatalogoDesdeBD(); // Carga real desde MySQL
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

    /**
     * Consulta todos los productos activos directamente desde la Base de Datos MySQL
     */
    public void cargarCatalogoDesdeBD() {
        listaProductosBase.clear();
        listaProductosBase.addAll(productoDAO.obtenerTodos());
        aplicarFiltroCatalogo();
    }

    private void aplicarFiltroCatalogo() {
        String catSel = (cbCategoria != null) ? cbCategoria.getValue() : "Todas";
        String matSel = (cbMaterial != null) ? cbMaterial.getValue() : "Todos";

        if (pnlCatalogo == null) return;

        pnlCatalogo.getChildren().clear();

        for (Producto p : listaProductosBase) {
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
                    int stockDisponible = obtenerStockProducto(item.getProductoId());

                    if (item.getCantidad() + 1 > stockDisponible) {
                        mostrarAlerta("Límite de Stock Alcanzado", 
                            "No puedes agregar más de " + stockDisponible + " unidad(es) de " + item.getNombreProducto() + ".");
                        return;
                    }

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

                    int stockDisponible = obtenerStockProducto(detalle.getProductoId());
                    btnSumar.setDisable(detalle.getCantidad() >= stockDisponible);

                    setGraphic(container);
                }
            }
        });

        tblCarrito.setItems(listaCarrito);
    }

    /**
     * Busca el stock actual en memoria del producto dado su ID
     */
    private int obtenerStockProducto(int productoId) {
        for (Producto p : listaProductosBase) {
            if (p.getId() == productoId) {
                return p.getStock();
            }
        }
        return 0;
    }

    public void agregarAlCarrito(Producto prod) {
        if (prod.getStock() <= 0) {
            mostrarAlerta("Sin Stock", "El producto \"" + prod.getNombre() + "\" no tiene unidades disponibles.");
            return;
        }

        // Advertencia preventiva para stock crítico (3 o menos unidades restantes)
        if (prod.getStock() <= 3) {
            mostrarAlerta("Advertencia de Stock Bajo", 
                "Quedan pocas unidades disponibles de \"" + prod.getNombre() + "\" (" + prod.getStock() + " piezas en stock).");
        }

        boolean existe = false;
        for (DetalleVenta item : listaCarrito) {
            if (item.getProductoId() == prod.getId()) {
                if (item.getCantidad() + 1 > prod.getStock()) {
                    mostrarAlerta("Límite de Stock", "No puedes agregar más unidades que las disponibles en inventario (" + prod.getStock() + ").");
                    return;
                }
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
        // Se fuerza el formato con Locale.US
        lblTotal.setText(String.format(Locale.US, "%.2f MXN", totalPagar));
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
        // Se fuerza el formato con Locale.US
        dialog.setHeaderText("Monto Total: " + String.format(Locale.US, "$%.2f MXN", totalPagar));

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

        // Determinación del método de pago elegido
        String metodoPago = (rbTransferencia != null && rbTransferencia.isSelected()) ? "Transferencia" : "Efectivo";

        if (metodoPago.equals("Transferencia")) {
            txtEfectivo.setText(String.format(Locale.US, "%.2f", totalPagar));
            txtEfectivo.setDisable(true);
            lblCambioCalculado.setText("$0.00 MXN");
            btnFinalizar.setDisable(false);
        } else {
            btnFinalizar.setDisable(true);
            txtEfectivo.textProperty().addListener((obs, oldVal, newVal) -> {
                try {
                    double efectivo = Double.parseDouble(newVal);
                    double cambio = efectivo - totalPagar;
                    if (cambio >= 0) {
                        // Se fuerza el formato con Locale.US
                        lblCambioCalculado.setText(String.format(Locale.US, "$%.2f MXN", cambio));
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
        }

        dialog.getDialogPane().setContent(grid);

        dialog.setResultConverter(dialogButton -> dialogButton == btnFinalizarType);

        dialog.showAndWait().ifPresent(confirmado -> {
            if (confirmado) {
                // 1. Crear el objeto Venta
                Venta venta = new Venta(0, null, metodoPago, totalPagar);

                // 2. Ejecutar Transacción SQL en MySQL via VentaDAO
                List<DetalleVenta> detalles = new ArrayList<>(listaCarrito);
                boolean exito = ventaDAO.registrarVenta(venta, detalles);

                if (exito) {
                    mostrarAlerta("Venta Exitosa", "Venta registrada con éxito y stock actualizado.");
                    handleLimpiarCarrito(null);
                    cargarCatalogoDesdeBD(); // Recarga catálogo para refrescar los nuevos niveles de stock
                } else {
                    mostrarAlerta("Error de Transacción", "No se pudo procesar la venta en la base de datos.");
                }
            }
        });
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