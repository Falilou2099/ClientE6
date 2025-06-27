package com.bigpharma.admin.views.products;

import com.bigpharma.admin.dao.ProductDAO;
import com.bigpharma.admin.models.Pharmacy;
import com.bigpharma.admin.models.Product;
import com.bigpharma.admin.models.User;
import com.bigpharma.admin.utils.AlertUtils;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.scene.text.Text;
import javafx.stage.Modality;
import javafx.stage.Stage;

import java.math.BigDecimal;
import java.util.List;

/**
 * Vue de liste des produits
 */
public class ProductListView extends BorderPane {
    
    private final User currentUser;
    private final Pharmacy currentPharmacy;
    private final ProductDAO productDAO;
    private TableView<Product> productTable;
    private ObservableList<Product> productData;
    private TextField searchField;
    private ComboBox<String> categoryFilter;
    
    // Couleurs de l'application
    private final String primaryColor = "#4e73df";
    private final String secondaryColor = "#1cc88a";
    private final String backgroundColor = "#f8f9fc";
    private final String textColor = "#5a5c69";
    
    /**
     * Constructeur
     * @param user L'utilisateur connecté
     * @param pharmacy La pharmacie sélectionnée
     */
    public ProductListView(User user, Pharmacy pharmacy) {
        this.currentUser = user;
        this.currentPharmacy = pharmacy;
        this.productDAO = new ProductDAO();
        
        initializeUI();
        loadProducts();
    }
    
    /**
     * Initialise l'interface utilisateur
     */
    private void initializeUI() {
        setPadding(new Insets(0));
        
        // En-tête
        VBox header = createHeader();
        setTop(header);
        
        // Tableau des produits
        productTable = createProductTable();
        setCenter(productTable);
        
        // Barre d'outils
        HBox toolbar = createToolbar();
        setBottom(toolbar);
    }
    
    /**
     * Crée l'en-tête de la vue
     * @return L'en-tête
     */
    private VBox createHeader() {
        VBox header = new VBox();
        header.setSpacing(10);
        header.setPadding(new Insets(0, 0, 10, 0));
        
        // Titre
        Text title = new Text("Gestion des Produits");
        title.setFont(Font.font("Arial", FontWeight.BOLD, 24));
        title.setFill(Color.web(textColor));
        
        // Sous-titre avec la pharmacie sélectionnée
        Text subtitle = new Text(currentPharmacy != null 
                ? "Pharmacie: " + currentPharmacy.getNom() 
                : "Toutes les pharmacies");
        subtitle.setFont(Font.font("Arial", 14));
        subtitle.setFill(Color.web(textColor));
        
        // Barre de recherche et filtres
        HBox searchBar = new HBox();
        searchBar.setSpacing(10);
        searchBar.setAlignment(Pos.CENTER_LEFT);
        
        searchField = new TextField();
        searchField.setPromptText("Rechercher un produit...");
        searchField.setPrefWidth(300);
        searchField.textProperty().addListener((obs, oldVal, newVal) -> {
            filterProducts();
        });
        
        Label categoryLabel = new Label("Catégorie:");
        categoryFilter = new ComboBox<>();
        categoryFilter.setPrefWidth(200);
        categoryFilter.getItems().add("Toutes les catégories");
        categoryFilter.getSelectionModel().selectFirst();
        
        // Charger les catégories
        List<String> categories = productDAO.getAllCategories(
                currentPharmacy != null ? currentPharmacy.getId() : null);
        categoryFilter.getItems().addAll(categories);
        
        // Ajouter un écouteur pour filtrer les produits
        categoryFilter.setOnAction(e -> filterProducts());
        
        // Bouton d'ajout de produit
        Button addButton = new Button("Ajouter un produit");
        addButton.setStyle("-fx-background-color: " + secondaryColor + "; -fx-text-fill: white;");
        addButton.setOnAction(e -> showAddProductDialog());
        
        searchBar.getChildren().addAll(searchField, categoryLabel, categoryFilter, addButton);
        
        // Ajouter les éléments à l'en-tête
        header.getChildren().addAll(title, subtitle, searchBar);
        
        return header;
    }
    
