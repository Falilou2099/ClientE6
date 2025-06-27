package com.bigpharma.admin.views.products;

import com.bigpharma.admin.dao.ProductDAO;
import com.bigpharma.admin.models.Product;
import com.bigpharma.admin.utils.AlertUtils;
import javafx.geometry.Insets;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.stage.FileChooser;
import javafx.stage.Stage;

import java.io.File;
import java.math.BigDecimal;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.Date;
import java.util.List;

/**
 * Boîte de dialogue pour ajouter ou modifier un produit
 */
public class ProductDialog extends Dialog<Product> {
    
    private final Product product;
    private final boolean isNewProduct;
    private final ProductDAO productDAO;
    
    // Champs du formulaire
    private TextField nameField;
    private TextArea descriptionArea;
    private TextField priceField;
    private TextField stockField;
    private ComboBox<String> categoryCombo;
    private CheckBox prescriptionCheck;
    private TextField imageUrlField;
    private ImageView imagePreview;
    private Button browseButton;
    private File selectedImageFile;
    
    /**
     * Constructeur
     * @param product Le produit à éditer ou un nouveau produit
     * @param isNewProduct true si c'est un nouveau produit, false sinon
     */
    public ProductDialog(Product product, boolean isNewProduct) {
        this.product = product;
        this.isNewProduct = isNewProduct;
        this.productDAO = new ProductDAO();
        
        // Configurer la boîte de dialogue
        setTitle(isNewProduct ? "Ajouter un produit" : "Modifier un produit");
        setHeaderText(isNewProduct ? "Créer un nouveau produit" : "Modifier le produit: " + product.getNom());
        
        // Créer le contenu
        createContent();
        
        // Configurer les boutons
        ButtonType saveButtonType = new ButtonType("Enregistrer", ButtonBar.ButtonData.OK_DONE);
        ButtonType cancelButtonType = new ButtonType("Annuler", ButtonBar.ButtonData.CANCEL_CLOSE);
        getDialogPane().getButtonTypes().addAll(saveButtonType, cancelButtonType);
        
        // Définir le convertisseur de résultat
        setResultConverter(buttonType -> {
            if (buttonType == saveButtonType) {
                return saveProduct();
            }
            return null;
        });
    }
    
    /**
     * Crée le contenu de la boîte de dialogue
     */
    private void createContent() {
        // Créer la grille de formulaire
        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(10);
        grid.setPadding(new Insets(20, 150, 10, 10));
        
        // Nom du produit
        grid.add(new Label("Nom:"), 0, 0);
        nameField = new TextField();
        nameField.setPromptText("Nom du produit");
        nameField.setText(product.getNom() != null ? product.getNom() : "");
        grid.add(nameField, 1, 0);
        
        // Description
        grid.add(new Label("Description:"), 0, 1);
        descriptionArea = new TextArea();
        descriptionArea.setPromptText("Description du produit");
        descriptionArea.setText(product.getDescription() != null ? product.getDescription() : "");
        descriptionArea.setPrefRowCount(3);
        grid.add(descriptionArea, 1, 1);
        
        // Prix
        grid.add(new Label("Prix (€):"), 0, 2);
        priceField = new TextField();
        priceField.setPromptText("Prix du produit");
        priceField.setText(product.getPrix() != null ? product.getPrix().toString() : "");
        grid.add(priceField, 1, 2);
        
        // Stock
        grid.add(new Label("Stock:"), 0, 3);
        stockField = new TextField();
        stockField.setPromptText("Quantité en stock");
        stockField.setText(product.getQuantiteStock() != null ? product.getQuantiteStock().toString() : "0");
        grid.add(stockField, 1, 3);
        
        // Catégorie
        grid.add(new Label("Catégorie:"), 0, 4);
        categoryCombo = new ComboBox<>();
        categoryCombo.setEditable(true);
        categoryCombo.setPromptText("Sélectionner ou saisir une catégorie");
        
        // Charger les catégories existantes
        List<String> categories = productDAO.getAllCategories(null);
        categoryCombo.getItems().addAll(categories);
        
        // Sélectionner la catégorie du produit si elle existe
        if (product.getCategorie() != null) {
            categoryCombo.setValue(product.getCategorie());
        }
        
        grid.add(categoryCombo, 1, 4);
        
        // Ordonnance requise
        grid.add(new Label("Ordonnance requise:"), 0, 5);
        prescriptionCheck = new CheckBox();
        prescriptionCheck.setSelected(product.getEstOrdonnance() != null ? product.getEstOrdonnance() : false);
        grid.add(prescriptionCheck, 1, 5);
        
        // Image URL
        grid.add(new Label("URL de l'image:"), 0, 6);
        
        HBox imageUrlBox = new HBox(10);
        imageUrlField = new TextField();
        imageUrlField.setPromptText("URL de l'image ou chemin local");
        imageUrlField.setText(product.getImage() != null ? product.getImage() : "");
        HBox.setHgrow(imageUrlField, Priority.ALWAYS);
        
        browseButton = new Button("Parcourir...");
        browseButton.setOnAction(e -> browseForImage());
        
        imageUrlBox.getChildren().addAll(imageUrlField, browseButton);
        grid.add(imageUrlBox, 1, 6);
        
        // Aperçu de l'image
        grid.add(new Label("Aperçu:"), 0, 7);
        imagePreview = new ImageView();
        imagePreview.setFitHeight(150);
        imagePreview.setFitWidth(150);
        imagePreview.setPreserveRatio(true);
        
        // Charger l'image si elle existe
        updateImagePreview();
        
        // Ajouter un écouteur pour mettre à jour l'aperçu quand l'URL change
        imageUrlField.textProperty().addListener((obs, oldVal, newVal) -> updateImagePreview());
        
        grid.add(imagePreview, 1, 7);
        
        // Ajouter la grille au panneau de la boîte de dialogue
        getDialogPane().setContent(grid);
    }
    
