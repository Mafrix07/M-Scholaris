package com.scholaris.controller;

import com.scholaris.dao.ClasseDAO;
import com.scholaris.dao.UtilisateurDAO;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import com.scholaris.model.Admin;
import com.scholaris.model.Utilisateur;
import com.scholaris.service.AuthService;

import java.io.IOException;
import java.sql.SQLException;
import java.util.List;

public class DashboardAdminController {

    @FXML private Label adminNameLabel;
    @FXML private Label statTotalUsers, statTotalStudents, statTotalTeachers, statTotalClasses;
    @FXML private VBox viewHome, viewUsers;
    @FXML private HBox menuHome, menuUsers, menuClasses, menuMatieres, menuBulletins;
    @FXML private TableView<Utilisateur> usersTable;
    @FXML private TableColumn<Utilisateur, String> colUserNom, colUserPrenom, colUserEmail, colUserRole;
    @FXML private TableColumn<Utilisateur, Boolean> colUserStatus;
    @FXML private TableColumn<Utilisateur, Void> colUserActions;
    @FXML private TextField userSearchField;

    private UtilisateurDAO utilisateurDAO;
    private ClasseDAO      classeDAO;
    private Admin          currentAdmin;
    private ObservableList<Utilisateur> masterData = FXCollections.observableArrayList();

    public void initialize() {
        try {
            this.utilisateurDAO = new UtilisateurDAO();
            this.classeDAO      = new ClasseDAO();
            
            this.currentAdmin = (Admin) AuthService.getUtilisateurConnecte();
            if (currentAdmin != null) {
                adminNameLabel.setText(currentAdmin.getNomComplet());
            }

            setupUsersTable();
            loadStats();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private void loadStats() throws SQLException {
        List<Utilisateur> all = utilisateurDAO.trouverTous();
        long students = all.stream().filter(u -> u.getRole().equals("etudiant")).count();
        long teachers = all.stream().filter(u -> u.getRole().equals("enseignant")).count();
        
        statTotalUsers.setText(String.valueOf(all.size()));
        statTotalStudents.setText(String.valueOf(students));
        statTotalTeachers.setText(String.valueOf(teachers));
        statTotalClasses.setText(String.valueOf(classeDAO.trouverTous().size()));
    }

    private void setupUsersTable() throws SQLException {
        colUserNom.setCellValueFactory(new PropertyValueFactory<>("nom"));
        colUserPrenom.setCellValueFactory(new PropertyValueFactory<>("prenom"));
        colUserEmail.setCellValueFactory(new PropertyValueFactory<>("email"));
        colUserRole.setCellValueFactory(new PropertyValueFactory<>("role"));
        
        // Custom cell for Role to add badges
        colUserRole.setCellFactory(column -> new TableCell<>() {
            @Override
            protected void updateItem(String role, boolean empty) {
                super.updateItem(role, empty);
                if (empty || role == null) {
                    setGraphic(null);
                } else {
                    Label label = new Label(role.toUpperCase());
                    label.getStyleClass().add("badge");
                    label.getStyleClass().add("badge-" + role.toLowerCase());
                    setGraphic(label);
                }
            }
        });

        // Custom cell for Status
        colUserStatus.setCellValueFactory(new PropertyValueFactory<>("actif"));
        colUserStatus.setCellFactory(column -> new TableCell<>() {
            @Override
            protected void updateItem(Boolean actif, boolean empty) {
                super.updateItem(actif, empty);
                if (empty || actif == null) setText(null);
                else setText(actif ? "Actif" : "Inactif");
            }
        });

        // Search logic
        masterData.setAll(utilisateurDAO.trouverTous());
        FilteredList<Utilisateur> filteredData = new FilteredList<>(masterData, p -> true);
        userSearchField.textProperty().addListener((observable, oldValue, newValue) -> {
            filteredData.setPredicate(user -> {
                if (newValue == null || newValue.isEmpty()) return true;
                String lowerCaseFilter = newValue.toLowerCase();
                if (user.getNom().toLowerCase().contains(lowerCaseFilter)) return true;
                if (user.getPrenom().toLowerCase().contains(lowerCaseFilter)) return true;
                return user.getEmail().toLowerCase().contains(lowerCaseFilter);
            });
        });
        usersTable.setItems(filteredData);
    }

    @FXML
    void showHome() {
        switchView(viewHome, menuHome);
    }

    @FXML
    void showUsers() {
        switchView(viewUsers, menuUsers);
    }

    @FXML void showClasses() { /* TODO */ }
    @FXML void showMatieres() { /* TODO */ }
    @FXML void showBulletins() { /* TODO */ }

    private void switchView(VBox view, HBox menu) {
        viewHome.setVisible(false);
        viewUsers.setVisible(false);
        view.setVisible(true);

        menuHome.getStyleClass().remove("sidebar-item-active");
        menuUsers.getStyleClass().remove("sidebar-item-active");
        menu.getStyleClass().add("sidebar-item-active");
    }

    @FXML
    void handleLogout(javafx.scene.input.MouseEvent event) throws IOException {
        AuthService auth = null;
        try { auth = new AuthService(); } catch (SQLException e) {}
        if (auth != null) auth.deconnecter();
        
        Parent root = FXMLLoader.load(getClass().getResource("/fxml/Login.fxml"));
        Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
        stage.setScene(new Scene(root));
        stage.show();
    }
}
