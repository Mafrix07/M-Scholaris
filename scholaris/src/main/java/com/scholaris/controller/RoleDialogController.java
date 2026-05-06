package com.scholaris.controller;

import com.scholaris.dao.ClasseDAO;
import com.scholaris.dao.UtilisateurDAO;
import com.scholaris.model.Classe;
import com.scholaris.model.Utilisateur;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

import java.sql.SQLException;
import java.util.List;

public class RoleDialogController {

    @FXML private Label lblUsername, lblRoleActuel;
    @FXML private ComboBox<String> roleCombo;
    @FXML private VBox classesSection;
    @FXML private ListView<Classe> classesList;

    private UtilisateurDAO utilisateurDAO;
    private ClasseDAO classeDAO;
    private Utilisateur currentUser;

    @FXML
    public void initialize() {
        try {
            this.utilisateurDAO = new UtilisateurDAO();
            this.classeDAO = new ClasseDAO();

            roleCombo.setItems(FXCollections.observableArrayList("admin", "enseignant", "etudiant"));
            roleCombo.valueProperty().addListener((obs, oldVal, newVal) -> {
                boolean isEnseignant = "enseignant".equals(newVal);
                classesSection.setVisible(isEnseignant);
                classesSection.setManaged(isEnseignant);
            });

            classesList.getSelectionModel().setSelectionMode(SelectionMode.MULTIPLE);
            classesList.setCellFactory(lv -> new ListCell<>() {
                @Override
                protected void updateItem(Classe c, boolean empty) {
                    super.updateItem(c, empty);
                    if (empty || c == null) setText(null);
                    else setText(c.getNom() + " (" + c.getNiveau() + ")");
                }
            });

            loadClasses();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private void loadClasses() throws SQLException {
        classesList.setItems(FXCollections.observableArrayList(classeDAO.trouverTous()));
    }

    public void setUser(Utilisateur u) {
        this.currentUser = u;
        lblUsername.setText("Utilisateur : " + u.getNomComplet() + " (" + u.getEmail() + ")");
        lblRoleActuel.setText(u.getRole().toUpperCase());
        lblRoleActuel.getStyleClass().setAll("badge", getRoleBadgeClass(u.getRole()));
        roleCombo.setValue(u.getRole());
    }

    private String getRoleBadgeClass(String role) {
        return switch (role.toLowerCase()) {
            case "admin" -> "badge-red";
            case "enseignant" -> "badge-blue";
            default -> "badge-green";
        };
    }

    @FXML
    private void handleEnregistrer() {
        try {
            String newRole = roleCombo.getValue();
            currentUser.setRole(newRole);
            utilisateurDAO.modifier(currentUser);
            
            // Si enseignant, on pourrait aussi enregistrer les liaisons enseignant_matiere ici
            // Mais pour simplifier, on se contente de modifier le rôle de base.
            
            handleClose();
        } catch (SQLException e) {
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("Erreur");
            alert.setContentText("Impossible d'enregistrer les modifications : " + e.getMessage());
            alert.showAndWait();
        }
    }

    @FXML
    private void handleClose() {
        ((Stage) lblUsername.getScene().getWindow()).close();
    }
}
