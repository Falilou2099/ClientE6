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
import javafx.application.Platform;
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
    
    // Liste des activités
    private ObservableList<Activite> activitiesList = FXCollections.observableArrayList();
    
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
    
    // Table et colonnes pour les activités récentes
    @FXML private TableView<Activite> recentActivitiesTable;
    @FXML private TableColumn<Activite, LocalDateTime> activityDateColumn;
    @FXML private TableColumn<Activite, String> activityTypeColumn;
    @FXML private TableColumn<Activite, String> activityDescriptionColumn;
    @FXML private TableColumn<Activite, String> activityUserColumn;
    
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
    
    
    // Listes observables pour les tableaux
    private ObservableList<Produit> productsList = FXCollections.observableArrayList();
    private ObservableList<Fournisseur> suppliersList = FXCollections.observableArrayList();
    private ObservableList<Commande> ordersList = FXCollections.observableArrayList();
    private ObservableList<Stock> stocksList = FXCollections.observableArrayList();
    
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
        try {
            // Obtenir les informations de la pharmacie
            Pharmacie pharmacie = new PharmacieService().getPharmacieById(pharmacieId);
            if (pharmacie != null) {
                pharmacyNameLabel.setText(pharmacie.getNom());
            }
        } catch (Exception e) {
            e.printStackTrace();
            AlertUtils.showErrorAlert("Erreur", "Erreur lors du chargement des informations", 
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
            int pendingOrders = commandeService.getCommandesByStatus(pharmacieId, "En attente").size();
            pendingOrdersLabel.setText(String.valueOf(pendingOrders));
            
            // Nombre de fournisseurs
            int suppliersCount = fournisseurService.getAllFournisseurs(pharmacieId).size();
            suppliersCountLabel.setText(String.valueOf(suppliersCount));
            
            // Nombre de ventes totales (à implémenter)
            totalSalesLabel.setText("0");
            
            // Nombre de nouveaux produits (derniers 30 jours)
            int newProducts = 0;
            LocalDate thirtyDaysAgo = LocalDate.now().minusDays(30);
            for (Produit produit : produitService.getAllProduits(pharmacieId)) {
                if (produit.getDateCreation() != null && 
                    produit.getDateCreation().isAfter(thirtyDaysAgo)) {
                    newProducts++;
                }
            }
            newProductsLabel.setText(String.valueOf(newProducts));
            
            // Nombre d'alertes de stock
            int stockAlerts = 0;
            for (Stock stock : stockService.getAllStocks(pharmacieId)) {
                if (stock.getQuantite() <= stock.getQuantiteMinimum()) {
                    stockAlerts++;
                }
            }
            stockAlertsLabel.setText(String.valueOf(stockAlerts));
            
        } catch (Exception e) {
            e.printStackTrace();
            AlertUtils.showErrorAlert("Erreur", "Erreur lors du chargement des données", 
                    "Impossible de charger les données du tableau de bord: " + e.getMessage());
        }
    }
    
    /**
     * Initialise les filtres pour les tableaux
     */
    private void initializeFilters() {
        try {
            // Catégories de produits
            List<String> categories = produitService.getAllCategories(pharmacieId);
            productCategoryFilter.getItems().add("Toutes les catégories");
            productCategoryFilter.getItems().addAll(categories);
            productCategoryFilter.setValue("Toutes les catégories");
            
            // Statuts des commandes
            orderStatusFilter.getItems().add("Tous les statuts");
            orderStatusFilter.getItems().addAll("En attente", "En cours", "Livrée", "Annulée");
            orderStatusFilter.setValue("Tous les statuts");
            
            // Filtres pour les alertes de stock
            stockAlertFilter.getItems().add("Tous les stocks");
            stockAlertFilter.getItems().addAll("Stocks bas", "Stocks normaux", "Stocks élevés");
            stockAlertFilter.setValue("Tous les stocks");
            
            // Ajouter les écouteurs d'événements pour les filtres
            productCategoryFilter.setOnAction(e -> filterProducts());
            orderStatusFilter.setOnAction(e -> filterOrders());
            stockAlertFilter.setOnAction(e -> filterStocks());
            
            // Ajouter les écouteurs d'événements pour les champs de recherche
            productSearchField.textProperty().addListener((observable, oldValue, newValue) -> filterProducts());
            supplierSearchField.textProperty().addListener((observable, oldValue, newValue) -> filterSuppliers());
            orderSearchField.textProperty().addListener((observable, oldValue, newValue) -> filterOrders());
            stockSearchField.textProperty().addListener((observable, oldValue, newValue) -> filterStocks());
            
        } catch (Exception e) {
            e.printStackTrace();
            System.out.println("Erreur lors de l'initialisation des filtres: " + e.getMessage());
        }
    }
    
    /**
     * Configure les colonnes des tableaux
     */
    private void configureTableColumns() {
        try {
            // Configuration des colonnes pour les produits
            productIdColumn.setCellValueFactory(new PropertyValueFactory<>("id"));
            productNameColumn.setCellValueFactory(new PropertyValueFactory<>("nom"));
            productDescColumn.setCellValueFactory(new PropertyValueFactory<>("description"));
            productPriceColumn.setCellValueFactory(new PropertyValueFactory<>("prixVente"));
            productCostColumn.setCellValueFactory(new PropertyValueFactory<>("prixAchat"));
            productStockColumn.setCellValueFactory(new PropertyValueFactory<>("quantiteStock"));
            productCategoryColumn.setCellValueFactory(new PropertyValueFactory<>("categorie"));
            productExpiryColumn.setCellValueFactory(new PropertyValueFactory<>("dateExpiration"));
            
            // Configuration des colonnes pour les fournisseurs
            supplierIdColumn.setCellValueFactory(new PropertyValueFactory<>("id"));
            supplierNameColumn.setCellValueFactory(new PropertyValueFactory<>("nom"));
            supplierAddressColumn.setCellValueFactory(new PropertyValueFactory<>("adresse"));
            supplierPhoneColumn.setCellValueFactory(new PropertyValueFactory<>("telephone"));
            supplierEmailColumn.setCellValueFactory(new PropertyValueFactory<>("email"));
            supplierSiretColumn.setCellValueFactory(new PropertyValueFactory<>("siret"));
            
            // Configuration des colonnes pour les commandes
            orderIdColumn.setCellValueFactory(new PropertyValueFactory<>("id"));
            orderDateColumn.setCellValueFactory(new PropertyValueFactory<>("dateCommande"));
            orderSupplierColumn.setCellValueFactory(new PropertyValueFactory<>("fournisseurNom"));
            orderStatusColumn.setCellValueFactory(new PropertyValueFactory<>("statut"));
            orderTotalColumn.setCellValueFactory(new PropertyValueFactory<>("montantTotal"));
            orderDeliveryDateColumn.setCellValueFactory(new PropertyValueFactory<>("dateLivraison"));
            
            // Configuration des colonnes pour les stocks
            stockProductIdColumn.setCellValueFactory(new PropertyValueFactory<>("produitId"));
            stockProductNameColumn.setCellValueFactory(new PropertyValueFactory<>("produitNom"));
            stockQuantityColumn.setCellValueFactory(new PropertyValueFactory<>("quantite"));
            stockMinimumColumn.setCellValueFactory(new PropertyValueFactory<>("quantiteMinimum"));
            stockStatusColumn.setCellValueFactory(new PropertyValueFactory<>("statut"));
            stockExpiryColumn.setCellValueFactory(new PropertyValueFactory<>("dateExpiration"));
            stockLastMovementColumn.setCellValueFactory(new PropertyValueFactory<>("dernierMouvement"));
            
            // Configuration des colonnes pour les activités
            activityDateColumn.setCellValueFactory(new PropertyValueFactory<>("date"));
            activityTypeColumn.setCellValueFactory(new PropertyValueFactory<>("type"));
            activityDescriptionColumn.setCellValueFactory(new PropertyValueFactory<>("description"));
            activityUserColumn.setCellValueFactory(new PropertyValueFactory<>("utilisateur"));
            
        } catch (Exception e) {
            e.printStackTrace();
            System.out.println("Erreur lors de la configuration des colonnes: " + e.getMessage());
        }
    }
    
    /**
     * Charge les données des produits
     */
    private void loadProductsData() {
        try {
            // Vider la liste existante
            productsList.clear();
            
            // Récupérer tous les produits de la pharmacie
            List<Produit> products = produitService.getAllProduits(pharmacieId);
            
            // Ajouter les produits à la liste observable
            productsList.addAll(products);
            
            // Appliquer les filtres
            filterProducts();
            
        } catch (Exception e) {
            e.printStackTrace();
            AlertUtils.showErrorAlert("Erreur", "Erreur lors du chargement des produits", 
                    "Impossible de charger les produits: " + e.getMessage());
        }
    }
    
    /**
     * Charge les données des fournisseurs
     */
    private void loadSuppliersData() {
        try {
            // Vider la liste existante
            suppliersList.clear();
            
            // Récupérer tous les fournisseurs de la pharmacie
            List<Fournisseur> suppliers = fournisseurService.getAllFournisseurs(pharmacieId);
            
            // Ajouter les fournisseurs à la liste observable
            suppliersList.addAll(suppliers);
            
            // Appliquer les filtres
            filterSuppliers();
            
        } catch (Exception e) {
            e.printStackTrace();
            AlertUtils.showErrorAlert("Erreur", "Erreur lors du chargement des fournisseurs", 
                    "Impossible de charger les fournisseurs: " + e.getMessage());
        }
    }
    
    /**
     * Charge les données des commandes
     */
    private void loadOrdersData() {
        try {
            // Vider la liste existante
            ordersList.clear();
            
            // Récupérer toutes les commandes de la pharmacie
            List<Commande> orders = commandeService.getAllCommandes(pharmacieId);
            
            // Ajouter les commandes à la liste observable
            ordersList.addAll(orders);
            
            // Appliquer les filtres
            filterOrders();
            
        } catch (Exception e) {
            e.printStackTrace();
            AlertUtils.showErrorAlert("Erreur", "Erreur lors du chargement des commandes", 
                    "Impossible de charger les commandes: " + e.getMessage());
        }
    }
    
    /**
     * Charge les données des stocks
     */
    private void loadStockData() {
        try {
            // Vider la liste existante
            stocksList.clear();
            
            // Récupérer tous les stocks de la pharmacie
            List<Stock> stocks = stockService.getAllStocks(pharmacieId);
            
            // Ajouter les stocks à la liste observable
            stocksList.addAll(stocks);
            
            // Appliquer les filtres
            filterStocks();
            
        } catch (Exception e) {
            e.printStackTrace();
            AlertUtils.showErrorAlert("Erreur", "Erreur lors du chargement des stocks", 
                    "Impossible de charger les stocks: " + e.getMessage());
        }
    }
    
    /**
     * Charge les données des activités récentes
     */
    private void loadActivitiesData() {
        try {
            // Vider la liste existante
            activitiesList.clear();
            
            // Récupérer toutes les activités de la pharmacie (limité aux 50 plus récentes)
            List<Activite> activities = activiteService.getRecentActivities(pharmacieId, 50);
            
            // Ajouter les activités à la liste observable
            activitiesList.addAll(activities);
            
            // Mettre à jour le tableau
            recentActivitiesTable.setItems(activitiesList);
            
        } catch (Exception e) {
            e.printStackTrace();
            AlertUtils.showErrorAlert("Erreur", "Erreur lors du chargement des activités", 
                    "Impossible de charger les activités récentes: " + e.getMessage());
        }
    }
    
    /**
     * Filtre les produits en fonction des critères de recherche et de la catégorie sélectionnée
     */
    private void filterProducts() {
        try {
            // Créer une liste filtrée
            FilteredList<Produit> filteredData = new FilteredList<>(productsList, p -> true);
            
            // Appliquer le filtre de recherche
            String searchText = productSearchField.getText().toLowerCase();
            String categoryFilter = productCategoryFilter.getValue();
            
            filteredData.setPredicate(produit -> {
                // Si le texte de recherche est vide et la catégorie est "Toutes les catégories", afficher tous les produits
                if (searchText == null || searchText.isEmpty()) {
                    if (categoryFilter.equals("Toutes les catégories")) {
                        return true;
                    } else {
                        return produit.getCategorie().equals(categoryFilter);
                    }
                }
                
                // Comparer le nom et la description du produit avec le texte de recherche
                String lowerCaseFilter = searchText.toLowerCase();
                boolean matchesSearch = false;
                
                if (produit.getNom().toLowerCase().contains(lowerCaseFilter)) {
                    matchesSearch = true;
                } else if (produit.getDescription().toLowerCase().contains(lowerCaseFilter)) {
                    matchesSearch = true;
                } else if (String.valueOf(produit.getId()).contains(lowerCaseFilter)) {
                    matchesSearch = true;
                }
                
                // Vérifier si la catégorie correspond
                boolean matchesCategory = categoryFilter.equals("Toutes les catégories") || 
                                         produit.getCategorie().equals(categoryFilter);
                
                return matchesSearch && matchesCategory;
            });
            
            // Créer une liste triée
            SortedList<Produit> sortedData = new SortedList<>(filteredData);
            sortedData.comparatorProperty().bind(productsTable.comparatorProperty());
            
            // Mettre à jour le tableau
            productsTable.setItems(sortedData);
            
        } catch (Exception e) {
            e.printStackTrace();
            System.out.println("Erreur lors du filtrage des produits: " + e.getMessage());
        }
    }
    
    /**
     * Filtre les fournisseurs en fonction des critères de recherche
     */
    private void filterSuppliers() {
        try {
            // Créer une liste filtrée
            FilteredList<Fournisseur> filteredData = new FilteredList<>(suppliersList, p -> true);
            
            // Appliquer le filtre de recherche
            String searchText = supplierSearchField.getText().toLowerCase();
            
            filteredData.setPredicate(fournisseur -> {
                // Si le texte de recherche est vide, afficher tous les fournisseurs
                if (searchText == null || searchText.isEmpty()) {
                    return true;
                }
                
                // Comparer le nom, l'adresse, le téléphone et l'email du fournisseur avec le texte de recherche
                String lowerCaseFilter = searchText.toLowerCase();
                
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
                } else if (String.valueOf(fournisseur.getId()).contains(lowerCaseFilter)) {
                    return true;
                }
                
                return false;
            });
            
            // Créer une liste triée
            SortedList<Fournisseur> sortedData = new SortedList<>(filteredData);
            sortedData.comparatorProperty().bind(suppliersTable.comparatorProperty());
            
            // Mettre à jour le tableau
            suppliersTable.setItems(sortedData);
            
        } catch (Exception e) {
            e.printStackTrace();
            System.out.println("Erreur lors du filtrage des fournisseurs: " + e.getMessage());
        }
    }
    
    /**
     * Filtre les commandes en fonction des critères de recherche et du statut sélectionné
     */
    private void filterOrders() {
        try {
            // Créer une liste filtrée
            FilteredList<Commande> filteredData = new FilteredList<>(ordersList, p -> true);
            
            // Appliquer le filtre de recherche
            String searchText = orderSearchField.getText().toLowerCase();
            String statusFilter = orderStatusFilter.getValue();
            
            filteredData.setPredicate(commande -> {
                // Si le texte de recherche est vide et le statut est "Tous les statuts", afficher toutes les commandes
                if ((searchText == null || searchText.isEmpty()) && statusFilter.equals("Tous les statuts")) {
                    return true;
                }
                
                // Vérifier si le statut correspond
                boolean matchesStatus = statusFilter.equals("Tous les statuts") || 
                                       commande.getStatut().equals(statusFilter);
                
                // Si le texte de recherche est vide, filtrer uniquement par statut
                if (searchText == null || searchText.isEmpty()) {
                    return matchesStatus;
                }
                
                // Comparer l'ID, le fournisseur et le statut de la commande avec le texte de recherche
                String lowerCaseFilter = searchText.toLowerCase();
                boolean matchesSearch = false;
                
                if (String.valueOf(commande.getId()).contains(lowerCaseFilter)) {
                    matchesSearch = true;
                } else if (commande.getFournisseurNom().toLowerCase().contains(lowerCaseFilter)) {
                    matchesSearch = true;
                } else if (commande.getStatut().toLowerCase().contains(lowerCaseFilter)) {
                    matchesSearch = true;
                }
                
                return matchesSearch && matchesStatus;
            });
            
            // Créer une liste triée
            SortedList<Commande> sortedData = new SortedList<>(filteredData);
            sortedData.comparatorProperty().bind(ordersTable.comparatorProperty());
            
            // Mettre à jour le tableau
            ordersTable.setItems(sortedData);
            
        } catch (Exception e) {
            e.printStackTrace();
            System.out.println("Erreur lors du filtrage des commandes: " + e.getMessage());
        }
    }
    
    /**
     * Filtre les stocks en fonction des critères de recherche et du filtre d'alerte sélectionné
     */
    private void filterStocks() {
        try {
            // Créer une liste filtrée
            FilteredList<Stock> filteredData = new FilteredList<>(stocksList, p -> true);
            
            // Appliquer le filtre de recherche
            String searchText = stockSearchField.getText().toLowerCase();
            String alertFilter = stockAlertFilter.getValue();
            
            filteredData.setPredicate(stock -> {
                // Si le texte de recherche est vide et le filtre est "Tous les stocks", afficher tous les stocks
                if ((searchText == null || searchText.isEmpty()) && alertFilter.equals("Tous les stocks")) {
                    return true;
                }
                
                // Vérifier si le filtre d'alerte correspond
                boolean matchesAlert = false;
                if (alertFilter.equals("Tous les stocks")) {
                    matchesAlert = true;
                } else if (alertFilter.equals("Stocks bas") && stock.getQuantite() <= stock.getQuantiteMinimum()) {
                    matchesAlert = true;
                } else if (alertFilter.equals("Stocks normaux") && 
                           stock.getQuantite() > stock.getQuantiteMinimum() && 
                           stock.getQuantite() < stock.getQuantiteMinimum() * 2) {
                    matchesAlert = true;
                } else if (alertFilter.equals("Stocks élevés") && stock.getQuantite() >= stock.getQuantiteMinimum() * 2) {
                    matchesAlert = true;
                }
                
                // Si le texte de recherche est vide, filtrer uniquement par alerte
                if (searchText == null || searchText.isEmpty()) {
                    return matchesAlert;
                }
                
                // Comparer l'ID du produit, le nom du produit et le statut du stock avec le texte de recherche
                String lowerCaseFilter = searchText.toLowerCase();
                boolean matchesSearch = false;
                
                if (String.valueOf(stock.getProduitId()).contains(lowerCaseFilter)) {
                    matchesSearch = true;
                } else if (stock.getProduitNom().toLowerCase().contains(lowerCaseFilter)) {
                    matchesSearch = true;
                } else if (stock.getStatut().toLowerCase().contains(lowerCaseFilter)) {
                    matchesSearch = true;
                }
                
                return matchesSearch && matchesAlert;
            });
            
            // Créer une liste triée
            SortedList<Stock> sortedData = new SortedList<>(filteredData);
            sortedData.comparatorProperty().bind(stockTable.comparatorProperty());
            
            // Mettre à jour le tableau
            stockTable.setItems(sortedData);
            
        } catch (Exception e) {
            e.printStackTrace();
            System.out.println("Erreur lors du filtrage des stocks: " + e.getMessage());
        }
    }
    
    /**
     * Gère l'ajout d'un nouveau produit
     */
    @FXML
    protected void handleAddProduct() {
        try {
            // Créer et afficher le dialogue d'ajout de produit Swing
            javax.swing.SwingUtilities.invokeLater(() -> {
                com.gestionpharma.AjoutProduitDialog dialog = new com.gestionpharma.AjoutProduitDialog(null);
                dialog.setVisible(true);
                
                // Vérifier si l'utilisateur a confirmé l'ajout
                if (dialog.estConfirme()) {
                    Produit nouveauProduit = dialog.getProduit();
                    if (nouveauProduit != null) {
                        try {
                            // Définir l'ID de la pharmacie
                            nouveauProduit.setPharmacieId(pharmacieId);
                            nouveauProduit.setDateCreation(LocalDate.now());
                            
                            // Ajouter le produit à la base de données
                            boolean success = produitService.addProduit(nouveauProduit);
                            
                            if (success) {
                                // Enregistrer l'activité
                                String message = "Ajout du produit: " + nouveauProduit.getNom() + 
                                        " (Catégorie: " + nouveauProduit.getCategorie() + 
                                        ", Prix: " + nouveauProduit.getPrixVente() + "€)";
                                activiteService.ajouterActivite("Produit", message, 
                                        currentAdmin.getPrenom() + " " + currentAdmin.getNom(), pharmacieId);
                                
                                // Recharger les données
                                Platform.runLater(() -> {
                                    loadProductsData();
                                    loadDashboardData();
                                });
                                
                                javax.swing.JOptionPane.showMessageDialog(null, 
                                        "Produit ajouté avec succès!", 
                                        "Succès", javax.swing.JOptionPane.INFORMATION_MESSAGE);
                            } else {
                                javax.swing.JOptionPane.showMessageDialog(null, 
                                        "Erreur lors de l'ajout du produit", 
                                        "Erreur", javax.swing.JOptionPane.ERROR_MESSAGE);
                            }
                        } catch (Exception e) {
                            e.printStackTrace();
                            javax.swing.JOptionPane.showMessageDialog(null, 
                                    "Erreur lors de l'ajout: " + e.getMessage(), 
                                    "Erreur", javax.swing.JOptionPane.ERROR_MESSAGE);
                        }
                    }
                }
            });
            
            // Enregistrer l'activité
            activiteService.ajouterActivite("Produit", "Ajout d'un produit", 
                    currentAdmin.getPrenom() + " " + currentAdmin.getNom(), pharmacieId);
        } catch (Exception e) {
            e.printStackTrace();
            AlertUtils.showErrorAlert("Erreur", "Erreur lors de l'ajout de produit", 
                    "Impossible d'ouvrir le dialogue d'ajout: " + e.getMessage());
        }
    }
    
    /**
     * Gère la modification d'un produit
     */
    @FXML
    protected void handleEditProduct() {
        try {
            // Vérifier qu'un produit est sélectionné
            Produit selectedProduct = productsTable.getSelectionModel().getSelectedItem();
            if (selectedProduct == null) {
                AlertUtils.showWarningAlert("Attention", "Aucun produit sélectionné", 
                        "Veuillez sélectionner un produit à modifier.");
                return;
            }
            
            // Créer un dialogue pour modifier les informations du produit
            Dialog<Produit> dialog = new Dialog<>();
            dialog.setTitle("Modifier un produit");
            dialog.setHeaderText("Modifiez les informations du produit");
            
            // Boutons
            ButtonType saveButtonType = new ButtonType("Enregistrer", ButtonBar.ButtonData.OK_DONE);
            dialog.getDialogPane().getButtonTypes().addAll(saveButtonType, ButtonType.CANCEL);
            
            // Grille pour les champs
            GridPane grid = new GridPane();
            grid.setHgap(10);
            grid.setVgap(10);
            grid.setPadding(new Insets(20, 150, 10, 10));
            
            // Champs de saisie
            TextField nomField = new TextField(selectedProduct.getNom());
            TextField descriptionField = new TextField(selectedProduct.getDescription());
            TextField prixVenteField = new TextField(String.valueOf(selectedProduct.getPrixVente()));
            TextField prixAchatField = new TextField(String.valueOf(selectedProduct.getPrixAchat()));
            
            ComboBox<String> categorieComboBox = new ComboBox<>();
            categorieComboBox.getItems().addAll(produitService.getAllCategories(pharmacieId));
            categorieComboBox.setValue(selectedProduct.getCategorie());
            
            DatePicker dateExpirationPicker = new DatePicker();
            if (selectedProduct.getDateExpiration() != null) {
                dateExpirationPicker.setValue(selectedProduct.getDateExpiration());
            }
            
            // Ajouter les champs à la grille
            grid.add(new Label("Nom:"), 0, 0);
            grid.add(nomField, 1, 0);
            grid.add(new Label("Description:"), 0, 1);
            grid.add(descriptionField, 1, 1);
            grid.add(new Label("Prix de vente:"), 0, 2);
            grid.add(prixVenteField, 1, 2);
            grid.add(new Label("Prix d'achat:"), 0, 3);
            grid.add(prixAchatField, 1, 3);
            grid.add(new Label("Catégorie:"), 0, 4);
            grid.add(categorieComboBox, 1, 4);
            grid.add(new Label("Date d'expiration:"), 0, 5);
            grid.add(dateExpirationPicker, 1, 5);
            
            dialog.getDialogPane().setContent(grid);
            
            // Focus sur le premier champ
            Platform.runLater(() -> nomField.requestFocus());
            
            // Convertir le résultat
            dialog.setResultConverter(dialogButton -> {
                if (dialogButton == saveButtonType) {
                    try {
                        // Valider les champs obligatoires
                        if (nomField.getText().isEmpty()) {
                            throw new IllegalArgumentException("Le nom du produit est obligatoire");
                        }
                        
                        if (prixVenteField.getText().isEmpty() || prixAchatField.getText().isEmpty()) {
                            throw new IllegalArgumentException("Les prix sont obligatoires");
                        }
                        
                        if (categorieComboBox.getValue() == null) {
                            throw new IllegalArgumentException("La catégorie est obligatoire");
                        }
                        
                        // Mettre à jour le produit
                        selectedProduct.setNom(nomField.getText());
                        selectedProduct.setDescription(descriptionField.getText());
                        selectedProduct.setPrixVente(Double.parseDouble(prixVenteField.getText()));
                        selectedProduct.setPrixAchat(Double.parseDouble(prixAchatField.getText()));
                        selectedProduct.setCategorie(categorieComboBox.getValue());
                        
                        if (dateExpirationPicker.getValue() != null) {
                            selectedProduct.setDateExpiration(dateExpirationPicker.getValue());
                        }
                        
                        return selectedProduct;
                    } catch (NumberFormatException e) {
                        AlertUtils.showErrorAlert("Erreur", "Format invalide", 
                                "Les prix doivent être des nombres valides");
                        return null;
                    } catch (IllegalArgumentException e) {
                        AlertUtils.showErrorAlert("Erreur", "Champs obligatoires", e.getMessage());
                        return null;
                    }
                }
                return null;
            });
            
            // Afficher le dialogue et traiter le résultat
            Optional<Produit> result = dialog.showAndWait();
            
            result.ifPresent(produit -> {
                try {
                    // Mettre à jour le produit dans la base de données
                    boolean success = produitService.updateProduit(produit);
                    
                    if (success) {
                        // Enregistrer l'activité
                        activiteService.ajouterActivite("Produit", "Modification du produit: " + produit.getNom(), 
                                currentAdmin.getPrenom() + " " + currentAdmin.getNom(), pharmacieId);
                        
                        // Recharger les données
                        loadProductsData();
                        
                        AlertUtils.showInfoAlert("Succès", "Produit modifié", 
                                "Le produit a été modifié avec succès.");
                    } else {
                        AlertUtils.showErrorAlert("Erreur", "Erreur lors de la modification du produit", 
                                "Impossible de modifier le produit. Veuillez réessayer.");
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                    AlertUtils.showErrorAlert("Erreur", "Erreur lors de la modification du produit", 
                            "Impossible de modifier le produit: " + e.getMessage());
                }
            });
            
        } catch (Exception e) {
            e.printStackTrace();
            AlertUtils.showErrorAlert("Erreur", "Erreur lors de la modification du produit", 
                    "Impossible de modifier le produit: " + e.getMessage());
        }
    }
    
    /**
     * Gère la suppression d'un produit
     */
    @FXML
    protected void handleDeleteProduct() {
        try {
            // Vérifier qu'un produit est sélectionné
            Produit selectedProduct = productsTable.getSelectionModel().getSelectedItem();
            if (selectedProduct == null) {
                AlertUtils.showWarningAlert("Attention", "Aucun produit sélectionné", 
                        "Veuillez sélectionner un produit à supprimer.");
                return;
            }
            
            // Demander confirmation
            Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
            alert.setTitle("Confirmation de suppression");
            alert.setHeaderText("Êtes-vous sûr de vouloir supprimer ce produit ?");
            alert.setContentText("Cette action est irréversible.");
            
            Optional<ButtonType> result = alert.showAndWait();
            if (result.get() == ButtonType.OK){
                // Supprimer le produit
                boolean success = produitService.deleteProduit(selectedProduct.getId());
                
                if (success) {
                    // Enregistrer l'activité
                    activiteService.ajouterActivite("Suppression", 
                            "Suppression du produit: " + selectedProduct.getNom(), 
                            currentAdmin.getUsername(), pharmacieId);
                    
                    // Recharger les données
                    loadProductsData();
                    loadDashboardData();
                    
                    AlertUtils.showInfoAlert("Succès", "Produit supprimé", 
                            "Le produit a été supprimé avec succès.");
                } else {
                    AlertUtils.showErrorAlert("Erreur", "Erreur lors de la suppression du produit", 
                            "Impossible de supprimer le produit. Veuillez réessayer.");
                }
            }
            
        } catch (Exception e) {
            e.printStackTrace();
            AlertUtils.showErrorAlert("Erreur", "Erreur lors de la suppression du produit", 
                    "Impossible de supprimer le produit: " + e.getMessage());
        }
    }

    /**
     * Gère la suppression d'un fournisseur
     */
    @FXML
    protected void handleDeleteSupplier() {
        try {
            // Vérifier qu'un fournisseur est sélectionné
            Fournisseur selectedSupplier = suppliersTable.getSelectionModel().getSelectedItem();
            if (selectedSupplier == null) {
                AlertUtils.showWarningAlert("Attention", "Aucun fournisseur sélectionné", 
                        "Veuillez sélectionner un fournisseur à supprimer.");
                return;
            }
            
            // Demander confirmation
            Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
            alert.setTitle("Confirmation de suppression");
            alert.setHeaderText("Êtes-vous sûr de vouloir supprimer ce fournisseur ?");
            alert.setContentText("Cette action est irréversible.");
            
            Optional<ButtonType> result = alert.showAndWait();
            if (result.get() == ButtonType.OK){
                // Supprimer le fournisseur
                boolean success = fournisseurService.deleteFournisseur(selectedSupplier.getId());
                
                if (success) {
                    // Enregistrer l'activité
                    activiteService.ajouterActivite("Suppression", 
                            "Suppression du fournisseur: " + selectedSupplier.getNom(), 
                            currentAdmin.getUsername(), pharmacieId);
                    
                    // Recharger les données
                    loadSuppliersData();
                    loadDashboardData();
                    
                    AlertUtils.showInfoAlert("Succès", "Fournisseur supprimé", 
                            "Le fournisseur a été supprimé avec succès.");
                } else {
                    AlertUtils.showErrorAlert("Erreur", "Erreur lors de la suppression du fournisseur", 
                            "Impossible de supprimer le fournisseur. Veuillez réessayer.");
                }
            }
            
        } catch (Exception e) {
            e.printStackTrace();
            AlertUtils.showErrorAlert("Erreur", "Erreur lors de la suppression du fournisseur", 
                    "Impossible de supprimer le fournisseur: " + e.getMessage());
        }
    }

    /**
     * Gère l'ajout d'un nouveau fournisseur
     */
    @FXML
    protected void handleAddSupplier() {
        try {
            // Créer et afficher le dialogue d'ajout de fournisseur Swing
            javax.swing.SwingUtilities.invokeLater(() -> {
                com.gestionpharma.AjoutFournisseurDialog dialog = new com.gestionpharma.AjoutFournisseurDialog(null);
                dialog.setVisible(true);
                
                // Vérifier si l'utilisateur a confirmé l'ajout
                if (dialog.estConfirme()) {
                    Fournisseur nouveauFournisseur = dialog.getFournisseur();
                    if (nouveauFournisseur != null) {
                        try {
                            // Définir l'ID de la pharmacie
                            nouveauFournisseur.setPharmacieId(pharmacieId);
                            
                            // Ajouter le fournisseur à la base de données
                            boolean success = fournisseurService.addFournisseur(nouveauFournisseur);
                            
                            if (success) {
                                // Enregistrer l'activité
                                String message = "Ajout du fournisseur: " + nouveauFournisseur.getNom() + 
                                        " (Tel: " + nouveauFournisseur.getTelephone() + 
                                        ", Email: " + nouveauFournisseur.getEmail() + ")";
                                activiteService.ajouterActivite("Fournisseur", message, 
                                        currentAdmin.getPrenom() + " " + currentAdmin.getNom(), pharmacieId);
                                
                                // Recharger les données
                                Platform.runLater(() -> {
                                    loadSuppliersData();
                                    loadDashboardData();
                                });
                                
                                javax.swing.JOptionPane.showMessageDialog(null, 
                                        "Fournisseur ajouté avec succès!", 
                                        "Succès", javax.swing.JOptionPane.INFORMATION_MESSAGE);
                            } else {
                                javax.swing.JOptionPane.showMessageDialog(null, 
                                        "Erreur lors de l'ajout du fournisseur", 
                                        "Erreur", javax.swing.JOptionPane.ERROR_MESSAGE);
                            }
                        } catch (Exception e) {
                            e.printStackTrace();
                            javax.swing.JOptionPane.showMessageDialog(null, 
                                    "Erreur lors de l'ajout: " + e.getMessage(), 
                                    "Erreur", javax.swing.JOptionPane.ERROR_MESSAGE);
                        }
                    }
                }
            });
            
        } catch (Exception e) {
            e.printStackTrace();
            AlertUtils.showErrorAlert("Erreur", "Erreur lors de l'ajout de fournisseur", 
                    "Impossible d'ouvrir le dialogue d'ajout: " + e.getMessage());
        }
    }
    
    /**
     * Gère la modification d'un fournisseur
     */
    @FXML
    protected void handleEditSupplier() {
        try {
            // Vérifier qu'un fournisseur est sélectionné
            Fournisseur selectedSupplier = suppliersTable.getSelectionModel().getSelectedItem();
            if (selectedSupplier == null) {
                AlertUtils.showWarningAlert("Attention", "Aucun fournisseur sélectionné", 
                        "Veuillez sélectionner un fournisseur à modifier.");
                return;
            }
            
            // Créer un dialogue pour modifier les informations du fournisseur
            Dialog<Fournisseur> dialog = new Dialog<>();
            dialog.setTitle("Modifier un fournisseur");
            dialog.setHeaderText("Modifiez les informations du fournisseur");
            
            // Boutons
            ButtonType saveButtonType = new ButtonType("Enregistrer", ButtonBar.ButtonData.OK_DONE);
            dialog.getDialogPane().getButtonTypes().addAll(saveButtonType, ButtonType.CANCEL);
            
            // Grille pour les champs
            GridPane grid = new GridPane();
            grid.setHgap(10);
            grid.setVgap(10);
            grid.setPadding(new Insets(20, 150, 10, 10));
            
            // Champs de saisie
            TextField nomField = new TextField(selectedSupplier.getNom());
            TextField adresseField = new TextField(selectedSupplier.getAdresse());
            TextField telephoneField = new TextField(selectedSupplier.getTelephone());
            TextField emailField = new TextField(selectedSupplier.getEmail());
            TextField siretField = new TextField(selectedSupplier.getSiret());
            
            // Ajouter les champs à la grille
            grid.add(new Label("Nom:"), 0, 0);
            grid.add(nomField, 1, 0);
            grid.add(new Label("Adresse:"), 0, 1);
            grid.add(adresseField, 1, 1);
            grid.add(new Label("Téléphone:"), 0, 2);
            grid.add(telephoneField, 1, 2);
            grid.add(new Label("Email:"), 0, 3);
            grid.add(emailField, 1, 3);
            grid.add(new Label("SIRET:"), 0, 4);
            grid.add(siretField, 1, 4);
            
            dialog.getDialogPane().setContent(grid);
            
            // Focus sur le premier champ
            Platform.runLater(() -> nomField.requestFocus());
            
            // Convertir le résultat
            dialog.setResultConverter(dialogButton -> {
                if (dialogButton == saveButtonType) {
                    try {
                        // Valider les champs obligatoires
                        if (nomField.getText().isEmpty()) {
                            throw new IllegalArgumentException("Le nom du fournisseur est obligatoire");
                        }
                        
                        if (telephoneField.getText().isEmpty()) {
                            throw new IllegalArgumentException("Le téléphone est obligatoire");
                        }
                        
                        if (emailField.getText().isEmpty()) {
                            throw new IllegalArgumentException("L'email est obligatoire");
                        }
                        
                        // Mettre à jour le fournisseur
                        selectedSupplier.setNom(nomField.getText());
                        selectedSupplier.setAdresse(adresseField.getText());
                        selectedSupplier.setTelephone(telephoneField.getText());
                        selectedSupplier.setEmail(emailField.getText());
                        selectedSupplier.setSiret(siretField.getText());
                        
                        return selectedSupplier;
                    } catch (IllegalArgumentException e) {
                        AlertUtils.showErrorAlert("Erreur", "Champs obligatoires", e.getMessage());
                        return null;
                    }
                }
                return null;
            });
            
            // Afficher le dialogue et traiter le résultat
            Optional<Fournisseur> result = dialog.showAndWait();
            
            result.ifPresent(fournisseur -> {
                try {
                    // Mettre à jour le fournisseur dans la base de données
                    boolean success = fournisseurService.updateFournisseur(fournisseur);
                    
                    if (success) {
                        // Enregistrer l'activité
                        activiteService.ajouterActivite("Fournisseur", "Modification du fournisseur: " + fournisseur.getNom(), 
                                currentAdmin.getPrenom() + " " + currentAdmin.getNom(), pharmacieId);
                        
                        // Recharger les données
                        loadSuppliersData();
                        
                        AlertUtils.showInfoAlert("Succès", "Fournisseur modifié", 
                                "Le fournisseur a été modifié avec succès.");
                    } else {
                        AlertUtils.showErrorAlert("Erreur", "Erreur lors de la modification du fournisseur", 
                                "Impossible de modifier le fournisseur. Veuillez réessayer.");
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                    AlertUtils.showErrorAlert("Erreur", "Erreur lors de la modification du fournisseur", 
                            "Impossible de modifier le fournisseur: " + e.getMessage());
                }
            });
            
        } catch (Exception e) {
            e.printStackTrace();
            AlertUtils.showErrorAlert("Erreur", "Erreur lors de la modification du fournisseur", 
                    "Impossible de modifier le fournisseur: " + e.getMessage());
        }
    }
    
    /**
     * Gère la création d'une nouvelle commande
     */
    @FXML
    protected void handleNewOrder() {
        try {
            // Créer et afficher le dialogue de nouvelle commande
            javax.swing.SwingUtilities.invokeLater(() -> {
                com.gestionpharma.NouvelleCommandeDialog dialog = new com.gestionpharma.NouvelleCommandeDialog(null);
                dialog.setVisible(true);
                
                // Rafraîchir la liste des commandes si une commande a été créée
                if (dialog.estConfirme()) {
                    loadOrdersData();
                }
            });
            
            // Enregistrer l'activité
            activiteService.ajouterActivite("Commande", "Création d'une nouvelle commande", 
                    currentAdmin.getPrenom() + " " + currentAdmin.getNom(), pharmacieId);
        } catch (Exception e) {
            e.printStackTrace();
            AlertUtils.showErrorAlert("Erreur", "Erreur lors de la création de commande", 
                    "Impossible de créer une nouvelle commande: " + e.getMessage());
        }
    }
    
    /**
     * Gère la modification d'une commande existante
     */
    @FXML
    protected void handleEditOrder() {
        try {
            // TODO: Implémenter la modification de commande
            AlertUtils.showInfoAlert("Information", "Fonctionnalité en développement", 
                    "La modification de commandes sera disponible dans une future mise à jour.");
            
            // Enregistrer l'activité
            activiteService.ajouterActivite("Commande", "Tentative de modification d'une commande", 
                    currentAdmin.getPrenom() + " " + currentAdmin.getNom(), pharmacieId);
        } catch (Exception e) {
            e.printStackTrace();
            AlertUtils.showErrorAlert("Erreur", "Erreur lors de la modification de commande", 
                    "Impossible de modifier la commande: " + e.getMessage());
        }
    }
    
    /**
     * Gère l'annulation d'une commande
     */
    @FXML
    protected void handleCancelOrder() {
        try {
            // TODO: Implémenter l'annulation de commande
            AlertUtils.showInfoAlert("Information", "Fonctionnalité en développement", 
                    "L'annulation de commandes sera disponible dans une future mise à jour.");
            
            // Enregistrer l'activité
            activiteService.ajouterActivite("Commande", "Tentative d'annulation d'une commande", 
                    currentAdmin.getPrenom() + " " + currentAdmin.getNom(), pharmacieId);
        } catch (Exception e) {
            e.printStackTrace();
            AlertUtils.showErrorAlert("Erreur", "Erreur lors de l'annulation de commande", 
                    "Impossible d'annuler la commande: " + e.getMessage());
        }
    }
    
    /**
 * Gère l'entrée de stock pour un produit
 */
@FXML
protected void handleStockEntry() {
    try {
        // Vérifier qu'un produit est sélectionné
        Produit selectedProduct = productsTable.getSelectionModel().getSelectedItem();
        if (selectedProduct == null) {
            AlertUtils.showWarningAlert("Attention", "Aucun produit sélectionné", 
                    "Veuillez sélectionner un produit pour ajouter du stock.");
            return;
        }
        
        // Afficher une boîte de dialogue pour entrer la quantité
        TextInputDialog dialog = new TextInputDialog("1");
        dialog.setTitle("Entrée de stock");
        dialog.setHeaderText("Ajout au stock pour: " + selectedProduct.getNom());
        dialog.setContentText("Quantité à ajouter:");
        
        Optional<String> result = dialog.showAndWait();
        
        if (result.isPresent()) {
            try {
                int quantite = Integer.parseInt(result.get());
                
                if (quantite <= 0) {
                    AlertUtils.showWarningAlert("Attention", "Quantité invalide", 
                            "Veuillez entrer une quantité positive.");
                    return;
                }
                
                // Ajouter la quantité au stock
                boolean success = stockService.ajouterStock(selectedProduct.getId(), quantite, pharmacieId);
                
                if (success) {
                    // Enregistrer l'activité
                    activiteService.ajouterActivite("Stock", "Entrée de stock: " + quantite + " unités de " + selectedProduct.getNom(), 
                            currentAdmin.getPrenom() + " " + currentAdmin.getNom(), pharmacieId);
                    
                    // Recharger les données
                    loadProductsData();
                    loadStockData();
                    loadDashboardData();
                    
                    AlertUtils.showInfoAlert("Succès", "Stock mis à jour", 
                            "Le stock a été mis à jour avec succès.");
                }
                
            } catch (NumberFormatException e) {
                AlertUtils.showWarningAlert("Attention", "Quantité invalide", 
                        "Veuillez entrer un nombre entier valide.");
            }
        }
        
    } catch (Exception e) {
        e.printStackTrace();
        AlertUtils.showErrorAlert("Erreur", "Erreur lors de l'entrée de stock", 
                "Impossible d'effectuer l'entrée de stock: " + e.getMessage());
    }
}

/**
 * Gère la sortie de stock pour un produit
 */
@FXML
protected void handleStockExit() {
    try {
        // Vérifier qu'un produit est sélectionné
        Produit selectedProduct = productsTable.getSelectionModel().getSelectedItem();
        if (selectedProduct == null) {
            AlertUtils.showWarningAlert("Attention", "Aucun produit sélectionné", 
                    "Veuillez sélectionner un produit pour retirer du stock.");
            return;
        }
        
        // Vérifier le stock disponible
        Stock stock = stockService.getStockByProduitAndPharmacie(selectedProduct.getId(), pharmacieId);
        if (stock == null || stock.getQuantite() <= 0) {
            AlertUtils.showWarningAlert("Attention", "Stock insuffisant", 
                    "Ce produit n'a pas de stock disponible.");
            return;
        }
        
        // Afficher une boîte de dialogue pour entrer la quantité à retirer
        TextInputDialog dialog = new TextInputDialog("1");
        dialog.setTitle("Sortie de stock");
        dialog.setHeaderText("Retrait du stock pour: " + selectedProduct.getNom() + "\nStock actuel: " + stock.getQuantite());
        dialog.setContentText("Quantité à retirer:");
        
        Optional<String> result = dialog.showAndWait();
        
        if (result.isPresent()) {
            try {
                int quantite = Integer.parseInt(result.get());
                
                if (quantite <= 0) {
                    AlertUtils.showWarningAlert("Attention", "Quantité invalide", 
                            "Veuillez entrer une quantité positive.");
                    return;
                }
                
                if (quantite > stock.getQuantite()) {
                    AlertUtils.showWarningAlert("Attention", "Quantité excessive", 
                            "La quantité à retirer ne peut pas être supérieure au stock disponible (" + stock.getQuantite() + ").");
                    return;
                }
                
                // Afficher une boîte de dialogue pour entrer la raison
                TextInputDialog reasonDialog = new TextInputDialog("Vente");
                reasonDialog.setTitle("Raison du retrait");
                reasonDialog.setHeaderText("Veuillez indiquer la raison de ce retrait");
                reasonDialog.setContentText("Raison:");
                
                Optional<String> reasonResult = reasonDialog.showAndWait();
                String raison = reasonResult.orElse("Non spécifiée");
                
                // Retirer la quantité du stock
                boolean success = stockService.retirerStock(selectedProduct.getId(), quantite, raison, pharmacieId);
                
                if (success) {
                    // Enregistrer l'activité
                    activiteService.ajouterActivite("Stock", "Sortie de stock: " + quantite + " unités de " + selectedProduct.getNom() + " (Raison: " + raison + ")", 
                            currentAdmin.getPrenom() + " " + currentAdmin.getNom(), pharmacieId);
                    
                    // Recharger les données
                    loadProductsData();
                    loadStockData();
                    loadDashboardData();
                    
                    AlertUtils.showInfoAlert("Succès", "Stock mis à jour", 
                            "Le stock a été mis à jour avec succès.");
                }
                
            } catch (NumberFormatException e) {
                AlertUtils.showWarningAlert("Attention", "Quantité invalide", 
                        "Veuillez entrer un nombre entier valide.");
            }
        }
        
    } catch (Exception e) {
        e.printStackTrace();
        AlertUtils.showErrorAlert("Erreur", "Erreur lors de la sortie de stock", 
                "Impossible d'effectuer la sortie de stock: " + e.getMessage());
    }
}

/**
 * Gère l'ajustement de stock pour un produit
 */
@FXML
protected void handleStockAdjustment() {
    try {
        // Vérifier qu'un produit est sélectionné
        Produit selectedProduct = productsTable.getSelectionModel().getSelectedItem();
        if (selectedProduct == null) {
            AlertUtils.showWarningAlert("Attention", "Aucun produit sélectionné", 
                    "Veuillez sélectionner un produit pour ajuster le stock.");
            return;
        }
        
        // Récupérer le stock actuel
        Stock stock = stockService.getStockByProduitAndPharmacie(selectedProduct.getId(), pharmacieId);
        int stockActuel = (stock != null) ? stock.getQuantite() : 0;
        
        // Afficher une boîte de dialogue pour entrer la nouvelle quantité
        TextInputDialog dialog = new TextInputDialog(String.valueOf(stockActuel));
        dialog.setTitle("Ajustement de stock");
        dialog.setHeaderText("Ajustement du stock pour: " + selectedProduct.getNom() + "\nStock actuel: " + stockActuel);
        dialog.setContentText("Nouvelle quantité totale:");
        
        Optional<String> result = dialog.showAndWait();
        
        if (result.isPresent()) {
            try {
                int nouvelleQuantite = Integer.parseInt(result.get());
                
                if (nouvelleQuantite < 0) {
                    AlertUtils.showWarningAlert("Attention", "Quantité invalide", 
                            "La quantité ne peut pas être négative.");
                    return;
                }
                
                // Afficher une boîte de dialogue pour entrer la raison
                TextInputDialog reasonDialog = new TextInputDialog("Inventaire");
                reasonDialog.setTitle("Raison de l'ajustement");
                reasonDialog.setHeaderText("Veuillez indiquer la raison de cet ajustement");
                reasonDialog.setContentText("Raison:");
                
                Optional<String> reasonResult = reasonDialog.showAndWait();
                String raison = reasonResult.orElse("Inventaire");
                
                // Ajuster le stock
                boolean success = stockService.ajusterStock(selectedProduct.getId(), nouvelleQuantite, raison, pharmacieId);
                
                if (success) {
                    // Enregistrer l'activité
                    String message = "Ajustement de stock: de " + stockActuel + " à " + nouvelleQuantite + 
                            " unités de " + selectedProduct.getNom() + " (Raison: " + raison + ")"; 
                    activiteService.ajouterActivite("Stock", message, 
                            currentAdmin.getPrenom() + " " + currentAdmin.getNom(), pharmacieId);
                    
                    // Recharger les données
                    loadProductsData();
                    loadStockData();
                    loadDashboardData();
                    
                    AlertUtils.showInfoAlert("Succès", "Stock mis à jour", 
                            "Le stock a été ajusté avec succès.");
                }
                
            } catch (NumberFormatException e) {
                AlertUtils.showWarningAlert("Attention", "Quantité invalide", 
                        "Veuillez entrer un nombre entier valide.");
            }
        }
        
    } catch (Exception e) {
        e.printStackTrace();
        AlertUtils.showErrorAlert("Erreur", "Erreur lors de l'ajustement de stock", 
                "Impossible d'effectuer l'ajustement de stock: " + e.getMessage());
    }
}
}
