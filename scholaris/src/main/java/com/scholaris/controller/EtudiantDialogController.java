package com.scholaris.controller;

import com.scholaris.dao.ClasseDAO;
import com.scholaris.dao.EtudiantDAO;
import com.scholaris.model.Classe;
import com.scholaris.model.Etudiant;
import javafx.fxml.FXML;
import javafx.scene.control.ComboBox;
import javafx.scene.control.DatePicker;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.stage.Stage;

import java.sql.SQLException;
import java.util.List;

public class EtudiantDialogController {

    @FXML private Label lblTitle;
    @FXML private TextField fieldMatricule, fieldNom, fieldPrenom, fieldEmail;
    @FXML private DatePicker fieldDateNaissance;
    @FXML private ComboBox<Classe> fieldClasse;

    private Etudiant currentEtudiant;
    private EtudiantDAO etudiantDAO;
    private ClasseDAO classeDAO;
    private boolean saved = false;

    @FXML
    public void initialize() {
        try {
            this.etudiantDAO = new EtudiantDAO();
            this.classeDAO = new ClasseDAO();
            
            List<Classe> classes = classeDAO.trouverTous();
            fieldClasse.getItems().addAll(classes);
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public void setEtudiant(Etudiant e) {
        this.currentEtudiant = e;
        if (e != null) {
            lblTitle.setText("Modifier l'étudiant");
            fieldMatricule.setText(e.getMatricule());
            fieldNom.setText(e.getNom());
            fieldPrenom.setText(e.getPrenom());
            fieldEmail.setText(e.getEmail());
            fieldDateNaissance.setValue(e.getDateNaissance());
            fieldClasse.setValue(e.getClasse());
            // fieldMatricule.setDisable(true); // Souvent bloqué en édition
        } else {
            lblTitle.setText("Ajouter un étudiant");
        }
    }

    @FXML
    private void handleEnregistrer() {
        if (validate()) {
            try {
                if (currentEtudiant == null) {
                    currentEtudiant = new Etudiant();
                    updateModel();
                    currentEtudiant.setMotDePasse("etudiant123"); // Password par défaut
                    etudiantDAO.ajouter(currentEtudiant);
                } else {
                    updateModel();
                    etudiantDAO.modifier(currentEtudiant);
                }
                saved = true;
                handleClose();
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
    }

    private boolean validate() {
        return !fieldNom.getText().isEmpty() && !fieldPrenom.getText().isEmpty() && fieldClasse.getValue() != null;
    }

    private void updateModel() {
        currentEtudiant.setNom(fieldNom.getText());
        currentEtudiant.setPrenom(fieldPrenom.getText());
        currentEtudiant.setEmail(fieldEmail.getText());
        currentEtudiant.setMatricule(fieldMatricule.getText());
        currentEtudiant.setDateNaissance(fieldDateNaissance.getValue());
        currentEtudiant.setClasse(fieldClasse.getValue());
    }

    @FXML
    private void handleClose() {
        ((Stage) fieldNom.getScene().getWindow()).close();
    }

    public boolean isSaved() {
        return saved;
    }
}
