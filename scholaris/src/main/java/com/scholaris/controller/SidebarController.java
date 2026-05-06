package com.scholaris.controller;

import com.scholaris.model.Utilisateur;
import com.scholaris.service.SessionManager;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.stage.Stage;

import java.io.IOException;

public class SidebarController {

    @FXML private VBox navContainer;
    
    // Admin Items
    @FXML private HBox navAdminDashboard, navAdminEtudiants, navAdminSaisie, navAdminBulletins, navAdminUsers, navAdminParams;
    @FXML private Region adminSeparator;
    @FXML private Label adminLabel;

    // Teacher Items
    @FXML private HBox navTeacherDashboard, navTeacherSaisie, navTeacherMesEtudiants;

    // Student Items
    @FXML private HBox navStudentBulletin, navStudentNotes;

    @FXML private Circle userAvatar;
    @FXML private Label userRoleLabel;
    @FXML private Label userNameLabel;

    private MainLayoutController mainController;

    @FXML
    public void initialize() {
        Utilisateur user = SessionManager.getInstance().getCurrentUser();
        if (user != null) {
            userNameLabel.setText(user.getNom() + " " + user.getPrenom());
            userRoleLabel.setText(user.getRole().toUpperCase());
            
            // Avatar color
            String name = user.getNom() + user.getPrenom();
            Color c = Color.hsb((name.hashCode() & 0xFF) * 360.0 / 255, 0.5, 0.7);
            userAvatar.setFill(c);

            configureVisibility(user.getRole().toLowerCase());
        }
    }

    public void setMainController(MainLayoutController mainController) {
        this.mainController = mainController;
    }

    private void configureVisibility(String role) {
        // Reset visibility
        navAdminDashboard.setManaged(false); navAdminDashboard.setVisible(false);
        navAdminEtudiants.setManaged(false); navAdminEtudiants.setVisible(false);
        navAdminSaisie.setManaged(false); navAdminSaisie.setVisible(false);
        navAdminBulletins.setManaged(false); navAdminBulletins.setVisible(false);
        navAdminUsers.setManaged(false); navAdminUsers.setVisible(false);
        navAdminParams.setManaged(false); navAdminParams.setVisible(false);
        adminSeparator.setManaged(false); adminSeparator.setVisible(false);
        adminLabel.setManaged(false); adminLabel.setVisible(false);

        navTeacherDashboard.setManaged(false); navTeacherDashboard.setVisible(false);
        navTeacherSaisie.setManaged(false); navTeacherSaisie.setVisible(false);
        navTeacherMesEtudiants.setManaged(false); navTeacherMesEtudiants.setVisible(false);

        navStudentBulletin.setManaged(false); navStudentBulletin.setVisible(false);
        navStudentNotes.setManaged(false); navStudentNotes.setVisible(false);

        switch (role) {
            case "admin":
                navAdminDashboard.setManaged(true); navAdminDashboard.setVisible(true);
                navAdminEtudiants.setManaged(true); navAdminEtudiants.setVisible(true);
                navAdminSaisie.setManaged(true); navAdminSaisie.setVisible(true);
                navAdminBulletins.setManaged(true); navAdminBulletins.setVisible(true);
                navAdminUsers.setManaged(true); navAdminUsers.setVisible(true);
                navAdminParams.setManaged(true); navAdminParams.setVisible(true);
                adminSeparator.setManaged(true); adminSeparator.setVisible(true);
                adminLabel.setManaged(true); adminLabel.setVisible(true);
                break;
            case "enseignant":
                navTeacherDashboard.setManaged(true); navTeacherDashboard.setVisible(true);
                navTeacherSaisie.setManaged(true); navTeacherSaisie.setVisible(true);
                navTeacherMesEtudiants.setManaged(true); navTeacherMesEtudiants.setVisible(true);
                break;
            case "etudiant":
                navStudentBulletin.setManaged(true); navStudentBulletin.setVisible(true);
                navStudentNotes.setManaged(true); navStudentNotes.setVisible(true);
                break;
        }
    }

    @FXML private void handleNavAdminDashboard() { load("/fxml/DashboardAdminView.fxml", navAdminDashboard); }
    @FXML private void handleNavAdminEtudiants() { load("/fxml/EtudiantView.fxml", navAdminEtudiants); }
    @FXML private void handleNavSaisieNotes() { load("/fxml/SaisieNotesView.fxml", null); } // Shared
    @FXML private void handleNavAdminBulletins() { load("/fxml/BulletinView.fxml", navAdminBulletins); }
    @FXML private void handleNavAdminUsers() { load("/fxml/UtilisateursView.fxml", navAdminUsers); }
    @FXML private void handleNavAdminParams() { load("/fxml/ParametresView.fxml", navAdminParams); }

    @FXML private void handleNavTeacherDashboard() { load("/fxml/EnseignantDashboardView.fxml", navTeacherDashboard); }
    @FXML private void handleNavTeacherMesEtudiants() { load("/fxml/MesEtudiantsView.fxml", navTeacherMesEtudiants); }

    @FXML private void handleNavStudentBulletin() { load("/fxml/BulletinEtudiantView.fxml", navStudentBulletin); }
    @FXML private void handleNavStudentNotes() { load("/fxml/MesNotesView.fxml", navStudentNotes); }

    private void load(String fxml, HBox item) {
        if (mainController != null) {
            mainController.loadView(fxml);
            updateActiveStyle(item);
        } else {
            // If Sidebar is included in MainLayout.fxml, it will get the controller later or we need another way
            // Actually Sidebar.fxml is included in MainLayout.fxml. 
            // We need a way to link them.
        }
    }

    private void updateActiveStyle(HBox activeItem) {
        navContainer.getChildren().forEach(node -> {
            if (node instanceof HBox) node.getStyleClass().remove("sidebar-item-active");
        });
        if (activeItem != null) activeItem.getStyleClass().add("sidebar-item-active");
    }

    @FXML
    private void handleLogout() throws IOException {
        SessionManager.getInstance().logout();
        Parent root = FXMLLoader.load(getClass().getResource("/fxml/Login.fxml"));
        Stage stage = (Stage) navContainer.getScene().getWindow();
        stage.setScene(new Scene(root));
        stage.show();
    }
}
