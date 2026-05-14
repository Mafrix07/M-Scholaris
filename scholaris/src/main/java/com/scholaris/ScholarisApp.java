package com.scholaris;

import com.scholaris.service.NavigationService;
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
        // Initialiser le service de navigation
        NavigationService.setPrimaryStage(primaryStage);

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
        primaryStage.setResizable(true);
        primaryStage.show();
    }
}
