package com.bigpharma.admin.views;

import com.bigpharma.admin.dao.UserDAO;
import com.bigpharma.admin.models.User;
import com.bigpharma.admin.utils.AlertUtils;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.scene.text.Text;
import javafx.stage.Stage;

/**
 * Vue de connexion pour l'application d'administration
 */
public class LoginView extends BorderPane {
    
    private final Stage primaryStage;
    private TextField emailField;
    private PasswordField passwordField;
    private Button loginButton;
    
    /**
     * Constructeur
     * @param primaryStage La fenêtre principale
     */
    public LoginView(Stage primaryStage) {
        this.primaryStage = primaryStage;
        initializeUI();
    }
    
    /**
     * Initialise l'interface utilisateur
     */
    private void initializeUI() {
        // Définir les couleurs de l'application (reprenant les codes couleurs de l'application PHP)
        String primaryColor = "#4e73df";
        String secondaryColor = "#1cc88a";
        String backgroundColor = "#f8f9fc";
        
        // Définir le fond
        setBackground(new Background(new BackgroundFill(
                Color.web(backgroundColor),
                CornerRadii.EMPTY,
                Insets.EMPTY)));
        
        // En-tête
        HBox header = createHeader(primaryColor);
        setTop(header);
        
        // Contenu principal - Formulaire de connexion
        VBox loginForm = createLoginForm(primaryColor, secondaryColor);
        setCenter(loginForm);
        
        // Pied de page
        HBox footer = createFooter();
        setBottom(footer);
    }
    
    /**
     * Crée l'en-tête de la vue
     * @param primaryColor Couleur principale
     * @return L'en-tête
     */
    private HBox createHeader(String primaryColor) {
        HBox header = new HBox();
        header.setAlignment(Pos.CENTER);
        header.setPadding(new Insets(20, 10, 20, 10));
        header.setBackground(new Background(new BackgroundFill(
                Color.web(primaryColor),
                CornerRadii.EMPTY,
                Insets.EMPTY)));
        
        // Logo et titre
        try {
            ImageView logoView = new ImageView(new Image(getClass().getResourceAsStream("/resources/images/logo_white.png")));
            logoView.setFitHeight(50);
            logoView.setFitWidth(50);
            header.getChildren().add(logoView);
        } catch (Exception e) {
            System.err.println("Impossible de charger le logo: " + e.getMessage());
        }
        
        Text title = new Text("BigPharma - Panneau d'Administration");
        title.setFont(Font.font("Arial", FontWeight.BOLD, 24));
        title.setFill(Color.WHITE);
        header.getChildren().add(title);
        header.setSpacing(15);
        
        return header;
    }
    
    /**
     * Crée le formulaire de connexion
     * @param primaryColor Couleur principale
     * @param secondaryColor Couleur secondaire
     * @return Le formulaire de connexion
     */
    private VBox createLoginForm(String primaryColor, String secondaryColor) {
        VBox loginForm = new VBox();
        loginForm.setAlignment(Pos.CENTER);
        loginForm.setSpacing(15);
        loginForm.setPadding(new Insets(50));
        loginForm.setMaxWidth(400);
        
        // Titre du formulaire
        Text formTitle = new Text("Connexion");
        formTitle.setFont(Font.font("Arial", FontWeight.BOLD, 20));
        formTitle.setFill(Color.web(primaryColor));
        
        // Champ email
        Label emailLabel = new Label("Email:");
        emailField = new TextField();
        emailField.setPromptText("Entrez votre email");
        emailField.setPrefHeight(40);
        
        // Champ mot de passe
        Label passwordLabel = new Label("Mot de passe:");
        passwordField = new PasswordField();
        passwordField.setPromptText("Entrez votre mot de passe");
        passwordField.setPrefHeight(40);
        
        // Bouton de connexion
        loginButton = new Button("Se connecter");
        loginButton.setPrefHeight(40);
        loginButton.setPrefWidth(200);
        loginButton.setStyle("-fx-background-color: " + primaryColor + "; -fx-text-fill: white;");
        
        // Ajouter l'action de connexion
        loginButton.setOnAction(e -> handleLogin());
        
        // Ajouter les éléments au formulaire
        loginForm.getChildren().addAll(
                formTitle,
                new Separator(),
                emailLabel,
                emailField,
                passwordLabel,
                passwordField,
                loginButton
        );
        
        // Centrer le formulaire
        BorderPane.setAlignment(loginForm, Pos.CENTER);
        
        return loginForm;
    }
    
    /**
     * Crée le pied de page
     * @return Le pied de page
     */
    private HBox createFooter() {
        HBox footer = new HBox();
        footer.setAlignment(Pos.CENTER);
        footer.setPadding(new Insets(10));
        
        Text footerText = new Text("© 2025 BigPharma - Tous droits réservés");
        footerText.setFont(Font.font("Arial", 12));
        footer.getChildren().add(footerText);
        
        return footer;
    }
    
    /**
     * Gère l'action de connexion
     */
    private void handleLogin() {
        String email = emailField.getText().trim();
        String password = passwordField.getText();
        
        // Vérifier que les champs ne sont pas vides
        if (email.isEmpty() || password.isEmpty()) {
            AlertUtils.showError("Erreur de connexion", "Veuillez remplir tous les champs.");
            return;
        }
        
        // Tenter l'authentification
        UserDAO userDAO = new UserDAO();
        User user = userDAO.authenticate(email, password);
        
        if (user != null) {
            // Vérifier si l'utilisateur a accès à l'application lourde
            if ("both".equals(user.getAppAccess()) || "heavy".equals(user.getAppAccess())) {
                // Connexion réussie, ouvrir la vue principale
                openMainView(user);
            } else {
                AlertUtils.showError("Accès refusé", 
                        "Vous n'avez pas les permissions nécessaires pour accéder à cette application.");
            }
        } else {
            // Échec de l'authentification
            AlertUtils.showError("Erreur de connexion", 
                    "Email ou mot de passe incorrect. Veuillez réessayer.");
        }
    }
    
    /**
     * Ouvre la vue principale après une connexion réussie
     * @param user L'utilisateur connecté
     */
    private void openMainView(User user) {
        try {
            MainView mainView = new MainView(primaryStage, user);
            Scene scene = new Scene(mainView, 1200, 800);
            scene.getStylesheets().add(getClass().getResource("/resources/css/styles.css").toExternalForm());
            
            primaryStage.setScene(scene);
            primaryStage.setTitle("BigPharma - Panneau d'Administration - " + user.getEmail());
            primaryStage.centerOnScreen();
            
        } catch (Exception e) {
            e.printStackTrace();
            AlertUtils.showError("Erreur", "Impossible d'ouvrir l'application: " + e.getMessage());
        }
    }
}
