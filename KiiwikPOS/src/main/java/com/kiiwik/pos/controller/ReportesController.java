package com.kiiwik.pos.controller;

import com.kiiwik.pos.model.Venta;
import java.net.URL;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
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

    private final ObservableList<Venta> listaVentas = FXCollections.observableArrayList();
    private FilteredList<Venta> listaFiltrada;
    private final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        configurarTabla();
        cargarDatosPrueba();
        inicializarFechas();
        calcularMetricas();
    }

    private void configurarTabla() {
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

    @FXML
    private void handleGenerarReporte(ActionEvent event) {
        LocalDate fechaInicio = dpInicio.getValue();
        LocalDate fechaFin = dpFin.getValue();

        if (fechaInicio == null || fechaFin == null) {
            return;
        }

        listaFiltrada.setPredicate(venta -> {
            try {
                // Parseamos la fecha del string del objeto Venta
                LocalDate fechaVenta = LocalDate.parse(venta.getFecha().substring(0, 10));
                return (!fechaVenta.isBefore(fechaInicio)) && (!fechaVenta.isAfter(fechaFin));
            } catch (Exception e) {
                return true;
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

        lblTotalVendido.setText(String.format("%.2f MXN", totalVendido));
        lblCantidadVentas.setText(String.valueOf(cantidad));
        lblTicketPromedio.setText(String.format("%.2f MXN", promedio));
    }

    private void cargarDatosPrueba() {
        listaVentas.clear();
        String hoy = LocalDateTime.now().format(formatter);
        String ayer = LocalDateTime.now().minusDays(1).format(formatter);
        String haceTresDias = LocalDateTime.now().minusDays(3).format(formatter);

        listaVentas.addAll(
            new Venta(1, hoy, "Efectivo", 285.00),
            new Venta(2, hoy, "Transferencia", 450.00),
            new Venta(3, ayer, "Efectivo", 120.00),
            new Venta(4, ayer, "Efectivo", 600.00),
            new Venta(5, haceTresDias, "Transferencia", 210.00)
        );
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