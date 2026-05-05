package com.scholaris.controller;

import com.scholaris.dao.ClasseDAO;
import com.scholaris.dao.EtudiantDAO;
import com.scholaris.dao.UtilisateurDAO;
import javafx.animation.PauseTransition;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.stage.Stage;
import javafx.util.Duration;
import com.scholaris.model.Classe;
import com.scholaris.model.Etudiant;
import com.scholaris.service.AuthService;

import java.io.IOException;
import java.sql.SQLException;
import java.util.List;

public class SignupController {

    @FXML private TextField nomField;
    @FXML private TextField prenomField;
    @FXML private TextField emailField;
    @FXML private TextField matriculeField;
    @FXML private ComboBox<Classe> classeCombo;
    @FXML private DatePicker dateNaissancePicker;
    @FXML private PasswordField passwordField;
    @FXML private PasswordField confirmPasswordField;
    @FXML private Label messageLabel;

    private UtilisateurDAO utilisateurDAO;
    private EtudiantDAO    etudiantDAO;
    private ClasseDAO      classeDAO;
    private AuthService    authService;

    public void initialize() {
        try {
            this.utilisateurDAO = new UtilisateurDAO();
            this.etudiantDAO    = new EtudiantDAO();
            this.classeDAO      = new ClasseDAO();
            this.authService    = new AuthService();
            
            chargerClasses();
        } catch (SQLException e) {
            showError("Erreur de connexion.");
        }
    }

    private void chargerClasses() throws SQLException {
        List<Classe> classes = classeDAO.trouverTous();
        classeCombo.getItems().addAll(classes);
        // Custom cell factory pour afficher le nom de la classe
        classeCombo.setCellFactory(lv -> new ListCell<>() {
            @Override
            protected void updateItem(Classe item, boolean empty) {
                super.updateItem(item, empty);
                setText(empty ? "" : item.getNom());
            }
        });
        classeCombo.setButtonCell(new ListCell<>() {
            @Override
            protected void updateItem(Classe item, boolean empty) {
                super.updateItem(item, empty);
                setText(empty ? "" : item.getNom());
            }
        });
    }

    @FXML
    void handleSignup(ActionEvent event) {
        String nom = nomField.getText();
        String prenom = prenomField.getText();
        String email = emailField.getText();
        String matricule = matriculeField.getText();
        Classe classe = classeCombo.getValue();
        String password = passwordField.getText();
        String confirm = confirmPasswordField.getText();

        if (nom.isEmpty() || prenom.isEmpty() || email.isEmpty() || matricule.isEmpty() || 
            classe == null || password.isEmpty()) {
            showError("Veuillez remplir tous les champs.");
            return;
        }

        if (!password.equals(confirm)) {
            showError("Les mots de passe ne correspondent pas.");
            return;
        }

        if (password.length() < 6) {
            showError("Le mot de passe doit faire au moins 6 caractères.");
            return;
        }

        try {
            // Vérifier si l'email existe déjà
            if (utilisateurDAO.trouverParEmail(email) != null) {
                showError("Cet email est déjà utilisé.");
                return;
            }

            // Créer l'étudiant
            Etudiant e = new Etudiant();
            e.setNom(nom);
            e.setPrenom(prenom);
            e.setEmail(email);
            e.setMotDePasse(authService.hacherMotDePasse(password));
            e.setMatricule(matricule);
            e.setClasse(classe);
            e.setActif(true);
            if (dateNaissancePicker.getValue() != null) {
                e.setDateNaissance(dateNaissancePicker.getValue());
            }

            // L'ajout dans EtudiantDAO.ajouter() gère déjà la transaction et l'ajout dans utilisateur
            etudiantDAO.ajouter(e);

            showSuccess("Compte créé avec succès ! Redirection...");
            
            PauseTransition pause = new PauseTransition(Duration.seconds(2));
            pause.setOnFinished(f -> {
                try {
                    handleGoToLogin(event);
                } catch (IOException ex) {
                    ex.printStackTrace();
                }
            });
            pause.play();

        } catch (SQLException e1) {
            showError("Erreur lors de la création du compte.");
            e1.printStackTrace();
        }
    }

    @FXML
    void handleGoToLogin(ActionEvent event) throws IOException {
        Parent root = FXMLLoader.load(getClass().getResource("/fxml/Login.fxml"));
        Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
        stage.setScene(new Scene(root));
        stage.show();
    }

    private void showError(String message) {
        messageLabel.setText(message);
        messageLabel.setStyle("-fx-text-fill: red;");
        messageLabel.setVisible(true);
    }

    private void showSuccess(String message) {
        messageLabel.setText(message);
        messageLabel.setStyle("-fx-text-fill: green;");
        messageLabel.setVisible(true);
    }
}
