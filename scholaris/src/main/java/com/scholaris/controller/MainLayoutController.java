package com.scholaris.controller;

import com.scholaris.model.Utilisateur;
import com.scholaris.service.SessionManager;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.control.Label;
import javafx.scene.layout.StackPane;

import java.io.IOException;

public class MainLayoutController {

    @FXML private StackPane contentArea;
    @FXML private Label userNameLabel;
    @FXML private Label userRoleLabel;
    @FXML private javafx.scene.shape.Circle userAvatar;
    @FXML private SidebarController sidebarController;

    @FXML
    public void initialize() {
        Utilisateur user = SessionManager.getInstance().getCurrentUser();
        if (user != null) {
            userNameLabel.setText(user.getNom() + " " + user.getPrenom());
            userRoleLabel.setText(user.getRole().toUpperCase());
            
            // Avatar color
            String name = user.getNom() + user.getPrenom();
            javafx.scene.paint.Color c = javafx.scene.paint.Color.hsb((name.hashCode() & 0xFF) * 360.0 / 255, 0.5, 0.7);
            userAvatar.setFill(c);

            // Liaison avec la sidebar
            if (sidebarController != null) {
                sidebarController.setMainController(this);
            }

            String role = user.getRole().toUpperCase();
            switch (role) {
                case "ADMIN" -> loadView("/fxml/DashboardAdminView.fxml");
                case "ENSEIGNANT" -> loadView("/fxml/EnseignantDashboardView.fxml");
                case "ETUDIANT" -> loadView("/fxml/MesNotesView.fxml");
                default -> loadView("/fxml/Login.fxml");
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
