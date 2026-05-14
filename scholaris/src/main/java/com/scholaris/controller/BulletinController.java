package com.scholaris.controller;

import com.scholaris.dao.EtudiantDAO;
import com.scholaris.dao.MatiereDAO;
import com.scholaris.model.Etudiant;
import com.scholaris.model.Matiere;
import com.scholaris.model.Note;
import com.scholaris.service.BulletinService;
import com.scholaris.service.MoyenneService;
import com.scholaris.service.RangService;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.stage.FileChooser;

import java.io.File;
import java.io.IOException;
import java.sql.SQLException;
import java.util.List;
import java.util.Map;

public class BulletinController extends BaseController {

    @FXML private TextField searchEtudiant;
    @FXML private ListView<Etudiant> etudiantListView;
    @FXML private Label lblNomEtudiant, lblInfoEtudiant, lblNomComplet, lblMatricule, lblClasse, lblPeriode, lblMoyGeneral, lblAppGeneral, lblRang, lblTrimestre;
    @FXML private TableView<BulletinRow> bulletinTable;
    @FXML private TableColumn<BulletinRow, String> colMatiere, colNote, colMoyClasse, colApp;

    private EtudiantDAO etudiantDAO;
    private MoyenneService moyenneService;
    private BulletinService bulletinService;
    private MatiereDAO matiereDAO;
    private ObservableList<Etudiant> etudiantMasterList = FXCollections.observableArrayList();

    @FXML
    public void initialize() {
        try {
            this.etudiantDAO = new EtudiantDAO();
            this.moyenneService = new MoyenneService();
            this.bulletinService = new BulletinService();
            this.matiereDAO = new MatiereDAO();

            setupListView();
            setupTableView();
            loadEtudiants();
        } catch (SQLException e ) {
            e.printStackTrace();
        }
    }

