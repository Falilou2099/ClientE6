package com.gestionpharma.controllers;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.TextField;
import javafx.scene.control.PasswordField;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.stage.Stage;
import javafx.scene.Node;
import javafx.event.ActionEvent;

import com.gestionpharma.services.AuthService;
import com.gestionpharma.models.Admin;

public class LoginController {
    @FXML private TextField usernameField;
    @FXML private PasswordField passwordField;
    @FXML private TextField pharmacyName;
    @FXML private TextField pharmacyAddress;
    @FXML private TextField pharmacyPhone;
    @FXML private TextField pharmacyEmail;
    @FXML private TextField newUsername;
    @FXML private PasswordField newPassword;
    @FXML private PasswordField confirmPassword;

    @FXML
    protected void handleLogin(ActionEvent event) {
        String username = usernameField.getText();
        String password = passwordField.getText();

        if (username.isEmpty() || password.isEmpty()) {
            showError("Erreur", "Veuillez remplir tous les champs");
            return;
        }

        Admin admin = AuthService.login(username, password);
        if (admin != null) {
            try {
                FXMLLoader loader = new FXMLLoader(getClass().getResource("/views/admin_panel.fxml"));
                Scene scene = new Scene(loader.load());
                
                AdminPanelController controller = loader.getController();
                controller.setAdmin(admin);
                
                Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
                stage.setScene(scene);
                stage.show();
            } catch (Exception e) {
                e.printStackTrace();
                showError("Erreur", "Erreur lors du chargement du panneau d'administration");
            }
        } else {
            showError("Erreur", "Nom d'utilisateur ou mot de passe incorrect");
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
