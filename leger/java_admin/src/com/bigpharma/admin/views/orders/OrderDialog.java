package com.bigpharma.admin.views.orders;

import com.bigpharma.admin.dao.OrderItemDAO;
import com.bigpharma.admin.dao.PharmacyDAO;
import com.bigpharma.admin.dao.ProductDAO;
import com.bigpharma.admin.models.Order;
import com.bigpharma.admin.models.OrderItem;
import com.bigpharma.admin.models.Pharmacy;
import com.bigpharma.admin.models.Product;
import com.bigpharma.admin.utils.AlertUtils;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.scene.text.Text;
import javafx.stage.Stage;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

/**
 * Boîte de dialogue pour ajouter ou modifier une commande
 */
public class OrderDialog extends Dialog<Order> {
    
    private final Order order;
    private final boolean isNewOrder;
    private final PharmacyDAO pharmacyDAO;
    private final ProductDAO productDAO;
    private final OrderItemDAO orderItemDAO;
    
    // Champs du formulaire
    private ComboBox<Pharmacy> pharmacyCombo;
    private TextField clientIdField;
    private TextArea notesArea;
    private ComboBox<String> statusCombo;
    private TableView<OrderItem> itemsTable;
    private ObservableList<OrderItem> itemsData;
    private ComboBox<Product> productCombo;
    private TextField quantityField;
    private Label totalLabel;
    
    /**
     * Constructeur
     * @param order La commande à éditer ou une nouvelle commande
     * @param isNewOrder true si c'est une nouvelle commande, false sinon
     */
    public OrderDialog(Order order, boolean isNewOrder) {
        this.order = order;
        this.isNewOrder = isNewOrder;
        this.pharmacyDAO = new PharmacyDAO();
        this.productDAO = new ProductDAO();
        this.orderItemDAO = new OrderItemDAO();
        
        // Configurer la boîte de dialogue
        setTitle(isNewOrder ? "Ajouter une commande" : "Modifier la commande");
        setHeaderText(isNewOrder ? "Créer une nouvelle commande" : "Modifier la commande #" + order.getReference());
        
        // Créer le contenu
        createContent();
        
        // Configurer les boutons
        ButtonType saveButtonType = new ButtonType("Enregistrer", ButtonBar.ButtonData.OK_DONE);
        ButtonType cancelButtonType = new ButtonType("Annuler", ButtonBar.ButtonData.CANCEL_CLOSE);
        getDialogPane().getButtonTypes().addAll(saveButtonType, cancelButtonType);
        
        // Définir le convertisseur de résultat
        setResultConverter(buttonType -> {
            if (buttonType == saveButtonType) {
                return saveOrder();
            }
            return null;
        });
        
        // Définir la taille de la boîte de dialogue
        getDialogPane().setPrefSize(800, 600);
    }
    
    /**
     * Crée le contenu de la boîte de dialogue
     */
    private void createContent() {
        BorderPane mainPane = new BorderPane();
        mainPane.setPadding(new Insets(20));
        
        // Formulaire d'informations de la commande
        VBox formBox = createFormBox();
        mainPane.setTop(formBox);
        
        // Tableau des produits de la commande
        VBox itemsBox = createItemsBox();
        mainPane.setCenter(itemsBox);
        
        // Pied de page avec le total
        HBox footerBox = createFooterBox();
        mainPane.setBottom(footerBox);
        
        // Charger les produits de la commande si c'est une commande existante
        if (!isNewOrder) {
            loadOrderItems();
        } else {
            itemsData = FXCollections.observableArrayList();
            itemsTable.setItems(itemsData);
        }
        
        // Mettre à jour le total
        updateTotal();
        
        getDialogPane().setContent(mainPane);
    }
    
