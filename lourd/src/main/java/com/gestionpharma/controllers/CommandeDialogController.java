package com.gestionpharma.controllers;

import com.gestionpharma.models.Commande;
import com.gestionpharma.models.DetailCommande;
import com.gestionpharma.models.Fournisseur;
import com.gestionpharma.models.Produit;
import com.gestionpharma.services.FournisseurService;
import com.gestionpharma.services.ProduitService;
import com.gestionpharma.utils.AlertUtils;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.geometry.Insets;
import javafx.scene.Node;
import javafx.util.Callback;
import javafx.util.StringConverter;

import java.io.IOException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * Contrôleur pour la boîte de dialogue d'ajout/modification de commande
 */
public class CommandeDialogController {
    
    @FXML private ComboBox<Fournisseur> fournisseurCombo;
    @FXML private DatePicker dateCommandePicker;
    @FXML private ComboBox<String> statutCombo;
    @FXML private TextArea notesField;
    @FXML private Button addProduitButton;
    @FXML private TableView<DetailCommande> produitsTable;
    @FXML private TableColumn<DetailCommande, String> produitNomColumn;
    @FXML private TableColumn<DetailCommande, Integer> quantiteColumn;
    @FXML private TableColumn<DetailCommande, Double> prixUnitaireColumn;
    @FXML private TableColumn<DetailCommande, Double> totalColumn;
    @FXML private TableColumn<DetailCommande, Void> actionsColumn;
    @FXML private Label totalCommandeLabel;
    
    private Commande commande;
    private int pharmacieId;
    private final ObservableList<DetailCommande> detailsCommande = FXCollections.observableArrayList();
    private final FournisseurService fournisseurService = new FournisseurService();
    private final ProduitService produitService = new ProduitService();
    
    /**
     * Initialise la boîte de dialogue
     */
    @FXML
    public void initialize() {
        // Configurer les statuts de commande
        statutCombo.setItems(FXCollections.observableArrayList(
            "En attente", "Validée", "En cours", "Livrée", "Annulée"
        ));
        statutCombo.setValue("En attente");
        
        // Configurer le DatePicker pour afficher un format français
        dateCommandePicker.setValue(LocalDate.now());
        dateCommandePicker.setConverter(new StringConverter<LocalDate>() {
            private DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("dd/MM/yyyy");
            
            @Override
            public String toString(LocalDate date) {
                if (date != null) {
                    return dateFormatter.format(date);
                } else {
                    return "";
                }
            }
            
            @Override
            public LocalDate fromString(String string) {
                if (string != null && !string.isEmpty()) {
                    return LocalDate.parse(string, dateFormatter);
                } else {
                    return null;
                }
            }
        });
        
        // Configurer la ComboBox des fournisseurs
        fournisseurCombo.setConverter(new StringConverter<Fournisseur>() {
            @Override
            public String toString(Fournisseur fournisseur) {
                return fournisseur != null ? fournisseur.getNom() : "";
            }
            
            @Override
            public Fournisseur fromString(String string) {
                return null; // Non utilisé
            }
        });
        
        // Configurer le tableau des produits
        produitsTable.setItems(detailsCommande);
        
        produitNomColumn.setCellValueFactory(cellData -> 
            new SimpleStringProperty(cellData.getValue().getProduit().getNom()));
        
        quantiteColumn.setCellValueFactory(cellData -> 
            new SimpleIntegerProperty(cellData.getValue().getQuantite()).asObject());
        
        prixUnitaireColumn.setCellValueFactory(cellData -> 
            new SimpleDoubleProperty(cellData.getValue().getPrixUnitaire()).asObject());
        
        totalColumn.setCellValueFactory(cellData -> 
            new SimpleDoubleProperty(cellData.getValue().getQuantite() * cellData.getValue().getPrixUnitaire()).asObject());
        
        // Ajouter une colonne d'actions pour supprimer des produits
        actionsColumn.setCellFactory(param -> new TableCell<DetailCommande, Void>() {
            private final Button deleteButton = new Button("Supprimer");
            
            {
                deleteButton.setOnAction(event -> {
                    DetailCommande detail = getTableView().getItems().get(getIndex());
                    detailsCommande.remove(detail);
                    calculerTotal();
                });
            }
            
            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                if (empty) {
                    setGraphic(null);
                } else {
                    setGraphic(deleteButton);
                }
            }
        });
        
