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
    @FXML private ComboBox<com.scholaris.model.Matiere> matiereCombo;
    @FXML private VBox classesSection;
    @FXML private ListView<Classe> classesList;

    private UtilisateurDAO utilisateurDAO;
    private ClasseDAO classeDAO;
    private com.scholaris.dao.MatiereDAO matiereDAO;
    private com.scholaris.dao.EnseignantMatiereDAO emDAO;
    private Utilisateur currentUser;

    @FXML
    public void initialize() {
        try {
            this.utilisateurDAO = new UtilisateurDAO();
            this.classeDAO = new ClasseDAO();
            this.matiereDAO = new com.scholaris.dao.MatiereDAO();
            this.emDAO = new com.scholaris.dao.EnseignantMatiereDAO();

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

            loadData();
        } catch (java.sql.SQLException e) {
            e.printStackTrace();
        }
    }

    private void loadData() throws java.sql.SQLException {
        classesList.setItems(FXCollections.observableArrayList(classeDAO.trouverTous()));
        matiereCombo.setItems(FXCollections.observableArrayList(matiereDAO.trouverTous()));
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
        java.sql.Connection conn = null;
        try {
            conn = com.scholaris.util.DbConnection.getInstance();
            conn.setAutoCommit(false); // Démarrer la transaction

            String oldRole = currentUser.getRole().toLowerCase();
            String newRole = roleCombo.getValue().toLowerCase();
            
            // 1. Mise à jour du rôle dans la table utilisateur
            currentUser.setRole(newRole);
            utilisateurDAO.modifier(currentUser);
            
            // 2. Si l'étudiant est promu (devient Enseignant ou Admin)
            if ("etudiant".equals(oldRole) && !"etudiant".equals(newRole)) {
                int id = currentUser.getId();
                
                // Nettoyage complet du passé scolaire pour éviter les erreurs de clés étrangères
                try (java.sql.PreparedStatement ps1 = conn.prepareStatement("DELETE FROM note WHERE etudiant_id = ?");
                     java.sql.PreparedStatement ps2 = conn.prepareStatement("DELETE FROM bulletin WHERE etudiant_id = ?");
                     java.sql.PreparedStatement ps3 = conn.prepareStatement("DELETE FROM etudiant WHERE utilisateur_id = ?")) {
                    
                    ps1.setInt(1, id);
                    ps1.executeUpdate();
                    
                    ps2.setInt(1, id);
                    ps2.executeUpdate();
                    
                    ps3.setInt(1, id);
                    ps3.executeUpdate();
                }
            }
            
            // 3. Si un non-étudiant est rétrogradé en Étudiant
            if (!"etudiant".equals(oldRole) && "etudiant".equals(newRole)) {
                List<Classe> classes = classeDAO.trouverTous();
                if (!classes.isEmpty()) {
                    try (java.sql.PreparedStatement ps = conn.prepareStatement(
                            "INSERT INTO etudiant (utilisateur_id, matricule, classe_id) VALUES (?, ?, ?)")) {
                        ps.setInt(1, currentUser.getId());
                        ps.setString(2, "MAT-" + System.currentTimeMillis() % 100000);
                        ps.setInt(3, classes.get(0).getId());
                        ps.executeUpdate();
                    }
                }
            }

            // 4. Gestion des attributions Enseignant
            if ("enseignant".equals(newRole)) {
                com.scholaris.model.Matiere mat = matiereCombo.getValue();
                ObservableList<Classe> selectedClasses = classesList.getSelectionModel().getSelectedItems();

                if (mat != null && !selectedClasses.isEmpty()) {
                    for (Classe c : selectedClasses) {
                        try (java.sql.PreparedStatement ps = conn.prepareStatement(
                                "INSERT INTO enseignant_matiere (utilisateur_id, matiere_id, classe_id) VALUES (?, ?, ?)")) {
                            ps.setInt(1, currentUser.getId());
                            ps.setInt(2, mat.getId());
                            ps.setInt(3, c.getId());
                            ps.executeUpdate();
                        }
                    }
                }
            }

            conn.commit(); // Valider toute l'opération
            handleClose();
            
        } catch (java.sql.SQLException e) {
            if (conn != null) {
                try { conn.rollback(); } catch (java.sql.SQLException ex) { ex.printStackTrace(); }
            }
            e.printStackTrace();
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("Erreur critique");
            alert.setContentText("Échec de la promotion : " + e.getMessage());
            alert.showAndWait();
        } finally {
            if (conn != null) {
                try { conn.setAutoCommit(true); } catch (java.sql.SQLException e) { e.printStackTrace(); }
            }
        }
    }

    @FXML
    private void handleClose() {
        ((Stage) lblUsername.getScene().getWindow()).close();
    }
}
