package com.kiiwik.pos.controller;

import com.kiiwik.pos.dao.VentaDAO;
import com.kiiwik.pos.model.Venta;
import java.net.URL;
import java.time.LocalDate;
import java.util.List;
import java.util.Locale;
import java.util.ResourceBundle;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.DatePicker;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.stage.Stage;

public class ReportesController implements Initializable {

    @FXML private DatePicker dpInicio;
    @FXML private DatePicker dpFin;
    @FXML private Label lblTotalVendido;
    @FXML private Label lblCantidadVentas;
    @FXML private Label lblTicketPromedio;

    @FXML private TableView<Venta> tblReportes;
    @FXML private TableColumn<Venta, Integer> colIdVenta;
    @FXML private TableColumn<Venta, String> colFecha;
    @FXML private TableColumn<Venta, String> colMetodoPago;
    @FXML private TableColumn<Venta, Double> colTotal;

    private final VentaDAO ventaDAO = new VentaDAO();
    private final ObservableList<Venta> listaVentas = FXCollections.observableArrayList();
    private FilteredList<Venta> listaFiltrada;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        configurarTabla();
        cargarVentasDesdeBD();
        inicializarFechas();
        handleGenerarReporte(null); // Aplica el filtro de fecha inicial

        // Evento: Doble clic en una fila de la tabla para ver sus productos
        tblReportes.setOnMouseClicked(event -> {
            if (event.getClickCount() == 2 && tblReportes.getSelectionModel().getSelectedItem() != null) {
                Venta ventaSeleccionada = tblReportes.getSelectionModel().getSelectedItem();
                mostrarModalDetalle(ventaSeleccionada);
            }
        });
    }

    private void configurarTabla() {
        // Vincula los atributos del modelo Venta a las columnas de la tabla
        colIdVenta.setCellValueFactory(new PropertyValueFactory<>("idVenta"));
        colFecha.setCellValueFactory(new PropertyValueFactory<>("fecha"));
        colMetodoPago.setCellValueFactory(new PropertyValueFactory<>("metodoPago"));
        colTotal.setCellValueFactory(new PropertyValueFactory<>("total"));

        listaFiltrada = new FilteredList<>(listaVentas, v -> true);
        tblReportes.setItems(listaFiltrada);
    }

    private void inicializarFechas() {
        dpInicio.setValue(LocalDate.now().minusDays(7));
        dpFin.setValue(LocalDate.now());
    }

    /**
     * Consulta el historial de ventas reales desde MySQL usando VentaDAO
     */
    private void cargarVentasDesdeBD() {
        listaVentas.clear();
        List<Venta> ventasBD = ventaDAO.obtenerTodas();
        if (ventasBD != null) {
            listaVentas.addAll(ventasBD);
        }
    }

    @FXML
    private void handleGenerarReporte(ActionEvent event) {
        LocalDate fechaInicio = dpInicio.getValue();
        LocalDate fechaFin = dpFin.getValue();

        if (fechaInicio == null || fechaFin == null) {
            return;
        }

        listaFiltrada.setPredicate(venta -> {
            if (venta.getFecha() == null || venta.getFecha().length() < 10) {
                return false;
            }
            try {
                // Parsea la fecha del String devuelto por MySQL (ej. "2026-08-14 11:21:49")
                LocalDate fechaVenta = LocalDate.parse(venta.getFecha().substring(0, 10));
                return (!fechaVenta.isBefore(fechaInicio)) && (!fechaVenta.isAfter(fechaFin));
            } catch (Exception e) {
                return false;
            }
        });

        calcularMetricas();
    }

    private void calcularMetricas() {
        double totalVendido = 0.0;
        int cantidad = listaFiltrada.size();

        for (Venta v : listaFiltrada) {
            totalVendido += v.getTotal();
        }

        double promedio = cantidad > 0 ? totalVendido / cantidad : 0.0;

        // Se usa Locale.US para forzar el uso del punto decimal (.) en las tarjetas
        lblTotalVendido.setText(String.format(Locale.US, "%.2f MXN", totalVendido));
        lblCantidadVentas.setText(String.valueOf(cantidad));
        lblTicketPromedio.setText(String.format(Locale.US, "%.2f MXN", promedio));
    }

    /**
     * Ventana emergente que muestra los productos incluidos en la venta seleccionada
     */
    private void mostrarModalDetalle(Venta venta) {
        List<String> productos = ventaDAO.obtenerDetallesTexto(venta.getIdVenta());

        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Detalle de Venta #" + venta.getIdVenta());
        alert.setHeaderText("Productos incluidos en la Venta #" + venta.getIdVenta());

        StringBuilder contenido = new StringBuilder();
        contenido.append("Fecha: ").append(venta.getFecha()).append("\n");
        contenido.append("Método de Pago: ").append(venta.getMetodoPago()).append("\n");
        contenido.append("Total: $").append(String.format(Locale.US, "%.2f", venta.getTotal())).append(" MXN\n\n");
        contenido.append("--- LISTA DE PRODUCTOS ---\n");

        if (productos.isEmpty()) {
            contenido.append("No se encontraron detalles para esta venta.");
        } else {
            for (String p : productos) {
                contenido.append(p).append("\n");
            }
        }

        alert.setContentText(contenido.toString());
        alert.showAndWait();
    }

    @FXML
    private void handleIrPuntoDeVenta(ActionEvent event) {
        cambiarVista(event, "/com/kiiwik/pos/views/PuntoDeVenta.fxml");
    }

    @FXML
    private void handleIrInventario(ActionEvent event) {
        cambiarVista(event, "/com/kiiwik/pos/views/Inventario.fxml");
    }

    private void cambiarVista(ActionEvent event, String fxmlPath) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource(fxmlPath));
            Parent root = loader.load();
            Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
            stage.setScene(new Scene(root));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}