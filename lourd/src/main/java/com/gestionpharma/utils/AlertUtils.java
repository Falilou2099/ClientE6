package com.gestionpharma.utils;

import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.ButtonType;
import javafx.stage.Stage;
import javafx.scene.control.DialogPane;

import java.util.Optional;

/**
 * Classe utilitaire pour afficher des alertes et des messages à l'utilisateur
 */
public class AlertUtils {
    
    /**
     * Affiche une alerte d'information
     * @param title Titre de l'alerte
     * @param header En-tête de l'alerte
     * @param content Contenu de l'alerte
     */
    public static void showInfoAlert(String title, String header, String content) {
        Alert alert = new Alert(AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(header);
        alert.setContentText(content);
        
        // Appliquer le style CSS
        DialogPane dialogPane = alert.getDialogPane();
        dialogPane.getStylesheets().add(
            AlertUtils.class.getResource("/css/modern-style.css").toExternalForm());
        dialogPane.getStyleClass().add("alert-dialog");
        
        Stage stage = (Stage) alert.getDialogPane().getScene().getWindow();
        stage.setAlwaysOnTop(true);
        
        alert.showAndWait();
    }
    
    /**
     * Affiche une alerte d'erreur
     * @param title Titre de l'alerte
     * @param header En-tête de l'alerte
     * @param content Contenu de l'alerte
     */
    public static void showErrorAlert(String title, String header, String content) {
        Alert alert = new Alert(AlertType.ERROR);
        alert.setTitle(title);
        alert.setHeaderText(header);
        alert.setContentText(content);
        
        // Appliquer le style CSS
        DialogPane dialogPane = alert.getDialogPane();
        dialogPane.getStylesheets().add(
            AlertUtils.class.getResource("/css/modern-style.css").toExternalForm());
        dialogPane.getStyleClass().add("alert-dialog");
        
        Stage stage = (Stage) alert.getDialogPane().getScene().getWindow();
        stage.setAlwaysOnTop(true);
        
        alert.showAndWait();
    }
    
    /**
     * Affiche une alerte d'avertissement
     * @param title Titre de l'alerte
     * @param header En-tête de l'alerte
     * @param content Contenu de l'alerte
     */
    public static void showWarningAlert(String title, String header, String content) {
        Alert alert = new Alert(AlertType.WARNING);
        alert.setTitle(title);
        alert.setHeaderText(header);
        alert.setContentText(content);
        
        // Appliquer le style CSS
        DialogPane dialogPane = alert.getDialogPane();
        dialogPane.getStylesheets().add(
            AlertUtils.class.getResource("/css/modern-style.css").toExternalForm());
        dialogPane.getStyleClass().add("alert-dialog");
        
        Stage stage = (Stage) alert.getDialogPane().getScene().getWindow();
        stage.setAlwaysOnTop(true);
        
        alert.showAndWait();
    }
    
    /**
     * Affiche une alerte de confirmation et retourne la réponse de l'utilisateur
     * @param title Titre de l'alerte
     * @param header En-tête de l'alerte
     * @param content Contenu de l'alerte
     * @return true si l'utilisateur a confirmé, false sinon
     */
    public static boolean showConfirmationAlert(String title, String header, String content) {
        Alert alert = new Alert(AlertType.CONFIRMATION);
        alert.setTitle(title);
        alert.setHeaderText(header);
        alert.setContentText(content);
        
        // Appliquer le style CSS
        DialogPane dialogPane = alert.getDialogPane();
        dialogPane.getStylesheets().add(
            AlertUtils.class.getResource("/css/modern-style.css").toExternalForm());
        dialogPane.getStyleClass().add("alert-dialog");
        
        Stage stage = (Stage) alert.getDialogPane().getScene().getWindow();
        stage.setAlwaysOnTop(true);
        
        Optional<ButtonType> result = alert.showAndWait();
        return result.isPresent() && result.get() == ButtonType.OK;
    }
}
