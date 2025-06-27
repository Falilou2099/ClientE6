package com.bigpharma.admin.views;

import com.bigpharma.admin.dao.PharmacyDAO;
import com.bigpharma.admin.models.Pharmacy;
import com.bigpharma.admin.models.User;
import com.bigpharma.admin.utils.AlertUtils;
import com.bigpharma.admin.views.products.ProductListView;
import com.bigpharma.admin.views.orders.OrderListView;
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
 * Vue principale de l'application d'administration
 * Contient le menu latéral et le contenu principal
 */
public class MainView extends BorderPane {
    
    private final Stage primaryStage;
    private final User currentUser;
    private Pharmacy currentPharmacy;
    private Pane contentPane;
    private ComboBox<Pharmacy> pharmacySelector;
    
    // Couleurs de l'application (reprenant les codes couleurs de l'application PHP)
    private final String primaryColor = "#4e73df";
    private final String secondaryColor = "#1cc88a";
    private final String backgroundColor = "#f8f9fc";
    private final String sidebarColor = "#2d3748";
    private final String textColor = "#5a5c69";
    
    /**
     * Constructeur
     * @param primaryStage La fenêtre principale
     * @param user L'utilisateur connecté
     */
    public MainView(Stage primaryStage, User user) {
        this.primaryStage = primaryStage;
        this.currentUser = user;
        
        // Charger la pharmacie de l'utilisateur si elle existe
        if (user.getPharmacyId() != null) {
            PharmacyDAO pharmacyDAO = new PharmacyDAO();
            this.currentPharmacy = pharmacyDAO.findById(user.getPharmacyId());
        }
        
        initializeUI();
    }
    
    /**
     * Initialise l'interface utilisateur
     */
    private void initializeUI() {
        // Définir le fond
        setBackground(new Background(new BackgroundFill(
                Color.web(backgroundColor),
                CornerRadii.EMPTY,
                Insets.EMPTY)));
        
        // En-tête
        HBox header = createHeader();
        setTop(header);
        
        // Menu latéral
        VBox sidebar = createSidebar();
        setLeft(sidebar);
        
        // Contenu principal
        contentPane = new StackPane();
        contentPane.setPadding(new Insets(20));
        setCenter(contentPane);
        
        // Pied de page
        HBox footer = createFooter();
        setBottom(footer);
        
        // Afficher la vue des produits par défaut
        showProductsView();
    }
    
    /**
     * Crée l'en-tête de la vue
     * @return L'en-tête
     */
    private HBox createHeader() {
        HBox header = new HBox();
        header.setAlignment(Pos.CENTER_LEFT);
        header.setPadding(new Insets(10, 20, 10, 20));
        header.setSpacing(20);
        header.setBackground(new Background(new BackgroundFill(
                Color.WHITE,
                CornerRadii.EMPTY,
                Insets.EMPTY)));
        header.setBorder(new Border(new BorderStroke(
                Color.LIGHTGRAY,
                BorderStrokeStyle.SOLID,
                CornerRadii.EMPTY,
                new BorderWidths(0, 0, 1, 0))));
        
        // Logo et titre
        try {
            ImageView logoView = new ImageView(new Image(getClass().getResourceAsStream("/resources/images/logo.png")));
            logoView.setFitHeight(40);
            logoView.setFitWidth(40);
            header.getChildren().add(logoView);
        } catch (Exception e) {
            System.err.println("Impossible de charger le logo: " + e.getMessage());
        }
        
        Text title = new Text("BigPharma");
        title.setFont(Font.font("Arial", FontWeight.BOLD, 20));
        title.setFill(Color.web(primaryColor));
        header.getChildren().add(title);
        
        // Sélecteur de pharmacie (si l'utilisateur est un admin)
        if ("admin".equals(currentUser.getRole())) {
            Region spacer = new Region();
            HBox.setHgrow(spacer, Priority.ALWAYS);
            header.getChildren().add(spacer);
            
            Label pharmacyLabel = new Label("Pharmacie: ");
            pharmacyLabel.setFont(Font.font("Arial", 14));
            
            pharmacySelector = new ComboBox<>();
            pharmacySelector.setPrefWidth(200);
            
            // Charger les pharmacies
            PharmacyDAO pharmacyDAO = new PharmacyDAO();
            pharmacySelector.getItems().addAll(pharmacyDAO.findActivePharmacies());
            
            // Sélectionner la pharmacie de l'utilisateur si elle existe
            if (currentPharmacy != null) {
                pharmacySelector.getSelectionModel().select(currentPharmacy);
            }
            
            // Ajouter un écouteur pour changer de pharmacie
            pharmacySelector.setOnAction(e -> {
                currentPharmacy = pharmacySelector.getSelectionModel().getSelectedItem();
                refreshContent();
            });
            
            header.getChildren().addAll(pharmacyLabel, pharmacySelector);
        }
        
        // Informations utilisateur
        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);
        header.getChildren().add(spacer);
        
