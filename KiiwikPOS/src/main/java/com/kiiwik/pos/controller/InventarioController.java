package com.kiiwik.pos.controller;

import com.kiiwik.pos.model.Producto;
import java.net.URL;
import java.util.Optional;
import java.util.ResourceBundle;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.geometry.Insets;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.GridPane;
import javafx.stage.Stage;

public class InventarioController implements Initializable {

    @FXML private TextField txtBuscar;
    @FXML private ComboBox<String> cmbMaterial;
    @FXML private TableView<Producto> tblInventario;
    @FXML private TableColumn<Producto, Integer> colCodigo;
    @FXML private TableColumn<Producto, String> colNombre;
    @FXML private TableColumn<Producto, String> colCategoria;
    @FXML private TableColumn<Producto, String> colMaterial;
    @FXML private TableColumn<Producto, Double> colPrecio;
    @FXML private TableColumn<Producto, Integer> colStock;

    private final ObservableList<Producto> listaProductos = FXCollections.observableArrayList();
    private FilteredList<Producto> listaFiltrada;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        configurarTabla();
        cargarDatosPrueba();
        configurarFiltros();
    }

    private void configurarTabla() {
        colCodigo.setCellValueFactory(new PropertyValueFactory<>("id"));
        colNombre.setCellValueFactory(new PropertyValueFactory<>("nombre"));
        colCategoria.setCellValueFactory(new PropertyValueFactory<>("categoria"));
        colMaterial.setCellValueFactory(new PropertyValueFactory<>("material"));
        colPrecio.setCellValueFactory(new PropertyValueFactory<>("precio"));
        colStock.setCellValueFactory(new PropertyValueFactory<>("stock"));

        listaFiltrada = new FilteredList<>(listaProductos, p -> true);
        tblInventario.setItems(listaFiltrada);
    }

    private void configurarFiltros() {
        cmbMaterial.setItems(FXCollections.observableArrayList(
            "Todos", "Acero inoxidable", "Laminado", "Plata", "Rodio"
        ));
        cmbMaterial.setValue("Todos");

        // Escuchadores de cambio para filtrado en tiempo real
        txtBuscar.textProperty().addListener((obs, oldVal, newVal) -> aplicarFiltros());
        cmbMaterial.valueProperty().addListener((obs, oldVal, newVal) -> aplicarFiltros());
    }

    private void aplicarFiltros() {
        String busqueda = txtBuscar.getText() == null ? "" : txtBuscar.getText().toLowerCase().trim();
        String materialSel = cmbMaterial.getValue() == null ? "Todos" : cmbMaterial.getValue();

        listaFiltrada.setPredicate(producto -> {
            boolean coincideTexto = busqueda.isEmpty() ||
                    producto.getNombre().toLowerCase().contains(busqueda) ||
                    String.valueOf(producto.getId()).contains(busqueda) ||
                    producto.getCategoria().toLowerCase().contains(busqueda);

            boolean coincideMaterial = materialSel.equals("Todos") ||
                    producto.getMaterial().equalsIgnoreCase(materialSel);

            return coincideTexto && coincideMaterial;
        });
    }

    @FXML
    private void handleNuevoProducto(ActionEvent event) {
        mostrarDialogoProducto(null);
    }

    @FXML
    private void handleEditarProducto(ActionEvent event) {
        Producto seleccionado = tblInventario.getSelectionModel().getSelectedItem();
        if (seleccionado == null) {
            mostrarAlerta("Sin selección", "Por favor selecciona un producto de la tabla para editar.");
            return;
        }
        mostrarDialogoProducto(seleccionado);
    }

    @FXML
    private void handleEliminarProducto(ActionEvent event) {
        Producto seleccionado = tblInventario.getSelectionModel().getSelectedItem();
        if (seleccionado == null) {
            mostrarAlerta("Sin selección", "Por favor selecciona un producto para desactivar.");
            return;
        }

        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
        confirm.setTitle("Confirmar Desactivación");
        confirm.setHeaderText("¿Estás seguro de dar de baja este producto?");
        confirm.setContentText("El producto \"" + seleccionado.getNombre() + "\" (ID: " + seleccionado.getId() + ") cambiará a estado inactivo (activo = 0).");

        Optional<ButtonType> result = confirm.showAndWait();
        if (result.isPresent() && result.get() == ButtonType.OK) {
            // Se remueve de la vista activa para simular el borrado lógico en interfaz
            listaProductos.remove(seleccionado);
            mostrarAlerta("Producto Desactivado", "El producto fue dado de baja correctamente.");
        }
    }

    private void mostrarDialogoProducto(Producto pExistente) {
        boolean esNuevo = (pExistente == null);
        Dialog<Producto> dialog = new Dialog<>();
        dialog.setTitle(esNuevo ? "Agregar Nuevo Producto" : "Editar Producto");
        dialog.setHeaderText(esNuevo ? "Ingresa los datos del nuevo producto" : "Modifica los datos del producto ID: " + pExistente.getId());

        ButtonType btnGuardarType = new ButtonType("Guardar", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(btnGuardarType, ButtonType.CANCEL);

        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(10);
        grid.setPadding(new Insets(20, 150, 10, 10));

        TextField txtNombre = new TextField(esNuevo ? "" : pExistente.getNombre());
        ComboBox<String> cbCat = new ComboBox<>(FXCollections.observableArrayList("Anillo", "Arete", "Brazalete", "Cadena", "Dije", "Pulsera", "Tobillera"));
        cbCat.setValue(esNuevo ? "Arete" : pExistente.getCategoria());

        ComboBox<String> cbMat = new ComboBox<>(FXCollections.observableArrayList("Acero inoxidable", "Laminado", "Plata", "Rodio"));
        cbMat.setValue(esNuevo ? "Laminado" : pExistente.getMaterial());

        TextField txtPrecio = new TextField(esNuevo ? "" : String.valueOf(pExistente.getPrecio()));
        TextField txtStock = new TextField(esNuevo ? "" : String.valueOf(pExistente.getStock()));

        grid.add(new Label("Nombre:"), 0, 0);
        grid.add(txtNombre, 1, 0);
        grid.add(new Label("Categoría:"), 0, 1);
        grid.add(cbCat, 1, 1);
        grid.add(new Label("Material:"), 0, 2);
        grid.add(cbMat, 1, 2);
        grid.add(new Label("Precio ($):"), 0, 3);
        grid.add(txtPrecio, 1, 3);
        grid.add(new Label("Stock:"), 0, 4);
        grid.add(txtStock, 1, 4);

        dialog.getDialogPane().setContent(grid);

        dialog.setResultConverter(dialogButton -> {
            if (dialogButton == btnGuardarType) {
                try {
                    String nombre = txtNombre.getText();
                    String cat = cbCat.getValue();
                    String mat = cbMat.getValue();
                    double precio = Double.parseDouble(txtPrecio.getText());
                    int stock = Integer.parseInt(txtStock.getText());

                    if (esNuevo) {
                        int nuevoId = listaProductos.stream().mapToInt(Producto::getId).max().orElse(0) + 1;
                        return new Producto(nuevoId, nombre, cat, mat, precio, stock);
                    } else {
                        pExistente.setNombre(nombre);
                        pExistente.setCategoria(cat);
                        pExistente.setMaterial(mat);
                        pExistente.setPrecio(precio);
                        pExistente.setStock(stock);
                        return pExistente;
                    }
                } catch (NumberFormatException e) {
                    mostrarAlerta("Error de entrada", "Precio y Stock deben ser números válidos.");
                }
            }
            return null;
        });

        Optional<Producto> res = dialog.showAndWait();
        res.ifPresent(p -> {
            if (esNuevo) {
                listaProductos.add(p);
            }
            tblInventario.refresh();
        });
    }

    private void cargarDatosPrueba() {
        listaProductos.clear();
        listaProductos.addAll(
            new Producto(1, "Aretes de acero inoxidable", "Arete", "Acero inoxidable", 55.00, 6),
            new Producto(2, "Aretes de Laminado", "Arete", "Laminado", 65.00, 80),
            new Producto(3, "Aretes de perla", "Arete", "Laminado", 130.00, 3),
            new Producto(4, "Aretes con zirconia", "Arete", "Plata", 150.00, 10),
            new Producto(5, "Aretes de gota", "Arete", "Plata", 70.00, 2),
            new Producto(6, "Aretes de corazón", "Arete", "Laminado", 120.00, 1),
            new Producto(7, "Aretes de flor", "Arete", "Rodio", 150.00, 2),
            new Producto(8, "Aretes de mariposa", "Arete", "Rodio", 95.00, 5),
            new Producto(9, "Arracadas lisas", "Arete", "Laminado", 95.00, 2),
            new Producto(10, "Arracadas con piedras", "Arete", "Laminado", 120.00, 1),
            new Producto(11, "Arracadas gruesas", "Arete", "Laminado", 120.00, 3),
            new Producto(12, "Anillos ajustables", "Anillo", "Rodio", 65.00, 16),
            new Producto(13, "Anillos con zirconia", "Anillo", "Rodio", 85.00, 10),
            new Producto(14, "Anillos Anti-estrès", "Anillo", "Laminado", 150.00, 5),
            new Producto(15, "Anillos tipo compromiso", "Anillo", "Laminado", 130.00, 6),
            new Producto(16, "collares gargatilla", "Cadena", "Laminado", 140.00, 2),
            new Producto(17, "Collares con dije", "Cadena", "Laminado", 210.00, 7),
            new Producto(18, "Cadenas gruesas", "Cadena", "Rodio", 250.00, 7),
            new Producto(19, "Cadenas delgadas", "Cadena", "Rodio", 140.00, 5),
            new Producto(20, "Pulseras de acero inoxidable", "Pulsera", "Acero inoxidable", 75.00, 6),
            new Producto(21, "Pulseras de eslabón", "Pulsera", "Rodio", 110.00, 7),
            new Producto(22, "Pulseras con dijes", "Pulsera", "Laminado", 110.00, 10),
            new Producto(23, "Tobilleras colgante", "Tobillera", "Laminado", 95.00, 6),
            new Producto(24, "Dijes variados", "Dije", "Laminado", 65.00, 8),
            new Producto(25, "Broqueles", "Arete", "Plata", 150.00, 10),
            new Producto(26, "Ear cuffs", "Arete", "Laminado", 95.00, 2),
            new Producto(27, "Sets de aretes", "Arete", "Laminado", 120.00, 3),
            new Producto(28, "Juegos de collar y aretes", "Arete", "Laminado", 210.00, 5),
            new Producto(29, "Brazaletes", "Brazalete", "Laminado", 120.00, 3),
            new Producto(30, "Jugui", "Arete", "Rodio", 55.00, 60),
            new Producto(31, "Cadenas Pl", "Cadena", "Plata", 450.00, 4),
            new Producto(32, "Pulsera placa", "Pulsera", "Plata", 385.00, 3),
            new Producto(33, "Anillos Pl", "Anillo", "Plata", 275.00, 7),
            new Producto(34, "Arete largo", "Arete", "Laminado", 120.00, 7),
            new Producto(35, "Dije Pl", "Dije", "Plata", 100.00, 8),
            new Producto(36, "Esclava Hombre", "Pulsera", "Plata", 600.00, 4)
        );
    }

    private void mostrarAlerta(String titulo, String msg) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(titulo);
        alert.setHeaderText(null);
        alert.setContentText(msg);
        alert.showAndWait();
    }

    @FXML
    private void handleIrPuntoDeVenta(ActionEvent event) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/kiiwik/pos/views/PuntoDeVenta.fxml"));
            Parent root = loader.load();
            Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
            stage.setScene(new Scene(root));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}