    /**
     * Crée le formulaire d'informations de la commande
     * @return Le formulaire
     */
    private VBox createFormBox() {
        VBox formBox = new VBox();
        formBox.setSpacing(10);
        formBox.setPadding(new Insets(0, 0, 20, 0));
        
        // Titre
        Text title = new Text("Informations de la commande");
        title.setFont(Font.font("Arial", FontWeight.BOLD, 18));
        title.setFill(Color.web("#4e73df"));
        
        // Grille de formulaire
        GridPane formGrid = new GridPane();
        formGrid.setHgap(20);
        formGrid.setVgap(10);
        formGrid.setPadding(new Insets(10));
        formGrid.setStyle("-fx-background-color: #f8f9fc; -fx-border-color: #e3e6f0; -fx-border-radius: 5;");
        
        // Référence (non modifiable)
        formGrid.add(new Label("Référence:"), 0, 0);
        TextField referenceField = new TextField(order.getReference());
        referenceField.setEditable(false);
        referenceField.setDisable(true);
        formGrid.add(referenceField, 1, 0);
        
        // Pharmacie
        formGrid.add(new Label("Pharmacie:"), 0, 1);
        pharmacyCombo = new ComboBox<>();
        pharmacyCombo.setPromptText("Sélectionner une pharmacie");
        
        // Charger les pharmacies
        List<Pharmacy> pharmacies = pharmacyDAO.findAll();
        pharmacyCombo.setItems(FXCollections.observableArrayList(pharmacies));
        
        // Définir le convertisseur pour afficher le nom de la pharmacie
        pharmacyCombo.setCellFactory(lv -> new ListCell<Pharmacy>() {
            @Override
            protected void updateItem(Pharmacy pharmacy, boolean empty) {
                super.updateItem(pharmacy, empty);
                setText(empty ? "" : pharmacy.getNom());
            }
        });
        
        pharmacyCombo.setButtonCell(new ListCell<Pharmacy>() {
            @Override
            protected void updateItem(Pharmacy pharmacy, boolean empty) {
                super.updateItem(pharmacy, empty);
                setText(empty ? "" : pharmacy.getNom());
            }
        });
        
        // Sélectionner la pharmacie si elle existe
        if (order.getPharmacyId() != null) {
            Pharmacy selectedPharmacy = pharmacies.stream()
                    .filter(p -> p.getId().equals(order.getPharmacyId()))
                    .findFirst()
                    .orElse(null);
            
            if (selectedPharmacy != null) {
                pharmacyCombo.setValue(selectedPharmacy);
            }
        }
        
        formGrid.add(pharmacyCombo, 1, 1);
        
        // Client ID
        formGrid.add(new Label("Client ID:"), 0, 2);
        clientIdField = new TextField();
        clientIdField.setPromptText("ID du client");
        if (order.getClientId() != null) {
            clientIdField.setText(order.getClientId().toString());
        }
        formGrid.add(clientIdField, 1, 2);
        
        // Statut
        formGrid.add(new Label("Statut:"), 2, 0);
        statusCombo = new ComboBox<>();
        statusCombo.getItems().addAll(
                "pending", 
                "processing", 
                "shipped", 
                "delivered", 
                "cancelled");
        
        // Sélectionner le statut actuel
        if (order.getStatut() != null) {
            statusCombo.setValue(order.getStatut());
        } else {
            statusCombo.setValue("pending");
        }
        
        formGrid.add(statusCombo, 3, 0);
        
        // Notes
        formGrid.add(new Label("Notes:"), 2, 1);
        notesArea = new TextArea();
        notesArea.setPromptText("Notes sur la commande");
        notesArea.setPrefRowCount(2);
        if (order.getNotes() != null) {
            notesArea.setText(order.getNotes());
        }
        GridPane.setRowSpan(notesArea, 2);
        formGrid.add(notesArea, 3, 1);
        
        formBox.getChildren().addAll(title, formGrid);
        
        return formBox;
    }
    
    /**
     * Crée la section des produits de la commande
     * @return La section
     */
    private VBox createItemsBox() {
        VBox itemsBox = new VBox();
        itemsBox.setSpacing(10);
        itemsBox.setPadding(new Insets(0, 0, 20, 0));
        
        // Titre
        Text title = new Text("Produits de la commande");
        title.setFont(Font.font("Arial", FontWeight.BOLD, 18));
        title.setFill(Color.web("#4e73df"));
        
        // Tableau des produits
        itemsTable = createItemsTable();
        
        // Formulaire d'ajout de produit
        GridPane addItemGrid = new GridPane();
        addItemGrid.setHgap(10);
        addItemGrid.setVgap(10);
        addItemGrid.setPadding(new Insets(10));
        addItemGrid.setStyle("-fx-background-color: #f8f9fc; -fx-border-color: #e3e6f0; -fx-border-radius: 5;");
        
        // Produit
        addItemGrid.add(new Label("Produit:"), 0, 0);
        productCombo = new ComboBox<>();
        productCombo.setPromptText("Sélectionner un produit");
        
        // Charger les produits
        List<Product> products = productDAO.findAll();
        productCombo.setItems(FXCollections.observableArrayList(products));
        
        // Définir le convertisseur pour afficher le nom du produit
        productCombo.setCellFactory(lv -> new ListCell<Product>() {
            @Override
            protected void updateItem(Product product, boolean empty) {
                super.updateItem(product, empty);
                setText(empty ? "" : product.getNom());
            }
        });
        
        productCombo.setButtonCell(new ListCell<Product>() {
            @Override
            protected void updateItem(Product product, boolean empty) {
                super.updateItem(product, empty);
                setText(empty ? "" : product.getNom());
            }
        });
        
        addItemGrid.add(productCombo, 1, 0);
        
        // Quantité
        addItemGrid.add(new Label("Quantité:"), 2, 0);
        quantityField = new TextField();
        quantityField.setPromptText("Quantité");
        addItemGrid.add(quantityField, 3, 0);
        
        // Bouton d'ajout
        Button addButton = new Button("Ajouter");
        addButton.setStyle("-fx-background-color: #1cc88a; -fx-text-fill: white;");
        addButton.setOnAction(e -> addItemToOrder());
        addItemGrid.add(addButton, 4, 0);
        
        itemsBox.getChildren().addAll(title, itemsTable, addItemGrid);
        
        return itemsBox;
    }
    
