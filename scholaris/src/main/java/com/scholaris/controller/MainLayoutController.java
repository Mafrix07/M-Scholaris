package com.scholaris.controller;

import com.scholaris.model.Utilisateur;
import com.scholaris.service.SessionManager;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.layout.StackPane;

import java.io.IOException;

public class MainLayoutController {

    @FXML
    private StackPane contentArea;

    @FXML
    public void initialize() {
        Utilisateur user = SessionManager.getInstance().getCurrentUser();
        if (user != null) {
            String role = user.getRole().toUpperCase();
            switch (role) {
                case "ADMIN":
                    loadView("/fxml/DashboardAdminView.fxml");
                    break;
                case "ENSEIGNANT":
                    loadView("/fxml/EnseignantDashboardView.fxml");
                    break;
                case "ETUDIANT":
                    loadView("/fxml/BulletinEtudiantView.fxml");
                    break;
                default:
                    // Fallback or error
                    break;
            }
        }
    }

    public void loadView(String fxmlPath) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource(fxmlPath));
            Parent view = loader.load();
            
            // Si le controller enfant a besoin d'une référence au MainLayoutController
            Object controller = loader.getController();
            if (controller instanceof BaseController) {
                ((BaseController) controller).setMainController(this);
            }

            contentArea.getChildren().setAll(view);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
