package com.scholaris.controller;

import com.scholaris.dao.EtudiantDAO;
import com.scholaris.dao.UtilisateurDAO;
import com.scholaris.model.Admin;
import com.scholaris.model.Etudiant;
import com.scholaris.model.Professeur;
import com.scholaris.model.Utilisateur;
import com.scholaris.service.AuthService;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

import java.sql.SQLException;

public class CreateUserDialogController {

    @FXML private TextField fieldUsername;
    @FXML private PasswordField fieldPassword;
    @FXML private ComboBox<String> roleCombo;
    @FXML private VBox etudiantSection;
    @FXML private ComboBox<Etudiant> etudiantCombo;

    private UtilisateurDAO utilisateurDAO;
    private EtudiantDAO etudiantDAO;
    private AuthService authService;

    @FXML
    public void initialize() {
        try {
            this.utilisateurDAO = new UtilisateurDAO();
            this.etudiantDAO = new EtudiantDAO();
            this.authService = new AuthService();

            roleCombo.setItems(FXCollections.observableArrayList("admin", "enseignant", "etudiant"));
            roleCombo.getSelectionModel().select("etudiant");
            
            roleCombo.valueProperty().addListener((obs, oldVal, newVal) -> {
                boolean isEtudiant = "etudiant".equals(newVal);
                etudiantSection.setVisible(isEtudiant);
                etudiantSection.setManaged(isEtudiant);
            });

            etudiantCombo.setCellFactory(lv -> new ListCell<>() {
                @Override
                protected void updateItem(Etudiant e, boolean empty) {
                    super.updateItem(e, empty);
                    if (empty || e == null) setText(null);
                    else setText(e.getNomComplet() + " (" + e.getMatricule() + ")");
                }
            });
            etudiantCombo.setButtonCell(new ListCell<>() {
                @Override
                protected void updateItem(Etudiant e, boolean empty) {
                    super.updateItem(e, empty);
                    if (empty || e == null) setText(null);
                    else setText(e.getNomComplet());
                }
            });

            loadEtudiants();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private void loadEtudiants() throws SQLException {
        etudiantCombo.setItems(FXCollections.observableArrayList(etudiantDAO.trouverTous()));
    }

    @FXML
    private void handleCreer() {
        String email = fieldUsername.getText();
        String password = fieldPassword.getText();
        String role = roleCombo.getValue();

        if (email.isEmpty() || password.isEmpty()) {
            showAlert(Alert.AlertType.WARNING, "Champs requis", "Veuillez remplir tous les champs.");
            return;
        }

        try {
            Utilisateur u;
            switch (role.toLowerCase()) {
                case "admin" -> u = new Admin();
                case "enseignant" -> u = new Professeur();
                default -> u = new Etudiant();
            }
            u.setEmail(email);
            u.setMotDePasse(authService.hacherMotDePasse(password));
            u.setRole(role);
            u.setActif(true);

            Etudiant selected = etudiantCombo.getValue();
            if ("etudiant".equals(role) && selected != null) {
                u.setNom(selected.getNom());
                u.setPrenom(selected.getPrenom());
            } else {
                u.setNom("Nouvel");
                u.setPrenom("Utilisateur");
            }

            utilisateurDAO.ajouter(u);
            handleClose();
        } catch (SQLException e) {
            showAlert(Alert.AlertType.ERROR, "Erreur", "Impossible de créer l'utilisateur : " + e.getMessage());
        }
    }

    @FXML
    private void handleClose() {
        ((Stage) fieldUsername.getScene().getWindow()).close();
    }

    private void showAlert(Alert.AlertType type, String titre, String msg) {
        Alert alert = new Alert(type);
        alert.setTitle(titre);
        alert.setContentText(msg);
        alert.showAndWait();
    }
}
