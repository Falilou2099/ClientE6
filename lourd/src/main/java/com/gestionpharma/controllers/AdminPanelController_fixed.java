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
 * Méthodes de gestion des fournisseurs, produits et commandes
 * à copier dans le fichier AdminPanelController.java
 */
public class AdminPanelController_fixed {

    /**
     * Méthode utilitaire pour valider le formulaire de fournisseur
     */
    private void validateSupplierForm(Node saveButton, TextField nomField, TextField contactField, 
                                     TextField telephoneField, TextField emailField, ComboBox<String> typeField) {
        boolean isValid = !nomField.getText().trim().isEmpty() && 
                         !contactField.getText().trim().isEmpty() && 
                         !telephoneField.getText().trim().isEmpty() && 
                         !emailField.getText().trim().isEmpty() && 
                         typeField.getValue() != null;
        saveButton.setDisable(!isValid);
    }
    
    /**
     * Gère l'ajout d'un nouveau fournisseur
     */
    @FXML
    protected void handleAddSupplier() {
        try {
            // Créer un nouveau fournisseur vide
            Fournisseur nouveauFournisseur = new Fournisseur();
            
            // Créer la boîte de dialogue
            Dialog<ButtonType> dialog = new Dialog<>();
            dialog.setTitle("Gestion des Fournisseurs Pharmaceutiques");
            dialog.setHeaderText("Informations du Fournisseur");
            
            // Définir les boutons
            ButtonType saveButtonType = new ButtonType("Ajouter", ButtonBar.ButtonData.OK_DONE);
            dialog.getDialogPane().getButtonTypes().addAll(saveButtonType, ButtonType.CANCEL);
            
            // Créer la grille pour les champs
            GridPane grid = new GridPane();
            grid.setHgap(10);
            grid.setVgap(10);
            grid.setPadding(new Insets(20, 150, 10, 10));
            
            // Créer les champs
            TextField idField = new TextField();
            idField.setPromptText("Auto-généré");
            idField.setDisable(true);
            
            TextField nomField = new TextField();
            nomField.setPromptText("Nom du fournisseur");
            
            TextField contactField = new TextField();
            contactField.setPromptText("Personne contact");
            
            TextField telephoneField = new TextField();
            telephoneField.setPromptText("Numéro de téléphone");
            
            TextField emailField = new TextField();
            emailField.setPromptText("Adresse email");
            
            ComboBox<String> typeMedicamentsField = new ComboBox<>();
            typeMedicamentsField.getItems().addAll("Antibiotiques", "Analgésiques", "Anti-inflammatoires", "Cardiovasculaires", "Dermatologiques", "Autres");
            typeMedicamentsField.setPromptText("Type de médicaments");
            
            // Ajouter les champs à la grille
            grid.add(new Label("ID (auto-généré) :"), 0, 0);
            grid.add(idField, 1, 0);
            grid.add(new Label("Nom du Fournisseur *:"), 0, 1);
            grid.add(nomField, 1, 1);
            grid.add(new Label("Personne Contact *:"), 0, 2);
            grid.add(contactField, 1, 2);
            grid.add(new Label("Téléphone *:"), 0, 3);
            grid.add(telephoneField, 1, 3);
            grid.add(new Label("Email *:"), 0, 4);
            grid.add(emailField, 1, 4);
            grid.add(new Label("Type de Médicaments *:"), 0, 5);
            grid.add(typeMedicamentsField, 1, 5);
            
            dialog.getDialogPane().setContent(grid);
            
            // Activer/Désactiver le bouton Enregistrer selon la validation des champs
            Node saveButton = dialog.getDialogPane().lookupButton(saveButtonType);
            saveButton.setDisable(true);
            
            // Valider les champs obligatoires
            nomField.textProperty().addListener((observable, oldValue, newValue) -> {
                validateSupplierForm(saveButton, nomField, contactField, telephoneField, emailField, typeMedicamentsField);
            });
            
            contactField.textProperty().addListener((observable, oldValue, newValue) -> {
                validateSupplierForm(saveButton, nomField, contactField, telephoneField, emailField, typeMedicamentsField);
            });
            
            telephoneField.textProperty().addListener((observable, oldValue, newValue) -> {
                validateSupplierForm(saveButton, nomField, contactField, telephoneField, emailField, typeMedicamentsField);
            });
            
            emailField.textProperty().addListener((observable, oldValue, newValue) -> {
                validateSupplierForm(saveButton, nomField, contactField, telephoneField, emailField, typeMedicamentsField);
            });
            
            typeMedicamentsField.getSelectionModel().selectedItemProperty().addListener((observable, oldValue, newValue) -> {
                validateSupplierForm(saveButton, nomField, contactField, telephoneField, emailField, typeMedicamentsField);
            });
            
            // Afficher la boite de dialogue et attendre la réponse
            Optional<ButtonType> result = dialog.showAndWait();
            
            // Traiter la réponse
            if (result.isPresent() && result.get() == saveButtonType) {
                // Récupérer les valeurs des champs
                nouveauFournisseur.setNom(nomField.getText().trim());
                nouveauFournisseur.setAdresse(contactField.getText().trim()); // On utilise le champ adresse pour stocker la personne contact
                nouveauFournisseur.setTelephone(telephoneField.getText().trim());
                nouveauFournisseur.setEmail(emailField.getText().trim());
                nouveauFournisseur.setSiret(typeMedicamentsField.getValue()); // On utilise le champ SIRET pour stocker le type de médicaments
                
                boolean success = fournisseurService.ajouterFournisseur(nouveauFournisseur, pharmacieId);
                
                if (success) {
                    // Enregistrer l'activité
                    activiteService.ajouterActivite("Fournisseur", "Ajout du fournisseur " + nouveauFournisseur.getNom(), 
                            currentAdmin.getPrenom() + " " + currentAdmin.getNom(), pharmacieId);
                    
                    // Recharger les données
                    loadSuppliersData();
                    
                    AlertUtils.showInfoAlert("Succès", "Fournisseur ajouté", 
                            "Le fournisseur a été ajouté avec succès.");
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
            AlertUtils.showErrorAlert("Erreur", "Erreur lors de l'ajout", 
                    "Impossible d'ajouter le fournisseur: " + e.getMessage());
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
            
            // Créer la boîte de dialogue
            Dialog<ButtonType> dialog = new Dialog<>();
            dialog.setTitle("Gestion des Fournisseurs Pharmaceutiques");
            dialog.setHeaderText("Modifier les informations du Fournisseur");
            
            // Définir les boutons
            ButtonType saveButtonType = new ButtonType("Modifier", ButtonBar.ButtonData.OK_DONE);
            dialog.getDialogPane().getButtonTypes().addAll(saveButtonType, ButtonType.CANCEL);
            
            // Créer la grille pour les champs
            GridPane grid = new GridPane();
            grid.setHgap(10);
            grid.setVgap(10);
            grid.setPadding(new Insets(20, 150, 10, 10));
            
            // Créer les champs avec les valeurs actuelles
            TextField idField = new TextField(String.valueOf(selectedSupplier.getId()));
            idField.setDisable(true);
            
            TextField nomField = new TextField(selectedSupplier.getNom());
            TextField contactField = new TextField(selectedSupplier.getAdresse()); // Utilise adresse pour personne contact
            TextField telephoneField = new TextField(selectedSupplier.getTelephone());
            TextField emailField = new TextField(selectedSupplier.getEmail());
            
            ComboBox<String> typeMedicamentsField = new ComboBox<>();
            typeMedicamentsField.getItems().addAll("Antibiotiques", "Analgésiques", "Anti-inflammatoires", "Cardiovasculaires", "Dermatologiques", "Autres");
            typeMedicamentsField.setValue(selectedSupplier.getSiret()); // Utilise SIRET pour type de médicaments
            
            // Ajouter les champs à la grille
            grid.add(new Label("ID :"), 0, 0);
            grid.add(idField, 1, 0);
            grid.add(new Label("Nom du Fournisseur *:"), 0, 1);
            grid.add(nomField, 1, 1);
            grid.add(new Label("Personne Contact *:"), 0, 2);
            grid.add(contactField, 1, 2);
            grid.add(new Label("Téléphone *:"), 0, 3);
            grid.add(telephoneField, 1, 3);
            grid.add(new Label("Email *:"), 0, 4);
            grid.add(emailField, 1, 4);
            grid.add(new Label("Type de Médicaments *:"), 0, 5);
            grid.add(typeMedicamentsField, 1, 5);
            
            dialog.getDialogPane().setContent(grid);
            
            // Activer/Désactiver le bouton Enregistrer selon la validation des champs
            Node saveButton = dialog.getDialogPane().lookupButton(saveButtonType);
            
            // Valider les champs obligatoires
            nomField.textProperty().addListener((observable, oldValue, newValue) -> {
                validateSupplierForm(saveButton, nomField, contactField, telephoneField, emailField, typeMedicamentsField);
            });
            
            contactField.textProperty().addListener((observable, oldValue, newValue) -> {
                validateSupplierForm(saveButton, nomField, contactField, telephoneField, emailField, typeMedicamentsField);
            });
            
            telephoneField.textProperty().addListener((observable, oldValue, newValue) -> {
                validateSupplierForm(saveButton, nomField, contactField, telephoneField, emailField, typeMedicamentsField);
            });
            
            emailField.textProperty().addListener((observable, oldValue, newValue) -> {
                validateSupplierForm(saveButton, nomField, contactField, telephoneField, emailField, typeMedicamentsField);
            });
            
            typeMedicamentsField.getSelectionModel().selectedItemProperty().addListener((observable, oldValue, newValue) -> {
                validateSupplierForm(saveButton, nomField, contactField, telephoneField, emailField, typeMedicamentsField);
            });
            
            // Validation initiale
            validateSupplierForm(saveButton, nomField, contactField, telephoneField, emailField, typeMedicamentsField);
            
            // Afficher la boite de dialogue et attendre la réponse
            Optional<ButtonType> result = dialog.showAndWait();
            
            // Traiter la réponse
            if (result.isPresent() && result.get() == saveButtonType) {
                // Récupérer les valeurs des champs
                selectedSupplier.setNom(nomField.getText().trim());
                selectedSupplier.setAdresse(contactField.getText().trim());
                selectedSupplier.setTelephone(telephoneField.getText().trim());
                selectedSupplier.setEmail(emailField.getText().trim());
                selectedSupplier.setSiret(typeMedicamentsField.getValue());
                
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
            }
        } catch (Exception e) {
            e.printStackTrace();
            AlertUtils.showErrorAlert("Erreur", "Erreur lors de la modification", 
                    "Impossible de modifier le fournisseur: " + e.getMessage());
        }
    }
    
    /**
     * Gère la création d'une nouvelle commande
     */
    @FXML
    protected void handleCreateOrder() {
        try {
            // Vérifier qu'un fournisseur est sélectionné
            Fournisseur selectedSupplier = suppliersTable.getSelectionModel().getSelectedItem();
            if (selectedSupplier == null) {
                AlertUtils.showWarningAlert("Attention", "Aucun fournisseur sélectionné", 
                        "Veuillez sélectionner un fournisseur pour créer une commande.");
                return;
            }
            
            // Créer la boîte de dialogue
            Dialog<ButtonType> dialog = new Dialog<>();
            dialog.setTitle("Création d'une commande");
            dialog.setHeaderText("Nouvelle commande pour " + selectedSupplier.getNom());
            
            // Définir les boutons
            ButtonType saveButtonType = new ButtonType("Créer", ButtonBar.ButtonData.OK_DONE);
            dialog.getDialogPane().getButtonTypes().addAll(saveButtonType, ButtonType.CANCEL);
            
            // Créer la grille pour les champs
            GridPane grid = new GridPane();
            grid.setHgap(10);
            grid.setVgap(10);
            grid.setPadding(new Insets(20, 150, 10, 10));
            
            // Créer les champs
            DatePicker dateLivraisonField = new DatePicker(LocalDate.now().plusDays(7));
            
            TextField montantField = new TextField();
            montantField.setPromptText("Montant total");
            
            ComboBox<String> statutField = new ComboBox<>();
            statutField.getItems().addAll("En attente", "En cours", "Livrée", "Annulée");
            statutField.setValue("En attente");
            
            TextArea notesField = new TextArea();
            notesField.setPromptText("Notes sur la commande");
            notesField.setPrefRowCount(3);
            
            // Ajouter les champs à la grille
            grid.add(new Label("Fournisseur:"), 0, 0);
            grid.add(new Label(selectedSupplier.getNom()), 1, 0);
            grid.add(new Label("Date de livraison prévue *:"), 0, 1);
            grid.add(dateLivraisonField, 1, 1);
            grid.add(new Label("Montant total *:"), 0, 2);
            grid.add(montantField, 1, 2);
            grid.add(new Label("Statut *:"), 0, 3);
            grid.add(statutField, 1, 3);
            grid.add(new Label("Notes:"), 0, 4);
            grid.add(notesField, 1, 4);
            
            dialog.getDialogPane().setContent(grid);
            
            // Activer/Désactiver le bouton Enregistrer selon la validation des champs
            Node saveButton = dialog.getDialogPane().lookupButton(saveButtonType);
            saveButton.setDisable(true);
            
            // Valider les champs obligatoires
            montantField.textProperty().addListener((observable, oldValue, newValue) -> {
                boolean isValid = dateLivraisonField.getValue() != null && 
                                 !montantField.getText().trim().isEmpty() && 
                                 statutField.getValue() != null;
                saveButton.setDisable(!isValid);
            });
            
            dateLivraisonField.valueProperty().addListener((observable, oldValue, newValue) -> {
                boolean isValid = dateLivraisonField.getValue() != null && 
                                 !montantField.getText().trim().isEmpty() && 
                                 statutField.getValue() != null;
                saveButton.setDisable(!isValid);
            });
            
            statutField.valueProperty().addListener((observable, oldValue, newValue) -> {
                boolean isValid = dateLivraisonField.getValue() != null && 
                                 !montantField.getText().trim().isEmpty() && 
                                 statutField.getValue() != null;
                saveButton.setDisable(!isValid);
            });
            
            // Afficher la boite de dialogue et attendre la réponse
            Optional<ButtonType> result = dialog.showAndWait();
            
            // Traiter la réponse
            if (result.isPresent() && result.get() == saveButtonType) {
                // Créer la nouvelle commande
                Commande nouvelleCommande = new Commande();
                nouvelleCommande.setFournisseurId(selectedSupplier.getId());
                nouvelleCommande.setPharmacieId(pharmacieId);
                nouvelleCommande.setDateCommande(LocalDate.now());
                nouvelleCommande.setDateLivraison(dateLivraisonField.getValue());
                
                try {
                    nouvelleCommande.setMontantTotal(Double.parseDouble(montantField.getText().trim()));
                } catch (NumberFormatException e) {
                    AlertUtils.showErrorAlert("Erreur", "Format incorrect", 
                            "Veuillez entrer un montant valide.");
                    return;
                }
                
                nouvelleCommande.setStatut(statutField.getValue());
                nouvelleCommande.setNotes(notesField.getText().trim());
                
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
            }
        } catch (Exception e) {
            e.printStackTrace();
            AlertUtils.showErrorAlert("Erreur", "Erreur lors de la création", 
                    "Impossible de créer la commande: " + e.getMessage());
        }
    }
}
