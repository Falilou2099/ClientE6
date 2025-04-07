package com.gestionpharma.controllers;

import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.collections.transformation.SortedList;
import javafx.event.ActionEvent;
import javafx.scene.layout.GridPane;
import javafx.geometry.Insets;
import javafx.scene.Node;
import java.util.Optional;

import java.net.URL;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Date;
import java.util.List;
import java.util.ResourceBundle;
import java.util.stream.Collectors;

import com.gestionpharma.models.*;
import com.gestionpharma.services.*;
import com.gestionpharma.config.DatabaseConfig;
import com.gestionpharma.utils.AlertUtils;

/**
 * Contrôleur pour le panneau d'administration
 */
public class AdminPanelController implements Initializable {
    // Services
    private ProduitService produitService;
    private FournisseurService fournisseurService;
    private CommandeService commandeService;
    private StockService stockService;
    private ActiviteService activiteService;
    
    // Admin et pharmacie courante
    private Admin currentAdmin;
    private int pharmacieId;
    
    // Labels d'en-tête
    @FXML private Label adminNameLabel;
    @FXML private Label pharmacyNameLabel;
    @FXML private Label dateLabel;
    
    // Style CSS pour améliorer la visibilité
    private final String HEADER_STYLE = "-fx-font-size: 14px; -fx-font-weight: bold;";
    private final String VALUE_STYLE = "-fx-font-size: 16px; -fx-font-weight: bold;";
    private final String DASHBOARD_TITLE_STYLE = "-fx-font-size: 18px; -fx-font-weight: bold;";
    private final String ERROR_STYLE = "-fx-text-fill: red; -fx-font-weight: bold;";
    private final String WARNING_STYLE = "-fx-text-fill: orange; -fx-font-weight: bold;";
    
    // Indicateurs du tableau de bord
    @FXML private Label totalProductsLabel;
    @FXML private Label pendingOrdersLabel;
    @FXML private Label suppliersCountLabel;
    @FXML private Label totalSalesLabel;
    @FXML private Label newProductsLabel;
    @FXML private Label stockAlertsLabel;
    
    // Tableaux et colonnes pour les produits
    @FXML private TableView<Produit> productsTable;
    @FXML private TableColumn<Produit, Integer> productIdColumn;
    @FXML private TableColumn<Produit, String> productNameColumn;
    @FXML private TableColumn<Produit, String> productDescColumn;
    @FXML private TableColumn<Produit, Double> productPriceColumn;
    @FXML private TableColumn<Produit, Double> productCostColumn;
    @FXML private TableColumn<Produit, Integer> productStockColumn;
    @FXML private TableColumn<Produit, String> productCategoryColumn;
    @FXML private TableColumn<Produit, LocalDate> productExpiryColumn;
    @FXML private TextField productSearchField;
    @FXML private ComboBox<String> productCategoryFilter;
    
    // Tableaux et colonnes pour les fournisseurs
    @FXML private TableView<Fournisseur> suppliersTable;
    @FXML private TableColumn<Fournisseur, Integer> supplierIdColumn;
    @FXML private TableColumn<Fournisseur, String> supplierNameColumn;
    @FXML private TableColumn<Fournisseur, String> supplierAddressColumn;
    @FXML private TableColumn<Fournisseur, String> supplierPhoneColumn;
    @FXML private TableColumn<Fournisseur, String> supplierEmailColumn;
    @FXML private TableColumn<Fournisseur, String> supplierSiretColumn;
    @FXML private TextField supplierSearchField;
    
    // Tableaux et colonnes pour les commandes
    @FXML private TableView<Commande> ordersTable;
    @FXML private TableColumn<Commande, Integer> orderIdColumn;
    @FXML private TableColumn<Commande, LocalDateTime> orderDateColumn;
    @FXML private TableColumn<Commande, String> orderSupplierColumn;
    @FXML private TableColumn<Commande, String> orderStatusColumn;
    @FXML private TableColumn<Commande, Double> orderTotalColumn;
    @FXML private TableColumn<Commande, LocalDate> orderDeliveryDateColumn;
    @FXML private TextField orderSearchField;
    @FXML private ComboBox<String> orderStatusFilter;
    
    // Tableaux et colonnes pour les stocks
    @FXML private TableView<Stock> stockTable;
    @FXML private TableColumn<Stock, Integer> stockProductIdColumn;
    @FXML private TableColumn<Stock, String> stockProductNameColumn;
    @FXML private TableColumn<Stock, Integer> stockQuantityColumn;
    @FXML private TableColumn<Stock, Integer> stockMinimumColumn;
    @FXML private TableColumn<Stock, String> stockStatusColumn;
    @FXML private TableColumn<Stock, LocalDate> stockExpiryColumn;
    @FXML private TableColumn<Stock, LocalDateTime> stockLastMovementColumn;
    @FXML private TextField stockSearchField;
    @FXML private ComboBox<String> stockAlertFilter;
    
    // Tableau des activités récentes
    @FXML private TableView<Activite> activitiesTable;
    @FXML private TableColumn<Activite, LocalDateTime> activityDateColumn;
    @FXML private TableColumn<Activite, String> activityTypeColumn;
    @FXML private TableColumn<Activite, String> activityDescriptionColumn;
    @FXML private TableColumn<Activite, String> activityUserColumn;
    
    // Listes observables pour les tableaux
    private ObservableList<Produit> productsList = FXCollections.observableArrayList();
    private ObservableList<Fournisseur> suppliersList = FXCollections.observableArrayList();
    private ObservableList<Commande> ordersList = FXCollections.observableArrayList();
    private ObservableList<Stock> stocksList = FXCollections.observableArrayList();
    private ObservableList<Activite> activitiesList = FXCollections.observableArrayList();
    
    @Override
    public void initialize(URL url, ResourceBundle rb) {
        // Initialiser les services
        produitService = new ProduitService();
        fournisseurService = new FournisseurService();
        commandeService = new CommandeService();
        stockService = new StockService();
        activiteService = new ActiviteService();
        
        // Afficher la date du jour
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");
        dateLabel.setText(LocalDateTime.now().format(formatter));
        
        // Appliquer les styles pour améliorer la visibilité
        applyStyles();
        
        // Initialiser les filtres
        initializeFilters();
        
        // Configurer les colonnes des tableaux
        configureTableColumns();
    }
    
