package com.project.controller;

import com.project.dao.ZadanieDAO;
import com.project.model.Projekt;
import com.project.model.Zadanie;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.GridPane;
import javafx.stage.Stage;
import javafx.stage.WindowEvent;

import java.util.List;
import java.util.Optional;
import java.util.concurrent.ExecutorService;

public class ZadaniaController {

    @FXML private Label lblTytul;
    @FXML private Button btnPowrot;

    // Podpinamy tabelę zadań
    @FXML private TableView<Zadanie> tblZadania;
    @FXML private TableColumn<Zadanie, Integer> colZadanieId;
    @FXML private TableColumn<Zadanie, String> colNazwa;
    @FXML private TableColumn<Zadanie, String> colOpis;
    @FXML private TableColumn<Zadanie, java.time.LocalDate> colData;

    private ExecutorService wykonawca;
    private ZadanieDAO zadanieDAO;
    private Projekt projekt;
    private ObservableList<Zadanie> listaZadan;

    public ZadaniaController(Projekt projekt, ZadanieDAO zadanieDAO, ExecutorService wykonawca) {
        this.projekt = projekt;
        this.zadanieDAO = zadanieDAO;
        this.wykonawca = wykonawca;
    }

    @FXML
    public void initialize() {
        lblTytul.setText("Zadania dla projektu: " + projekt.getNazwa());
        lblTytul.setStyle("-fx-font-size: 18px; -fx-font-weight: bold; -fx-padding: 0 0 10 0;");

        // Konfiguracja kolumn
        colZadanieId.setCellValueFactory(new PropertyValueFactory<>("zadanieId"));
        colNazwa.setCellValueFactory(new PropertyValueFactory<>("nazwa"));
        colOpis.setCellValueFactory(new PropertyValueFactory<>("opis"));
        colData.setCellValueFactory(new PropertyValueFactory<>("data"));

        listaZadan = FXCollections.observableArrayList();
        tblZadania.setItems(listaZadan);

        // Ładujemy dane z bazy przy starcie
        loadZadania();
    }

    private void loadZadania() {
        wykonawca.execute(() -> {
            List<Zadanie> pobraneZadania = zadanieDAO.getZadania(projekt.getProjektId());
            Platform.runLater(() -> {
                listaZadan.clear();
                listaZadan.addAll(pobraneZadania);
            });
        });
    }

    @FXML
    private void onActionBtnDodajZadanie(ActionEvent event) {
        // Tworzymy nowe, puste zadanie i przypisujemy je do obecnego projektu
        Zadanie noweZadanie = new Zadanie();
        noweZadanie.setProjektId(projekt.getProjektId());
        edytujZadanie(noweZadanie);
    }

    @FXML
    private void onActionBtnUsunZadanie(ActionEvent event) {
        Zadanie wybrane = tblZadania.getSelectionModel().getSelectedItem();
        if (wybrane == null) {
            Alert alert = new Alert(Alert.AlertType.WARNING, "Wybierz zadanie do usunięcia!");
            alert.showAndWait();
            return;
        }

        Alert alert = new Alert(Alert.AlertType.CONFIRMATION, "Usunąć zadanie?", ButtonType.YES, ButtonType.NO);
        alert.showAndWait().ifPresent(res -> {
            if (res == ButtonType.YES) {
                wykonawca.execute(() -> {
                    zadanieDAO.deleteZadanie(wybrane.getZadanieId());
                    loadZadania(); // Odświeżamy tabelę
                });
            }
        });
    }

    @FXML
    private void onActionBtnPowrot(ActionEvent event) {
        if (btnPowrot != null && btnPowrot.getScene() != null) {
            Stage stage = (Stage) btnPowrot.getScene().getWindow();
            stage.fireEvent(new WindowEvent(stage, WindowEvent.WINDOW_CLOSE_REQUEST));
        }
    }

    private void edytujZadanie(Zadanie zadanie) {
        Dialog<Zadanie> dialog = new Dialog<>();
        dialog.setTitle("Edycja zadania");
        dialog.setHeaderText(zadanie.getZadanieId() != null ? "Edytujesz zadanie" : "Dodaj nowe zadanie");

        ButtonType btnZapisz = new ButtonType("Zapisz", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(btnZapisz, ButtonType.CANCEL);

        TextField txtNazwa = new TextField(zadanie.getNazwa() != null ? zadanie.getNazwa() : "");
        TextField txtOpis = new TextField(zadanie.getOpis() != null ? zadanie.getOpis() : "");
        DatePicker dtData = new DatePicker(zadanie.getData());

        GridPane grid = new GridPane();
        grid.setHgap(10); grid.setVgap(10);
        grid.setPadding(new Insets(20, 100, 10, 10));

        grid.add(new Label("Nazwa:"), 0, 0); grid.add(txtNazwa, 1, 0);
        grid.add(new Label("Opis:"), 0, 1); grid.add(txtOpis, 1, 1);
        grid.add(new Label("Data:"), 0, 2); grid.add(dtData, 1, 2);

        dialog.getDialogPane().setContent(grid);

        dialog.setResultConverter(b -> {
            if (b == btnZapisz) {
                zadanie.setNazwa(txtNazwa.getText().trim());
                zadanie.setOpis(txtOpis.getText().trim());
                zadanie.setData(dtData.getValue());
                return zadanie;
            }
            return null;
        });

        Optional<Zadanie> wynik = dialog.showAndWait();
        wynik.ifPresent(z -> {
            wykonawca.execute(() -> {
                zadanieDAO.setZadanie(z);
                loadZadania(); // Po zapisie do bazy odświeżamy tabele
            });
        });
    }
}