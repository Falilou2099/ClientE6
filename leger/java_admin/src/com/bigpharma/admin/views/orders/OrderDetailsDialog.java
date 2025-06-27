package com.bigpharma.admin.views.orders;

import com.bigpharma.admin.dao.OrderDAO;
import com.bigpharma.admin.dao.OrderItemDAO;
import com.bigpharma.admin.dao.PharmacyDAO;
import com.bigpharma.admin.dao.ProductDAO;
import com.bigpharma.admin.models.Order;
import com.bigpharma.admin.models.OrderItem;
import com.bigpharma.admin.models.Pharmacy;
import com.bigpharma.admin.models.Product;
import javafx.collections.FXCollections;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.scene.text.Text;
import javafx.stage.Stage;

import java.math.BigDecimal;
import java.text.SimpleDateFormat;
import java.util.List;

/**
 * Boîte de dialogue pour afficher les détails d'une commande
 */
public class OrderDetailsDialog extends Dialog<Void> {
    
    private final Order order;
    private final OrderDAO orderDAO;
    private final OrderItemDAO orderItemDAO;
    private final PharmacyDAO pharmacyDAO;
    private final ProductDAO productDAO;
    private TableView<OrderItem> itemsTable;
    
    /**
     * Constructeur
     * @param order La commande à afficher
     */
    public OrderDetailsDialog(Order order) {
        this.order = order;
        this.orderDAO = new OrderDAO();
        this.orderItemDAO = new OrderItemDAO();
        this.pharmacyDAO = new PharmacyDAO();
        this.productDAO = new ProductDAO();
        
        // Configurer la boîte de dialogue
        setTitle("Détails de la commande");
        setHeaderText("Commande #" + order.getReference());
        
        // Créer le contenu
        createContent();
        
        // Configurer les boutons
        ButtonType closeButtonType = new ButtonType("Fermer", ButtonBar.ButtonData.CANCEL_CLOSE);
        getDialogPane().getButtonTypes().add(closeButtonType);
        
        // Définir la taille de la boîte de dialogue
        getDialogPane().setPrefSize(800, 600);
    }
    
    /**
     * Crée le contenu de la boîte de dialogue
     */
    private void createContent() {
        BorderPane mainPane = new BorderPane();
        mainPane.setPadding(new Insets(20));
        
        // En-tête avec les informations de la commande
        VBox headerBox = createHeaderBox();
        mainPane.setTop(headerBox);
        
        // Tableau des produits de la commande
        itemsTable = createItemsTable();
        mainPane.setCenter(itemsTable);
        
        // Pied de page avec le total
        HBox footerBox = createFooterBox();
        mainPane.setBottom(footerBox);
        
        // Charger les produits de la commande
        loadOrderItems();
        
        getDialogPane().setContent(mainPane);
    }
    
    /**
     * Crée l'en-tête avec les informations de la commande
     * @return L'en-tête
     */
    private VBox createHeaderBox() {
        VBox headerBox = new VBox();
        headerBox.setSpacing(10);
        headerBox.setPadding(new Insets(0, 0, 20, 0));
        
        // Titre
        Text title = new Text("Informations de la commande");
        title.setFont(Font.font("Arial", FontWeight.BOLD, 18));
        title.setFill(Color.web("#4e73df"));
        
        // Grille d'informations
        GridPane infoGrid = new GridPane();
        infoGrid.setHgap(20);
        infoGrid.setVgap(10);
        infoGrid.setPadding(new Insets(10));
        infoGrid.setStyle("-fx-background-color: #f8f9fc; -fx-border-color: #e3e6f0; -fx-border-radius: 5;");
        
        // Référence
        infoGrid.add(createInfoLabel("Référence:"), 0, 0);
        infoGrid.add(createInfoValue(order.getReference()), 1, 0);
        
        // Date
        SimpleDateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy HH:mm");
        infoGrid.add(createInfoLabel("Date:"), 0, 1);
        infoGrid.add(createInfoValue(order.getDateCommande() != null 
                ? dateFormat.format(order.getDateCommande()) : "N/A"), 1, 1);
        
        // Statut
        infoGrid.add(createInfoLabel("Statut:"), 0, 2);
        Label statusLabel = createInfoValue(order.getStatut());
        switch (order.getStatut()) {
            case "pending":
                statusLabel.setTextFill(Color.web("#f6c23e"));
                break;
            case "processing":
                statusLabel.setTextFill(Color.web("#4e73df"));
                break;
            case "shipped":
                statusLabel.setTextFill(Color.web("#36b9cc"));
                break;
            case "delivered":
                statusLabel.setTextFill(Color.web("#1cc88a"));
                break;
            case "cancelled":
                statusLabel.setTextFill(Color.web("#e74a3b"));
                break;
        }
        infoGrid.add(statusLabel, 1, 2);
        
        // Pharmacie
        infoGrid.add(createInfoLabel("Pharmacie:"), 2, 0);
        Pharmacy pharmacy = order.getPharmacyId() != null 
                ? pharmacyDAO.findById(order.getPharmacyId()) : null;
        infoGrid.add(createInfoValue(pharmacy != null ? pharmacy.getNom() : "N/A"), 3, 0);
        
        // Client
        infoGrid.add(createInfoLabel("Client ID:"), 2, 1);
        infoGrid.add(createInfoValue(order.getClientId() != null 
                ? order.getClientId().toString() : "N/A"), 3, 1);
        
        // Notes
        infoGrid.add(createInfoLabel("Notes:"), 2, 2);
        infoGrid.add(createInfoValue(order.getNotes() != null ? order.getNotes() : "Aucune note"), 3, 2);
        
        headerBox.getChildren().addAll(title, infoGrid);
        
        return headerBox;
    }
    
    /**
     * Crée un label d'information
     * @param text Le texte du label
     * @return Le label
     */
    private Label createInfoLabel(String text) {
        Label label = new Label(text);
        label.setFont(Font.font("Arial", FontWeight.BOLD, 14));
        return label;
    }
    
    /**
     * Crée un label de valeur
     * @param text Le texte du label
     * @return Le label
     */
    private Label createInfoValue(String text) {
        Label label = new Label(text);
        label.setFont(Font.font("Arial", 14));
        return label;
    }
    
    /**
     * Crée le tableau des produits de la commande
     * @return Le tableau
     */
    private TableView<OrderItem> createItemsTable() {
        TableView<OrderItem> table = new TableView<>();
        table.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
        
        // Colonnes du tableau
        TableColumn<OrderItem, Integer> idCol = new TableColumn<>("ID");
        idCol.setCellValueFactory(new PropertyValueFactory<>("id"));
        idCol.setPrefWidth(50);
        
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
        
        // Ajouter les colonnes au tableau
        table.getColumns().addAll(idCol, productIdCol, quantityCol, priceCol, totalCol);
        
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
        Label totalLabel = new Label("Total de la commande:");
        totalLabel.setFont(Font.font("Arial", FontWeight.BOLD, 16));
        
        // Valeur du total
        Label totalValue = new Label(String.format("%.2f €", order.getMontantTotal()));
        totalValue.setFont(Font.font("Arial", FontWeight.BOLD, 16));
        totalValue.setTextFill(Color.web("#4e73df"));
        
        footerBox.getChildren().addAll(totalLabel, totalValue);
        
        return footerBox;
    }
    
    /**
     * Charge les produits de la commande
     */
    private void loadOrderItems() {
        List<OrderItem> items = orderItemDAO.findByOrderId(order.getId());
        itemsTable.setItems(FXCollections.observableArrayList(items));
    }
}
