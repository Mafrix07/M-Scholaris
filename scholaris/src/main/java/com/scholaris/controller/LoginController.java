package com.scholaris.controller;

import com.scholaris.model.Utilisateur;
import com.scholaris.service.AuthService;
import com.scholaris.service.NavigationService;
import com.scholaris.service.SessionManager;
import javafx.animation.TranslateTransition;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.scene.layout.VBox;
import javafx.util.Duration;

import java.sql.SQLException;

public class LoginController {

    @FXML private TextField emailField;
    @FXML private PasswordField passwordField;
    @FXML private TextField passwordVisibleField;
    @FXML private Button togglePasswordBtn;
    @FXML private Label errorLabel;
    @FXML private VBox loginCard;

    private boolean isPasswordVisible = false;
    private AuthService authService;

    @FXML
    public void initialize() {
        try {
            this.authService = new AuthService();
        } catch (SQLException e) {
            e.printStackTrace();
        }

        // Synchronize text between fields
        passwordField.textProperty().bindBidirectional(passwordVisibleField.textProperty());
    }

    @FXML
    void handleLogin(ActionEvent event) {
        System.out.println("Login attempt started...");
        String email = emailField.getText().trim();
        String password = passwordField.getText();

        if (email.isEmpty() || password.isEmpty()) {
            showError("Veuillez remplir tous les champs.");
            return;
        }

        try {
            Utilisateur user = authService.login(email, password);

            if (user == null) {
                System.out.println("Login failed: invalid credentials.");
                showError("Email ou mot de passe incorrect.");
                return;
            }

            System.out.println("Login successful for: " + user.getNomComplet());
            // Stocker l'utilisateur connecté en session
            SessionManager.getInstance().setCurrentUser(user);

            // Redirection vers le layout principal (qui gère la sidebar et les vues par rôle)
            NavigationService.loadView("MainLayout.fxml");

        } catch (SQLException e) {
            showError("Erreur de connexion à la base de données.");
            e.printStackTrace();
        }
    }

    @FXML
    void handleTogglePassword(ActionEvent event) {
        isPasswordVisible = !isPasswordVisible;
        
        if (isPasswordVisible) {
            passwordVisibleField.setVisible(true);
            passwordField.setVisible(false);
            togglePasswordBtn.setText("🙈");
        } else {
            passwordVisibleField.setVisible(false);
            passwordField.setVisible(true);
            togglePasswordBtn.setText("👁️");
        }
    }

    @FXML
    void handleSupport(ActionEvent event) {
        // Logique pour contacter le support (ex: ouvrir une URL ou afficher une popup)
        System.out.println("Contact support requested.");
    }

    @FXML
    void handleGoToSignup(ActionEvent event) {
        System.out.println("Navigating to Signup...");
        NavigationService.loadView("Signup.fxml");
    }

    private void showError(String message) {
        errorLabel.setText(message);
        errorLabel.setVisible(true);

        // Animation shake légère sur la carte
        TranslateTransition tt = new TranslateTransition(Duration.millis(50), loginCard);
        tt.setFromX(0);
        tt.setByX(10);
        tt.setCycleCount(4);
        tt.setAutoReverse(true);
        tt.play();
    }
}
