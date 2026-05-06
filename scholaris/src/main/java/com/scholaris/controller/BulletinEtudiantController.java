package com.scholaris.controller;

import com.scholaris.dao.EtudiantDAO;
import com.scholaris.dao.MatiereDAO;
import com.scholaris.model.Etudiant;
import com.scholaris.model.Matiere;
import com.scholaris.model.Utilisateur;
import com.scholaris.service.BulletinService;
import com.scholaris.service.MoyenneService;
import com.scholaris.service.SessionManager;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.stage.FileChooser;

import java.io.File;
import java.io.IOException;
import java.sql.SQLException;
import java.util.Map;

public class BulletinEtudiantController extends BaseController {

    @FXML private Label lblNomComplet, lblMatricule, lblClasse, lblPeriode, lblMoyGeneral, lblAppGeneral, lblRang, lblTrimestreBadge;
    @FXML private ComboBox<String> trimestreCombo;
    @FXML private TableView<BulletinController.BulletinRow> bulletinTable;
    @FXML private TableColumn<BulletinController.BulletinRow, String> colMatiere, colNote, colMoyClasse, colApp;

    private EtudiantDAO etudiantDAO;
    private MoyenneService moyenneService;
    private BulletinService bulletinService;
    private MatiereDAO matiereDAO;
    private Etudiant currentEtudiant;

    @FXML
    public void initialize() {
        try {
            this.etudiantDAO = new EtudiantDAO();
            this.moyenneService = new MoyenneService();
            this.bulletinService = new BulletinService();
            this.matiereDAO = new MatiereDAO();

            trimestreCombo.setItems(FXCollections.observableArrayList("Trimestre I", "Trimestre II", "Trimestre III"));
            trimestreCombo.getSelectionModel().select(0);
            trimestreCombo.valueProperty().addListener((obs, oldVal, newVal) -> loadBulletin());

            Utilisateur user = SessionManager.getInstance().getCurrentUser();
            if (user instanceof Etudiant) {
                this.currentEtudiant = (Etudiant) user;
            } else if (user != null) {
                this.currentEtudiant = etudiantDAO.trouverParId(user.getId());
            }

            setupTableView();
            if (currentEtudiant != null) {
                loadBulletin();
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
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

    private void loadBulletin() {
        if (currentEtudiant == null) return;
        String trimestre = trimestreCombo.getValue();
        if (trimestre == null) trimestre = "Trimestre I";

        try {
            lblNomComplet.setText(currentEtudiant.getNomComplet());
            lblMatricule.setText(currentEtudiant.getMatricule());
            lblClasse.setText(currentEtudiant.getClasse().getNom());
            lblPeriode.setText(trimestre + " · 2025");
            lblTrimestreBadge.setText(trimestre);
            
            double moyGen = moyenneService.calculerMoyenneGenerale(currentEtudiant.getId(), trimestre, 2025);
            lblMoyGeneral.setText(String.format("%.2f", moyGen));
            String app = getAppreciation(moyGen);
            lblAppGeneral.setText(app);
            lblAppGeneral.getStyleClass().setAll("badge", getBadgeClass(app));
            
            ObservableList<BulletinController.BulletinRow> rows = FXCollections.observableArrayList();
            Map<Integer, Double> moyennes = moyenneService.calculerMoyennesToutesMatieres(currentEtudiant.getId(), trimestre, 2025);
            for (Map.Entry<Integer, Double> entry : moyennes.entrySet()) {
                Matiere m = matiereDAO.trouverParId(entry.getKey());
                BulletinController.BulletinRow row = new BulletinController.BulletinRow();
                row.matiere = m.getNom();
                row.note = entry.getValue();
                row.moyClasse = 12.5; // TODO
                row.appreciation = getAppreciation(row.note);
                rows.add(row);
            }
            bulletinTable.setItems(rows);
            
        } catch (SQLException ex) {
            ex.printStackTrace();
        }
    }

    private String getAppreciation(double v) {
        if (v >= 16) return "Très Bien";
        if (v >= 14) return "Bien";
        if (v >= 12) return "Assez Bien";
        if (v >= 10) return "Passable";
        return "Insuffisant";
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
    private void handleTelecharger() {
        if (currentEtudiant == null) return;
        String trimestre = trimestreCombo.getValue();

        FileChooser chooser = new FileChooser();
        chooser.setInitialFileName("Bulletin_" + currentEtudiant.getMatricule() + "_" + trimestre.replace(" ", "") + ".pdf");
        chooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("PDF", "*.pdf"));
        File file = chooser.showSaveDialog(bulletinTable.getScene().getWindow());
        
        if (file != null) {
            try {
                bulletinService.genererEtSauvegarderBulletin(currentEtudiant.getId(), trimestre, 2025);
                showAlert(Alert.AlertType.INFORMATION, "Succès", "Bulletin téléchargé avec succès !");
            } catch (SQLException | IOException ex) {
                ex.printStackTrace();
                showAlert(Alert.AlertType.ERROR, "Erreur", "Erreur lors du téléchargement : " + ex.getMessage());
            }
        }
    }

    private void showAlert(Alert.AlertType type, String titre, String msg) {
        Alert alert = new Alert(type);
        alert.setTitle(titre);
        alert.setContentText(msg);
        alert.showAndWait();
    }
}
