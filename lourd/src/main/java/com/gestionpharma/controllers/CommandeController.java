package com.gestionpharma.controllers;

import com.gestionpharma.models.Commande;
import com.gestionpharma.models.DetailCommande;
import com.gestionpharma.models.Fournisseur;
import com.gestionpharma.services.CommandeService;
import com.gestionpharma.services.FournisseurService;
import com.gestionpharma.utils.AlertUtils;
import com.gestionpharma.utils.SessionManager;

import javafx.beans.property.SimpleDoubleProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.input.KeyCode;
import javafx.scene.layout.VBox;

import java.io.IOException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Optional;

/**
 * Contrôleur pour la gestion des commandes
 */
public class CommandeController {

    @FXML private TableView<Commande> commandesTable;
    @FXML private TableColumn<Commande, Integer> idColumn;
    @FXML private TableColumn<Commande, String> fournisseurColumn;
    @FXML private TableColumn<Commande, String> dateCommandeColumn;
    @FXML private TableColumn<Commande, String> dateLivraisonColumn;
    @FXML private TableColumn<Commande, String> statutColumn;
    @FXML private TableColumn<Commande, Double> montantColumn;
    @FXML private TableColumn<Commande, Void> actionsColumn;
    @FXML private TextField searchField;
    @FXML private Button addButton;
    @FXML private ComboBox<String> filterCombo;
    @FXML private Label totalCommandesLabel;
    @FXML private Label commandesEnAttenteLabel;
    
    private final CommandeService commandeService = new CommandeService();
    private final FournisseurService fournisseurService = new FournisseurService();
    private final ObservableList<Commande> commandes = FXCollections.observableArrayList();
    private final DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("dd/MM/yyyy");
    private int pharmacieId;
    
    /**
     * Initialise le contrôleur
     */
    @FXML
    public void initialize() {
        // Récupérer l'ID de la pharmacie à partir de la session
        this.pharmacieId = SessionManager.getPharmacieId();
        
        // Configurer les filtres
        filterCombo.setItems(FXCollections.observableArrayList(
                "Toutes", "En attente", "Validées", "En cours", "Livrées", "Annulées"
        ));
        filterCombo.setValue("Toutes");
        filterCombo.setOnAction(e -> filtrerCommandes());
        
        // Configurer la recherche
        searchField.setPromptText("Rechercher une commande...");
        searchField.setOnKeyPressed(e -> {
            if (e.getCode() == KeyCode.ENTER) {
                rechercherCommandes();
            }
        });
        
        // Configurer le tableau
        idColumn.setCellValueFactory(new PropertyValueFactory<>("id"));
        
        fournisseurColumn.setCellValueFactory(cellData -> {
            String fournisseurNom = cellData.getValue().getFournisseurNom();
            return new SimpleStringProperty(fournisseurNom != null ? fournisseurNom : "");
        });
        
        dateCommandeColumn.setCellValueFactory(cellData -> {
            LocalDate date = cellData.getValue().getDateCommande();
            return new SimpleStringProperty(date != null ? dateFormatter.format(date) : "");
        });
        
        dateLivraisonColumn.setCellValueFactory(cellData -> {
            LocalDate date = cellData.getValue().getDateLivraison();
            return new SimpleStringProperty(date != null ? dateFormatter.format(date) : "N/A");
        });
        
        statutColumn.setCellValueFactory(new PropertyValueFactory<>("statut"));
        
        montantColumn.setCellValueFactory(cellData -> 
            new SimpleDoubleProperty(cellData.getValue().getMontantTotal()).asObject());
        
        // Formater la colonne montant pour afficher le symbole €
        montantColumn.setCellFactory(column -> new TableCell<Commande, Double>() {
            @Override
            protected void updateItem(Double item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                } else {
                    setText(String.format("%.2f €", item));
                }
            }
        });
        
        // Configurer la colonne d'actions
        setupActionsColumn();
        
        // Configurer l'événement de double-clic pour afficher les détails
        commandesTable.setRowFactory(tv -> {
            TableRow<Commande> row = new TableRow<>();
            row.setOnMouseClicked(event -> {
                if (event.getClickCount() == 2 && !row.isEmpty()) {
                    afficherDetailsCommande(row.getItem());
                }
            });
            return row;
        });
        
        // Charger les commandes
        chargerCommandes();
        
