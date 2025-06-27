package com.bigpharma.admin.views.pharmacies;

import com.bigpharma.admin.dao.PharmacyDAO;
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

import java.util.List;

/**
 * Vue de liste des pharmacies
 */
public class PharmacyListView extends BorderPane {
    
    private final User currentUser;
    private final PharmacyDAO pharmacyDAO;
    private TableView<Pharmacy> pharmacyTable;
    private ObservableList<Pharmacy> pharmacyData;
    private TextField searchField;
    
    // Couleurs de l'application
    private final String primaryColor = "#4e73df";
    private final String secondaryColor = "#1cc88a";
    private final String backgroundColor = "#f8f9fc";
    private final String textColor = "#5a5c69";
    
    /**
     * Constructeur
     * @param user L'utilisateur connecté
     */
    public PharmacyListView(User user) {
        this.currentUser = user;
        this.pharmacyDAO = new PharmacyDAO();
        
        initializeUI();
        loadPharmacies();
    }
    
    /**
     * Initialise l'interface utilisateur
     */
    private void initializeUI() {
        setPadding(new Insets(0));
        
        // En-tête
        VBox header = createHeader();
        setTop(header);
        
        // Tableau des pharmacies
        pharmacyTable = createPharmacyTable();
        setCenter(pharmacyTable);
        
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
        Text title = new Text("Gestion des Pharmacies");
        title.setFont(Font.font("Arial", FontWeight.BOLD, 24));
        title.setFill(Color.web(textColor));
        
        // Barre de recherche
        HBox searchBar = new HBox();
        searchBar.setSpacing(10);
        searchBar.setAlignment(Pos.CENTER_LEFT);
        
        searchField = new TextField();
        searchField.setPromptText("Rechercher une pharmacie...");
        searchField.setPrefWidth(300);
        searchField.textProperty().addListener((obs, oldVal, newVal) -> {
            filterPharmacies();
        });
        
        // Bouton d'ajout de pharmacie
        Button addButton = new Button("Nouvelle pharmacie");
        addButton.setStyle("-fx-background-color: " + secondaryColor + "; -fx-text-fill: white;");
        addButton.setOnAction(e -> showAddPharmacyDialog());
        
        searchBar.getChildren().addAll(searchField, addButton);
        
        // Ajouter les éléments à l'en-tête
        header.getChildren().addAll(title, searchBar);
        
        return header;
    }
    
    /**
     * Crée le tableau des pharmacies
     * @return Le tableau des pharmacies
     */
    private TableView<Pharmacy> createPharmacyTable() {
        TableView<Pharmacy> table = new TableView<>();
        table.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
        
        // Colonnes du tableau
        TableColumn<Pharmacy, Integer> idCol = new TableColumn<>("ID");
        idCol.setCellValueFactory(new PropertyValueFactory<>("id"));
        idCol.setPrefWidth(50);
        
        TableColumn<Pharmacy, String> nameCol = new TableColumn<>("Nom");
        nameCol.setCellValueFactory(new PropertyValueFactory<>("nom"));
        nameCol.setPrefWidth(200);
        
        TableColumn<Pharmacy, String> addressCol = new TableColumn<>("Adresse");
        addressCol.setCellValueFactory(new PropertyValueFactory<>("adresse"));
        addressCol.setPrefWidth(250);
        
        TableColumn<Pharmacy, String> phoneCol = new TableColumn<>("Téléphone");
        phoneCol.setCellValueFactory(new PropertyValueFactory<>("telephone"));
        phoneCol.setPrefWidth(120);
        
        TableColumn<Pharmacy, String> emailCol = new TableColumn<>("Email");
        emailCol.setCellValueFactory(new PropertyValueFactory<>("email"));
        emailCol.setPrefWidth(150);
        
        TableColumn<Pharmacy, Void> actionsCol = new TableColumn<>("Actions");
        actionsCol.setPrefWidth(200);
        actionsCol.setCellFactory(col -> new TableCell<Pharmacy, Void>() {
            private final Button editButton = new Button("Modifier");
            private final Button deleteButton = new Button("Supprimer");
            private final HBox buttonsBox = new HBox(5);
            
            {
                editButton.setStyle("-fx-background-color: " + primaryColor + "; -fx-text-fill: white;");
                deleteButton.setStyle("-fx-background-color: #e74a3b; -fx-text-fill: white;");
                
                editButton.setOnAction(e -> {
                    Pharmacy pharmacy = getTableView().getItems().get(getIndex());
                    showEditPharmacyDialog(pharmacy);
                });
                
                deleteButton.setOnAction(e -> {
                    Pharmacy pharmacy = getTableView().getItems().get(getIndex());
                    deletePharmacy(pharmacy);
                });
                
                buttonsBox.getChildren().addAll(editButton, deleteButton);
                buttonsBox.setAlignment(Pos.CENTER);
            }
            
            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                setGraphic(empty ? null : buttonsBox);
            }
        });
        