    /**
     * Ouvre un sélecteur de fichier pour choisir une image
     */
    private void browseForImage() {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Sélectionner une image");
        fileChooser.getExtensionFilters().addAll(
                new FileChooser.ExtensionFilter("Images", "*.png", "*.jpg", "*.jpeg", "*.gif")
        );
        
        // Afficher le sélecteur de fichier
        File file = fileChooser.showOpenDialog(getDialogPane().getScene().getWindow());
        if (file != null) {
            selectedImageFile = file;
            imageUrlField.setText(file.getAbsolutePath());
            updateImagePreview();
        }
    }
    
    /**
     * Met à jour l'aperçu de l'image
     */
    private void updateImagePreview() {
        String imageUrl = imageUrlField.getText();
        if (imageUrl != null && !imageUrl.isEmpty()) {
            try {
                // Vérifier si c'est une URL ou un chemin local
                if (imageUrl.startsWith("http://") || imageUrl.startsWith("https://")) {
                    // URL distante
                    imagePreview.setImage(new Image(imageUrl, true));
                } else if (selectedImageFile != null && selectedImageFile.exists()) {
                    // Fichier local sélectionné
                    imagePreview.setImage(new Image(selectedImageFile.toURI().toString()));
                } else {
                    // Chemin local existant
                    File file = new File(imageUrl);
                    if (file.exists()) {
                        imagePreview.setImage(new Image(file.toURI().toString()));
                    } else {
                        // Image par défaut
                        imagePreview.setImage(new Image(getClass().getResourceAsStream("/resources/images/product_default.png")));
                    }
                }
            } catch (Exception e) {
                // En cas d'erreur, utiliser l'image par défaut
                try {
                    imagePreview.setImage(new Image(getClass().getResourceAsStream("/resources/images/product_default.png")));
                } catch (Exception ex) {
                    // Ignorer
                }
            }
        } else {
            // Aucune image, utiliser l'image par défaut
            try {
                imagePreview.setImage(new Image(getClass().getResourceAsStream("/resources/images/product_default.png")));
            } catch (Exception e) {
                // Ignorer
            }
        }
    }
    
    /**
     * Enregistre le produit avec les valeurs du formulaire
     * @return Le produit enregistré
     */
    private Product saveProduct() {
        // Valider les champs obligatoires
        if (nameField.getText().isEmpty()) {
            AlertUtils.showWarning("Champ obligatoire", "Le nom du produit est obligatoire.");
            return null;
        }
        
        if (categoryCombo.getValue() == null || categoryCombo.getValue().isEmpty()) {
            AlertUtils.showWarning("Champ obligatoire", "La catégorie est obligatoire.");
            return null;
        }
        
        try {
            // Valider le prix
            BigDecimal price = new BigDecimal(priceField.getText().replace(",", "."));
            if (price.compareTo(BigDecimal.ZERO) < 0) {
                AlertUtils.showWarning("Valeur invalide", "Le prix ne peut pas être négatif.");
                return null;
            }
            
            // Valider le stock
            int stock = Integer.parseInt(stockField.getText());
            if (stock < 0) {
                AlertUtils.showWarning("Valeur invalide", "Le stock ne peut pas être négatif.");
                return null;
            }
            
            // Mettre à jour le produit avec les valeurs du formulaire
            product.setNom(nameField.getText());
            product.setDescription(descriptionArea.getText());
            product.setPrix(price);
            product.setQuantiteStock(stock);
            product.setCategorie(categoryCombo.getValue());
            product.setEstOrdonnance(prescriptionCheck.isSelected());
            
            // Gérer l'image
            if (selectedImageFile != null) {
                // Copier l'image vers le dossier des images
                String imageName = saveImageFile(selectedImageFile);
                product.setImage(imageName);
            } else if (!imageUrlField.getText().isEmpty()) {
                product.setImage(imageUrlField.getText());
            }
            
            // Mettre à jour les dates
            if (isNewProduct) {
                product.setDateAjout(new Date());
            }
            product.setDateModification(new Date());
            
            return product;
            
        } catch (NumberFormatException e) {
            AlertUtils.showWarning("Valeur invalide", 
                    "Le prix et le stock doivent être des nombres valides.");
            return null;
        } catch (Exception e) {
            AlertUtils.showError("Erreur", 
                    "Une erreur est survenue lors de l'enregistrement du produit: " + e.getMessage());
            return null;
        }
    }
    
    /**
     * Enregistre un fichier image dans le dossier des images
     * @param file Le fichier image à enregistrer
     * @return Le nom du fichier enregistré
     */
    private String saveImageFile(File file) {
        try {
            // Créer le dossier des images s'il n'existe pas
            String imagesDir = "C:/xampp/htdocs/bigpharma/public/images/products";
            Path imagesDirPath = Paths.get(imagesDir);
            if (!Files.exists(imagesDirPath)) {
                Files.createDirectories(imagesDirPath);
            }
            
            // Générer un nom unique pour l'image
            String fileName = System.currentTimeMillis() + "_" + file.getName();
            Path targetPath = imagesDirPath.resolve(fileName);
            
            // Copier le fichier
            Files.copy(file.toPath(), targetPath, StandardCopyOption.REPLACE_EXISTING);
            
            return fileName;
            
        } catch (Exception e) {
            AlertUtils.showWarning("Erreur d'image", 
                    "Impossible d'enregistrer l'image. L'URL de l'image sera utilisée à la place.");
            return file.getAbsolutePath();
        }
    }
}
