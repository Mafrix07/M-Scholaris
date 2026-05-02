package com.scholaris.controller;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.stage.Stage;
import model.Utilisateur;
import service.AuthService;

import java.io.IOException;
import java.sql.SQLException;

public class LoginController {

    @FXML private TextField emailField;
    @FXML private PasswordField passwordField;
    @FXML private TextField passwordVisibleField;
    @FXML private Label errorLabel;
    @FXML private Button togglePasswordBtn;

    private AuthService authService;
    private boolean isPasswordVisible = false;

    public void initialize() {
        try {
            this.authService = new AuthService();
        } catch (SQLException e) {
            showError("Erreur de connexion à la base de données.");
        }
    }

    @FXML
    void handleLogin(ActionEvent event) {
        String email = emailField.getText();
        String password = isPasswordVisible ? passwordVisibleField.getText() : passwordField.getText();

        if (email.isEmpty() || password.isEmpty()) {
            showError("Veuillez remplir tous les champs.");
            return;
        }

        try {
            Utilisateur u = authService.authentifier(email, password);
            if (u != null) {
                redirectUser(u, event);
            } else {
                showError("Email ou mot de passe incorrect.");
            }
        } catch (SQLException | IOException e) {
            showError("Une erreur est survenue lors de la connexion.");
            e.printStackTrace();
        }
    }

    @FXML
    void handleTogglePassword(ActionEvent event) {
        if (isPasswordVisible) {
            passwordField.setText(passwordVisibleField.getText());
            passwordVisibleField.setVisible(false);
            passwordVisibleField.setManaged(false);
            passwordField.setVisible(true);
            passwordField.setManaged(true);
            togglePasswordBtn.setText("👁");
        } else {
            passwordVisibleField.setText(passwordField.getText());
            passwordField.setVisible(false);
            passwordField.setManaged(false);
            passwordVisibleField.setVisible(true);
            passwordVisibleField.setManaged(true);
            togglePasswordBtn.setText("🔒");
        }
        isPasswordVisible = !isPasswordVisible;
    }

    @FXML
    void handleGoToSignup(ActionEvent event) throws IOException {
        changeScene("/fxml/Signup.fxml", event);
    }

    private void redirectUser(Utilisateur u, ActionEvent event) throws IOException {
        String fxmlFile = "";
        switch (u.getRole().toLowerCase()) {
            case "admin" -> fxmlFile = "/fxml/DashboardAdmin.fxml";
            case "enseignant" -> fxmlFile = "/fxml/DashboardEnseignant.fxml";
            case "etudiant" -> fxmlFile = "/fxml/DashboardEtudiant.fxml";
            default -> {
                showError("Rôle inconnu.");
                return;
            }
        }
        
        FXMLLoader loader = new FXMLLoader(getClass().getResource(fxmlFile));
        Parent root = loader.load();
        
        // Optionnel : passer l'utilisateur au controller suivant
        // if (u.getRole().equals("admin")) {
        //     DashboardAdminController controller = loader.getController();
        //     controller.setAdmin((Admin)u);
        // }

        Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
        stage.setScene(new Scene(root));
        stage.show();
    }

    private void changeScene(String fxml, ActionEvent event) throws IOException {
        Parent root = FXMLLoader.load(getClass().getResource(fxml));
        Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
        stage.setScene(new Scene(root));
        stage.show();
    }

    private void showError(String message) {
        errorLabel.setText(message);
        errorLabel.setVisible(true);
    }
}
