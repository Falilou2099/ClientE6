package com.bigpharma.admin.utils;

import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import javafx.stage.Stage;

import java.util.Optional;

/**
 * Classe utilitaire pour afficher des alertes et des boîtes de dialogue
 */
public class AlertUtils {
    
    /**
     * Affiche une alerte d'information
     * @param title Le titre de l'alerte
     * @param message Le message à afficher
     */
    public static void showInfo(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
    
    /**
     * Affiche une alerte d'erreur
     * @param title Le titre de l'alerte
     * @param message Le message d'erreur à afficher
     */
    public static void showError(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
    
    /**
     * Affiche une alerte d'avertissement
     * @param title Le titre de l'alerte
     * @param message Le message d'avertissement à afficher
     */
    public static void showWarning(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.WARNING);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
    
    /**
     * Affiche une boîte de dialogue de confirmation
     * @param title Le titre de la boîte de dialogue
     * @param message Le message à afficher
     * @return true si l'utilisateur a confirmé, false sinon
     */
    public static boolean showConfirmation(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        
        Optional<ButtonType> result = alert.showAndWait();
        return result.isPresent() && result.get() == ButtonType.OK;
    }
    
    /**
     * Affiche une boîte de dialogue personnalisée
     * @param alertType Le type d'alerte
     * @param title Le titre de la boîte de dialogue
     * @param header L'en-tête de la boîte de dialogue (peut être null)
     * @param message Le message à afficher
     * @return Le résultat de la boîte de dialogue
     */
    public static Optional<ButtonType> showCustomDialog(Alert.AlertType alertType, String title, String header, String message) {
        Alert alert = new Alert(alertType);
        alert.setTitle(title);
        alert.setHeaderText(header);
        alert.setContentText(message);
        return alert.showAndWait();
    }
    
    /**
     * Centre une alerte sur une fenêtre parente
     * @param alert L'alerte à centrer
     * @param owner La fenêtre parente
     */
    public static void centerAlert(Alert alert, Stage owner) {
        if (owner != null) {
            alert.initOwner(owner);
        }
    }
}