        // Configurer le bouton d'ajout
        addButton.setOnAction(e -> ajouterCommande());
    }
    
    /**
     * Configure la colonne d'actions avec les boutons
     */
    private void setupActionsColumn() {
        actionsColumn.setCellFactory(col -> new TableCell<Commande, Void>() {
            private final Button editButton = new Button("Modifier");
            private final Button deleteButton = new Button("Supprimer");
            private final Button statusButton = new Button("Statut");
            
            {
                editButton.getStyleClass().add("btn-edit");
                editButton.setOnAction(e -> {
                    Commande commande = getTableView().getItems().get(getIndex());
                    modifierCommande(commande);
                });
                
                deleteButton.getStyleClass().add("btn-delete");
                deleteButton.setOnAction(e -> {
                    Commande commande = getTableView().getItems().get(getIndex());
                    supprimerCommande(commande);
                });
                
                statusButton.getStyleClass().add("btn-primary");
                statusButton.setOnAction(e -> {
                    Commande commande = getTableView().getItems().get(getIndex());
                    changerStatutCommande(commande);
                });
            }
            
            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                
                if (empty) {
                    setGraphic(null);
                } else {
                    // Créer une HBox pour contenir les boutons
                    javafx.scene.layout.HBox buttons = new javafx.scene.layout.HBox(5);
                    buttons.getChildren().addAll(editButton, statusButton, deleteButton);
                    setGraphic(buttons);
                }
            }
        });
    }
    
    /**
     * Charge toutes les commandes de la pharmacie
     */
    public void chargerCommandes() {
        commandes.clear();
        List<Commande> listeCommandes = commandeService.getAllCommandes(pharmacieId);
        commandes.addAll(listeCommandes);
        commandesTable.setItems(commandes);
        
        // Mettre à jour les statistiques
        mettreAJourStatistiques();
    }
    
    /**
     * Met à jour les statistiques des commandes
     */
    private void mettreAJourStatistiques() {
        double totalMontant = commandes.stream()
                .mapToDouble(Commande::getMontantTotal)
                .sum();
        
        totalCommandesLabel.setText(String.format("%.2f €", totalMontant));
        
        long commandesEnAttente = commandes.stream()
                .filter(c -> "En attente".equals(c.getStatut()))
                .count();
        
        commandesEnAttenteLabel.setText(String.valueOf(commandesEnAttente));
    }
    
    /**
     * Filtre les commandes selon le statut sélectionné
     */
    @FXML
    private void filtrerCommandes() {
        String filtre = filterCombo.getValue();
        
        if ("Toutes".equals(filtre)) {
            chargerCommandes();
        } else {
            commandes.clear();
            List<Commande> listeCommandes = commandeService.rechercherCommandes(filtre, pharmacieId);
            commandes.addAll(listeCommandes);
            commandesTable.setItems(commandes);
            mettreAJourStatistiques();
        }
    }
    
    /**
     * Recherche des commandes par fournisseur ou statut
     */
    @FXML
    private void rechercherCommandes() {
        String terme = searchField.getText().trim();
        
        if (terme.isEmpty()) {
            chargerCommandes();
        } else {
            commandes.clear();
            List<Commande> listeCommandes = commandeService.rechercherCommandes(terme, pharmacieId);
            commandes.addAll(listeCommandes);
            commandesTable.setItems(commandes);
            mettreAJourStatistiques();
        }
    }
    
    /**
     * Affiche la boîte de dialogue pour ajouter une commande
     */
    @FXML
    private void ajouterCommande() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/gestionpharma/commande_dialog.fxml"));
            DialogPane dialogPane = loader.load();
            
            CommandeDialogController controller = loader.getController();
            controller.loadFournisseurs(pharmacieId);
            
            Dialog<ButtonType> dialog = new Dialog<>();
            dialog.setDialogPane(dialogPane);
            dialog.setTitle("Ajouter une commande");
            
            controller.configurerValidation(dialog);
            
            Optional<ButtonType> result = dialog.showAndWait();
            
            if (result.isPresent() && result.get() == ButtonType.OK) {
                Commande commande = controller.getCommande();
                commande.setPharmacieId(pharmacieId);
                
                if (commandeService.ajouterCommande(commande)) {
                    AlertUtils.showInfoAlert("Succès", "Commande ajoutée", 
                            "La commande a été ajoutée avec succès.");
                    chargerCommandes();
                } else {
                    AlertUtils.showErrorAlert("Erreur", "Erreur d'ajout", 
                            "Impossible d'ajouter la commande.");
                }
            }
            
        } catch (IOException e) {
            AlertUtils.showErrorAlert("Erreur", "Erreur de chargement", 
                    "Impossible de charger la boîte de dialogue : " + e.getMessage());
        }
    }
    
    /**
     * Affiche la boîte de dialogue pour modifier une commande
     * @param commande Commande à modifier
     */
    private void modifierCommande(Commande commande) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/gestionpharma/commande_dialog.fxml"));
            DialogPane dialogPane = loader.load();
            
            CommandeDialogController controller = loader.getController();
            controller.loadFournisseurs(pharmacieId);
            controller.setCommande(commande);
            
            Dialog<ButtonType> dialog = new Dialog<>();
            dialog.setDialogPane(dialogPane);
            dialog.setTitle("Modifier la commande");
            
            controller.configurerValidation(dialog);
            
            Optional<ButtonType> result = dialog.showAndWait();
            
            if (result.isPresent() && result.get() == ButtonType.OK) {
                Commande commandeModifiee = controller.getCommande();
                commandeModifiee.setId(commande.getId());
                commandeModifiee.setPharmacieId(pharmacieId);
                
                if (commandeService.modifierCommande(commandeModifiee)) {
                    AlertUtils.showInfoAlert("Succès", "Commande modifiée", 
                            "La commande a été modifiée avec succès.");
                    chargerCommandes();
                } else {
                    AlertUtils.showErrorAlert("Erreur", "Erreur de modification", 
                            "Impossible de modifier la commande.");
                }
            }
            
        } catch (IOException e) {
            AlertUtils.showErrorAlert("Erreur", "Erreur de chargement", 
                    "Impossible de charger la boîte de dialogue : " + e.getMessage());
        }
    }
    
    /**
     * Supprime une commande après confirmation
     * @param commande Commande à supprimer
     */
    private void supprimerCommande(Commande commande) {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Confirmation de suppression");
        alert.setHeaderText("Supprimer la commande #" + commande.getId());
        alert.setContentText("Êtes-vous sûr de vouloir supprimer cette commande ? Cette action est irréversible.");
        
        Optional<ButtonType> result = alert.showAndWait();
        
        if (result.isPresent() && result.get() == ButtonType.OK) {
            if (commandeService.supprimerCommande(commande.getId())) {
                AlertUtils.showInfoAlert("Succès", "Commande supprimée", 
                        "La commande a été supprimée avec succès.");
                chargerCommandes();
            } else {
                AlertUtils.showErrorAlert("Erreur", "Erreur de suppression", 
                        "Impossible de supprimer la commande.");
            }
        }
    }
    
    /**
     * Affiche une boîte de dialogue pour changer le statut d'une commande
     * @param commande Commande dont on veut changer le statut
     */
    private void changerStatutCommande(Commande commande) {
        ComboBox<String> statutCombo = new ComboBox<>();
        statutCombo.setItems(FXCollections.observableArrayList(
                "En attente", "Validée", "En cours", "Livrée", "Annulée"
        ));
        statutCombo.setValue(commande.getStatut());
        
        VBox content = new VBox(10);
        content.getChildren().addAll(
                new Label("Sélectionnez le nouveau statut :"),
                statutCombo
        );
        
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Changer le statut");
        alert.setHeaderText("Changer le statut de la commande #" + commande.getId());
        alert.getDialogPane().setContent(content);
        
        Optional<ButtonType> result = alert.showAndWait();
        
        if (result.isPresent() && result.get() == ButtonType.OK) {
            String nouveauStatut = statutCombo.getValue();
            
            if (commandeService.updateStatutCommande(commande.getId(), nouveauStatut)) {
                AlertUtils.showInfoAlert("Succès", "Statut modifié", 
                        "Le statut de la commande a été mis à jour avec succès.");
                
                // Mettre à jour la date de livraison si le statut est "Livrée"
                if ("Livrée".equals(nouveauStatut)) {
                    commandeService.updateDateLivraison(commande.getId(), LocalDate.now());
                }
                
                chargerCommandes();
            } else {
                AlertUtils.showErrorAlert("Erreur", "Erreur de modification", 
                        "Impossible de modifier le statut de la commande.");
            }
        }
    }
    
    /**
     * Affiche les détails d'une commande
     * @param commande Commande à afficher
     */
    private void afficherDetailsCommande(Commande commande) {
        try {
            Dialog<ButtonType> dialog = new Dialog<>();
            dialog.setTitle("Détails de la commande #" + commande.getId());
            
            // Créer le contenu de la boîte de dialogue
            VBox content = new VBox(10);
            content.setStyle("-fx-padding: 20px;");
            
            // Informations générales
            Label titleLabel = new Label("Commande #" + commande.getId());
            titleLabel.setStyle("-fx-font-size: 18px; -fx-font-weight: bold;");
            
            Label fournisseurLabel = new Label("Fournisseur: " + commande.getFournisseurNom());
            Label dateCommandeLabel = new Label("Date de commande: " + 
                    (commande.getDateCommande() != null ? dateFormatter.format(commande.getDateCommande()) : "N/A"));
            Label dateLivraisonLabel = new Label("Date de livraison: " + 
                    (commande.getDateLivraison() != null ? dateFormatter.format(commande.getDateLivraison()) : "Non livrée"));
            Label statutLabel = new Label("Statut: " + commande.getStatut());
            Label montantLabel = new Label(String.format("Montant total: %.2f €", commande.getMontantTotal()));
            
            content.getChildren().addAll(titleLabel, fournisseurLabel, dateCommandeLabel, 
                    dateLivraisonLabel, statutLabel, montantLabel);
            
            // Notes
            if (commande.getNotes() != null && !commande.getNotes().isEmpty()) {
                Label notesTitle = new Label("Notes:");
                notesTitle.setStyle("-fx-font-weight: bold;");
                TextArea notesArea = new TextArea(commande.getNotes());
                notesArea.setEditable(false);
                notesArea.setPrefRowCount(3);
                content.getChildren().addAll(new Separator(), notesTitle, notesArea);
            }
            
            // Détails des produits
            Label produitsTitle = new Label("Produits commandés:");
            produitsTitle.setStyle("-fx-font-weight: bold;");
            content.getChildren().addAll(new Separator(), produitsTitle);
            
            TableView<DetailCommande> detailsTable = new TableView<>();
            
            TableColumn<DetailCommande, String> produitColumn = new TableColumn<>("Produit");
            produitColumn.setCellValueFactory(cellData -> 
                new SimpleStringProperty(cellData.getValue().getProduit().getNom()));
            produitColumn.setPrefWidth(200);
            
            TableColumn<DetailCommande, Integer> quantiteColumn = new TableColumn<>("Quantité");
            quantiteColumn.setCellValueFactory(new PropertyValueFactory<>("quantite"));
            quantiteColumn.setPrefWidth(80);
            
            TableColumn<DetailCommande, Double> prixColumn = new TableColumn<>("Prix unitaire");
            prixColumn.setCellValueFactory(new PropertyValueFactory<>("prixUnitaire"));
            prixColumn.setCellFactory(column -> new TableCell<DetailCommande, Double>() {
                @Override
                protected void updateItem(Double item, boolean empty) {
                    super.updateItem(item, empty);
                    if (empty || item == null) {
                        setText(null);
                    } else {
                        setText(String.format("%.2f €", item));
                    }
                }
            });
            prixColumn.setPrefWidth(100);
            
            TableColumn<DetailCommande, Double> totalColumn = new TableColumn<>("Total");
            totalColumn.setCellValueFactory(cellData -> 
                new SimpleDoubleProperty(cellData.getValue().getMontantTotal()).asObject());
            totalColumn.setCellFactory(column -> new TableCell<DetailCommande, Double>() {
                @Override
                protected void updateItem(Double item, boolean empty) {
                    super.updateItem(item, empty);
                    if (empty || item == null) {
                        setText(null);
                    } else {
                        setText(String.format("%.2f €", item));
                    }
                }
            });
            totalColumn.setPrefWidth(100);
            
            detailsTable.getColumns().addAll(produitColumn, quantiteColumn, prixColumn, totalColumn);
            detailsTable.setItems(FXCollections.observableArrayList(commande.getDetailsCommande()));
            detailsTable.setPrefHeight(200);
            
            content.getChildren().add(detailsTable);
            
            // Configurer la boîte de dialogue
            dialog.getDialogPane().setContent(content);
            dialog.getDialogPane().getButtonTypes().add(ButtonType.CLOSE);
            
            dialog.showAndWait();
            
        } catch (Exception e) {
            AlertUtils.showErrorAlert("Erreur", "Erreur d'affichage", 
                    "Impossible d'afficher les détails de la commande : " + e.getMessage());
        }
    }
}
