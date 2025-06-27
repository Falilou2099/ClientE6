package com.gestionpharma.controllers;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.application.Platform;
import javafx.scene.Scene;
import javafx.scene.control.TextField;
import javafx.scene.control.PasswordField;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.ButtonBar;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Label;
import javafx.stage.Stage;
import javafx.scene.Node;
import javafx.event.ActionEvent;

import java.util.Optional;

import com.gestionpharma.config.DatabaseConfig;
import com.gestionpharma.services.AuthService;
import com.gestionpharma.models.Admin;

public class LoginController {
    @FXML private TextField emailField; // Renommé de usernameField à emailField
    @FXML private PasswordField passwordField;
    @FXML private TextField pharmacyName;
    @FXML private TextField pharmacyAddress;
    @FXML private TextField pharmacyPhone;
    @FXML private TextField pharmacyEmail;
    @FXML private TextField newUsername;
    @FXML private PasswordField newPassword;
    @FXML private PasswordField confirmPassword;
    @FXML private Label emailLabel; // Nouveau label pour l'email

    @FXML
    protected void handleLogin(ActionEvent event) {
        String email = emailField.getText();
        String password = passwordField.getText();

        if (email.isEmpty() || password.isEmpty()) {
            showError("Erreur", "Veuillez remplir tous les champs");
            return;
        }
        
        // Validation basique de l'email
        if (!email.contains("@") || !email.contains(".")) {
            showError("Erreur", "Veuillez entrer une adresse email valide");
            return;
        }
        
        try {
            // Vérifier si le mode hors ligne est activé
            if (com.gestionpharma.config.DatabaseConfig.isOfflineMode()) {
                // Demander à l'utilisateur s'il souhaite réessayer la connexion ou continuer en mode démo
                Alert alert = new Alert(Alert.AlertType.WARNING);
                alert.setTitle("Mode hors ligne");
                alert.setHeaderText("Connexion à la base de données impossible");
                alert.setContentText("Le serveur de base de données semble inaccessible. Voulez-vous réessayer de vous connecter ou utiliser l'application en mode démonstration avec des données fictives ?");
                
                javafx.scene.control.ButtonType retryButton = new javafx.scene.control.ButtonType("Réessayer", javafx.scene.control.ButtonBar.ButtonData.OK_DONE);
                javafx.scene.control.ButtonType demoButton = new javafx.scene.control.ButtonType("Mode démo", javafx.scene.control.ButtonBar.ButtonData.APPLY);
                
                alert.getButtonTypes().setAll(retryButton, demoButton);
                
                java.util.Optional<javafx.scene.control.ButtonType> result = alert.showAndWait();
                if (result.isPresent()) {
                    if (result.get() == retryButton) {
                        if (com.gestionpharma.config.DatabaseConfig.tryReconnect()) {
                            // Si la reconnexion a réussi, réessayer la connexion
                            handleLogin(event);
                            return;
                        } else {
                            showError("Erreur", "La reconnexion a échoué. Veuillez vérifier votre serveur MySQL.");
                            return;
                        }
                    } else if (result.get() == demoButton) {
                        // Accéder en mode démo avec un compte administrateur fictif
                        loadDemoMode(event);
                        return;
                    }
                }
                return; // Ne pas continuer si aucune option n'est sélectionnée
            }

            // Tentative de connexion normale
            Admin admin = AuthService.login(email, password);
            if (admin != null) {
                loadAdminPanel(event, admin);
            } else {
                showError("Erreur", "Email ou mot de passe incorrect, ou vous n'avez pas accès à l'application lourde");
            }
        } catch (Exception e) {
            e.printStackTrace();
            showError("Erreur de connexion", "Une erreur s'est produite lors de la tentative de connexion : " + e.getMessage());
        }
    }
    
    /**
     * Charge le panneau d'administration avec un utilisateur administrateur
     */
    private void loadAdminPanel(ActionEvent event, Admin admin) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/views/admin_panel.fxml"));
            Scene scene = new Scene(loader.load(), 1280, 720);
            scene.getStylesheets().add(getClass().getResource("/styles/main.css").toExternalForm());
            
            AdminPanelController controller = loader.getController();
            controller.setAdmin(admin);
            
            Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
            stage.setScene(scene);
            stage.setResizable(true);
            stage.setMaximized(true);
            stage.show();
        } catch (Exception e) {
            e.printStackTrace();
            showError("Erreur", "Erreur lors du chargement du panneau d'administration");
        }
    }
    
    /**
     * Charge l'application en mode démonstration avec des données fictives
     */
    private void loadDemoMode(ActionEvent event) {
        try {
            // Créer un administrateur factice pour le mode démo avec le constructeur disponible
            Admin demoAdmin = new Admin(999, "demo", "Utilisateur", "Démo", "demo@pharmacie.fr", 1);
            
            // Charger l'interface avec cet administrateur démo
            loadAdminPanel(event, demoAdmin);
            
            // Afficher un message indiquant le mode démo
            javafx.application.Platform.runLater(() -> {
                Alert alert = new Alert(Alert.AlertType.INFORMATION);
                alert.setTitle("Mode démonstration");
                alert.setHeaderText("Application lancée en mode démonstration");
                alert.setContentText("Vous utilisez l'application en mode démonstration avec des données fictives.\n" +
                                     "Certaines fonctionnalités peuvent être limitées.\n\n" +
                                     "Pour utiliser l'application complète, veuillez démarrer votre serveur MySQL.");
                alert.showAndWait();
            });
        } catch (Exception e) {
            e.printStackTrace();
            showError("Erreur", "Erreur lors du chargement du mode démonstration");
        }
    }

    @FXML
    protected void handleRegister() {
        if (!validateRegistrationFields()) {
            return;
        }

        // TODO: Implémenter la logique d'inscription
        showInfo("Inscription", "Tentative d'inscription pour la pharmacie : " + pharmacyName.getText());
    }

    private boolean validateRegistrationFields() {
        if (pharmacyName.getText().isEmpty() || pharmacyAddress.getText().isEmpty() ||
            pharmacyPhone.getText().isEmpty() || pharmacyEmail.getText().isEmpty() ||
            newUsername.getText().isEmpty() || newPassword.getText().isEmpty() ||
            confirmPassword.getText().isEmpty()) {
            showError("Erreur", "Tous les champs sont obligatoires");
            return false;
        }

        if (!newPassword.getText().equals(confirmPassword.getText())) {
            showError("Erreur", "Les mots de passe ne correspondent pas");
            return false;
        }

        return true;
    }

    private void showError(String title, String content) {
        Alert alert = new Alert(AlertType.ERROR);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.showAndWait();
    }

    private void showInfo(String title, String content) {
        Alert alert = new Alert(AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.showAndWait();
    }
}