    /**
     * Applique les styles CSS pour améliorer la visibilité des éléments de l'interface
     */
    private void applyStyles() {
        // Appliquer les styles aux labels d'en-tête
        if (adminNameLabel != null) adminNameLabel.setStyle(HEADER_STYLE);
        if (pharmacyNameLabel != null) pharmacyNameLabel.setStyle(DASHBOARD_TITLE_STYLE);
        if (dateLabel != null) dateLabel.setStyle(HEADER_STYLE);
        
        // Appliquer les styles aux indicateurs du tableau de bord
        if (totalProductsLabel != null) totalProductsLabel.setStyle(VALUE_STYLE);
        if (pendingOrdersLabel != null) pendingOrdersLabel.setStyle(VALUE_STYLE);
        if (suppliersCountLabel != null) suppliersCountLabel.setStyle(VALUE_STYLE);
        if (totalSalesLabel != null) totalSalesLabel.setStyle(VALUE_STYLE);
        if (newProductsLabel != null) newProductsLabel.setStyle(VALUE_STYLE);
        if (stockAlertsLabel != null) stockAlertsLabel.setStyle(VALUE_STYLE + "; -fx-text-fill: #e74c3c;");
    }
    
    /**
     * Définit l'administrateur courant et charge les données
     * @param admin L'administrateur connecté
     */
    public void setAdmin(Admin admin) {
        try {
            this.currentAdmin = admin;
            this.pharmacieId = admin.getPharmacieId();
            adminNameLabel.setText(admin.getPrenom() + " " + admin.getNom());
            
            // Chargement sécurisé des données
            try { loadPharmacyInfo(); } catch (Exception e) { 
                System.out.println("Erreur lors du chargement des informations de la pharmacie: " + e.getMessage()); 
            }
            
            try { loadDashboardData(); } catch (Exception e) { 
                System.out.println("Erreur lors du chargement des données du tableau de bord: " + e.getMessage()); 
            }
            
            try { loadProductsData(); } catch (Exception e) { 
                System.out.println("Erreur lors du chargement des produits: " + e.getMessage()); 
            }
            
            try { loadSuppliersData(); } catch (Exception e) { 
                System.out.println("Erreur lors du chargement des fournisseurs: " + e.getMessage()); 
            }
            
            try { loadOrdersData(); } catch (Exception e) { 
                System.out.println("Erreur lors du chargement des commandes: " + e.getMessage()); 
            }
            
            try { loadStockData(); } catch (Exception e) { 
                System.out.println("Erreur lors du chargement des stocks: " + e.getMessage()); 
            }
            
            try { loadActivitiesData(); } catch (Exception e) { 
                System.out.println("Erreur lors du chargement des activités: " + e.getMessage()); 
            }
            
        } catch (Exception e) {
            e.printStackTrace();
            System.out.println("Erreur générale lors de l'initialisation: " + e.getMessage());
        }
    }
    
    /**
     * Charge les informations de la pharmacie
     */
    private void loadPharmacyInfo() {
        try (Connection conn = DatabaseConfig.getConnection()) {
            String query = "SELECT * FROM pharmacies WHERE id = ?";
            PreparedStatement stmt = conn.prepareStatement(query);
            stmt.setInt(1, pharmacieId);
            ResultSet rs = stmt.executeQuery();
            
            if (rs.next()) {
                String pharmacyName = rs.getString("nom");
                pharmacyNameLabel.setText(pharmacyName);
            }
        } catch (SQLException e) {
            e.printStackTrace();
            AlertUtils.showErrorAlert("Erreur", "Erreur de base de données", 
                    "Impossible de charger les informations de la pharmacie: " + e.getMessage());
        }
    }
    
    /**
     * Charge les données du tableau de bord
     */
    private void loadDashboardData() {
        try {
            // Nombre total de produits
            int totalProducts = produitService.getAllProduits(pharmacieId).size();
            totalProductsLabel.setText(String.valueOf(totalProducts));
            
            // Nombre de commandes en attente
            int pendingOrders = commandeService.getCommandesEnAttente(pharmacieId).size();
            pendingOrdersLabel.setText(String.valueOf(pendingOrders));
            
            // Nombre de fournisseurs
            int suppliersCount = fournisseurService.getAllFournisseurs(pharmacieId).size();
            suppliersCountLabel.setText(String.valueOf(suppliersCount));
            
            // Nombre de ventes totales (à implémenter)
            totalSalesLabel.setText("0");
            
            // Nombre de nouveaux produits (derniers 30 jours)
            int newProducts = 0;
            try (Connection conn = DatabaseConfig.getConnection()) {
                // Vérifions si la colonne date_ajout existe, sinon utilisons une autre approche
                String query = "SELECT COUNT(*) as count FROM produits";
                // Si vous avez une colonne de date dans la table produits, vous pouvez la remplacer ici
                // Par exemple: "SELECT COUNT(*) as count FROM produits WHERE date_ajout >= DATE_SUB(CURDATE(), INTERVAL 30 DAY)"
                PreparedStatement stmt = conn.prepareStatement(query);
                ResultSet rs = stmt.executeQuery();
                
                if (rs.next()) {
                    newProducts = rs.getInt("count");
                }
            }
            newProductsLabel.setText(String.valueOf(newProducts));
            
            // Nombre de stocks en alerte
            int stockAlerts = stockService.getStocksEnAlerte(pharmacieId).size();
            stockAlertsLabel.setText(String.valueOf(stockAlerts));
            
        } catch (Exception e) {
            e.printStackTrace();
            AlertUtils.showErrorAlert("Erreur", "Erreur de chargement", 
                    "Impossible de charger les données du tableau de bord: " + e.getMessage());
        }
    }
    
    /**
     * Initialise les filtres des tableaux
     */
    private void initializeFilters() {
        // Filtre des catégories de produits
        productCategoryFilter.getItems().add("Toutes les catégories");
        productCategoryFilter.getSelectionModel().selectFirst();
        
        // Filtre des statuts de commandes
        orderStatusFilter.getItems().addAll("Tous les statuts", "En attente", "En cours", "Livrée", "Annulée");
        orderStatusFilter.getSelectionModel().selectFirst();
        
        // Filtre des alertes de stock
        stockAlertFilter.getItems().addAll("Tous les stocks", "En alerte", "Normal");
        stockAlertFilter.getSelectionModel().selectFirst();
    }
    
