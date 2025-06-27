package com.bigpharma.admin.views.orders;

import com.bigpharma.admin.dao.OrderDAO;
import com.bigpharma.admin.models.Order;
import com.bigpharma.admin.models.OrderItem;
import com.bigpharma.admin.models.Pharmacy;
import com.bigpharma.admin.models.User;
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
import javafx.stage.Modality;

import java.math.BigDecimal;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

/**
 * Vue de liste des commandes
 */
public class OrderListView extends BorderPane {
    
    private final User currentUser;
    private final Pharmacy currentPharmacy;
    private final OrderDAO orderDAO;
    private TableView<Order> orderTable;
    private ObservableList<Order> orderData;
    private TextField searchField;
    private ComboBox<String> statusFilter;
    
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
    public OrderListView(User user, Pharmacy pharmacy) {
        this.currentUser = user;
        this.currentPharmacy = pharmacy;
        this.orderDAO = new OrderDAO();
        
        initializeUI();
        loadOrders();
    }
    
    /**
     * Initialise l'interface utilisateur
     */
    private void initializeUI() {
        setPadding(new Insets(0));
        
        // En-tête
        VBox header = createHeader();
        setTop(header);
        
        // Tableau des commandes
        orderTable = createOrderTable();
        setCenter(orderTable);
        
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
        Text title = new Text("Gestion des Commandes");
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
        searchField.setPromptText("Rechercher une commande...");
        searchField.setPrefWidth(300);
        searchField.textProperty().addListener((obs, oldVal, newVal) -> {
            filterOrders();
        });
        
        Label statusLabel = new Label("Statut:");
        statusFilter = new ComboBox<>();
        statusFilter.setPrefWidth(200);
        statusFilter.getItems().addAll(
                "Tous les statuts",
                "pending", 
                "processing", 
                "shipped", 
                "delivered", 
                "cancelled");
        statusFilter.getSelectionModel().selectFirst();
        
        // Ajouter un écouteur pour filtrer les commandes
        statusFilter.setOnAction(e -> filterOrders());
        
        // Bouton d'ajout de commande
        Button addButton = new Button("Nouvelle commande");
        addButton.setStyle("-fx-background-color: " + secondaryColor + "; -fx-text-fill: white;");
        addButton.setOnAction(e -> showAddOrderDialog());
        
        searchBar.getChildren().addAll(searchField, statusLabel, statusFilter, addButton);
        
        // Ajouter les éléments à l'en-tête
        header.getChildren().addAll(title, subtitle, searchBar);
        
        return header;
    }
    
