package com.project.controller;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.project.dao.ProjektDAO;
import com.project.model.Projekt;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.ChoiceBox;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.control.cell.PropertyValueFactory;

import javafx.scene.control.Dialog;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.scene.control.DatePicker;
import javafx.scene.control.ButtonType;
import javafx.scene.control.ButtonBar;
import javafx.scene.layout.GridPane;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import java.util.Optional;

import javafx.scene.control.ContextMenu;
import javafx.scene.control.MenuItem;
import javafx.scene.control.Alert;

public class ProjectController {
    private static final Logger logger = LoggerFactory.getLogger(ProjectController.class);

    private ExecutorService wykonawca;
    private ProjektDAO projektDAO;
    private ObservableList<Projekt> projekty;

    private com.project.dao.ZadanieDAO zadanieDAO = new com.project.dao.ZadanieDAOImpl();

    private String search4 = "";
    private Integer pageNo = 0;
    private Integer pageSize = 10;

    @FXML private ChoiceBox<Integer> cbPageSizes;
    @FXML private TableView<Projekt> tblProjekt;
    @FXML private TableColumn<Projekt, Integer> colId;
    @FXML private TableColumn<Projekt, String> colNazwa;
    @FXML private TableColumn<Projekt, String> colOpis;
    @FXML private TableColumn<Projekt, LocalDateTime> colDataCzasUtworzenia;
    @FXML private TableColumn<Projekt, LocalDate> colDataOddania;
    @FXML private TextField txtSzukaj;
    @FXML private Button btnDalej, btnWstecz, btnPierwsza, btnOstatnia;

    public ProjectController(ProjektDAO projektDAO) {
        this.projektDAO = projektDAO;
        this.wykonawca = Executors.newFixedThreadPool(1);
    }

    @FXML
    public void initialize() {
        colId.setCellValueFactory(new PropertyValueFactory<>("projektId"));
        colNazwa.setCellValueFactory(new PropertyValueFactory<>("nazwa"));
        colOpis.setCellValueFactory(new PropertyValueFactory<>("opis"));
        colDataCzasUtworzenia.setCellValueFactory(new PropertyValueFactory<>("dataCzasUtworzenia"));
        colDataOddania.setCellValueFactory(new PropertyValueFactory<>("dataOddania"));

        projekty = FXCollections.observableArrayList();
        tblProjekt.setItems(projekty);

        cbPageSizes.getItems().addAll(5, 10, 20, 50, 100);
        cbPageSizes.setValue(pageSize);

        cbPageSizes.getSelectionModel().selectedItemProperty().addListener((observable, oldValue, newValue) -> {
            if (newValue != null) {
                pageSize = newValue; // Aktualizujemy rozmiar strony
                pageNo = 0;          // Wracamy na pierwsza strone
                wykonawca.execute(() -> loadPage(search4, pageNo, pageSize)); // Ładujemy dane z bazy
            }
        });
        wykonawca.execute(() -> loadPage(search4, pageNo, pageSize));

        // ---TWORZENIE MENU KONTEKSTOWEGO---
        ContextMenu contextMenu = new ContextMenu();

        // Opcja 1: Edytuj
        MenuItem menuEdycja = new MenuItem("Edytuj");
        menuEdycja.setOnAction(event -> {
            Projekt wybranyProjekt = tblProjekt.getSelectionModel().getSelectedItem();
            if (wybranyProjekt != null) {
                edytujProjekt(wybranyProjekt); // Uzycie metody do edycji
            }
        });

        // Opcja 2: Usuń
        MenuItem menuUsun = new MenuItem("Usuń");
        menuUsun.setOnAction(event -> {
            Projekt wybranyProjekt = tblProjekt.getSelectionModel().getSelectedItem();
            if (wybranyProjekt != null) {
                // Pytamy użytkownika, czy na pewno chce usunąć
                Alert alert = new Alert(Alert.AlertType.CONFIRMATION,
                        "Czy na pewno chcesz usunąć projekt: " + wybranyProjekt.getNazwa() + "?",
                        ButtonType.YES, ButtonType.NO);

                alert.showAndWait().ifPresent(response -> {
                    if (response == ButtonType.YES) {
                        wykonawca.execute(() -> {
                            try {
                                projektDAO.deleteProjekt(wybranyProjekt.getProjektId()); // Usuwa z bazy
                                Platform.runLater(() -> {
                                    projekty.remove(wybranyProjekt); // Usuwa z widoku tabeli
                                    tblProjekt.refresh();
                                });
                            } catch (Exception e) {
                                logger.error("Błąd usuwania", e);
                            }
                        });
                    }
                });
            }
        });

        contextMenu.getItems().addAll(menuEdycja, menuUsun);
        tblProjekt.setContextMenu(contextMenu); // Podpięcie menu do tabeli
    }

    private void loadPage(String search4, Integer pageNo, Integer pageSize) {
        try {
            List<Projekt> projektList = projektDAO.getProjekty(search4, pageNo * pageSize, pageSize);
            Platform.runLater(() -> {
                projekty.clear();
                projekty.addAll(projektList);
            });
        } catch (RuntimeException e) {
            logger.error("Błąd pobierania danych", e);
        }
    }

    public void shutdown() {
        if (wykonawca != null) {
            wykonawca.shutdown();
            try {
                if (!wykonawca.awaitTermination(5, TimeUnit.SECONDS)) {
                    wykonawca.shutdownNow();
                }
            } catch (InterruptedException e) {
                wykonawca.shutdownNow();
            }
        }
    }

    private Label getRightLabel(String text) {
        Label lbl = new Label(text);
        lbl.setMaxSize(Double.MAX_VALUE, Double.MAX_VALUE);
        lbl.setAlignment(Pos.CENTER_RIGHT);
        return lbl;
    }