        // Ajouter un écouteur pour recalculer le total quand les détails changent
        detailsCommande.addListener((javafx.collections.ListChangeListener.Change<? extends DetailCommande> c) -> {
            calculerTotal();
        });
    }
    
    /**
     * Calcule le total de la commande
     */
    private void calculerTotal() {
        double total = detailsCommande.stream()
            .mapToDouble(detail -> detail.getQuantite() * detail.getPrixUnitaire())
            .sum();
        
        totalCommandeLabel.setText(String.format("%.2f €", total));
    }
    
    /**
     * Charge les fournisseurs pour la pharmacie
     * @param pharmacieId ID de la pharmacie
     */
    public void loadFournisseurs(int pharmacieId) {
        this.pharmacieId = pharmacieId;
        List<Fournisseur> fournisseurs = fournisseurService.getFournisseursByPharmacie(pharmacieId);
        fournisseurCombo.setItems(FXCollections.observableArrayList(fournisseurs));
        
        if (!fournisseurs.isEmpty()) {
            fournisseurCombo.setValue(fournisseurs.get(0));
        }
    }
    
    /**
     * Configure la commande à modifier
     * @param commande Commande à modifier
     */
    public void setCommande(Commande commande) {
        this.commande = commande;
        
        fournisseurCombo.setValue(commande.getFournisseur());
        dateCommandePicker.setValue(commande.getDateCommande());
        statutCombo.setValue(commande.getStatut());
        notesField.setText(commande.getNotes());
        
        detailsCommande.clear();
        detailsCommande.addAll(commande.getDetailsCommande());
        
        calculerTotal();
    }
    
    /**
     * Récupère la commande configurée dans la boîte de dialogue
     * @return Commande configurée
     */
    public Commande getCommande() {
        if (commande == null) {
            commande = new Commande();
        }
        
        commande.setFournisseur(fournisseurCombo.getValue());
        commande.setDateCommande(dateCommandePicker.getValue());
        commande.setStatut(statutCombo.getValue());
        commande.setNotes(notesField.getText());
        commande.setPharmacieId(pharmacieId);
        
        List<DetailCommande> details = new ArrayList<>(detailsCommande);
        commande.setDetailsCommande(details);
        
        double total = details.stream()
            .mapToDouble(detail -> detail.getQuantite() * detail.getPrixUnitaire())
            .sum();
        commande.setMontantTotal(total);
        
        return commande;
    }
    
    /**
     * Gère l'ajout d'un produit à la commande
     */
    @FXML
    public void handleAddProduit() {
        try {
            // Charger la liste des produits
            List<Produit> produits = produitService.getProduitsByPharmacie(pharmacieId);
            
            if (produits.isEmpty()) {
                AlertUtils.showWarningAlert("Attention", "Aucun produit disponible", 
                    "Veuillez d'abord ajouter des produits pour pouvoir créer une commande.");
                return;
            }
            
            // Créer une boîte de dialogue pour sélectionner un produit
            Dialog<DetailCommande> dialog = new Dialog<>();
            dialog.setTitle("Ajouter un produit");
            dialog.setHeaderText("Sélectionnez un produit et sa quantité");
            
            // Ajouter les boutons
            ButtonType confirmButtonType = new ButtonType("Ajouter", ButtonBar.ButtonData.OK_DONE);
            dialog.getDialogPane().getButtonTypes().addAll(confirmButtonType, ButtonType.CANCEL);
            
            // Créer la grille pour les composants
            GridPane grid = new GridPane();
            grid.setHgap(10);
            grid.setVgap(10);
            grid.setPadding(new Insets(20, 150, 10, 10));
            
            // Créer la ComboBox pour les produits
            ComboBox<Produit> produitCombo = new ComboBox<>();
            produitCombo.setItems(FXCollections.observableArrayList(produits));
            produitCombo.setConverter(new StringConverter<Produit>() {
                @Override
                public String toString(Produit produit) {
                    return produit != null ? produit.getNom() : "";
                }
                
                @Override
                public Produit fromString(String string) {
                    return null; // Non utilisé
                }
            });
            
            if (!produits.isEmpty()) {
                produitCombo.setValue(produits.get(0));
            }
            
            Spinner<Integer> quantiteSpinner = new Spinner<>(1, 1000, 1);
            quantiteSpinner.setEditable(true);
            
            TextField prixField = new TextField();
            prixField.setPromptText("Prix unitaire");
            
            // Mettre à jour le prix quand un produit est sélectionné
            produitCombo.valueProperty().addListener((obs, oldVal, newVal) -> {
                if (newVal != null) {
                    prixField.setText(String.valueOf(newVal.getPrixAchat()));
                }
            });
            
            if (produitCombo.getValue() != null) {
                prixField.setText(String.valueOf(produitCombo.getValue().getPrixAchat()));
            }
            
            // Ajouter les composants à la grille
            grid.add(new Label("Produit:"), 0, 0);
            grid.add(produitCombo, 1, 0);
            grid.add(new Label("Quantité:"), 0, 1);
            grid.add(quantiteSpinner, 1, 1);
            grid.add(new Label("Prix unitaire (€):"), 0, 2);
            grid.add(prixField, 1, 2);
            
            dialog.getDialogPane().setContent(grid);
            
            // Activer/désactiver le bouton de confirmation en fonction des entrées
            Node confirmButton = dialog.getDialogPane().lookupButton(confirmButtonType);
            confirmButton.setDisable(produitCombo.getValue() == null);
            
            // Convertir le résultat en DetailCommande quand le bouton est cliqué
            dialog.setResultConverter(dialogButton -> {
                if (dialogButton == confirmButtonType) {
                    Produit produit = produitCombo.getValue();
                    int quantite = quantiteSpinner.getValue();
                    double prix;
                    
                    try {
                        prix = Double.parseDouble(prixField.getText());
                    } catch (NumberFormatException e) {
                        prix = produit.getPrixAchat();
                    }
                    
                    DetailCommande detail = new DetailCommande();
                    detail.setProduit(produit);
                    detail.setQuantite(quantite);
                    detail.setPrixUnitaire(prix);
                    
                    return detail;
                }
                return null;
            });
            
            // Afficher la boîte de dialogue et traiter le résultat
            Optional<DetailCommande> result = dialog.showAndWait();
            
            result.ifPresent(detail -> {
                // Vérifier si le produit existe déjà dans la commande
                Optional<DetailCommande> existingDetail = detailsCommande.stream()
                    .filter(d -> d.getProduit().getId() == detail.getProduit().getId())
                    .findFirst();
                
                if (existingDetail.isPresent()) {
                    // Mettre à jour la quantité du détail existant
                    DetailCommande existing = existingDetail.get();
                    existing.setQuantite(existing.getQuantite() + detail.getQuantite());
                    produitsTable.refresh();
                } else {
                    // Ajouter un nouveau détail
                    detailsCommande.add(detail);
                }
                
                calculerTotal();
            });
            
        } catch (Exception e) {
            AlertUtils.showErrorAlert("Erreur", "Erreur lors de l'ajout d'un produit", 
                "Une erreur s'est produite : " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    /**
     * Valide le formulaire et désactive le bouton OK si le formulaire n'est pas valide
     * @param dialog La boîte de dialogue à valider
     */
    public void configurerValidation(Dialog<?> dialog) {
        Button okButton = (Button) dialog.getDialogPane().lookupButton(ButtonType.OK);
        
        // Désactiver le bouton OK si aucun fournisseur n'est sélectionné ou si aucun produit n'est ajouté
        okButton.setDisable(fournisseurCombo.getValue() == null || detailsCommande.isEmpty());
        
        // Ajouter des écouteurs pour vérifier la validité du formulaire
        fournisseurCombo.valueProperty().addListener((obs, oldVal, newVal) -> {
            okButton.setDisable(newVal == null || detailsCommande.isEmpty());
        });
        
        detailsCommande.addListener((javafx.collections.ListChangeListener.Change<? extends DetailCommande> c) -> {
            okButton.setDisable(fournisseurCombo.getValue() == null || detailsCommande.isEmpty());
        });
    }
}