    /**
     * Crée le tableau des produits
     * @return Le tableau des produits
     */
    private TableView<Product> createProductTable() {
        TableView<Product> table = new TableView<>();
        table.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
        
        // Colonnes du tableau
        TableColumn<Product, Integer> idCol = new TableColumn<>("ID");
        idCol.setCellValueFactory(new PropertyValueFactory<>("id"));
        idCol.setPrefWidth(50);
        
        TableColumn<Product, String> imageCol = new TableColumn<>("Image");
        imageCol.setCellValueFactory(new PropertyValueFactory<>("image"));
        imageCol.setPrefWidth(80);
        imageCol.setCellFactory(col -> new TableCell<Product, String>() {
            private final ImageView imageView = new ImageView();
            
            @Override
            protected void updateItem(String imagePath, boolean empty) {
                super.updateItem(imagePath, empty);
                
                if (empty || imagePath == null) {
                    setGraphic(null);
                } else {
                    Product product = getTableView().getItems().get(getIndex());
                    try {
                        // Utiliser l'URL de l'image ou l'image par défaut
                        String imageUrl = product.getImageUrl();
                        if (imageUrl.startsWith("http")) {
                            imageView.setImage(new Image(imageUrl, true));
                        } else {
                            // Pour les images locales, utiliser une image par défaut
                            imageView.setImage(new Image(getClass().getResourceAsStream("/resources/images/product_default.png")));
                        }
                        imageView.setFitHeight(50);
                        imageView.setFitWidth(50);
                        imageView.setPreserveRatio(true);
                        setGraphic(imageView);
                    } catch (Exception e) {
                        // En cas d'erreur, utiliser une image par défaut
                        try {
                            imageView.setImage(new Image(getClass().getResourceAsStream("/resources/images/product_default.png")));
                            imageView.setFitHeight(50);
                            imageView.setFitWidth(50);
                            setGraphic(imageView);
                        } catch (Exception ex) {
                            setGraphic(null);
                        }
                    }
                }
            }
        });
        
        TableColumn<Product, String> nameCol = new TableColumn<>("Nom");
        nameCol.setCellValueFactory(new PropertyValueFactory<>("nom"));
        nameCol.setPrefWidth(200);
        
        TableColumn<Product, String> categoryCol = new TableColumn<>("Catégorie");
        categoryCol.setCellValueFactory(new PropertyValueFactory<>("categorie"));
        categoryCol.setPrefWidth(150);
        
        TableColumn<Product, BigDecimal> priceCol = new TableColumn<>("Prix");
        priceCol.setCellValueFactory(new PropertyValueFactory<>("prix"));
        priceCol.setPrefWidth(100);
        priceCol.setCellFactory(col -> new TableCell<Product, BigDecimal>() {
            @Override
            protected void updateItem(BigDecimal price, boolean empty) {
                super.updateItem(price, empty);
                if (empty || price == null) {
                    setText(null);
                } else {
                    setText(String.format("%.2f €", price));
                }
            }
        });
        
        TableColumn<Product, Integer> stockCol = new TableColumn<>("Stock");
        stockCol.setCellValueFactory(new PropertyValueFactory<>("quantiteStock"));
        stockCol.setPrefWidth(80);
        
        TableColumn<Product, Boolean> prescriptionCol = new TableColumn<>("Ordonnance");
        prescriptionCol.setCellValueFactory(new PropertyValueFactory<>("estOrdonnance"));
        prescriptionCol.setPrefWidth(100);
        prescriptionCol.setCellFactory(col -> new TableCell<Product, Boolean>() {
            @Override
            protected void updateItem(Boolean prescription, boolean empty) {
                super.updateItem(prescription, empty);
                if (empty || prescription == null) {
                    setText(null);
                } else {
                    setText(prescription ? "Oui" : "Non");
                }
            }
        });
        
        TableColumn<Product, Void> actionsCol = new TableColumn<>("Actions");
        actionsCol.setPrefWidth(150);
        actionsCol.setCellFactory(col -> new TableCell<Product, Void>() {
            private final Button editButton = new Button("Modifier");
            private final Button deleteButton = new Button("Supprimer");
            private final HBox buttonsBox = new HBox(5, editButton, deleteButton);
            
            {
                editButton.setStyle("-fx-background-color: " + primaryColor + "; -fx-text-fill: white;");
                deleteButton.setStyle("-fx-background-color: #e74a3b; -fx-text-fill: white;");
                
                editButton.setOnAction(e -> {
                    Product product = getTableView().getItems().get(getIndex());
                    showEditProductDialog(product);
                });
                
                deleteButton.setOnAction(e -> {
                    Product product = getTableView().getItems().get(getIndex());
                    showDeleteProductDialog(product);
                });
                
                buttonsBox.setAlignment(Pos.CENTER);
            }
            
            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                setGraphic(empty ? null : buttonsBox);
            }
        });
        