        Label userLabel = new Label(currentUser.getEmail());
        userLabel.setFont(Font.font("Arial", 14));
        
        Button logoutButton = new Button("Déconnexion");
        logoutButton.setStyle("-fx-background-color: " + primaryColor + "; -fx-text-fill: white;");
        logoutButton.setOnAction(e -> handleLogout());
        
        header.getChildren().addAll(userLabel, logoutButton);
        
        return header;
    }
    
    /**
     * Crée le menu latéral
     * @return Le menu latéral
     */
    private VBox createSidebar() {
        VBox sidebar = new VBox();
        sidebar.setPrefWidth(250);
        sidebar.setBackground(new Background(new BackgroundFill(
                Color.web(sidebarColor),
                CornerRadii.EMPTY,
                Insets.EMPTY)));
        sidebar.setPadding(new Insets(20, 0, 20, 0));
        sidebar.setSpacing(5);
        
        // Titre du menu
        Text menuTitle = new Text("MENU PRINCIPAL");
        menuTitle.setFont(Font.font("Arial", FontWeight.BOLD, 12));
        menuTitle.setFill(Color.LIGHTGRAY);
        VBox.setMargin(menuTitle, new Insets(0, 0, 10, 20));
        sidebar.getChildren().add(menuTitle);
        
        // Séparateur
        Separator separator = new Separator();
        separator.setStyle("-fx-background-color: rgba(255,255,255,0.2);");
        VBox.setMargin(separator, new Insets(0, 20, 10, 20));
        sidebar.getChildren().add(separator);
        
        // Boutons du menu
        Button dashboardButton = createMenuButton("Tableau de bord", "/resources/images/dashboard.png");
        dashboardButton.setOnAction(e -> showDashboardView());
        
        Button productsButton = createMenuButton("Produits", "/resources/images/products.png");
        productsButton.setOnAction(e -> showProductsView());
        
        Button ordersButton = createMenuButton("Commandes", "/resources/images/orders.png");
        ordersButton.setOnAction(e -> showOrdersView());
        
        Button stockButton = createMenuButton("Stock", "/resources/images/stock.png");
        stockButton.setOnAction(e -> showStockView());
        
        // Ajouter les boutons au menu
        sidebar.getChildren().addAll(
                dashboardButton,
                productsButton,
                ordersButton,
                stockButton
        );
        
        // Ajouter des options d'administration si l'utilisateur est un admin
        if ("admin".equals(currentUser.getRole())) {
            // Titre de la section admin
            Text adminTitle = new Text("ADMINISTRATION");
            adminTitle.setFont(Font.font("Arial", FontWeight.BOLD, 12));
            adminTitle.setFill(Color.LIGHTGRAY);
            VBox.setMargin(adminTitle, new Insets(20, 0, 10, 20));
            
            // Séparateur
            Separator adminSeparator = new Separator();
            adminSeparator.setStyle("-fx-background-color: rgba(255,255,255,0.2);");
            VBox.setMargin(adminSeparator, new Insets(0, 20, 10, 20));
            
            // Boutons d'administration
            Button usersButton = createMenuButton("Utilisateurs", "/resources/images/users.png");
            usersButton.setOnAction(e -> showUsersView());
            
            Button pharmaciesButton = createMenuButton("Pharmacies", "/resources/images/pharmacies.png");
            pharmaciesButton.setOnAction(e -> showPharmaciesView());
            
            // Ajouter les éléments au menu
            sidebar.getChildren().addAll(
                    adminTitle,
                    adminSeparator,
                    usersButton,
                    pharmaciesButton
            );
        }
        
        return sidebar;
    }
    
    /**
     * Crée un bouton pour le menu latéral
     * @param text Le texte du bouton
     * @param iconPath Le chemin de l'icône
     * @return Le bouton créé
     */
    private Button createMenuButton(String text, String iconPath) {
        Button button = new Button(text);
        button.setPrefWidth(250);
        button.setPrefHeight(40);
        button.setAlignment(Pos.CENTER_LEFT);
        button.setPadding(new Insets(0, 0, 0, 20));
        button.setFont(Font.font("Arial", 14));
        button.setTextFill(Color.WHITE);
        button.setStyle(
                "-fx-background-color: transparent; " +
                "-fx-border-width: 0; " +
                "-fx-cursor: hand;"
        );
        
        // Ajouter l'icône si disponible
        try {
            ImageView iconView = new ImageView(new Image(getClass().getResourceAsStream(iconPath)));
            iconView.setFitHeight(16);
            iconView.setFitWidth(16);
            button.setGraphic(iconView);
            button.setGraphicTextGap(10);
        } catch (Exception e) {
            System.err.println("Impossible de charger l'icône: " + e.getMessage());
        }
        
        // Ajouter des effets au survol
        button.setOnMouseEntered(e -> 
            button.setStyle(
                "-fx-background-color: rgba(255,255,255,0.1); " +
                "-fx-border-width: 0; " +
                "-fx-cursor: hand;"
            )
        );
        
        button.setOnMouseExited(e -> 
            button.setStyle(
                "-fx-background-color: transparent; " +
                "-fx-border-width: 0; " +
                "-fx-cursor: hand;"
            )
        );
        
        return button;
    }
    
    /**
     * Crée le pied de page
     * @return Le pied de page
     */
    private HBox createFooter() {
        HBox footer = new HBox();
        footer.setAlignment(Pos.CENTER);
        footer.setPadding(new Insets(10));
        footer.setBackground(new Background(new BackgroundFill(
                Color.WHITE,
                CornerRadii.EMPTY,
                Insets.EMPTY)));
        footer.setBorder(new Border(new BorderStroke(
                Color.LIGHTGRAY,
                BorderStrokeStyle.SOLID,
                CornerRadii.EMPTY,
                new BorderWidths(1, 0, 0, 0))));
        
        Text footerText = new Text("© 2025 BigPharma - Tous droits réservés");
        footerText.setFont(Font.font("Arial", 12));
        footerText.setFill(Color.web(textColor));
        footer.getChildren().add(footerText);
        
        return footer;
    }
    
    /**
     * Affiche la vue du tableau de bord
     */
    private void showDashboardView() {
        // À implémenter
        Text text = new Text("Tableau de bord - En construction");
        text.setFont(Font.font("Arial", FontWeight.BOLD, 24));
        text.setFill(Color.web(textColor));
        
        StackPane content = new StackPane(text);
        setContent(content);
    }
    
    /**
     * Affiche la vue des produits
     */
    private void showProductsView() {
        ProductListView productsView = new ProductListView(currentUser, currentPharmacy);
        setContent(productsView);
    }
    
    /**
     * Affiche la vue des commandes
     */
    private void showOrdersView() {
        OrderListView ordersView = new OrderListView(currentUser, currentPharmacy);
        setContent(ordersView);
    }
    
    /**
     * Affiche la vue du stock
     */
    private void showStockView() {
        // À implémenter
        Text text = new Text("Gestion du stock - En construction");
        text.setFont(Font.font("Arial", FontWeight.BOLD, 24));
        text.setFill(Color.web(textColor));
        
        StackPane content = new StackPane(text);
        setContent(content);
    }
    
    /**
     * Affiche la vue des utilisateurs (admin seulement)
     */
    private void showUsersView() {
        // À implémenter
        Text text = new Text("Gestion des utilisateurs - En construction");
        text.setFont(Font.font("Arial", FontWeight.BOLD, 24));
        text.setFill(Color.web(textColor));
        
        StackPane content = new StackPane(text);
        setContent(content);
    }
    
    /**
     * Affiche la vue des pharmacies (admin seulement)
     */
    private void showPharmaciesView() {
        // À implémenter
        Text text = new Text("Gestion des pharmacies - En construction");
        text.setFont(Font.font("Arial", FontWeight.BOLD, 24));
        text.setFill(Color.web(textColor));
        
        StackPane content = new StackPane(text);
        setContent(content);
    }
    
    /**
     * Définit le contenu principal
     * @param content Le contenu à afficher
     */
    private void setContent(Pane content) {
        contentPane.getChildren().clear();
        contentPane.getChildren().add(content);
    }
    
    /**
     * Rafraîchit le contenu après un changement de pharmacie
     */
    private void refreshContent() {
        // Recharger la vue actuelle avec la nouvelle pharmacie
        showProductsView();
    }
    
    /**
     * Gère la déconnexion
     */
    private void handleLogout() {
        boolean confirm = AlertUtils.showConfirmation(
                "Déconnexion", 
                "Êtes-vous sûr de vouloir vous déconnecter ?");
        
        if (confirm) {
            // Retourner à l'écran de connexion
            LoginView loginView = new LoginView(primaryStage);
            Scene scene = new Scene(loginView, 800, 600);
            scene.getStylesheets().add(getClass().getResource("/resources/css/styles.css").toExternalForm());
            
            primaryStage.setScene(scene);
            primaryStage.setTitle("BigPharma - Panneau d'Administration");
            primaryStage.centerOnScreen();
        }
    }
}
