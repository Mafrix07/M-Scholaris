package com.scholaris.controller;

import com.scholaris.dao.ClasseDAO;
import com.scholaris.dao.EtudiantDAO;
import com.scholaris.model.Classe;
import com.scholaris.model.Etudiant;
import com.scholaris.service.MoyenneService;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.collections.transformation.SortedList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.stage.Modality;
import javafx.stage.Stage;

import java.io.IOException;
import java.sql.SQLException;
import java.util.List;
import java.util.Optional;

public class EtudiantController extends BaseController {

    @FXML private TableView<Etudiant> etudiantTable;
    @FXML private TableColumn<Etudiant, String> colMatricule, colNom, colDate, colClasse, colMoyenne;
    @FXML private TableColumn<Etudiant, Void> colActions;
    @FXML private TextField searchField;
    @FXML private ComboBox<String> classeFilter, niveauFilter;
    @FXML private Label lblPagination;
    @FXML private HBox pageButtons;

    private EtudiantDAO etudiantDAO;
    private ClasseDAO classeDAO;
    private MoyenneService moyenneService;
    private ObservableList<Etudiant> masterData = FXCollections.observableArrayList();

    @FXML
    public void initialize() {
        try {
            this.etudiantDAO = new EtudiantDAO();
            this.classeDAO = new ClasseDAO();
            this.moyenneService = new MoyenneService();

            setupTable();
            loadData();
            setupFilters();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private void setupTable() {
        colMatricule.setCellValueFactory(d -> new SimpleStringProperty(d.getValue().getMatricule()));
        
        colNom.setCellFactory(tc -> new TableCell<>() {
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || getTableRow() == null || getTableRow().getItem() == null) {
                    setGraphic(null);
                } else {
                    Etudiant e = getTableRow().getItem();
                    HBox box = new HBox(10);
                    box.setAlignment(javafx.geometry.Pos.CENTER_LEFT);
                    Circle avatar = new Circle(15);
                    avatar.setFill(Color.hsb((e.getNom().hashCode() & 0xFF) * 360.0 / 255, 0.5, 0.7));
                    Label name = new Label(e.getNomComplet());
                    box.getChildren().addAll(avatar, name);
                    setGraphic(box);
                }
            }
        });

        colDate.setCellValueFactory(d -> new SimpleStringProperty(d.getValue().getDateNaissance().toString()));

        colClasse.setCellFactory(tc -> new TableCell<>() {
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || getTableRow() == null || getTableRow().getItem() == null) {
                    setGraphic(null);
                } else {
                    Etudiant e = getTableRow().getItem();
                    Label badge = new Label(e.getClasse().getNom());
                    badge.getStyleClass().addAll("badge", "badge-blue");
                    setGraphic(badge);
                }
            }
        });

        colMoyenne.setCellFactory(tc -> new TableCell<>() {
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || getTableRow() == null || getTableRow().getItem() == null) {
                    setGraphic(null);
                } else {
                    try {
                        Etudiant e = getTableRow().getItem();
                        double moy = moyenneService.calculerMoyenneGenerale(e.getId(), "Trimestre 1", 2025);
                        setText(String.format("%.2f", moy));
                        if (moy >= 10) getStyleClass().add("text-success");
                        else getStyleClass().add("text-error");
                    } catch (SQLException ex) {
                        setText("-");
                    }
                }
            }
        });

        colActions.setCellFactory(tc -> new TableCell<>() {
            private final Button btnEdit = new Button("✏️");
            private final Button btnDelete = new Button("🗑️");
            private final Button btnReport = new Button("📄");
            private final HBox pane = new HBox(8, btnEdit, btnDelete, btnReport);

            {
                btnEdit.getStyleClass().add("btn-text-blue");
                btnDelete.getStyleClass().add("btn-text-red");
                btnReport.getStyleClass().add("btn-primary");
                
                btnEdit.setOnAction(e -> handleModifier(getTableRow().getItem()));
                btnDelete.setOnAction(e -> handleSupprimer(getTableRow().getItem()));
                btnReport.setOnAction(e -> handleBulletin(getTableRow().getItem()));
            }

            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                setGraphic(empty ? null : pane);
            }
        });
    }

    private void loadData() throws SQLException {
        masterData.setAll(etudiantDAO.trouverTous());
    }

    private void setupFilters() throws SQLException {
        List<Classe> classes = classeDAO.trouverTous();
        classeFilter.getItems().add("Toutes les classes");
        classes.forEach(c -> classeFilter.getItems().add(c.getNom()));

        FilteredList<Etudiant> filteredData = new FilteredList<>(masterData, p -> true);
        searchField.textProperty().addListener((obs, oldVal, newVal) -> updateFilter(filteredData));
        classeFilter.valueProperty().addListener((obs, oldVal, newVal) -> updateFilter(filteredData));

        SortedList<Etudiant> sortedData = new SortedList<>(filteredData);
        sortedData.comparatorProperty().bind(etudiantTable.comparatorProperty());
        etudiantTable.setItems(sortedData);
    }

    private void updateFilter(FilteredList<Etudiant> filteredData) {
        filteredData.setPredicate(e -> {
            String search = searchField.getText().toLowerCase();
            boolean matchSearch = search.isEmpty() || 
                                 e.getNom().toLowerCase().contains(search) || 
                                 e.getPrenom().toLowerCase().contains(search) || 
                                 e.getMatricule().toLowerCase().contains(search);
            
            String classe = classeFilter.getValue();
            boolean matchClasse = classe == null || classe.equals("Toutes les classes") || 
                                  e.getClasse().getNom().equals(classe);
            
            return matchSearch && matchClasse;
        });
    }

    @FXML
    private void handleAjouter() {
        showDialog(null);
    }

    private void handleModifier(Etudiant e) {
        showDialog(e);
    }

    private void showDialog(Etudiant e) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/EtudiantDialog.fxml"));
            Parent root = loader.load();
            EtudiantDialogController ctrl = loader.getController();
            ctrl.setEtudiant(e);
            
            Stage stage = new Stage();
            stage.initModality(Modality.WINDOW_MODAL);
            stage.initOwner(etudiantTable.getScene().getWindow());
            stage.setTitle(e == null ? "Ajouter un étudiant" : "Modifier un étudiant");
            stage.setScene(new Scene(root));
            stage.showAndWait();
            
            if (ctrl.isSaved()) {
                loadData();
            }
        } catch (IOException | SQLException ex) {
            ex.printStackTrace();
        }
    }

    private void handleSupprimer(Etudiant e) {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Confirmation de suppression");
        alert.setHeaderText("Supprimer l'étudiant " + e.getNomComplet() + " ?");
        alert.setContentText("Cette action est irréversible.");

        Optional<ButtonType> result = alert.showAndWait();
        if (result.isPresent() && result.get() == ButtonType.OK) {
            try {
                etudiantDAO.supprimer(e.getId());
                loadData();
            } catch (SQLException ex) {
                ex.printStackTrace();
            }
        }
    }

    private void handleBulletin(Etudiant e) {
        if (mainController != null) {
            // Passer l'étudiant sélectionné au contrôleur du bulletin via une méthode statique ou un service de contexte
            // Pour l'instant, on charge juste la vue
            mainController.loadView("/fxml/BulletinView.fxml");
        }
    }

    @FXML private void handlePrevPage() {}
    @FXML private void handleNextPage() {}
}