        // Ajouter les colonnes au tableau
        table.getColumns().addAll(idCol, nameCol, addressCol, phoneCol, emailCol, actionsCol);
        
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
        refreshButton.setOnAction(e -> loadPharmacies());
        
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
     * Charge les pharmacies depuis la base de données
     */
    private void loadPharmacies() {
        List<Pharmacy> pharmacies = pharmacyDAO.findAll();
        pharmacyData = FXCollections.observableArrayList(pharmacies);
        pharmacyTable.setItems(pharmacyData);
    }
    
    /**
     * Filtre les pharmacies selon les critères de recherche
     */
    private void filterPharmacies() {
        String searchText = searchField.getText().toLowerCase();
        
        // Filtrer les pharmacies
        List<Pharmacy> allPharmacies = pharmacyDAO.findAll();
        
        // Appliquer les filtres
        List<Pharmacy> filteredPharmacies = allPharmacies.stream()
                .filter(pharmacy -> 
                    searchText.isEmpty() || 
                    (pharmacy.getNom() != null && pharmacy.getNom().toLowerCase().contains(searchText)) ||
                    (pharmacy.getAdresse() != null && pharmacy.getAdresse().toLowerCase().contains(searchText)) ||
                    (pharmacy.getTelephone() != null && pharmacy.getTelephone().contains(searchText)) ||
                    (pharmacy.getEmail() != null && pharmacy.getEmail().toLowerCase().contains(searchText))
                )
                .collect(java.util.stream.Collectors.toList());
        
        pharmacyData = FXCollections.observableArrayList(filteredPharmacies);
        pharmacyTable.setItems(pharmacyData);
    }
    
    /**
     * Affiche la boîte de dialogue pour ajouter une pharmacie
     */
    private void showAddPharmacyDialog() {
        // Vérifier si l'utilisateur a le droit d'ajouter une pharmacie
        if (!"admin".equals(currentUser.getRole())) {
            AlertUtils.showWarning("Attention", 
                    "Vous n'avez pas les droits pour ajouter une pharmacie.");
            return;
        }
        
        // Créer une nouvelle pharmacie
        Pharmacy newPharmacy = new Pharmacy();
        
        // Afficher la boîte de dialogue d'édition
        PharmacyDialog dialog = new PharmacyDialog(newPharmacy, true);
        dialog.initModality(Modality.APPLICATION_MODAL);
        dialog.showAndWait().ifPresent(pharmacy -> {
            // Sauvegarder la pharmacie
            Pharmacy savedPharmacy = pharmacyDAO.save(pharmacy);
            if (savedPharmacy != null) {
                AlertUtils.showInfo("Succès", "La pharmacie a été ajoutée avec succès.");
                loadPharmacies();
            } else {
                AlertUtils.showError("Erreur", "Impossible d'ajouter la pharmacie.");
            }
        });
    }
    
    /**
     * Affiche la boîte de dialogue pour modifier une pharmacie
     * @param pharmacy La pharmacie à modifier
     */
    private void showEditPharmacyDialog(Pharmacy pharmacy) {
        // Vérifier si l'utilisateur a le droit de modifier une pharmacie
        if (!"admin".equals(currentUser.getRole())) {
            AlertUtils.showWarning("Attention", 
                    "Vous n'avez pas les droits pour modifier une pharmacie.");
            return;
        }
        
        // Afficher la boîte de dialogue d'édition
        PharmacyDialog dialog = new PharmacyDialog(pharmacy, false);
        dialog.initModality(Modality.APPLICATION_MODAL);
        dialog.showAndWait().ifPresent(updatedPharmacy -> {
            // Mettre à jour la pharmacie
            Pharmacy savedPharmacy = pharmacyDAO.update(updatedPharmacy);
            if (savedPharmacy != null) {
                AlertUtils.showInfo("Succès", "La pharmacie a été modifiée avec succès.");
                loadPharmacies();
            } else {
                AlertUtils.showError("Erreur", "Impossible de modifier la pharmacie.");
            }
        });
    }
    
    /**
     * Supprime une pharmacie
     * @param pharmacy La pharmacie à supprimer
     */
    private void deletePharmacy(Pharmacy pharmacy) {
        // Vérifier si l'utilisateur a le droit de supprimer une pharmacie
        if (!"admin".equals(currentUser.getRole())) {
            AlertUtils.showWarning("Attention", 
                    "Vous n'avez pas les droits pour supprimer une pharmacie.");
            return;
        }
        
        // Demander confirmation
        boolean confirm = AlertUtils.showConfirmation(
                "Confirmation", 
                "Êtes-vous sûr de vouloir supprimer la pharmacie " + pharmacy.getNom() + " ?");
        
        if (confirm) {
            // Supprimer la pharmacie
            boolean deleted = pharmacyDAO.delete(pharmacy.getId());
            
            if (deleted) {
                AlertUtils.showInfo("Succès", "La pharmacie a été supprimée avec succès.");
                loadPharmacies();
            } else {
                AlertUtils.showError("Erreur", "Impossible de supprimer la pharmacie.");
            }
        }
    }
}
