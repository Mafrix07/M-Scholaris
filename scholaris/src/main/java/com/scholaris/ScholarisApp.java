package com.scholaris;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.stage.Stage;

/**
 * Classe gérant l'application JavaFX.
 */
public class ScholarisApp extends Application {

    @Override
    public void start(Stage primaryStage) throws Exception {
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/Login.fxml"));
        Parent root = loader.load();
        
        primaryStage.setTitle("Scholaris - Système de Gestion Scolaire");
        
        try {
            primaryStage.getIcons().add(new Image(getClass().getResourceAsStream("/images/Logo.png")));
        } catch (Exception e) {
            System.err.println("Logo non trouvé : " + e.getMessage());
        }
        
        Scene scene = new Scene(root);
        primaryStage.setScene(scene);
        primaryStage.setResizable(false);
        primaryStage.show();
    }
}