    @FXML private void onActionBtnSzukaj(ActionEvent event) {
        search4 = txtSzukaj.getText().trim();
        pageNo = 0;
        wykonawca.execute(() -> loadPage(search4, pageNo, pageSize));
    }
    @FXML private void onActionBtnDalej(ActionEvent event) {
        pageNo++;
        wykonawca.execute(() -> loadPage(search4, pageNo, pageSize));
    }
    @FXML private void onActionBtnWstecz(ActionEvent event) {
        if (pageNo > 0) {
            pageNo--;
            wykonawca.execute(() -> loadPage(search4, pageNo, pageSize));
        }
    }
    @FXML private void onActionBtnPierwsza(ActionEvent event) {
        pageNo = 0;
        wykonawca.execute(() -> loadPage(search4, pageNo, pageSize));
    }
    @FXML private void onActionBtnOstatnia(ActionEvent event) {
        wykonawca.execute(() -> {
            int totalRows = projektDAO.getRowsNumber();
            pageNo = Math.max(0, (totalRows - 1) / pageSize);
            loadPage(search4, pageNo, pageSize);
        });
    }
    @FXML private void onActionBtnDodaj(ActionEvent event) {
        edytujProjekt(new Projekt());
    }

    @FXML
    private void onActionBtnZadania(ActionEvent event) {
        Projekt wybranyProjekt = tblProjekt.getSelectionModel().getSelectedItem();
        if (wybranyProjekt == null) {
            Alert alert = new Alert(Alert.AlertType.WARNING, "Wybierz projekt!");
            alert.showAndWait();
            return;
        }
        openZadanieFrame(wybranyProjekt);
    }

    private javafx.stage.Stage openZadanieFrame(Projekt projekt) {
        try {
            javafx.fxml.FXMLLoader loader = new javafx.fxml.FXMLLoader(getClass().getResource("/fxml/Zadania.fxml"));
            loader.setControllerFactory(controllerClass -> new ZadaniaController(projekt, this.zadanieDAO, this.wykonawca));

            javafx.stage.Stage stage = new javafx.stage.Stage();
            stage.initModality(javafx.stage.Modality.APPLICATION_MODAL);
            stage.setTitle("Zadania dla projektu: " + projekt.getNazwa());
            stage.setScene(new javafx.scene.Scene(loader.load()));
            stage.show();
            return stage;
        } catch (java.io.IOException e) {
            throw new RuntimeException("Błąd otwierania okna zadań", e);
        }
    }

    @FXML
    private void onActionBtnUsun(ActionEvent event) {
        Projekt wybranyProjekt = tblProjekt.getSelectionModel().getSelectedItem();
        if (wybranyProjekt == null) {
            Alert alert = new Alert(Alert.AlertType.WARNING, "Proszę najpierw wybrać projekt z tabeli!");
            alert.showAndWait();
            return;
        }

        Alert alert = new Alert(Alert.AlertType.CONFIRMATION,
                "Czy na pewno chcesz usunąć projekt: " + wybranyProjekt.getNazwa() + "?",
                ButtonType.YES, ButtonType.NO);

        alert.showAndWait().ifPresent(response -> {
            if (response == ButtonType.YES) {
                wykonawca.execute(() -> {
                    try {
                        projektDAO.deleteProjekt(wybranyProjekt.getProjektId());
                        Platform.runLater(() -> {
                            projekty.remove(wybranyProjekt);
                            tblProjekt.refresh();
                        });
                    } catch (Exception e) {
                        logger.error("Błąd usuwania", e);
                    }
                });
            }
        });
    }


    private void edytujProjekt(Projekt projekt) {
        Dialog<Projekt> dialog = new Dialog<>();
        dialog.setTitle("Edycja");
        dialog.setHeaderText(projekt.getProjektId() != null ? "Edycja danych projektu" : "Dodawanie projektu");

        ButtonType buttonTypeOk = new ButtonType("Zapisz", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(buttonTypeOk, ButtonType.CANCEL);

        Label txtId = new Label(projekt.getProjektId() != null ? projekt.getProjektId().toString() : "");
        TextField txtNazwa = new TextField(projekt.getNazwa() != null ? projekt.getNazwa() : "");
        TextArea txtOpis = new TextArea(projekt.getOpis() != null ? projekt.getOpis() : "");
        txtOpis.setPrefRowCount(4);
        DatePicker dtDataOddania = new DatePicker(projekt.getDataOddania());

        GridPane grid = new GridPane();
        grid.setHgap(10); grid.setVgap(10);
        grid.setPadding(new Insets(20, 150, 10, 10));

        grid.add(new Label("Id:"), 0, 0); grid.add(txtId, 1, 0);
        grid.add(new Label("Nazwa:"), 0, 1); grid.add(txtNazwa, 1, 1);
        grid.add(new Label("Opis:"), 0, 2); grid.add(txtOpis, 1, 2);
        grid.add(new Label("Data oddania:"), 0, 3); grid.add(dtDataOddania, 1, 3);

        dialog.getDialogPane().setContent(grid);

        dialog.setResultConverter(b -> {
            if (b == buttonTypeOk) {
                projekt.setNazwa(txtNazwa.getText().trim());
                projekt.setOpis(txtOpis.getText().trim());
                projekt.setDataOddania(dtDataOddania.getValue());
                return projekt;
            }
            return null;
        });

        Optional<Projekt> result = dialog.showAndWait();
        result.ifPresent(p -> {
            wykonawca.execute(() -> {
                try {
                    projektDAO.setProjekt(p);
                    Platform.runLater(() -> loadPage(search4, pageNo, pageSize));
                } catch (Exception e) {
                    logger.error("Błąd zapisu", e);
                }
            });
        });
    }
}