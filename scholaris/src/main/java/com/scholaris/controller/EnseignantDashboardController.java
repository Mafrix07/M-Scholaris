package com.scholaris.controller;

import com.scholaris.dao.*;
import com.scholaris.model.*;
import com.scholaris.service.MoyenneService;
import com.scholaris.service.SessionManager;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class EnseignantDashboardController extends BaseController {

    @FXML private Label lblMesEtudiants, lblMesMatieres, lblMaMoyenne, lblEnAttente;
    @FXML private VBox mesClassesContainer, activiteContainer;
    @FXML private TableView<EtudiantNoteDTO> difficultesTable;
    @FXML private TableColumn<EtudiantNoteDTO, String> colMatricule, colNom, colClasse, colMatiere;
    @FXML private TableColumn<EtudiantNoteDTO, Double> colNote;

    private EnseignantMatiereDAO emDAO;
    private EtudiantDAO etudiantDAO;
    private NoteDAO noteDAO;
    private MoyenneService moyenneService;
    private EvenementDAO evenementDAO;
    private Professeur currentProf;

    @FXML
    public void initialize() {
        try {
            this.emDAO = new EnseignantMatiereDAO();
            this.etudiantDAO = new EtudiantDAO();
            this.noteDAO = new NoteDAO();
            this.moyenneService = new MoyenneService();
            this.evenementDAO = new EvenementDAO();

            Utilisateur user = SessionManager.getInstance().getCurrentUser();
            if (user instanceof Professeur) {
                this.currentProf = (Professeur) user;
            } else if (user != null) {
                // Fallback for generic Utilisateur with role teacher
                this.currentProf = new Professeur();
                this.currentProf.setId(user.getId());
                this.currentProf.setNom(user.getNom());
                this.currentProf.setPrenom(user.getPrenom());
            }

            if (currentProf != null) {
                loadKPIs();
                loadMesClasses();
                loadActiviteRecente();
                setupDifficultesTable();
                loadDifficultes();
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private void loadKPIs() throws SQLException {
        List<EnseignantMatiere> affectations = emDAO.trouverParProfesseur(currentProf.getId());
        lblMesMatieres.setText(String.valueOf(affectations.stream().map(EnseignantMatiere::getMatiereId).distinct().count()));

        Set<Integer> etudiantIds = new HashSet<>();
        for (EnseignantMatiere em : affectations) {
            List<Etudiant> etudiants = etudiantDAO.trouverParClasse(em.getClasseId());
            etudiants.forEach(e -> etudiantIds.add(e.getId()));
        }
        lblMesEtudiants.setText(String.valueOf(etudiantIds.size()));

        double totalMoyenne = 0;
        int count = 0;
        int enAttente = 0;
        int currentYear = java.time.LocalDate.now().getYear();
        String currentPeriode = "Trimestre 1"; // À rendre dynamique via un ConfigService plus tard

        for (EnseignantMatiere em : affectations) {
            List<Etudiant> etudiants = etudiantDAO.trouverParClasse(em.getClasseId());
            for (Etudiant e : etudiants) {
                double moy = moyenneService.calculerMoyenneMatiere(e.getId(), em.getMatiereId(), currentPeriode, currentYear);
                if (moy > 0) {
                    totalMoyenne += moy;
                    count++;
                } else {
                    enAttente++;
                }
            }
        }

        lblMaMoyenne.setText(count == 0 ? "0.00" : String.format("%.2f", totalMoyenne / count));
        lblEnAttente.setText(String.valueOf(enAttente));
    }

    private void loadMesClasses() throws SQLException {
        mesClassesContainer.getChildren().clear();
        int currentYear = java.time.LocalDate.now().getYear();
        String currentPeriode = "Trimestre 1";
        
        List<EnseignantMatiere> affectations = emDAO.trouverParProfesseur(currentProf.getId());
        for (EnseignantMatiere em : affectations) {
            VBox card = new VBox(10);
            card.getStyleClass().add("classe-card");
            card.setStyle("-fx-background-color: white; -fx-padding: 15; -fx-border-color: #E0E0E0; -fx-border-width: 0 0 0 4; -fx-border-left-color: #0D1B4B;");

            HBox header = new HBox();
            Label title = new Label(em.getClasse().getNom() + " · " + em.getMatiere().getNom());
            title.setStyle("-fx-font-weight: bold;");
            Region spacer = new Region();
            HBox.setHgrow(spacer, Priority.ALWAYS);
            Label badge = new Label(currentPeriode);
            badge.getStyleClass().add("badge-blue");
            header.getChildren().addAll(title, spacer, badge);

            List<Etudiant> etudiants = etudiantDAO.trouverParClasse(em.getClasseId());
            List<Note> notes = noteDAO.trouverParClasseEtMatiere(em.getClasseId(), em.getMatiereId(), currentPeriode, currentYear);
            double progress = etudiants.isEmpty() ? 0 : (double) notes.size() / etudiants.size();

            ProgressBar pb = new ProgressBar(progress);
            pb.setMaxWidth(Double.MAX_VALUE);
            pb.getStyleClass().add("progress-bar-blue");

            HBox footer = new HBox();
            Label stats = new Label(notes.size() + "/" + etudiants.size() + " notes saisies");
            stats.getStyleClass().add("text-muted");
            Region spacer2 = new Region();
            HBox.setHgrow(spacer2, Priority.ALWAYS);
            Hyperlink link = new Hyperlink("Saisir les notes →");
            link.getStyleClass().add("btn-text-blue");
            link.setOnAction(e -> handleSaisirNotes(em));
            footer.getChildren().addAll(stats, spacer2, link);

            card.getChildren().addAll(header, pb, footer);
            mesClassesContainer.getChildren().add(card);
        }
    }

    private void loadActiviteRecente() throws SQLException {
        activiteContainer.getChildren().clear();
        List<Evenement> events = evenementDAO.trouverTous(); 
        events.sort((e1, e2) -> e2.getDateEvent().compareTo(e1.getDateEvent()));
        
        int limit = Math.min(events.size(), 5);
        for (int i = 0; i < limit; i++) {
            Evenement ev = events.get(i);
            HBox item = new HBox(12);
            item.setAlignment(javafx.geometry.Pos.CENTER_LEFT);
            
            Region dot = new Region();
            dot.setPrefSize(10, 10);
            dot.setStyle("-fx-background-color: #0D1B4B; -fx-background-radius: 5;");
            
            VBox info = new VBox(2);
            Label desc = new Label(ev.getTitre());
            desc.setStyle("-fx-font-size: 13;");
            Label date = new Label(ev.getDateEvent().toString());
            date.getStyleClass().add("text-muted");
            date.setStyle("-fx-font-size: 11;");
            info.getChildren().addAll(desc, date);
            
            item.getChildren().addAll(dot, info);
            activiteContainer.getChildren().add(item);
        }
    }

    private void setupDifficultesTable() {
        colMatricule.setCellValueFactory(new PropertyValueFactory<>("matricule"));
        colNom.setCellValueFactory(new PropertyValueFactory<>("nom"));
        colClasse.setCellValueFactory(new PropertyValueFactory<>("classe"));
        colMatiere.setCellValueFactory(new PropertyValueFactory<>("matiere"));
        colNote.setCellValueFactory(new PropertyValueFactory<>("note"));
        
        colNote.setCellFactory(column -> new TableCell<>() {
            @Override
            protected void updateItem(Double note, boolean empty) {
                super.updateItem(note, empty);
                if (empty || note == null) {
                    setText(null);
                    setGraphic(null);
                } else {
                    setText(String.format("%.2f", note));
                    setTextFill(javafx.scene.paint.Color.RED);
                }
            }
        });
    }

    private void loadDifficultes() throws SQLException {
        List<EtudiantNoteDTO> list = new ArrayList<>();
        int currentYear = java.time.LocalDate.now().getYear();
        String currentPeriode = "Trimestre 1";
        
        List<EnseignantMatiere> affectations = emDAO.trouverParProfesseur(currentProf.getId());
        for (EnseignantMatiere em : affectations) {
            List<Etudiant> etudiants = etudiantDAO.trouverParClasse(em.getClasseId());
            for (Etudiant e : etudiants) {
                double moy = moyenneService.calculerMoyenneMatiere(e.getId(), em.getMatiereId(), currentPeriode, currentYear);
                if (moy > 0 && moy < 10) {
                    list.add(new EtudiantNoteDTO(e.getMatricule(), e.getNomComplet(), em.getClasse().getNom(), em.getMatiere().getNom(), moy));
                }
            }
        }
        difficultesTable.setItems(javafx.collections.FXCollections.observableArrayList(list));
    }

    @FXML
    private void handleVoirNotesAttente() {
        if (mainController != null) {
            mainController.loadView("/fxml/SaisieNotesView.fxml");
        }
    }

    private void handleSaisirNotes(EnseignantMatiere em) {
        if (mainController != null) {
            // Passer des paramètres au controller de saisie ? 
            // SaisieNotesController devra probablement les récupérer d'un singleton ou NavigationContext
            mainController.loadView("/fxml/SaisieNotesView.fxml");
        }
    }

    // DTO interne pour le tableau
    public static class EtudiantNoteDTO {
        private final String matricule, nom, classe, matiere;
        private final Double note;
        public EtudiantNoteDTO(String m, String n, String c, String mat, Double nt) {
            this.matricule = m; this.nom = n; this.classe = c; this.matiere = mat; this.note = nt;
        }
        public String getMatricule() { return matricule; }
        public String getNom() { return nom; }
        public String getClasse() { return classe; }
        public String getMatiere() { return matiere; }
        public Double getNote() { return note; }
    }
}