    /**
     * Configure les colonnes des tableaux
     */
    private void configureTableColumns() {
        // Configurer les colonnes de la table des produits
        productIdColumn.setCellValueFactory(new PropertyValueFactory<>("id"));
        productNameColumn.setCellValueFactory(new PropertyValueFactory<>("nom"));
        productDescColumn.setCellValueFactory(new PropertyValueFactory<>("description"));
        productPriceColumn.setCellValueFactory(new PropertyValueFactory<>("prixVente"));
        productCostColumn.setCellValueFactory(new PropertyValueFactory<>("prixAchat"));
        productStockColumn.setCellValueFactory(new PropertyValueFactory<>("quantiteStock"));
        productCategoryColumn.setCellValueFactory(new PropertyValueFactory<>("categorie"));
        productExpiryColumn.setCellValueFactory(new PropertyValueFactory<>("dateExpiration"));
        
        // Configurer les colonnes de la table des fournisseurs
        supplierIdColumn.setCellValueFactory(new PropertyValueFactory<>("id"));
        supplierNameColumn.setCellValueFactory(new PropertyValueFactory<>("nom"));
        supplierAddressColumn.setCellValueFactory(new PropertyValueFactory<>("adresse"));
        supplierPhoneColumn.setCellValueFactory(new PropertyValueFactory<>("telephone"));
        supplierEmailColumn.setCellValueFactory(new PropertyValueFactory<>("email"));
        supplierSiretColumn.setCellValueFactory(new PropertyValueFactory<>("siret"));
        
        // Configurer les colonnes de la table des commandes
        orderIdColumn.setCellValueFactory(new PropertyValueFactory<>("id"));
        orderDateColumn.setCellValueFactory(new PropertyValueFactory<>("dateCommande"));
        orderSupplierColumn.setCellValueFactory(new PropertyValueFactory<>("fournisseurNom"));
        orderStatusColumn.setCellValueFactory(new PropertyValueFactory<>("statut"));
        orderTotalColumn.setCellValueFactory(new PropertyValueFactory<>("montantTotal"));
        orderDeliveryDateColumn.setCellValueFactory(new PropertyValueFactory<>("dateLivraison"));
        
        // Configurer les colonnes de la table des stocks
        stockProductIdColumn.setCellValueFactory(new PropertyValueFactory<>("produitId"));
        stockProductNameColumn.setCellValueFactory(new PropertyValueFactory<>("produitNom"));
        stockQuantityColumn.setCellValueFactory(new PropertyValueFactory<>("quantite"));
        stockMinimumColumn.setCellValueFactory(new PropertyValueFactory<>("seuilMinimum"));
        stockStatusColumn.setCellValueFactory(new PropertyValueFactory<>("statut"));
        stockExpiryColumn.setCellValueFactory(new PropertyValueFactory<>("dateExpiration"));
        stockLastMovementColumn.setCellValueFactory(new PropertyValueFactory<>("dernierMouvement"));
        
        // Configurer les colonnes de la table des activités
        activityDateColumn.setCellValueFactory(new PropertyValueFactory<>("date"));
        activityTypeColumn.setCellValueFactory(new PropertyValueFactory<>("type"));
        activityDescriptionColumn.setCellValueFactory(new PropertyValueFactory<>("description"));
        activityUserColumn.setCellValueFactory(new PropertyValueFactory<>("utilisateur"));
    }
    /**
     * Charge les données des produits
     */
    private void loadProductsData() {
        try {
            // Vérifier si les composants UI sont initialisés
            if (productsTable == null) {
                System.out.println("Avertissement: Le tableau des produits n'est pas initialisé dans le fichier FXML.");
                return;
            }
            
            // Récupérer tous les produits avec le service
            List<Produit> produits = produitService.getAllProduits(pharmacieId);
            
            // Mettre à jour la liste observable
            productsList.clear();
            productsList.addAll(produits);
            
            // Configurer la recherche de produits si le champ de recherche existe
            if (productSearchField != null) {
                FilteredList<Produit> filteredProducts = new FilteredList<>(productsList, p -> true);
                productSearchField.textProperty().addListener((observable, oldValue, newValue) -> {
                    filteredProducts.setPredicate(produit -> {
                        if (newValue == null || newValue.isEmpty()) {
                            return true;
                        }
                        
                        String lowerCaseFilter = newValue.toLowerCase();
                        if (produit.getNom().toLowerCase().contains(lowerCaseFilter)) {
                            return true;
                        } else if (produit.getDescription().toLowerCase().contains(lowerCaseFilter)) {
                            return true;
                        } else if (produit.getCategorie().toLowerCase().contains(lowerCaseFilter)) {
                            return true;
                        }
                        return false;
                    });
                });
                
                // Configurer le filtre par catégorie si le composant existe
                if (productCategoryFilter != null) {
                    productCategoryFilter.valueProperty().addListener((observable, oldValue, newValue) -> {
                        filteredProducts.setPredicate(produit -> {
                            if (newValue == null || newValue.equals("Toutes les catégories")) {
                                return true;
                            }
                            return produit.getCategorie().equals(newValue);
                        });
                    });
                    
                    // Mettre à jour les catégories dans le filtre
                    List<String> categories = produits.stream()
                            .map(Produit::getCategorie)
                            .distinct()
                            .collect(Collectors.toList());
                    categories.add(0, "Toutes les catégories");
                    productCategoryFilter.setItems(FXCollections.observableArrayList(categories));
                    productCategoryFilter.getSelectionModel().selectFirst();
                }
                
                SortedList<Produit> sortedProducts = new SortedList<>(filteredProducts);
                sortedProducts.comparatorProperty().bind(productsTable.comparatorProperty());
                productsTable.setItems(sortedProducts);
            } else {
                // Si le champ de recherche n'existe pas, on affiche simplement tous les produits
                productsTable.setItems(productsList);
            }
            
        } catch (Exception e) {
            e.printStackTrace();
            System.out.println("Erreur lors du chargement des produits: " + e.getMessage());
        }
    }
    
    /**
     * Charge les données des fournisseurs
     */
    private void loadSuppliersData() {
        try {
            // Récupérer tous les fournisseurs avec le service
            List<Fournisseur> fournisseurs = fournisseurService.getAllFournisseurs(pharmacieId);
            
            // Mettre à jour la liste observable
            suppliersList.clear();
            suppliersList.addAll(fournisseurs);
            
            // Configurer la recherche de fournisseurs
            FilteredList<Fournisseur> filteredSuppliers = new FilteredList<>(suppliersList, p -> true);
            supplierSearchField.textProperty().addListener((observable, oldValue, newValue) -> {
                filteredSuppliers.setPredicate(fournisseur -> {
                    if (newValue == null || newValue.isEmpty()) {
                        return true;
                    }
                    
                    String lowerCaseFilter = newValue.toLowerCase();
                    if (fournisseur.getNom().toLowerCase().contains(lowerCaseFilter)) {
                        return true;
                    } else if (fournisseur.getAdresse().toLowerCase().contains(lowerCaseFilter)) {
                        return true;
                    } else if (fournisseur.getTelephone().toLowerCase().contains(lowerCaseFilter)) {
                        return true;
                    } else if (fournisseur.getEmail().toLowerCase().contains(lowerCaseFilter)) {
                        return true;
                    } else if (fournisseur.getSiret().toLowerCase().contains(lowerCaseFilter)) {
                        return true;
                    }
                    return false;
                });
            });
            
            SortedList<Fournisseur> sortedSuppliers = new SortedList<>(filteredSuppliers);
            sortedSuppliers.comparatorProperty().bind(suppliersTable.comparatorProperty());
            suppliersTable.setItems(sortedSuppliers);
            
        } catch (Exception e) {
            e.printStackTrace();
            AlertUtils.showErrorAlert("Erreur", "Erreur de chargement", 
                    "Impossible de charger les fournisseurs: " + e.getMessage());
        }
    }
    
