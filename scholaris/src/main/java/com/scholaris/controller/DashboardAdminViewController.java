package com.scholaris.controller;

import com.scholaris.dao.ClasseDAO;
import com.scholaris.dao.EtudiantDAO;
import com.scholaris.model.Classe;
import com.scholaris.model.Etudiant;
import com.scholaris.service.MoyenneService;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressBar;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;

import java.sql.SQLException;
import java.util.List;

public class DashboardAdminViewController extends BaseController {

    @FXML private Label lblEtudiants, lblClasses, lblMoyenne, lblDifficulte;
    @FXML private VBox classesContainer, bulletinsContainer;

    private EtudiantDAO etudiantDAO;
    private ClasseDAO classeDAO;
    private MoyenneService moyenneService;

    @FXML
    public void initialize() {
        try {
            this.etudiantDAO = new EtudiantDAO();
            this.classeDAO = new ClasseDAO();
            this.moyenneService = new MoyenneService();

            loadKPIs();
            loadClassesDistribution();
            loadRecentBulletins();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private void loadKPIs() throws SQLException {
        List<Etudiant> etudiants = etudiantDAO.trouverTous();
        lblEtudiants.setText(String.valueOf(etudiants.size()));
        
        List<Classe> classes = classeDAO.trouverTous();
        lblClasses.setText(String.valueOf(classes.size()));

        // Calculer la moyenne globale de l'école (moyenne des moyennes des élèves)
        double totalMoyenne = 0;
        int difficulteCount = 0;
        for (Etudiant e : etudiants) {
            double moy = moyenneService.calculerMoyenneGenerale(e.getId(), "Trimestre 1", 2025);
            totalMoyenne += moy;
            if (moy < 10) difficulteCount++;
        }
        
        double avg = etudiants.isEmpty() ? 0 : totalMoyenne / etudiants.size();
        lblMoyenne.setText(String.format("%.2f", avg));
        lblDifficulte.setText(String.valueOf(difficulteCount));
    }

    private void loadClassesDistribution() throws SQLException {
        classesContainer.getChildren().clear();
        List<Classe> classes = classeDAO.trouverTous();
        for (Classe c : classes) {
            List<Etudiant> list = etudiantDAO.trouverParClasse(c.getId());
            int count = list.size();
            
            VBox item = new VBox(5);
            HBox info = new HBox();
            Label name = new Label(c.getNom());
            name.getStyleClass().add("text-navy");
            Region spacer = new Region();
            HBox.setHgrow(spacer, Priority.ALWAYS);
            Label countLabel = new Label(count + " élèves");
            countLabel.getStyleClass().add("text-muted");
            info.getChildren().addAll(name, spacer, countLabel);
            
            ProgressBar pb = new ProgressBar(count / 40.0); // Supposons max 40 par classe
            pb.setMaxWidth(Double.MAX_VALUE);
            pb.getStyleClass().add("progress-bar-blue");
            
            item.getChildren().addAll(info, pb);
            classesContainer.getChildren().add(item);
        }
    }

    private void loadRecentBulletins() throws SQLException {
        bulletinsContainer.getChildren().clear();
        List<Etudiant> recent = etudiantDAO.trouverTous(); // Simuler récent avec tous
        int limit = Math.min(recent.size(), 5);
        for (int i = 0; i < limit; i++) {
            Etudiant e = recent.get(i);
            HBox item = new HBox(12);
            item.setAlignment(javafx.geometry.Pos.CENTER_LEFT);
            
            Circle avatar = new Circle(18);
            avatar.setFill(Color.hsb((e.getNom().hashCode() & 0xFF) * 360.0 / 255, 0.5, 0.7));
            
            VBox nameBox = new VBox(2);
            Label nameLabel = new Label(e.getNomComplet());
            nameLabel.setStyle("-fx-font-weight: bold; -fx-font-size: 13;");
            Label classLabel = new Label(e.getClasse().getNom() + " · T1");
            classLabel.getStyleClass().add("text-muted");
            classLabel.setStyle("-fx-font-size: 11;");
            nameBox.getChildren().addAll(nameLabel, classLabel);
            
            Region spacer = new Region();
            HBox.setHgrow(spacer, Priority.ALWAYS);
            
            Label badge = new Label("Généré");
            badge.getStyleClass().addAll("badge", "badge-green");
            
            item.getChildren().addAll(avatar, nameBox, spacer, badge);
            bulletinsContainer.getChildren().add(item);
        }
    }

    @FXML private void handleRefresh() { initialize(); }
    @FXML private void handleVoirClasses() { if (mainController != null) mainController.loadView("/fxml/EtudiantView.fxml"); }
    @FXML private void handleVoirBulletins() { if (mainController != null) mainController.loadView("/fxml/BulletinView.fxml"); }
}
