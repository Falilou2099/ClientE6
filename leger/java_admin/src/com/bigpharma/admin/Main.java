package com.bigpharma.admin;

import com.bigpharma.admin.views.LoginView;
import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.stage.Stage;

/**
 * Classe principale de l'application d'administration BigPharma
 * Point d'entrée de l'application JavaFX
 */
public class Main extends Application {
    
    @Override
    public void start(Stage primaryStage) {
        try {
            // Configurer la fenêtre principale
            primaryStage.setTitle("BigPharma - Panneau d'Administration");
            
            // Charger l'icône de l'application
            try {
                primaryStage.getIcons().add(new Image(getClass().getResourceAsStream("/resources/images/logo.png")));
            } catch (Exception e) {
                System.err.println("Impossible de charger l'icône de l'application: " + e.getMessage());
            }
            
            // Créer et afficher la vue de connexion
            LoginView loginView = new LoginView(primaryStage);
            Scene scene = new Scene(loginView, 800, 600);
            
            // Ajouter les styles CSS
            scene.getStylesheets().add(getClass().getResource("/resources/css/styles.css").toExternalForm());
            
            primaryStage.setScene(scene);
            primaryStage.setMinWidth(800);
            primaryStage.setMinHeight(600);
            primaryStage.show();
            
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    
    /**
     * Méthode principale
     * @param args Arguments de la ligne de commande
     */
    public static void main(String[] args) {
        launch(args);
    }
}