        // Ajouter les colonnes au tableau
        table.getColumns().addAll(
                idCol, imageCol, nameCol, categoryCol, priceCol, stockCol, prescriptionCol, actionsCol);
        
        return table;
    }
    
    /**
     * Crée la barre d'outils
     * @return La barre d'outils
     */
    private HBox createToolbar() {
        HBox toolbar = new HBox();
        toolbar.setAlignment(Pos.CENTER_RIGHT);
        toolbar.setPadding(new Insets(10, 0, 0, 0));
        toolbar.setSpacing(10);
        
        // Bouton pour rafraîchir la liste
        Button refreshButton = new Button("Rafraîchir");
        refreshButton.setStyle("-fx-background-color: " + primaryColor + "; -fx-text-fill: white;");
        refreshButton.setOnAction(e -> loadProducts());
        
        // Bouton pour exporter la liste (à implémenter)
        Button exportButton = new Button("Exporter");
        exportButton.setStyle("-fx-background-color: #36b9cc; -fx-text-fill: white;");
        exportButton.setOnAction(e -> {
            AlertUtils.showInfo("Export", "Fonctionnalité d'export en cours de développement.");
        });
        
        toolbar.getChildren().addAll(refreshButton, exportButton);
        
        return toolbar;
    }
    
    /**
     * Charge les produits depuis la base de données
     */
    private void loadProducts() {
        Integer pharmacyId = currentPharmacy != null ? currentPharmacy.getId() : null;
        List<Product> products = productDAO.findAllByPharmacyId(pharmacyId);
        
        productData = FXCollections.observableArrayList(products);
        productTable.setItems(productData);
    }
    
    /**
     * Filtre les produits selon les critères de recherche
     */
    private void filterProducts() {
        String searchText = searchField.getText().toLowerCase();
        String category = categoryFilter.getValue();
        
        if (category == null || category.equals("Toutes les catégories")) {
            category = "";
        }
        
        String finalCategory = category;
        
        // Filtrer les produits
        List<Product> filteredProducts;
        
        if (searchText.isEmpty() && finalCategory.isEmpty()) {
            // Aucun filtre
            filteredProducts = productDAO.findAllByPharmacyId(
                    currentPharmacy != null ? currentPharmacy.getId() : null);
        } else if (!searchText.isEmpty() && finalCategory.isEmpty()) {
            // Filtre par nom
            filteredProducts = productDAO.findByName(searchText, 
                    currentPharmacy != null ? currentPharmacy.getId() : null);
        } else if (searchText.isEmpty() && !finalCategory.isEmpty()) {
            // Filtre par catégorie
            filteredProducts = productDAO.findByCategory(finalCategory, 
                    currentPharmacy != null ? currentPharmacy.getId() : null);
        } else {
            // Filtre par nom et catégorie
            filteredProducts = productDAO.findByName(searchText, 
                    currentPharmacy != null ? currentPharmacy.getId() : null);
            
            // Filtrer davantage par catégorie
            filteredProducts.removeIf(product -> 
                    !product.getCategorie().equals(finalCategory));
        }
        
        productData = FXCollections.observableArrayList(filteredProducts);
        productTable.setItems(productData);
    }
    
    /**
     * Affiche la boîte de dialogue pour ajouter un produit
     */
    private void showAddProductDialog() {
        if (currentPharmacy == null && !"admin".equals(currentUser.getRole())) {
            AlertUtils.showWarning("Attention", 
                    "Vous devez sélectionner une pharmacie pour ajouter un produit.");
            return;
        }
        
        // Créer un nouveau produit
        Product newProduct = new Product();
        
        // Définir la pharmacie si elle est sélectionnée
        if (currentPharmacy != null) {
            newProduct.setPharmacyId(currentPharmacy.getId());
        }
        
        // Afficher la boîte de dialogue d'édition
        ProductDialog dialog = new ProductDialog(newProduct, true);
        dialog.initModality(Modality.APPLICATION_MODAL);
        dialog.showAndWait().ifPresent(product -> {
            // Sauvegarder le produit
            Product savedProduct = productDAO.save(product);
            if (savedProduct != null) {
                AlertUtils.showInfo("Succès", "Le produit a été ajouté avec succès.");
                loadProducts();
            } else {
                AlertUtils.showError("Erreur", "Impossible d'ajouter le produit.");
            }
        });
    }
    
    /**
     * Affiche la boîte de dialogue pour modifier un produit
     * @param product Le produit à modifier
     */
    private void showEditProductDialog(Product product) {
        // Vérifier si l'utilisateur a le droit de modifier ce produit
        if (currentPharmacy == null && product.getPharmacyId() != null && 
                !"admin".equals(currentUser.getRole())) {
            AlertUtils.showWarning("Attention", 
                    "Vous n'avez pas les droits pour modifier ce produit.");
            return;
        }
        
        // Afficher la boîte de dialogue d'édition
        ProductDialog dialog = new ProductDialog(product, false);
        dialog.initModality(Modality.APPLICATION_MODAL);
        dialog.showAndWait().ifPresent(updatedProduct -> {
            // Mettre à jour le produit
            Product savedProduct = productDAO.update(updatedProduct);
            if (savedProduct != null) {
                AlertUtils.showInfo("Succès", "Le produit a été mis à jour avec succès.");
                loadProducts();
            } else {
                AlertUtils.showError("Erreur", "Impossible de mettre à jour le produit.");
            }
        });
    }
    
    /**
     * Affiche la boîte de dialogue pour supprimer un produit
     * @param product Le produit à supprimer
     */
    private void showDeleteProductDialog(Product product) {
        // Vérifier si l'utilisateur a le droit de supprimer ce produit
        if (currentPharmacy == null && product.getPharmacyId() != null && 
                !"admin".equals(currentUser.getRole())) {
            AlertUtils.showWarning("Attention", 
                    "Vous n'avez pas les droits pour supprimer ce produit.");
            return;
        }
        
        // Demander confirmation
        boolean confirm = AlertUtils.showConfirmation(
                "Confirmation de suppression", 
                "Êtes-vous sûr de vouloir supprimer le produit \"" + product.getNom() + "\" ?");
        
        if (confirm) {
            // Supprimer le produit
            boolean deleted = productDAO.delete(product.getId());
            if (deleted) {
                AlertUtils.showInfo("Succès", "Le produit a été supprimé avec succès.");
                loadProducts();
            } else {
                AlertUtils.showError("Erreur", "Impossible de supprimer le produit.");
            }
        }
    }
}