    private void setupListView() {
        etudiantListView.setCellFactory(lv -> new ListCell<>() {
            @Override
            protected void updateItem(Etudiant e, boolean empty) {
                super.updateItem(e, empty);
                if (empty || e == null) setGraphic(null);
                else {
                    HBox box = new HBox(10);
                    box.setAlignment(javafx.geometry.Pos.CENTER_LEFT);
                    Circle avatar = new Circle(15);
                    avatar.setFill(Color.hsb((e.getNom().hashCode() & 0xFF) * 360.0 / 255, 0.5, 0.7));
                    VBox text = new VBox(2);
                    Label name = new Label(e.getNomComplet());
                    name.setStyle("-fx-font-weight: bold;");
                    Label info = new Label(e.getClasse().getNom());
                    info.getStyleClass().add("text-muted");
                    info.setStyle("-fx-font-size: 11;");
                    text.getChildren().addAll(name, info);
                    box.getChildren().addAll(avatar, text);
                    setGraphic(box);
                }
            }
        });

        etudiantListView.getSelectionModel().selectedItemProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal != null) loadBulletin(newVal);
        });

        FilteredList<Etudiant> filteredData = new FilteredList<>(etudiantMasterList, p -> true);
        searchEtudiant.textProperty().addListener((obs, oldVal, newVal) -> {
            filteredData.setPredicate(e -> {
                if (newVal == null || newVal.isEmpty()) return true;
                String lower = newVal.toLowerCase();
                return e.getNom().toLowerCase().contains(lower) || e.getPrenom().toLowerCase().contains(lower);
            });
        });
        etudiantListView.setItems(filteredData);
    }

    private void setupTableView() {
        colMatiere.setCellValueFactory(d -> new SimpleStringProperty(d.getValue().matiere));
        colNote.setCellValueFactory(d -> new SimpleStringProperty(String.format("%.2f", d.getValue().note)));
        colNote.setStyle("-fx-font-weight: bold;");
        colMoyClasse.setCellValueFactory(d -> new SimpleStringProperty(String.format("%.2f", d.getValue().moyClasse)));
        colApp.setCellValueFactory(d -> new SimpleStringProperty(d.getValue().appreciation));
        colApp.setCellFactory(tc -> new TableCell<>() {
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) setGraphic(null);
                else {
                    Label badge = new Label(item);
                    badge.getStyleClass().addAll("badge", getBadgeClass(item));
                    setGraphic(badge);
                }
            }
        });
    }

    private void loadEtudiants() throws SQLException {
        etudiantMasterList.setAll(etudiantDAO.trouverTous());
    }

    private void loadBulletin(Etudiant e) {
        try {
            int year = java.time.LocalDate.now().getYear();
            String periode = "Trimestre 1";

            lblNomEtudiant.setText(e.getNomComplet());
            lblInfoEtudiant.setText(e.getMatricule() + " · " + e.getClasse().getNom());
            lblNomComplet.setText(e.getNomComplet());
            lblMatricule.setText(e.getMatricule());
            lblClasse.setText(e.getClasse().getNom());
            lblPeriode.setText(periode + " · " + year);
            lblTrimestre.setText(periode);
            
            double moyGen = moyenneService.calculerMoyenneGenerale(e.getId(), periode, year);
            lblMoyGeneral.setText(String.format("%.2f", moyGen));
            String app = moyenneService.getMention(moyGen);
            lblAppGeneral.setText(app);
            lblAppGeneral.getStyleClass().setAll("badge", getBadgeClass(app));
            
            // Récupérer le rang
            var rangObj = new com.scholaris.dao.RangDAO().trouverRangGeneral(e.getId(), periode, year);
            if (rangObj != null) {
                lblRang.setText("Rang : " + rangObj.getRang() + " / " + rangObj.getEffectif());
            } else {
                lblRang.setText("Rang : - / -");
            }
            
            // Charger les notes par matière
            ObservableList<BulletinRow> rows = FXCollections.observableArrayList();
            Map<Integer, Double> moyennes = moyenneService.calculerMoyennesToutesMatieres(e.getId(), periode, year);
            for (Map.Entry<Integer, Double> entry : moyennes.entrySet()) {
                Matiere m = matiereDAO.trouverParId(entry.getKey());
                BulletinRow row = new BulletinRow();
                row.matiere = m.getNom();
                row.note = entry.getValue();
                
                double moyCls = 0;
                var listMoy = new com.scholaris.dao.NoteDAO().getMoyennesParMatiere(e.getClasse().getId(), m.getId(), periode, year);
                if (!listMoy.isEmpty()) {
                    moyCls = listMoy.stream().mapToDouble(d -> d[1]).average().orElse(0);
                }
                row.moyClasse = moyCls;
                row.appreciation = moyenneService.getMention(row.note);
                rows.add(row);
            }
            bulletinTable.setItems(rows);
            
        } catch (SQLException ex) {
            ex.printStackTrace();
        }
    }

    private String getBadgeClass(String app) {
        return switch (app) {
            case "Très Bien" -> "badge-green";
            case "Bien", "Assez Bien" -> "badge-blue";
            case "Passable" -> "badge-orange";
            default -> "badge-red";
        };
    }

    @FXML
    private void handleGenererPDF() {
        Etudiant e = etudiantListView.getSelectionModel().getSelectedItem();
        if (e == null) {
            showAlert(Alert.AlertType.WARNING, "Sélection requise", "Veuillez sélectionner un étudiant.");
            return;
        }

        int year = java.time.LocalDate.now().getYear();
        String periode = "Trimestre 1";

        try {
            bulletinService.genererEtSauvegarderBulletin(e.getId(), periode, year);
            showAlert(Alert.AlertType.INFORMATION, "Succès", "Bulletin généré dans target/bulletins/");
        } catch (SQLException | IOException ex) {
            ex.printStackTrace();
            showAlert(Alert.AlertType.ERROR, "Erreur", "Erreur lors de la génération : " + ex.getMessage());
        }
    }

    @FXML
    private void handleGenererClasse() {
        Etudiant e = etudiantListView.getSelectionModel().getSelectedItem();
        if (e == null) return;
        
        int classeId = e.getClasse().getId();
        int year = java.time.LocalDate.now().getYear();
        String periode = "Trimestre 1";

        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Génération en masse");
        alert.setHeaderText("Générer tous les bulletins pour la classe " + e.getClasse().getNom() + " ?");
        
        if (alert.showAndWait().get() == ButtonType.OK) {
            try {
                List<Etudiant> etudiants = etudiantDAO.trouverParClasse(classeId);
                for (Etudiant etu : etudiants) {
                    bulletinService.genererEtSauvegarderBulletin(etu.getId(), periode, year);
                }
                showAlert(Alert.AlertType.INFORMATION, "Succès", etudiants.size() + " bulletins générés dans target/bulletins/");
            } catch (SQLException | IOException ex) {
                ex.printStackTrace();
                showAlert(Alert.AlertType.ERROR, "Erreur", ex.getMessage());
            }
        }
    }

    private void showAlert(Alert.AlertType type, String titre, String msg) {
        Alert alert = new Alert(type);
        alert.setTitle(titre);
        alert.setContentText(msg);
        alert.showAndWait();
    }

    public static class BulletinRow {
        public String matiere;
        public double note;
        public double moyClasse;
        public String appreciation;
    }
}
