package com.gestionpharma;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

public class Main extends Application {

    @Override
    public void start(Stage primaryStage) throws Exception {
        // Configuration de la fenêtre principale
        primaryStage.setTitle("Gestion Pharmacie - Administration");
        
        // Chargement de l'interface de connexion
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/views/login.fxml"));
        Parent root = loader.load();
        
        // Configuration de la scène avec une taille plus grande
        Scene scene = new Scene(root, 1280, 720);
        scene.getStylesheets().add(getClass().getResource("/styles/main.css").toExternalForm());
        
        primaryStage.setScene(scene);
        primaryStage.setResizable(true); // Permettre le redimensionnement
        primaryStage.setMaximized(true); // Ouvrir en mode maximisé
        
        // Commenter la ligne suivante si vous préférez le mode plein écran complet
        // primaryStage.setFullScreen(true);
        
        primaryStage.show();
    }

    public static void main(String[] args) {
        launch(args);
    }
}