    /**
     * Crée le tableau des produits de la commande
     * @return Le tableau
     */
    private TableView<OrderItem> createItemsTable() {
        TableView<OrderItem> table = new TableView<>();
        table.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
        
        // Colonnes du tableau
        TableColumn<OrderItem, Integer> productIdCol = new TableColumn<>("Produit");
        productIdCol.setCellValueFactory(new PropertyValueFactory<>("productId"));
        productIdCol.setPrefWidth(200);
        productIdCol.setCellFactory(col -> new TableCell<OrderItem, Integer>() {
            @Override
            protected void updateItem(Integer productId, boolean empty) {
                super.updateItem(productId, empty);
                if (empty || productId == null) {
                    setText(null);
                } else {
                    Product product = productDAO.findById(productId);
                    setText(product != null ? product.getNom() : "Produit #" + productId);
                }
            }
        });
        
        TableColumn<OrderItem, Integer> quantityCol = new TableColumn<>("Quantité");
        quantityCol.setCellValueFactory(new PropertyValueFactory<>("quantite"));
        quantityCol.setPrefWidth(80);
        
        TableColumn<OrderItem, BigDecimal> priceCol = new TableColumn<>("Prix unitaire");
        priceCol.setCellValueFactory(new PropertyValueFactory<>("prixUnitaire"));
        priceCol.setPrefWidth(100);
        priceCol.setCellFactory(col -> new TableCell<OrderItem, BigDecimal>() {
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
        
        TableColumn<OrderItem, BigDecimal> totalCol = new TableColumn<>("Total");
        totalCol.setCellValueFactory(new PropertyValueFactory<>("prixTotal"));
        totalCol.setPrefWidth(100);
        totalCol.setCellFactory(col -> new TableCell<OrderItem, BigDecimal>() {
            @Override
            protected void updateItem(BigDecimal total, boolean empty) {
                super.updateItem(total, empty);
                if (empty || total == null) {
                    setText(null);
                } else {
                    setText(String.format("%.2f €", total));
                }
            }
        });
        
        TableColumn<OrderItem, Void> actionsCol = new TableColumn<>("Actions");
        actionsCol.setPrefWidth(100);
        actionsCol.setCellFactory(col -> new TableCell<OrderItem, Void>() {
            private final Button deleteButton = new Button("Supprimer");
            
            {
                deleteButton.setStyle("-fx-background-color: #e74a3b; -fx-text-fill: white;");
                deleteButton.setOnAction(e -> {
                    OrderItem item = getTableView().getItems().get(getIndex());
                    removeItemFromOrder(item);
                });
            }
            
            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                setGraphic(empty ? null : deleteButton);
            }
        });
        
        // Ajouter les colonnes au tableau
        table.getColumns().addAll(productIdCol, quantityCol, priceCol, totalCol, actionsCol);
        
        return table;
    }
    
    /**
     * Crée le pied de page avec le total
     * @return Le pied de page
     */
    private HBox createFooterBox() {
        HBox footerBox = new HBox();
        footerBox.setAlignment(Pos.CENTER_RIGHT);
        footerBox.setPadding(new Insets(20, 0, 0, 0));
        footerBox.setSpacing(10);
        
        // Label pour le total
        Label totalTextLabel = new Label("Total de la commande:");
        totalTextLabel.setFont(Font.font("Arial", FontWeight.BOLD, 16));
        
        // Valeur du total
        totalLabel = new Label(String.format("%.2f €", order.getMontantTotal()));
        totalLabel.setFont(Font.font("Arial", FontWeight.BOLD, 16));
        totalLabel.setTextFill(Color.web("#4e73df"));
        
        footerBox.getChildren().addAll(totalTextLabel, totalLabel);
        
        return footerBox;
    }
    
    /**
     * Charge les produits de la commande
     */
    private void loadOrderItems() {
        List<OrderItem> items = orderItemDAO.findByOrderId(order.getId());
        itemsData = FXCollections.observableArrayList(items);
        itemsTable.setItems(itemsData);
    }
    
    /**
     * Ajoute un produit à la commande
     */
    private void addItemToOrder() {
        // Vérifier les champs
        Product selectedProduct = productCombo.getValue();
        if (selectedProduct == null) {
            AlertUtils.showWarning("Champ obligatoire", "Veuillez sélectionner un produit.");
            return;
        }
        
        // Vérifier la quantité
        int quantity;
        try {
            quantity = Integer.parseInt(quantityField.getText());
            if (quantity <= 0) {
                AlertUtils.showWarning("Valeur invalide", "La quantité doit être supérieure à 0.");
                return;
            }
        } catch (NumberFormatException e) {
            AlertUtils.showWarning("Valeur invalide", "La quantité doit être un nombre entier.");
            return;
        }
        
        // Vérifier si le produit est déjà dans la commande
        boolean productExists = false;
        for (OrderItem item : itemsData) {
            if (item.getProductId().equals(selectedProduct.getId())) {
                // Mettre à jour la quantité
                item.setQuantite(item.getQuantite() + quantity);
                item.setPrixTotal(item.getPrixUnitaire().multiply(new BigDecimal(item.getQuantite())));
                productExists = true;
                break;
            }
        }
        
        // Si le produit n'est pas déjà dans la commande, l'ajouter
        if (!productExists) {
            OrderItem newItem = new OrderItem();
            newItem.setOrderId(order.getId());
            newItem.setProductId(selectedProduct.getId());
            newItem.setQuantite(quantity);
            newItem.setPrixUnitaire(selectedProduct.getPrix());
            newItem.setPrixTotal(selectedProduct.getPrix().multiply(new BigDecimal(quantity)));
            
            itemsData.add(newItem);
        }
        
        // Mettre à jour le tableau et le total
        itemsTable.refresh();
        updateTotal();
        
        // Réinitialiser les champs
        productCombo.setValue(null);
        quantityField.clear();
    }
    
    /**
     * Supprime un produit de la commande
     * @param item Le produit à supprimer
     */
    private void removeItemFromOrder(OrderItem item) {
        itemsData.remove(item);
        updateTotal();
    }
    
    /**
     * Met à jour le total de la commande
     */
    private void updateTotal() {
        BigDecimal total = BigDecimal.ZERO;
        
        for (OrderItem item : itemsData) {
            total = total.add(item.getPrixTotal());
        }
        
        order.setMontantTotal(total);
        totalLabel.setText(String.format("%.2f €", total));
    }
    
    /**
     * Enregistre la commande avec les valeurs du formulaire
     * @return La commande enregistrée
     */
    private Order saveOrder() {
        // Valider les champs obligatoires
        if (pharmacyCombo.getValue() == null) {
            AlertUtils.showWarning("Champ obligatoire", "La pharmacie est obligatoire.");
            return null;
        }
        
        if (statusCombo.getValue() == null) {
            AlertUtils.showWarning("Champ obligatoire", "Le statut est obligatoire.");
            return null;
        }
        
        if (itemsData.isEmpty()) {
            AlertUtils.showWarning("Commande vide", "La commande doit contenir au moins un produit.");
            return null;
        }
        
        try {
            // Mettre à jour la commande avec les valeurs du formulaire
            order.setPharmacyId(pharmacyCombo.getValue().getId());
            
            // Client ID
            if (!clientIdField.getText().isEmpty()) {
                order.setClientId(Integer.parseInt(clientIdField.getText()));
            }
            
            order.setStatut(statusCombo.getValue());
            order.setNotes(notesArea.getText());
            
            // Mettre à jour le total (déjà fait dans updateTotal())
            
            // Définir les produits de la commande
            order.setItems(new ArrayList<>(itemsData));
            
            return order;
            
        } catch (NumberFormatException e) {
            AlertUtils.showWarning("Valeur invalide", 
                    "L'ID du client doit être un nombre entier.");
            return null;
        } catch (Exception e) {
            AlertUtils.showError("Erreur", 
                    "Une erreur est survenue lors de l'enregistrement de la commande: " + e.getMessage());
            return null;
        }
    }
}
