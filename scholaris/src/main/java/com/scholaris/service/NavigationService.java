package com.scholaris.service;

import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.io.IOException;

public class NavigationService {
    
    private static Stage primaryStage;

    public static void setPrimaryStage(Stage stage) {
        primaryStage = stage;
    }

    public static void loadView(String fxmlFile) {
        try {
            Parent root = FXMLLoader.load(NavigationService.class.getResource("/fxml/" + fxmlFile));
            if (primaryStage != null) {
                primaryStage.setScene(new Scene(root));
                primaryStage.show();
            }
        } catch (IOException e) {
            System.err.println("Erreur de chargement de la vue : " + fxmlFile);
            e.printStackTrace();
        }
    }
}
