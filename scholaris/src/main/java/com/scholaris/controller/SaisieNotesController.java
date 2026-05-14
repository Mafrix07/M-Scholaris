package com.scholaris.controller;

import com.scholaris.dao.ClasseDAO;
import com.scholaris.dao.EtudiantDAO;
import com.scholaris.dao.MatiereDAO;
import com.scholaris.dao.NoteDAO;
import com.scholaris.model.Classe;
import com.scholaris.model.Etudiant;
import com.scholaris.model.Matiere;
import com.scholaris.model.Note;
import com.scholaris.service.MoyenneService;
import com.scholaris.service.SessionManager;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class SaisieNotesController extends BaseController {

    @FXML private Label lblRoleBadge, lblMoyenneClasse, lblInfoClasse, lblMoyResume, lblAppResume, lblContexteResume;
    @FXML private ComboBox<Classe> classeCombo;
    @FXML private ComboBox<Matiere> matiereCombo;
    @FXML private ComboBox<String> trimestreCombo;
    @FXML private TableView<NoteRow> notesTable;
    @FXML private TableColumn<NoteRow, String> colMatricule, colNom, colAppreciation, colRang;
    @FXML private TableColumn<NoteRow, Double> colNote;
    @FXML private TableColumn<NoteRow, Void> colAction;

    private NoteDAO noteDAO;
    private EtudiantDAO etudiantDAO;
    private ClasseDAO classeDAO;
    private MatiereDAO matiereDAO;
    private MoyenneService moyenneService;
    private ObservableList<NoteRow> tableData = FXCollections.observableArrayList();

    @FXML
    public void initialize() {
        try {
            this.noteDAO = new NoteDAO();
            this.etudiantDAO = new EtudiantDAO();
            this.classeDAO = new ClasseDAO();
            this.matiereDAO = new MatiereDAO();
            this.moyenneService = new MoyenneService();

            lblRoleBadge.setText(SessionManager.getInstance().getCurrentUser().getRole().toUpperCase());
            trimestreCombo.setItems(FXCollections.observableArrayList("Trimestre 1", "Trimestre 2", "Trimestre 3"));
            
            loadSelectors();
            setupTable();
            setupListeners();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private void loadSelectors() throws SQLException {
        classeCombo.getItems().addAll(classeDAO.trouverTous());
        matiereCombo.getItems().addAll(matiereDAO.trouverTous());
    }

    private void setupTable() {
        // ... (colMatricule and colNom code omitted for brevity but keeping it same as before)
        // actually I must provide exact literal code
        colMatricule.setCellValueFactory(d -> new SimpleStringProperty(d.getValue().etudiant.getMatricule()));
        colMatricule.getStyleClass().add("text-blue");

        colNom.setCellFactory(tc -> new TableCell<>() {
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || getTableRow() == null || getTableRow().getItem() == null) {
                    setGraphic(null);
                    setText(null);
                } else {
                    Etudiant e = getTableRow().getItem().etudiant;
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

        colNote.setCellValueFactory(d -> new SimpleObjectProperty<>(d.getValue().valeur));
        colNote.setCellFactory(tc -> new TableCell<>() {
            private final Spinner<Double> spinner = new Spinner<>(0.0, 20.0, 0.0, 0.5);
            {
                spinner.setEditable(true);
                spinner.valueProperty().addListener((obs, oldVal, newVal) -> {
                    if (getTableRow() != null && getTableRow().getItem() != null) {
                        NoteRow row = getTableRow().getItem();
                        row.valeur = newVal;
                        handleNoteChange(row);
                    }
                });
            }
            @Override
            protected void updateItem(Double item, boolean empty) {
                super.updateItem(item, empty);
                if (empty) setGraphic(null);
                else {
                    spinner.getValueFactory().setValue(item);
                    setGraphic(spinner);
                }
            }
        });

        colAppreciation.setCellFactory(tc -> new TableCell<>() {
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || getTableRow() == null || getTableRow().getItem() == null) {
                    setGraphic(null);
                } else {
                    NoteRow row = getTableRow().getItem();
                    Label badge = new Label(row.appreciation);
                    badge.getStyleClass().setAll("badge", getBadgeClass(row.valeur));
                    setGraphic(badge);
                }
            }
        });

        colRang.setCellValueFactory(d -> new SimpleStringProperty(d.getValue().rang));

        colAction.setCellFactory(tc -> new TableCell<>() {
            private final Button btnSave = new Button("Enregistrer");
            {
                btnSave.getStyleClass().add("btn-text-blue");
                btnSave.setOnAction(e -> handleEnregistrerLigne(getTableRow().getItem()));
            }
            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                setGraphic(empty ? null : btnSave);
            }
        });

        notesTable.setItems(tableData);
    }

    private void setupListeners() {
        classeCombo.valueProperty().addListener((obs, oldVal, newVal) -> loadNotes());
        matiereCombo.valueProperty().addListener((obs, oldVal, newVal) -> loadNotes());
        trimestreCombo.valueProperty().addListener((obs, oldVal, newVal) -> loadNotes());
    }

    private void loadNotes() {
        Classe c = classeCombo.getValue();
        Matiere m = matiereCombo.getValue();
        String t = trimestreCombo.getValue();
        int year = java.time.LocalDate.now().getYear();

        if (c != null && m != null && t != null) {
            try {
                tableData.clear();
                List<Etudiant> etudiants = etudiantDAO.trouverParClasse(c.getId());
                List<Note> allNotes = noteDAO.trouverParClasseEtMatiere(c.getId(), m.getId(), t, year);
                
                for (Etudiant e : etudiants) {
                    Note n = allNotes.stream()
                            .filter(note -> note.getEtudiantId() == e.getId())
                            .findFirst()
                            .orElse(null);
                            
                    NoteRow row = new NoteRow();
                    row.etudiant = e;
                    row.noteId = (n != null) ? n.getId() : 0;
                    row.valeur = (n != null) ? n.getValeur() : 0.0;
                    handleNoteChange(row);
                    tableData.add(row);
                }
                updateGlobalStats();
                lblContexteResume.setText(c.getNom() + " · " + m.getNom() + " · " + t);
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
    }

    private void handleNoteChange(NoteRow row) {
        row.appreciation = moyenneService.getMention(row.valeur);
        updateRanks();
        updateGlobalStats();
    }

    private void updateRanks() {
        List<NoteRow> sorted = new ArrayList<>(tableData);
        sorted.sort((r1, r2) -> Double.compare(r2.valeur, r1.valeur));
        for (int i = 0; i < sorted.size(); i++) {
            sorted.get(i).rang = (i + 1) + (i == 0 ? "er" : "ème");
        }
        notesTable.refresh();
    }

    private void updateGlobalStats() {
        double sum = 0;
        for (NoteRow r : tableData) sum += r.valeur;
        double avg = tableData.isEmpty() ? 0 : sum / tableData.size();
        
        lblMoyenneClasse.setText(String.format("%.2f", avg));
        lblMoyResume.setText("Moyenne : " + String.format("%.2f", avg));
        lblAppResume.setText(moyenneService.getMention(avg));
        lblAppResume.getStyleClass().setAll("badge", getBadgeClass(avg));
        
        Classe c = classeCombo.getValue();
        if (c != null) lblInfoClasse.setText(c.getNom() + " · " + tableData.size() + " élèves");
    }

    @FXML
    private void handleEnregistrerLigne(NoteRow row) {
        try {
            saveRow(row);
            showAlert(Alert.AlertType.INFORMATION, "Succès", "Note enregistrée pour " + row.etudiant.getNomComplet());
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    @FXML
    private void handleEnregistrerTout() {
        try {
            for (NoteRow row : tableData) saveRow(row);
            showAlert(Alert.AlertType.INFORMATION, "Succès", "Toutes les notes ont été enregistrées.");
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private void saveRow(NoteRow row) throws SQLException {
        Note n = new Note();
        n.setId(row.noteId);
        n.setEtudiantId(row.etudiant.getId());
        n.setMatiereId(matiereCombo.getValue().getId());
        n.setClasseId(classeCombo.getValue().getId());
        n.setValeur(row.valeur);
        n.setPeriode(trimestreCombo.getValue());
        n.setAnneeScolaire(java.time.LocalDate.now().getYear());
        
        if (n.getId() == 0) noteDAO.ajouter(n);
        else noteDAO.modifier(n);
        
        // Mettre à jour l'ID de la note dans la ligne de la table après insertion
        if (row.noteId == 0) {
            row.noteId = n.getId();
        }
    }

    @FXML private void handleAnnuler() { loadNotes(); }

    private String getBadgeClass(double v) {
        if (v >= 16) return "badge-green";
        if (v >= 12) return "badge-blue";
        if (v >= 10) return "badge-orange";
        return "badge-red";
    }

    private void showAlert(Alert.AlertType type, String titre, String msg) {
        Alert alert = new Alert(type);
        alert.setTitle(titre);
        alert.setContentText(msg);
        alert.showAndWait();
    }

    public static class NoteRow {
        public Etudiant etudiant;
        public int noteId;
        public double valeur;
        public String appreciation;
        public String rang;
    }
}