    /**
     * Crée le tableau des commandes
     * @return Le tableau des commandes
     */
    private TableView<Order> createOrderTable() {
        TableView<Order> table = new TableView<>();
        table.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
        
        // Colonnes du tableau
        TableColumn<Order, Integer> idCol = new TableColumn<>("ID");
        idCol.setCellValueFactory(new PropertyValueFactory<>("id"));
        idCol.setPrefWidth(50);
        
        TableColumn<Order, String> referenceCol = new TableColumn<>("Référence");
        referenceCol.setCellValueFactory(new PropertyValueFactory<>("reference"));
        referenceCol.setPrefWidth(120);
        
        TableColumn<Order, Date> dateCol = new TableColumn<>("Date");
        dateCol.setCellValueFactory(new PropertyValueFactory<>("dateCommande"));
        dateCol.setPrefWidth(120);
        dateCol.setCellFactory(col -> new TableCell<Order, Date>() {
            private final SimpleDateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy HH:mm");
            
            @Override
            protected void updateItem(Date date, boolean empty) {
                super.updateItem(date, empty);
                if (empty || date == null) {
                    setText(null);
                } else {
                    setText(dateFormat.format(date));
                }
            }
        });
        
        TableColumn<Order, String> statusCol = new TableColumn<>("Statut");
        statusCol.setCellValueFactory(new PropertyValueFactory<>("statut"));
        statusCol.setPrefWidth(100);
        statusCol.setCellFactory(col -> new TableCell<Order, String>() {
            @Override
            protected void updateItem(String status, boolean empty) {
                super.updateItem(status, empty);
                if (empty || status == null) {
                    setText(null);
                    setStyle("");
                } else {
                    setText(status);
                    switch (status) {
                        case "pending":
                            setStyle("-fx-text-fill: #f6c23e;"); // Jaune
                            break;
                        case "processing":
                            setStyle("-fx-text-fill: #4e73df;"); // Bleu
                            break;
                        case "shipped":
                            setStyle("-fx-text-fill: #36b9cc;"); // Cyan
                            break;
                        case "delivered":
                            setStyle("-fx-text-fill: #1cc88a;"); // Vert
                            break;
                        case "cancelled":
                            setStyle("-fx-text-fill: #e74a3b;"); // Rouge
                            break;
                        default:
                            setStyle("");
                            break;
                    }
                }
            }
        });
        
        TableColumn<Order, BigDecimal> totalCol = new TableColumn<>("Total");
        totalCol.setCellValueFactory(new PropertyValueFactory<>("montantTotal"));
        totalCol.setPrefWidth(100);
        totalCol.setCellFactory(col -> new TableCell<Order, BigDecimal>() {
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
        
        TableColumn<Order, Integer> clientCol = new TableColumn<>("Client ID");
        clientCol.setCellValueFactory(new PropertyValueFactory<>("clientId"));
        clientCol.setPrefWidth(80);
        
        TableColumn<Order, Integer> pharmacyCol = new TableColumn<>("Pharmacie ID");
        pharmacyCol.setCellValueFactory(new PropertyValueFactory<>("pharmacyId"));
        pharmacyCol.setPrefWidth(100);
        
        TableColumn<Order, Void> actionsCol = new TableColumn<>("Actions");
        actionsCol.setPrefWidth(250);
        actionsCol.setCellFactory(col -> new TableCell<Order, Void>() {
            private final Button viewButton = new Button("Détails");
            private final Button processButton = new Button("Traiter");
            private final Button deliverButton = new Button("Livrer");
            private final Button cancelButton = new Button("Annuler");
            private final HBox buttonsBox = new HBox(5);
            
            {
                viewButton.setStyle("-fx-background-color: " + primaryColor + "; -fx-text-fill: white;");
                processButton.setStyle("-fx-background-color: #36b9cc; -fx-text-fill: white;");
                deliverButton.setStyle("-fx-background-color: " + secondaryColor + "; -fx-text-fill: white;");
                cancelButton.setStyle("-fx-background-color: #e74a3b; -fx-text-fill: white;");
                
                viewButton.setOnAction(e -> {
                    Order order = getTableView().getItems().get(getIndex());
                    showOrderDetails(order);
                });
                
                processButton.setOnAction(e -> {
                    Order order = getTableView().getItems().get(getIndex());
                    processOrder(order);
                });
                
                deliverButton.setOnAction(e -> {
                    Order order = getTableView().getItems().get(getIndex());
                    deliverOrder(order);
                });
                
                cancelButton.setOnAction(e -> {
                    Order order = getTableView().getItems().get(getIndex());
                    cancelOrder(order);
                });
                
                buttonsBox.setAlignment(Pos.CENTER);
            }
            
            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                
                if (empty) {
                    setGraphic(null);
                } else {
                    // Récupérer la commande
                    Order order = getTableView().getItems().get(getIndex());
                    
                    // Ajouter les boutons en fonction du statut
                    buttonsBox.getChildren().clear();
                    buttonsBox.getChildren().add(viewButton);
                    
                    if ("pending".equals(order.getStatut())) {
                        buttonsBox.getChildren().addAll(processButton, cancelButton);
                    } else if ("processing".equals(order.getStatut())) {
                        buttonsBox.getChildren().add(deliverButton);
                    } else if ("shipped".equals(order.getStatut())) {
                        buttonsBox.getChildren().add(deliverButton);
                    }
                    
                    setGraphic(buttonsBox);
                }
            }
        });
        
        // Ajouter les colonnes au tableau
        table.getColumns().addAll(
                idCol, referenceCol, dateCol, statusCol, totalCol, clientCol, pharmacyCol, actionsCol);
        
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
        refreshButton.setOnAction(e -> loadOrders());
        
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
     * Charge les commandes depuis la base de données
     */
    private void loadOrders() {
        Integer pharmacyId = currentPharmacy != null ? currentPharmacy.getId() : null;
        List<Order> orders = orderDAO.findAllByPharmacyId(pharmacyId);
        
        orderData = FXCollections.observableArrayList(orders);
        orderTable.setItems(orderData);
    }
    
    /**
     * Filtre les commandes selon les critères de recherche
     */
    private void filterOrders() {
        String searchText = searchField.getText().toLowerCase();
        String status = statusFilter.getValue();
        
        if (status == null || status.equals("Tous les statuts")) {
            status = "";
        }
        
        String finalStatus = status;
        
        // Filtrer les commandes
        Integer pharmacyId = currentPharmacy != null ? currentPharmacy.getId() : null;
        List<Order> allOrders = orderDAO.findAllByPharmacyId(pharmacyId);
        
        // Appliquer les filtres
        List<Order> filteredOrders = allOrders.stream()
                .filter(order -> 
                    (finalStatus.isEmpty() || order.getStatut().equals(finalStatus)) &&
                    (searchText.isEmpty() || 
                     (order.getReference() != null && order.getReference().toLowerCase().contains(searchText)) ||
                     String.valueOf(order.getId()).contains(searchText))
                )
                .collect(java.util.stream.Collectors.toList());
        
        orderData = FXCollections.observableArrayList(filteredOrders);
        orderTable.setItems(orderData);
    }
    
    /**
     * Affiche la boîte de dialogue pour ajouter une commande
     */
    private void showAddOrderDialog() {
        if (currentPharmacy == null && !"admin".equals(currentUser.getRole())) {
            AlertUtils.showWarning("Attention", 
                    "Vous devez sélectionner une pharmacie pour ajouter une commande.");
            return;
        }
        
        // Créer une nouvelle commande
        Order newOrder = new Order();
        newOrder.setReference(orderDAO.generateReference());
        newOrder.setDateCommande(new Date());
        newOrder.setStatut("pending");
        newOrder.setMontantTotal(BigDecimal.ZERO);
        
        // Définir la pharmacie si elle est sélectionnée
        if (currentPharmacy != null) {
            newOrder.setPharmacyId(currentPharmacy.getId());
        }
        
        // Afficher la boîte de dialogue d'édition
        OrderDialog dialog = new OrderDialog(newOrder, true);
        dialog.initModality(Modality.APPLICATION_MODAL);
        dialog.showAndWait().ifPresent(order -> {
            // Sauvegarder la commande
            Order savedOrder = orderDAO.save(order);
            if (savedOrder != null) {
                AlertUtils.showInfo("Succès", "La commande a été ajoutée avec succès.");
                loadOrders();
            } else {
                AlertUtils.showError("Erreur", "Impossible d'ajouter la commande.");
            }
        });
    }
    
    /**
     * Affiche les détails d'une commande
     * @param order La commande à afficher
     */
    private void showOrderDetails(Order order) {
        OrderDetailsDialog dialog = new OrderDetailsDialog(order);
        dialog.initModality(Modality.APPLICATION_MODAL);
        dialog.showAndWait();
    }
    
    /**
     * Traite une commande (change son statut à "processing")
     * @param order La commande à traiter
     */
    private void processOrder(Order order) {
        // Vérifier si l'utilisateur a le droit de traiter cette commande
        if (currentPharmacy == null && order.getPharmacyId() != null && 
                !"admin".equals(currentUser.getRole())) {
            AlertUtils.showWarning("Attention", 
                    "Vous n'avez pas les droits pour traiter cette commande.");
            return;
        }
        
        // Demander confirmation
        boolean confirm = AlertUtils.showConfirmation(
                "Confirmation", 
                "Êtes-vous sûr de vouloir traiter la commande #" + order.getReference() + " ?");
        
        if (confirm) {
            // Mettre à jour le statut
            order.setStatut("processing");
            Order updatedOrder = orderDAO.update(order);
            
            if (updatedOrder != null) {
                AlertUtils.showInfo("Succès", "La commande a été traitée avec succès.");
                loadOrders();
            } else {
                AlertUtils.showError("Erreur", "Impossible de traiter la commande.");
            }
        }
    }
    
    /**
     * Livre une commande (change son statut à "delivered" et met à jour le stock)
     * @param order La commande à livrer
     */
    private void deliverOrder(Order order) {
        // Vérifier si l'utilisateur a le droit de livrer cette commande
        if (currentPharmacy == null && order.getPharmacyId() != null && 
                !"admin".equals(currentUser.getRole())) {
            AlertUtils.showWarning("Attention", 
                    "Vous n'avez pas les droits pour livrer cette commande.");
            return;
        }
        
        // Demander confirmation
        boolean confirm = AlertUtils.showConfirmation(
                "Confirmation", 
                "Êtes-vous sûr de vouloir marquer la commande #" + order.getReference() + 
                " comme livrée ? Cela mettra à jour le stock des produits.");
        
        if (confirm) {
            // Traiter la livraison
            boolean delivered = orderDAO.processDelivery(order.getId());
            
            if (delivered) {
                AlertUtils.showInfo("Succès", "La commande a été livrée avec succès et le stock a été mis à jour.");
                loadOrders();
            } else {
                AlertUtils.showError("Erreur", "Impossible de livrer la commande.");
            }
        }
    }
    
    /**
     * Annule une commande (change son statut à "cancelled")
     * @param order La commande à annuler
     */
    private void cancelOrder(Order order) {
        // Vérifier si l'utilisateur a le droit d'annuler cette commande
        if (currentPharmacy == null && order.getPharmacyId() != null && 
                !"admin".equals(currentUser.getRole())) {
            AlertUtils.showWarning("Attention", 
                    "Vous n'avez pas les droits pour annuler cette commande.");
            return;
        }
        
        // Demander confirmation
        boolean confirm = AlertUtils.showConfirmation(
                "Confirmation", 
                "Êtes-vous sûr de vouloir annuler la commande #" + order.getReference() + " ?");
        
        if (confirm) {
            // Mettre à jour le statut
            order.setStatut("cancelled");
            Order updatedOrder = orderDAO.update(order);
            
            if (updatedOrder != null) {
                AlertUtils.showInfo("Succès", "La commande a été annulée avec succès.");
                loadOrders();
            } else {
                AlertUtils.showError("Erreur", "Impossible d'annuler la commande.");
            }
        }
    }
}
