package com.scholaris.controller;

import com.scholaris.dao.EtudiantDAO;
import com.scholaris.dao.MatiereDAO;
import com.scholaris.model.Etudiant;
import com.scholaris.model.Matiere;
import com.scholaris.model.Utilisateur;
import com.scholaris.service.MoyenneService;
import com.scholaris.service.SessionManager;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressBar;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;

import java.sql.SQLException;
import java.util.Map;

public class MesNotesController extends BaseController {

    @FXML private ComboBox<String> trimestreCombo;
    @FXML private Label lblMoyBadge, lblMoyGen, lblRang, lblTrimestre;
    @FXML private GridPane notesGrid;

    private EtudiantDAO etudiantDAO;
    private MoyenneService moyenneService;
    private MatiereDAO matiereDAO;
    private Etudiant currentEtudiant;

    @FXML
    public void initialize() {
        try {
            this.etudiantDAO = new EtudiantDAO();
            this.moyenneService = new MoyenneService();
            this.matiereDAO = new MatiereDAO();

            trimestreCombo.setItems(FXCollections.observableArrayList("Trimestre I", "Trimestre II", "Trimestre III"));
            trimestreCombo.getSelectionModel().select(0);
            trimestreCombo.valueProperty().addListener((obs, oldVal, newVal) -> loadNotes());

            Utilisateur user = SessionManager.getInstance().getCurrentUser();
            if (user instanceof Etudiant) {
                this.currentEtudiant = (Etudiant) user;
            } else if (user != null) {
                this.currentEtudiant = etudiantDAO.trouverParId(user.getId());
            }

            if (currentEtudiant != null) {
                loadNotes();
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private void loadNotes() {
        if (currentEtudiant == null) return;
        String trimestre = trimestreCombo.getValue();
        if (trimestre == null) trimestre = "Trimestre I";
        int year = java.time.LocalDate.now().getYear();

        try {
            notesGrid.getChildren().clear();
            double moyGen = moyenneService.calculerMoyenneGenerale(currentEtudiant.getId(), trimestre, year);
            lblMoyGen.setText(String.format("%.2f", moyGen));
            lblMoyBadge.setText(String.format("Moyenne : %.2f", moyGen));
            lblTrimestre.setText(trimestre + " · " + year + "-" + (year+1));

            Map<Integer, Double> moyennes = moyenneService.calculerMoyennesToutesMatieres(currentEtudiant.getId(), trimestre, year);
            int row = 0, col = 0;
            for (Map.Entry<Integer, Double> entry : moyennes.entrySet()) {
                Matiere m = matiereDAO.trouverParId(entry.getKey());
                // Calculer moyenne classe pour cette matière
                double moyClasse = 0;
                var listMoy = new com.scholaris.dao.NoteDAO().getMoyennesParMatiere(currentEtudiant.getClasse().getId(), m.getId(), trimestre, year);
                if (!listMoy.isEmpty()) {
                    moyClasse = listMoy.stream().mapToDouble(d -> d[1]).average().orElse(0);
                }
                
                VBox card = createMatiereCard(m, entry.getValue(), moyClasse);
                notesGrid.add(card, col, row);
                col++;
                if (col > 1) { col = 0; row++; }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private VBox createMatiereCard(Matiere m, double note, double moyClasse) {
        VBox card = new VBox(8);
        card.getStyleClass().add("card");
        card.setStyle("-fx-padding: 20;");

        Label name = new Label(m.getNom());
        name.getStyleClass().add("text-navy");
        name.setStyle("-fx-font-weight: bold; -fx-font-size: 16;");

        HBox noteBox = new HBox(4);
        noteBox.setAlignment(javafx.geometry.Pos.BASELINE_LEFT);
        Label noteLabel = new Label(String.format("%.2f", note));
        noteLabel.setStyle("-fx-font-weight: bold; -fx-font-size: 28;");
        if (note >= 14) noteLabel.setStyle(noteLabel.getStyle() + " -fx-text-fill: #2E8B57;");
        else if (note >= 10) noteLabel.setStyle(noteLabel.getStyle() + " -fx-text-fill: #B8963E;");
        else noteLabel.setStyle(noteLabel.getStyle() + " -fx-text-fill: #D32F2F;");
        
        Label base = new Label("/20");
        base.getStyleClass().add("text-muted");
        noteBox.getChildren().addAll(noteLabel, base);

        String mention = moyenneService.getMention(note);
        Label appreciation = new Label(mention);
        appreciation.getStyleClass().addAll("badge", getBadgeClass(mention));

        Label moyClasseInfo = new Label(String.format("Moy. classe : %.2f", moyClasse));
        moyClasseInfo.getStyleClass().add("text-muted");
        moyClasseInfo.setStyle("-fx-font-size: 12;");

        Label labelTaNote = new Label("Ta note");
        labelTaNote.getStyleClass().add("text-faint");
        labelTaNote.setStyle("-fx-font-size: 10;");
        ProgressBar pbNote = new ProgressBar(note / 20.0);
        pbNote.setMaxWidth(Double.MAX_VALUE);
        pbNote.getStyleClass().add("progress-bar-blue");

        Label labelMoyClasse = new Label("Moy. classe");
        labelMoyClasse.getStyleClass().add("text-faint");
        labelMoyClasse.setStyle("-fx-font-size: 10;");
        ProgressBar pbMoy = new ProgressBar(moyClasse / 20.0);
        pbMoy.setMaxWidth(Double.MAX_VALUE);
        pbMoy.getStyleClass().add("progress-bar-gray");

        card.getChildren().addAll(name, noteBox, appreciation, moyClasseInfo, labelTaNote, pbNote, labelMoyClasse, pbMoy);
        return card;
    }

    private String getBadgeClass(String app) {
        return switch (app) {
            case "Très Bien" -> "badge-green";
            case "Bien", "Assez Bien" -> "badge-blue";
            case "Passable" -> "badge-orange";
            default -> "badge-red";
        };
    }
}