    /**
     * Charge les données des commandes
     */
    private void loadOrdersData() {
        try {
            // Récupérer toutes les commandes avec le service
            List<Commande> commandes = commandeService.getAllCommandes(pharmacieId);
            
            // Mettre à jour la liste observable
            ordersList.clear();
            ordersList.addAll(commandes);
            
            // Configurer la recherche de commandes
            FilteredList<Commande> filteredOrders = new FilteredList<>(ordersList, p -> true);
            orderSearchField.textProperty().addListener((observable, oldValue, newValue) -> {
                filteredOrders.setPredicate(commande -> {
                    if (newValue == null || newValue.isEmpty()) {
                        return true;
                    }
                    
                    String lowerCaseFilter = newValue.toLowerCase();
                    if (commande.getFournisseurNom().toLowerCase().contains(lowerCaseFilter)) {
                        return true;
                    } else if (commande.getStatut().toLowerCase().contains(lowerCaseFilter)) {
                        return true;
                    } else if (String.valueOf(commande.getId()).contains(lowerCaseFilter)) {
                        return true;
                    }
                    return false;
                });
            });
            
            orderStatusFilter.valueProperty().addListener((observable, oldValue, newValue) -> {
                filteredOrders.setPredicate(commande -> {
                    if (newValue == null || newValue.equals("Tous les statuts")) {
                        return true;
                    }
                    return commande.getStatut().equals(newValue);
                });
            });
            
            SortedList<Commande> sortedOrders = new SortedList<>(filteredOrders);
            sortedOrders.comparatorProperty().bind(ordersTable.comparatorProperty());
            ordersTable.setItems(sortedOrders);
            
        } catch (Exception e) {
            e.printStackTrace();
            AlertUtils.showErrorAlert("Erreur", "Erreur de chargement", 
                    "Impossible de charger les commandes: " + e.getMessage());
        }
    }
    
    /**
     * Charge les données des stocks
     */
    private void loadStockData() {
        try {
            // Récupérer tous les stocks avec le service
            List<Stock> stocks = stockService.getAllStocks(pharmacieId);
            
            // Mettre à jour la liste observable
            stocksList.clear();
            stocksList.addAll(stocks);
            
            // Configurer la recherche de stocks
            FilteredList<Stock> filteredStocks = new FilteredList<>(stocksList, p -> true);
            stockSearchField.textProperty().addListener((observable, oldValue, newValue) -> {
                filteredStocks.setPredicate(stock -> {
                    if (newValue == null || newValue.isEmpty()) {
                        return true;
                    }
                    
                    String lowerCaseFilter = newValue.toLowerCase();
                    if (stock.getProduitNom().toLowerCase().contains(lowerCaseFilter)) {
                        return true;
                    } else if (stock.getStatut().toLowerCase().contains(lowerCaseFilter)) {
                        return true;
                    }
                    return false;
                });
            });
            
            stockAlertFilter.valueProperty().addListener((observable, oldValue, newValue) -> {
                filteredStocks.setPredicate(stock -> {
                    if (newValue == null || newValue.equals("Tous les stocks")) {
                        return true;
                    } else if (newValue.equals("En alerte")) {
                        return stock.getStatut().equals("Alerte");
                    } else if (newValue.equals("Normal")) {
                        return stock.getStatut().equals("Normal");
                    }
                    return true;
                });
            });
            
            SortedList<Stock> sortedStocks = new SortedList<>(filteredStocks);
            sortedStocks.comparatorProperty().bind(stockTable.comparatorProperty());
            stockTable.setItems(sortedStocks);
            
        } catch (Exception e) {
            e.printStackTrace();
            AlertUtils.showErrorAlert("Erreur", "Erreur de chargement", 
                    "Impossible de charger les stocks: " + e.getMessage());
        }
    }
    
