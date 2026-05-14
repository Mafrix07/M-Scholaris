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
import javafx.scene.layout.VBox;
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
    @FXML private DatePicker dateNaissancePicker;
    @FXML private ComboBox<String> niveauCombo;
    @FXML private ComboBox<String> serieCombo;
    @FXML private VBox serieBox;
    @FXML private PasswordField passwordField;
    @FXML private PasswordField confirmPasswordField;
    @FXML private Label messageLabel;

    private UtilisateurDAO utilisateurDAO;
    private EtudiantDAO    etudiantDAO;
    private AuthService    authService;

    public void initialize() {
        try {
            this.utilisateurDAO = new UtilisateurDAO();
            this.etudiantDAO    = new EtudiantDAO();
            this.authService    = new AuthService();
            
            setupCombos();
        } catch (SQLException e) {
            showError("Erreur de connexion.");
        }
    }

    private void setupCombos() {
        // Peupler les niveaux
        niveauCombo.getItems().addAll("6ème", "5ème", "4ème", "3ème", "2nde", "1ère", "Terminale");
        
        // Cacher la série par défaut
        serieBox.setVisible(false);
        serieBox.setManaged(false);

        // Listener sur le niveau pour afficher/cacher et peupler la série
        niveauCombo.setOnAction(e -> {
            String niveau = niveauCombo.getValue();
            serieCombo.getItems().clear();
            
            if (niveau == null) return;

            switch (niveau) {
                case "2nde":
                    showSerie(true);
                    serieCombo.getItems().addAll("Générale", "Technique");
                    break;
                case "1ère":
                    showSerie(true);
                    serieCombo.getItems().addAll("A", "C", "D", "G");
                    break;
                case "Terminale":
                    showSerie(true);
                    serieCombo.getItems().addAll("A", "C", "D", "G", "F");
                    break;
                default:
                    showSerie(false);
                    break;
            }
        });
    }

    private void showSerie(boolean visible) {
        serieBox.setVisible(visible);
        serieBox.setManaged(visible);
    }

    @FXML
    void handleSignup(ActionEvent event) {
        String nom = nomField.getText().trim();
        String prenom = prenomField.getText().trim();
        String email = emailField.getText().trim();
        String password = passwordField.getText();
        String confirm = confirmPasswordField.getText();
        String niveau = niveauCombo.getValue();
        String serie = serieCombo.getValue();

        // Validations de base
        if (nom.isEmpty() || prenom.isEmpty() || email.isEmpty() || password.isEmpty() || niveau == null) {
            showError("Veuillez remplir tous les champs obligatoires.");
            return;
        }

        // Validation série si visible
        if (serieBox.isVisible() && (serie == null || serie.isEmpty())) {
            showError("Veuillez sélectionner une série.");
            return;
        }

        if (!password.equals(confirm)) {
            showError("Les mots de passe ne correspondent pas.");
            return;
        }

        if (password.length() < 8) {
            showError("Le mot de passe doit faire au moins 8 caractères.");
            return;
        }

        // Validation Email simple regex
        if (!email.matches("^[A-Za-z0-9+_.-]+@(.+)$")) {
            showError("Format d'email invalide.");
            return;
        }

        try {
            // 1. Vérifier unicité email
            if (utilisateurDAO.trouverParEmail(email) != null) {
                showError("Cet email est déjà utilisé.");
                return;
            }

            // 2. Trouver une classe disponible
            Classe classe = etudiantDAO.trouverClasseDisponible(niveau, serie);
            if (classe == null) {
                showError("Aucune classe disponible pour ce niveau. Contactez l'administration.");
                return;
            }

            // 3. Générer matricule
            String matricule = etudiantDAO.genererMatricule();

            // 4. Créer l'objet Etudiant
            Etudiant e = new Etudiant();
            e.setNom(nom);
            e.setPrenom(prenom);
            e.setEmail(email);
            e.setMotDePasse(authService.hacherMotDePasse(password)); // Assumé BCrypt par authService
            e.setMatricule(matricule);
            e.setClasse(classe);
            e.setActif(true);
            if (dateNaissancePicker.getValue() != null) {
                e.setDateNaissance(dateNaissancePicker.getValue());
            }

            // 5. Insertion en base (transactionnelle dans EtudiantDAO.ajouter)
            etudiantDAO.ajouter(e);

            // 6. Succès & Popup
            Alert alert = new Alert(Alert.AlertType.INFORMATION);
            alert.setTitle("Inscription réussie");
            alert.setHeaderText("Félicitations " + prenom + " !");
            alert.setContentText("Votre compte a été créé.\nVotre matricule est : " + matricule + 
                               "\nClasse affectée : " + classe.getNom());
            alert.showAndWait();

            handleGoToLogin(event);

        } catch (SQLException | IOException ex) {
            showError("Erreur lors de l'inscription : " + ex.getMessage());
            ex.printStackTrace();
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
