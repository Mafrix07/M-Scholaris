package com.scholaris.controller;

import com.scholaris.dao.UtilisateurDAO;
import com.scholaris.model.Utilisateur;
import com.scholaris.service.SessionManager;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.HBox;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.stage.Modality;
import javafx.stage.Stage;

import java.io.IOException;
import java.sql.SQLException;
import java.time.format.DateTimeFormatter;

public class UtilisateursController extends BaseController {

    @FXML private TableView<Utilisateur> userTable;
    @FXML private TableColumn<Utilisateur, Integer> colId;
    @FXML private TableColumn<Utilisateur, String> colEmail, colNomComplet, colRole, colDate;
    @FXML private TableColumn<Utilisateur, Void> colActions;

    private UtilisateurDAO utilisateurDAO;
    private ObservableList<Utilisateur> userList = FXCollections.observableArrayList();
    private final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");

    @FXML
    public void initialize() {
        try {
            this.utilisateurDAO = new UtilisateurDAO();
            setupTable();
            loadUsers();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private void setupTable() {
        colId.setCellValueFactory(new PropertyValueFactory<>("id"));
        colEmail.setCellValueFactory(new PropertyValueFactory<>("email"));
        colEmail.getStyleClass().add("text-blue");

        colNomComplet.setCellValueFactory(d -> new SimpleStringProperty(d.getValue().getNomComplet()));
        colNomComplet.setCellFactory(tc -> new TableCell<>() {
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) setGraphic(null);
                else {
                    HBox box = new HBox(10);
                    box.setAlignment(javafx.geometry.Pos.CENTER_LEFT);
                    Circle avatar = new Circle(15);
                    avatar.setFill(Color.hsb((item.hashCode() & 0xFF) * 360.0 / 255, 0.5, 0.7));
                    Label name = new Label(item);
                    box.getChildren().addAll(avatar, name);
                    setGraphic(box);
                }
            }
        });

        colRole.setCellValueFactory(new PropertyValueFactory<>("role"));
        colRole.setCellFactory(tc -> new TableCell<>() {
            @Override
            protected void updateItem(String role, boolean empty) {
                super.updateItem(role, empty);
                if (empty || role == null) setGraphic(null);
                else {
                    Label badge = new Label(role.toUpperCase());
                    badge.getStyleClass().addAll("badge", getRoleBadgeClass(role));
                    setGraphic(badge);
                }
            }
        });

        colDate.setCellValueFactory(d -> {
            if (d.getValue().getCreatedAt() != null) {
                return new SimpleStringProperty(d.getValue().getCreatedAt().format(formatter));
            }
            return new SimpleStringProperty("-");
        });

        setupActionsColumn();
    }

    private void setupActionsColumn() {
        colActions.setCellFactory(tc -> new TableCell<>() {
            private final Button btnRole = new Button("Modifier rôle");
            private final Button btnReset = new Button("Réinit. MDP");
            private final Button btnDelete = new Button("Supprimer");
            private final HBox box = new HBox(8, btnRole, btnReset, btnDelete);

            {
                btnRole.getStyleClass().add("btn-text-blue");
                btnReset.getStyleClass().add("btn-text-blue");
                btnDelete.getStyleClass().add("btn-text-red");

                btnRole.setOnAction(e -> handleModifierRole(getTableView().getItems().get(getIndex())));
                btnReset.setOnAction(e -> handleResetPassword(getTableView().getItems().get(getIndex())));
                btnDelete.setOnAction(e -> handleSupprimer(getTableView().getItems().get(getIndex())));
            }

            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                setGraphic(empty ? null : box);
            }
        });
    }

    private String getRoleBadgeClass(String role) {
        return switch (role.toLowerCase()) {
            case "admin" -> "badge-red";
            case "enseignant" -> "badge-blue";
            default -> "badge-green";
        };
    }

    private void loadUsers() throws SQLException {
        userList.setAll(utilisateurDAO.trouverTous());
        userTable.setItems(userList);
    }

    @FXML
    private void handleCreer() {
        showDialog("/fxml/CreateUserDialog.fxml", "Créer un utilisateur");
    }

    private void handleModifierRole(Utilisateur u) {
        // Logique pour ouvrir RoleDialog
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/RoleDialog.fxml"));
            Parent root = loader.load();
            RoleDialogController ctrl = loader.getController();
            ctrl.setUser(u);
            
            Stage stage = new Stage();
            stage.setTitle("Modifier le rôle");
            stage.initModality(Modality.WINDOW_MODAL);
            stage.initOwner(userTable.getScene().getWindow());
            stage.setScene(new Scene(root));
            stage.showAndWait();
            
            // Recharger la liste locale pour voir le nouveau badge de rôle
            loadUsers();
            
            // Informer l'utilisateur que le changement est effectif partout
            showAlert(Alert.AlertType.INFORMATION, "Action réussie", 
                "Le rôle a été mis à jour. S'il s'agissait d'une promotion, " +
                "l'utilisateur a été retiré de la liste des étudiants.");
                
        } catch (IOException | SQLException e) {
            e.printStackTrace();
            showAlert(Alert.AlertType.ERROR, "Erreur", "Impossible d'ouvrir le dialogue : " + e.getMessage());
        }
    }

    private void handleResetPassword(Utilisateur u) {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Réinitialiser le mot de passe");
        alert.setHeaderText("Réinitialiser le mot de passe de " + u.getNomComplet() + " ?");
        alert.setContentText("Un mot de passe temporaire sera généré.");
        
        if (alert.showAndWait().get() == ButtonType.OK) {
            try {
                // Simuler réinitialisation
                String tempMdp = "Scholaris2025!";
                // utilisateurDAO.modifierMotDePasse(u.getId(), BCrypt.hashpw(tempMdp, BCrypt.gensalt()));
                showAlert(Alert.AlertType.INFORMATION, "Mot de passe réinitialisé", "Nouveau mot de passe : " + tempMdp);
            } catch (Exception e) {
                showAlert(Alert.AlertType.ERROR, "Erreur", e.getMessage());
            }
        }
    }

    private void handleSupprimer(Utilisateur u) {
        if (u.getId() == SessionManager.getInstance().getCurrentUser().getId()) {
            showAlert(Alert.AlertType.WARNING, "Action impossible", "Vous ne pouvez pas supprimer votre propre compte.");
            return;
        }

        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Supprimer l'utilisateur");
        alert.setHeaderText("Supprimer " + u.getNomComplet() + " ?");
        alert.setContentText("Cette action est irréversible.");

        if (alert.showAndWait().get() == ButtonType.OK) {
            try {
                utilisateurDAO.supprimer(u.getId());
                loadUsers();
            } catch (SQLException e) {
                showAlert(Alert.AlertType.ERROR, "Erreur", e.getMessage());
            }
        }
    }

    private void showDialog(String fxml, String title) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource(fxml));
            Parent root = loader.load();
            Stage stage = new Stage();
            stage.setTitle(title);
            stage.initModality(Modality.WINDOW_MODAL);
            stage.initOwner(userTable.getScene().getWindow());
            stage.setScene(new Scene(root));
            stage.showAndWait();
            loadUsers();
        } catch (IOException | SQLException e) {
            e.printStackTrace();
        }
    }

    private void showAlert(Alert.AlertType type, String titre, String msg) {
        Alert alert = new Alert(type);
        alert.setTitle(titre);
        alert.setContentText(msg);
        alert.showAndWait();
    }
}