    /**
     * Charge les données des activités récentes
     */
    private void loadActivitiesData() {
        try {
            // Récupérer les activités récentes avec le service (limité à 50)
            List<Activite> activites = activiteService.getActivitesRecentes(pharmacieId, 50);
            
            // Mettre à jour la liste observable
            activitiesList.clear();
            activitiesList.addAll(activites);
            
            // Mettre à jour la table si elle existe
            if (activitiesTable != null) {
                activitiesTable.setItems(activitiesList);
            } else {
                System.out.println("Avertissement: Le tableau des activités n'est pas initialisé dans le fichier FXML.");
            }
            
        } catch (Exception e) {
            e.printStackTrace();
            // Utiliser System.out.println au lieu d'une alerte pour éviter des erreurs en cascade
            System.out.println("Erreur lors du chargement des activités: " + e.getMessage());
        }
    }
    /**
     * Gère l'ajout d'un nouveau produit
     */
    @FXML
    protected void handleAddProduct() {
        try {
            // Créer un nouveau produit vide
            Produit nouveauProduit = new Produit();
            
            // Créer la boîte de dialogue
            Dialog<ButtonType> dialog = new Dialog<>();
            dialog.setTitle("Ajouter un nouveau produit");
            dialog.setHeaderText("Veuillez saisir les informations du produit");
            
            // Définir les boutons
            ButtonType saveButtonType = new ButtonType("Enregistrer", ButtonBar.ButtonData.OK_DONE);
            dialog.getDialogPane().getButtonTypes().addAll(saveButtonType, ButtonType.CANCEL);
            
            // Créer la grille pour les champs
            GridPane grid = new GridPane();
            grid.setHgap(10);
            grid.setVgap(10);
            grid.setPadding(new Insets(20, 150, 10, 10));
            
            // Créer les champs
            TextField nomField = new TextField();
            nomField.setPromptText("Nom du produit");
            TextArea descriptionField = new TextArea();
            descriptionField.setPromptText("Description");
            descriptionField.setPrefRowCount(3);
            TextField prixAchatField = new TextField();
            prixAchatField.setPromptText("Prix d'achat");
            TextField prixVenteField = new TextField();
            prixVenteField.setPromptText("Prix de vente");
            ComboBox<String> categorieField = new ComboBox<>();
            categorieField.getItems().addAll("Médicament", "Parapharmacie", "Matériel médical", "Autre");
            categorieField.setPromptText("Catégorie");
            DatePicker expirationField = new DatePicker();
            expirationField.setPromptText("Date d'expiration");
            TextField seuilAlerteField = new TextField();
            seuilAlerteField.setPromptText("Seuil d'alerte");
            seuilAlerteField.setText("10"); // Valeur par défaut
            
            // Ajouter les champs à la grille
            grid.add(new Label("Nom *:"), 0, 0);
            grid.add(nomField, 1, 0);
            grid.add(new Label("Description:"), 0, 1);
            grid.add(descriptionField, 1, 1);
            grid.add(new Label("Prix d'achat *:"), 0, 2);
            grid.add(prixAchatField, 1, 2);
            grid.add(new Label("Prix de vente *:"), 0, 3);
            grid.add(prixVenteField, 1, 3);
            grid.add(new Label("Catégorie *:"), 0, 4);
            grid.add(categorieField, 1, 4);
            grid.add(new Label("Date d'expiration:"), 0, 5);
            grid.add(expirationField, 1, 5);
            grid.add(new Label("Seuil d'alerte:"), 0, 6);
            grid.add(seuilAlerteField, 1, 6);
            
            dialog.getDialogPane().setContent(grid);
            
            // Activer/Désactiver le bouton Enregistrer selon la validation des champs
            Node saveButton = dialog.getDialogPane().lookupButton(saveButtonType);
            saveButton.setDisable(true);
            
            // Valider les champs obligatoires
            nomField.textProperty().addListener((observable, oldValue, newValue) -> {
                saveButton.setDisable(newValue.trim().isEmpty() || prixAchatField.getText().trim().isEmpty() || 
                                      prixVenteField.getText().trim().isEmpty() || 
                                      categorieField.getSelectionModel().isEmpty());
            });
            
            prixAchatField.textProperty().addListener((observable, oldValue, newValue) -> {
                saveButton.setDisable(newValue.trim().isEmpty() || nomField.getText().trim().isEmpty() || 
                                      prixVenteField.getText().trim().isEmpty() || 
                                      categorieField.getSelectionModel().isEmpty());
            });
            
            prixVenteField.textProperty().addListener((observable, oldValue, newValue) -> {
                saveButton.setDisable(newValue.trim().isEmpty() || nomField.getText().trim().isEmpty() || 
                                      prixAchatField.getText().trim().isEmpty() || 
                                      categorieField.getSelectionModel().isEmpty());
            });
            
            categorieField.getSelectionModel().selectedItemProperty().addListener((observable, oldValue, newValue) -> {
                saveButton.setDisable(newValue == null || nomField.getText().trim().isEmpty() || 
                                      prixAchatField.getText().trim().isEmpty() || 
                                      prixVenteField.getText().trim().isEmpty());
            });
            
            // Afficher la boite de dialogue et attendre la réponse
            Optional<ButtonType> result = dialog.showAndWait();
            
            // Traiter la réponse
            if (result.isPresent() && result.get() == saveButtonType) {
                // Récupérer les valeurs des champs
                nouveauProduit.setNom(nomField.getText().trim());
                nouveauProduit.setDescription(descriptionField.getText().trim());
                
                try {
                    // Conversion des valeurs numériques
                    nouveauProduit.setPrixAchat(Double.parseDouble(prixAchatField.getText().trim()));
                    nouveauProduit.setPrixVente(Double.parseDouble(prixVenteField.getText().trim()));
                    
                    if (!seuilAlerteField.getText().trim().isEmpty()) {
                        nouveauProduit.setSeuilAlerte(Integer.parseInt(seuilAlerteField.getText().trim()));
                    } else {
                        nouveauProduit.setSeuilAlerte(10); // Valeur par défaut
                    }
                } catch (NumberFormatException e) {
                    AlertUtils.showErrorAlert("Erreur", "Format incorrect", 
                            "Veuillez entrer des valeurs numériques valides pour les prix et le seuil d'alerte.");
                    return;
                }
                
                nouveauProduit.setCategorie(categorieField.getValue());
                
                if (expirationField.getValue() != null) {
                    nouveauProduit.setDateExpiration(expirationField.getValue());
                }
                
                boolean success = produitService.ajouterProduit(nouveauProduit, pharmacieId);
                
                if (success) {
                    // Enregistrer l'activité
                    activiteService.ajouterActivite("Produit", "Ajout du produit " + nouveauProduit.getNom(), 
                            currentAdmin.getPrenom() + " " + currentAdmin.getNom(), pharmacieId);
                    
                    // Recharger les données
                    loadProductsData();
                    loadDashboardData();
                    
                    AlertUtils.showInfoAlert("Succès", "Produit ajouté", 
                            "Le produit a été ajouté avec succès.");
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
            AlertUtils.showErrorAlert("Erreur", "Erreur lors de l'ajout", 
                    "Impossible d'ajouter le produit: " + e.getMessage());
        }
    }
    
    /**
     * Gère la modification d'un produit existant
     */
    @FXML
    protected void handleEditProduct() {
        try {
            Produit selectedProduct = productsTable.getSelectionModel().getSelectedItem();
            if (selectedProduct == null) {
                AlertUtils.showWarningAlert("Attention", "Aucun produit sélectionné", 
                        "Veuillez sélectionner un produit à modifier.");
                return;
            }
            
            // TODO: Implémenter un dialogue pour modifier un produit
            
            // Exemple d'implémentation (à remplacer par un dialogue)
            selectedProduct.setDescription(selectedProduct.getDescription() + " (modifié)");
            
            boolean success = produitService.modifierProduit(selectedProduct);
            
            if (success) {
                // Enregistrer l'activité
                activiteService.ajouterActivite("Produit", "Modification du produit " + selectedProduct.getNom(), 
                        currentAdmin.getPrenom() + " " + currentAdmin.getNom(), pharmacieId);
                
                // Recharger les données
                loadProductsData();
                
                AlertUtils.showInfoAlert("Succès", "Produit modifié", 
                        "Le produit a été modifié avec succès.");
            }
        } catch (Exception e) {
            e.printStackTrace();
            AlertUtils.showErrorAlert("Erreur", "Erreur lors de la modification", 
                    "Impossible de modifier le produit: " + e.getMessage());
        }
    }
    
    /**
     * Gère la suppression d'un produit
     */
    @FXML
    protected void handleDeleteProduct() {
        try {
            Produit selectedProduct = productsTable.getSelectionModel().getSelectedItem();
            if (selectedProduct == null) {
                AlertUtils.showWarningAlert("Attention", "Aucun produit sélectionné", 
                        "Veuillez sélectionner un produit à supprimer.");
                return;
            }
            
            boolean confirm = AlertUtils.showConfirmationAlert("Confirmation", "Supprimer le produit", 
                    "Êtes-vous sûr de vouloir supprimer le produit " + selectedProduct.getNom() + " ?");
            
            if (confirm) {
                boolean success = produitService.supprimerProduit(selectedProduct.getId());
                
                if (success) {
                    // Enregistrer l'activité
                    activiteService.ajouterActivite("Produit", "Suppression du produit " + selectedProduct.getNom(), 
                            currentAdmin.getPrenom() + " " + currentAdmin.getNom(), pharmacieId);
                    
                    // Recharger les données
                    loadProductsData();
                    loadDashboardData();
                    
                    AlertUtils.showInfoAlert("Succès", "Produit supprimé", 
                            "Le produit a été supprimé avec succès.");
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
            AlertUtils.showErrorAlert("Erreur", "Erreur lors de la suppression", 
                    "Impossible de supprimer le produit: " + e.getMessage());
        }
    }
    
    /**
     * Gère l'entrée de stock pour un produit depuis la liste des produits
     */
    @FXML
    protected void handleProductStockEntry() {
        try {
            Produit selectedProduct = productsTable.getSelectionModel().getSelectedItem();
            if (selectedProduct == null) {
                AlertUtils.showWarningAlert("Attention", "Aucun produit sélectionné", 
                        "Veuillez sélectionner un produit pour enregistrer une entrée de stock.");
                return;
            }
            
            // TODO: Implémenter un dialogue pour enregistrer une entrée de stock
            
            // Exemple d'implémentation (à remplacer par un dialogue)
            int quantite = 10;
            
            // Vérifier si le stock existe déjà
            Stock stock = stockService.getStockByProduitAndPharmacie(selectedProduct.getId(), pharmacieId);
            
            boolean success;
            if (stock != null) {
                // Mettre à jour le stock existant
                success = stockService.updateQuantiteStock(stock.getId(), stock.getQuantite() + quantite);
            } else {
                // Créer un nouveau stock
                Stock nouveauStock = new Stock();
                nouveauStock.setProduitId(selectedProduct.getId());
                nouveauStock.setPharmacieId(pharmacieId);
                nouveauStock.setQuantite(quantite);
                nouveauStock.setSeuilMinimum(5);
                nouveauStock.setStatut(quantite <= 5 ? "Alerte" : "Normal");
                
                success = stockService.ajouterStock(nouveauStock);
            }
            
            if (success) {
                // Enregistrer l'activité
                activiteService.ajouterActivite("Stock", "Entrée de stock pour " + selectedProduct.getNom() + " (" + quantite + " unités)", 
                        currentAdmin.getPrenom() + " " + currentAdmin.getNom(), pharmacieId);
                
                // Recharger les données
                loadStockData();
                loadProductsData();
                loadDashboardData();
                
                AlertUtils.showInfoAlert("Succès", "Entrée de stock", 
                        "L'entrée de stock a été enregistrée avec succès.");
            }
        } catch (Exception e) {
            e.printStackTrace();
            AlertUtils.showErrorAlert("Erreur", "Erreur lors de l'entrée de stock", 
                    "Impossible d'enregistrer l'entrée de stock: " + e.getMessage());
        }
    }
    
    /**
     * Gère la sortie de stock pour un produit depuis la liste des produits
     */
    @FXML
    protected void handleProductStockExit() {
        try {
            Produit selectedProduct = productsTable.getSelectionModel().getSelectedItem();
            if (selectedProduct == null) {
                AlertUtils.showWarningAlert("Attention", "Aucun produit sélectionné", 
                        "Veuillez sélectionner un produit pour enregistrer une sortie de stock.");
                return;
            }
            
            if (success) {
                // Enregistrer l'activité
                activiteService.ajouterActivite("Produit", "Suppression du produit " + selectedProduct.getNom(), 
                        currentAdmin.getPrenom() + " " + currentAdmin.getNom(), pharmacieId);

                // Recharger les données
                loadProductsData();
                loadDashboardData();

                AlertUtils.showInfoAlert("Succès", "Produit supprimé", 
                        "Le produit a été supprimé avec succès.");
            }
        }
    } catch (Exception e) {
        e.printStackTrace();
        AlertUtils.showErrorAlert("Erreur", "Erreur lors de la suppression", 
                "Impossible de supprimer le produit: " + e.getMessage());
    }
}

/**
 * Gère l'entrée de stock pour un produit depuis la liste des produits
 */
@FXML
protected void handleProductStockEntry() {
    try {
        Produit selectedProduct = productsTable.getSelectionModel().getSelectedItem();
        if (selectedProduct == null) {
            AlertUtils.showWarningAlert("Attention", "Aucun produit sélectionné", 
                    "Veuillez sélectionner un produit pour enregistrer une entrée de stock.");
            return;
        }

        // TODO: Implémenter un dialogue pour enregistrer une entrée de stock

        // Exemple d'implémentation (à remplacer par un dialogue)
        int quantite = 10;

        // Vérifier si le stock existe déjà
        Stock stock = stockService.getStockByProduitAndPharmacie(selectedProduct.getId(), pharmacieId);

        boolean success;
        if (stock != null) {
            // Mettre à jour le stock existant
            success = stockService.updateQuantiteStock(stock.getId(), stock.getQuantite() + quantite);
        } else {
            // Créer un nouveau stock
            Stock nouveauStock = new Stock();
            nouveauStock.setProduitId(selectedProduct.getId());
            nouveauStock.setPharmacieId(pharmacieId);
            nouveauStock.setQuantite(quantite);
            nouveauStock.setSeuilMinimum(5);
            nouveauStock.setStatut(quantite <= 5 ? "Alerte" : "Normal");

            success = stockService.ajouterStock(nouveauStock);
    @FXML
    protected void handleCreateOrder() {
        try {
            // TODO: Implémenter un dialogue pour créer une commande
            
            // Exemple d'implémentation (à remplacer par un dialogue)
            Fournisseur selectedSupplier = suppliersTable.getSelectionModel().getSelectedItem();
            if (selectedSupplier == null) {
                AlertUtils.showWarningAlert("Attention", "Aucun fournisseur sélectionné", 
                        "Veuillez sélectionner un fournisseur pour créer une commande.");
                return;
            }
            
            Commande nouvelleCommande = new Commande();
            nouvelleCommande.setFournisseurId(selectedSupplier.getId());
            nouvelleCommande.setFournisseurNom(selectedSupplier.getNom());
            nouvelleCommande.setPharmacieId(pharmacieId);
            nouvelleCommande.setStatut("En attente");
            nouvelleCommande.setMontantTotal(0.0);
            nouvelleCommande.setDateCommande(LocalDate.now());
            
            boolean success = commandeService.ajouterCommande(nouvelleCommande);
            
            if (success) {
                // Enregistrer l'activité
                activiteService.ajouterActivite("Commande", "Création d'une commande pour " + selectedSupplier.getNom(), 
                        currentAdmin.getPrenom() + " " + currentAdmin.getNom(), pharmacieId);
                
                // Recharger les données
                loadOrdersData();
                loadDashboardData();
                
                AlertUtils.showInfoAlert("Succès", "Commande créée", 
                        "La commande a été créée avec succès.");
            }
        } catch (Exception e) {
            e.printStackTrace();
            AlertUtils.showErrorAlert("Erreur", "Erreur lors de la création", 
                    "Impossible de créer la commande: " + e.getMessage());
        }
    }
    
    /**
     * Gère la modification d'un fournisseur existant
     */
    @FXML
    protected void handleEditSupplier() {
        try {
            Fournisseur selectedSupplier = suppliersTable.getSelectionModel().getSelectedItem();
            if (selectedSupplier == null) {
                AlertUtils.showWarningAlert("Attention", "Aucun fournisseur sélectionné", 
                        "Veuillez sélectionner un fournisseur à modifier.");
                return;
            }
            
            // TODO: Implémenter un dialogue pour modifier un fournisseur
            
            // Exemple d'implémentation (à remplacer par un dialogue)
            selectedSupplier.setAdresse(selectedSupplier.getAdresse() + " (modifiée)");
            
            boolean success = fournisseurService.modifierFournisseur(selectedSupplier);
            
            if (success) {
                // Enregistrer l'activité
                activiteService.ajouterActivite("Fournisseur", "Modification du fournisseur " + selectedSupplier.getNom(), 
                        currentAdmin.getPrenom() + " " + currentAdmin.getNom(), pharmacieId);
                
                // Recharger les données
                loadSuppliersData();
                
                AlertUtils.showInfoAlert("Succès", "Fournisseur modifié", 
                        "Le fournisseur a été modifié avec succès.");
            }
        } catch (Exception e) {
            e.printStackTrace();
            AlertUtils.showErrorAlert("Erreur", "Erreur lors de la modification", 
                    "Impossible de modifier le fournisseur: " + e.getMessage());
        }
    }
    
    /**
     * Gère la suppression d'un fournisseur
     */
    @FXML
    protected void handleDeleteSupplier() {
        try {
            Fournisseur selectedSupplier = suppliersTable.getSelectionModel().getSelectedItem();
            if (selectedSupplier == null) {
                AlertUtils.showWarningAlert("Attention", "Aucun fournisseur sélectionné", 
                        "Veuillez sélectionner un fournisseur à supprimer.");
                return;
            }
            
            boolean confirm = AlertUtils.showConfirmationAlert("Confirmation", "Supprimer le fournisseur", 
                    "Êtes-vous sûr de vouloir supprimer le fournisseur " + selectedSupplier.getNom() + " ?");
            
            if (confirm) {
                boolean success = fournisseurService.supprimerFournisseur(selectedSupplier.getId());
                
                if (success) {
                    // Enregistrer l'activité
                    activiteService.ajouterActivite("Fournisseur", "Suppression du fournisseur " + selectedSupplier.getNom(), 
                            currentAdmin.getPrenom() + " " + currentAdmin.getNom(), pharmacieId);
                    
                    // Recharger les données
                    loadSuppliersData();
                    loadDashboardData();
                    
                    AlertUtils.showInfoAlert("Succès", "Fournisseur supprimé", 
                            "Le fournisseur a été supprimé avec succès.");
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
            AlertUtils.showErrorAlert("Erreur", "Erreur lors de la suppression", 
                    "Impossible de supprimer le fournisseur: " + e.getMessage());
        }
    }
    
    /**
     * Gère la création d'une nouvelle commande
     */
    @FXML
    protected void handleNewOrder() {
        try {
            // TODO: Implémenter un dialogue pour créer une nouvelle commande
            
            // Exemple d'implémentation (à remplacer par un dialogue)
            // Rediriger vers la gestion des fournisseurs pour sélectionner un fournisseur
            AlertUtils.showInfoAlert("Information", "Création de commande", 
                    "Veuillez sélectionner un fournisseur dans l'onglet 'Fournisseurs' puis cliquer sur 'Nouvelle commande'.");
            
        } catch (Exception e) {
            e.printStackTrace();
            AlertUtils.showErrorAlert("Erreur", "Erreur lors de la création", 
                    "Impossible de créer une nouvelle commande: " + e.getMessage());
        }
    }
    
    /**
     * Gère la modification d'une commande existante
     */
    @FXML
    protected void handleEditOrder() {
        try {
            // Vérifier qu'une commande est sélectionnée
            Commande selectedOrder = ordersTable.getSelectionModel().getSelectedItem();
            if (selectedOrder == null) {
                AlertUtils.showWarningAlert("Attention", "Aucune commande sélectionnée", 
                        "Veuillez sélectionner une commande à modifier.");
                return;
            }
            
            // Vérifier que la commande peut être modifiée (statut)
            if (!selectedOrder.getStatut().equals("En attente")) {
                AlertUtils.showWarningAlert("Attention", "Modification impossible", 
                        "Seules les commandes en attente peuvent être modifiées.");
                return;
            }
            
            // TODO: Implémenter un dialogue pour modifier une commande
            
            // Exemple d'implémentation (à remplacer par un dialogue)
            AlertUtils.showInfoAlert("Information", "Modification de commande", 
                    "La fonctionnalité de modification de commande sera disponible prochainement.");
            
        } catch (Exception e) {
            e.printStackTrace();
            AlertUtils.showErrorAlert("Erreur", "Erreur lors de la modification", 
                    "Impossible de modifier la commande: " + e.getMessage());
        }
    }
    
    /**
     * Gère l'annulation d'une commande
     */
    @FXML
    protected void handleCancelOrder() {
        try {
            // Vérifier qu'une commande est sélectionnée
            Commande selectedOrder = ordersTable.getSelectionModel().getSelectedItem();
            if (selectedOrder == null) {
                AlertUtils.showWarningAlert("Attention", "Aucune commande sélectionnée", 
                        "Veuillez sélectionner une commande à annuler.");
                return;
            }
            
            // Vérifier que la commande peut être annulée (statut)
            if (selectedOrder.getStatut().equals("Livrée") || selectedOrder.getStatut().equals("Annulée")) {
                AlertUtils.showWarningAlert("Attention", "Annulation impossible", 
                        "Les commandes livrées ou déjà annulées ne peuvent pas être annulées.");
                return;
            }
            
            boolean confirm = AlertUtils.showConfirmationAlert("Confirmation", "Annuler la commande", 
                    "Êtes-vous sûr de vouloir annuler la commande #" + selectedOrder.getId() + " ?");
            
            if (confirm) {
                // Mettre à jour le statut de la commande
                selectedOrder.setStatut("Annulée");
                boolean success = commandeService.updateStatutCommande(selectedOrder.getId(), "Annulée");
                
                if (success) {
                    // Enregistrer l'activité
                    activiteService.ajouterActivite("Commande", "Annulation de la commande #" + selectedOrder.getId(), 
                            currentAdmin.getPrenom() + " " + currentAdmin.getNom(), pharmacieId);
                    
                    // Recharger les données
                    loadOrdersData();
                    loadDashboardData();
                    
                    AlertUtils.showInfoAlert("Succès", "Commande annulée", 
                            "La commande a été annulée avec succès.");
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
            AlertUtils.showErrorAlert("Erreur", "Erreur lors de l'annulation", 
                    "Impossible d'annuler la commande: " + e.getMessage());
        }
    }
    
    /**
     * Gère l'entrée de stock
     */
    @FXML
    protected void handleStockEntry() {
        try {
            // Vérifier qu'un produit est sélectionné
            Stock selectedStock = stockTable.getSelectionModel().getSelectedItem();
            if (selectedStock == null) {
                AlertUtils.showWarningAlert("Attention", "Aucun produit sélectionné", 
                        "Veuillez sélectionner un produit pour effectuer une entrée de stock.");
                return;
            }
            
            // TODO: Implémenter un dialogue pour saisir la quantité et la date de péremption
            
            // Exemple d'implémentation (à remplacer par un dialogue)
            int quantiteAjoutee = 10; // À remplacer par la valeur saisie dans le dialogue
            
            // Mettre à jour le stock
            int nouveauStock = selectedStock.getQuantite() + quantiteAjoutee;
            selectedStock.setQuantite(nouveauStock);
            
            // Créer un nouvel objet Stock pour l'ajout
            Stock stockToAdd = new Stock();
            stockToAdd.setProduitId(selectedStock.getProduitId());
            stockToAdd.setPharmacieId(pharmacieId);
            stockToAdd.setQuantite(quantiteAjoutee);
            stockToAdd.setSeuilMinimum(selectedStock.getSeuilMinimum());
            stockToAdd.setStatut("Normal");
            
            boolean success = stockService.ajouterStock(stockToAdd);
            
            if (success) {
                // Enregistrer l'activité
                activiteService.ajouterActivite("Stock", "Entrée de stock: +" + quantiteAjoutee + " " + selectedStock.getProduitNom(), 
                        currentAdmin.getPrenom() + " " + currentAdmin.getNom(), pharmacieId);
                
                // Recharger les données
                loadStockData();
                loadDashboardData();
                
                AlertUtils.showInfoAlert("Succès", "Entrée de stock", 
                        "L'entrée de stock a été enregistrée avec succès.");
            }
        } catch (Exception e) {
            e.printStackTrace();
            AlertUtils.showErrorAlert("Erreur", "Erreur lors de l'entrée de stock", 
                    "Impossible d'enregistrer l'entrée de stock: " + e.getMessage());
        }
    }
    
    /**
     * Gère l'ajustement de stock
     */
    @FXML
    protected void handleStockAdjustment() {
        try {
            // Vérifier qu'un produit est sélectionné
            Stock selectedStock = stockTable.getSelectionModel().getSelectedItem();
            if (selectedStock == null) {
                AlertUtils.showWarningAlert("Attention", "Aucun produit sélectionné", 
                        "Veuillez sélectionner un produit pour effectuer un ajustement de stock.");
                return;
            }
            
            // TODO: Implémenter un dialogue pour saisir la nouvelle quantité et la raison
            
            // Exemple d'implémentation (à remplacer par un dialogue)
            int nouvelleQuantite = 20; // À remplacer par la valeur saisie dans le dialogue
            String raison = "Inventaire"; // À remplacer par la valeur saisie dans le dialogue
            
            int difference = nouvelleQuantite - selectedStock.getQuantite();
            
            // Mettre à jour le stock
            selectedStock.setQuantite(nouvelleQuantite);
            
            // Mettre à jour directement la quantité du stock existant
            boolean success = stockService.updateQuantiteStock(selectedStock.getId(), nouvelleQuantite);
            
            if (success) {
                // Enregistrer l'activité
                String operation = difference >= 0 ? "+" + difference : String.valueOf(difference);
                activiteService.ajouterActivite("Stock", "Ajustement de stock: " + operation + " " + selectedStock.getProduitNom() + " (" + raison + ")", 
                        currentAdmin.getPrenom() + " " + currentAdmin.getNom(), pharmacieId);
                
                // Recharger les données
                loadStockData();
                loadDashboardData();
                
                AlertUtils.showInfoAlert("Succès", "Ajustement de stock", 
                        "L'ajustement de stock a été enregistré avec succès.");
            }
        } catch (Exception e) {
            e.printStackTrace();
            AlertUtils.showErrorAlert("Erreur", "Erreur lors de l'ajustement de stock", 
                    "Impossible d'enregistrer l'ajustement de stock: " + e.getMessage());
        }
    }
    
    /**
     * Gère la sortie de stock
     */
    @FXML
    protected void handleStockExit() {
        try {
            // Vérifier qu'un produit est sélectionné
            Stock selectedStock = stockTable.getSelectionModel().getSelectedItem();
            if (selectedStock == null) {
                AlertUtils.showWarningAlert("Attention", "Aucun produit sélectionné", 
                        "Veuillez sélectionner un produit pour effectuer une sortie de stock.");
                return;
            }
            
            // TODO: Implémenter un dialogue pour saisir la quantité et la raison
            
            // Exemple d'implémentation (à remplacer par un dialogue)
            int quantiteSortie = 5; // À remplacer par la valeur saisie dans le dialogue
            String raison = "Vente"; // À remplacer par la valeur saisie dans le dialogue
            
            // Vérifier que la quantité en stock est suffisante
            if (selectedStock.getQuantite() < quantiteSortie) {
                AlertUtils.showWarningAlert("Attention", "Stock insuffisant", 
                        "La quantité en stock est insuffisante pour effectuer cette sortie.");
                return;
            }
            
            // Mettre à jour le stock
            int nouveauStock = selectedStock.getQuantite() - quantiteSortie;
            selectedStock.setQuantite(nouveauStock);
            
            // Mettre à jour directement la quantité du stock existant
            boolean success = stockService.updateQuantiteStock(selectedStock.getId(), nouveauStock);
            
            if (success) {
                // Enregistrer l'activité
                activiteService.ajouterActivite("Stock", "Sortie de stock: -" + quantiteSortie + " " + selectedStock.getProduitNom() + " (" + raison + ")", 
                        currentAdmin.getPrenom() + " " + currentAdmin.getNom(), pharmacieId);
                
                // Recharger les données
                loadStockData();
                loadDashboardData();
                
                AlertUtils.showInfoAlert("Succès", "Sortie de stock", 
                        "La sortie de stock a été enregistrée avec succès.");
            }
        } catch (Exception e) {
            e.printStackTrace();
            AlertUtils.showErrorAlert("Erreur", "Erreur lors de la sortie de stock", 
                    "Impossible d'enregistrer la sortie de stock: " + e.getMessage());
        }
    }
}
