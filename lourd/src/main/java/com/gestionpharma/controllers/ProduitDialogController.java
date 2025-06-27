
package com.gestionpharma.controllers;

import com.gestionpharma.models.Produit;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.util.StringConverter;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;
import java.util.List;

/**
 * Contrôleur pour la boîte de dialogue d'ajout/modification de produit
 */
public class ProduitDialogController {
    public ProduitDialogController() {
        System.out.println("[DEBUG] Constructeur ProduitDialogController appelé");
    }
    
    @FXML private TextField nomField;
    @FXML private TextArea descriptionField;
    @FXML private TextField prixAchatField;
    @FXML private TextField prixVenteField;
    @FXML private ComboBox<String> categorieCombo;
    @FXML private TextField quantiteField;
    @FXML private TextField seuilAlerteField;
    @FXML private DatePicker dateExpirationPicker;
    
    private Produit produit;
    
    /**
     * Initialise la boîte de dialogue
     */
    @FXML
    public void initialize() {
        System.out.println("[DEBUG] Méthode initialize appelée");
        // Configurer les catégories par défaut
        List<String> categories = Arrays.asList(
            "Analgésiques", "Anti-inflammatoires", "Antibiotiques", 
            "Antihistaminiques", "Gastro-entérologie", "Dermatologie",
            "Cardiologie", "Vitamines", "Compléments alimentaires", "Autres"
        );
        System.out.println("[DEBUG] categorieCombo = " + categorieCombo);
        categorieCombo.setItems(FXCollections.observableArrayList(categories));
        System.out.println("[DEBUG] Catégories injectées : " + categories);
        
        // Configurer le DatePicker pour afficher un format français
        dateExpirationPicker.setConverter(new StringConverter<LocalDate>() {
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
        
        // Configurer les champs numériques pour n'accepter que des nombres
        prixAchatField.textProperty().addListener((observable, oldValue, newValue) -> {
            if (!newValue.matches("\\d*(\\.\\d*)?")) {
                prixAchatField.setText(oldValue);
            }
        });
        
        prixVenteField.textProperty().addListener((observable, oldValue, newValue) -> {
            if (!newValue.matches("\\d*(\\.\\d*)?")) {
                prixVenteField.setText(oldValue);
            }
        });
        
        quantiteField.textProperty().addListener((observable, oldValue, newValue) -> {
            if (!newValue.matches("\\d*")) {
                quantiteField.setText(oldValue);
            }
        });
        
        seuilAlerteField.textProperty().addListener((observable, oldValue, newValue) -> {
            if (!newValue.matches("\\d*")) {
                seuilAlerteField.setText(oldValue);
            }
        });
    }
    
    /**
     * Configure le produit à modifier
     * @param produit Produit à modifier
     */
    public void setProduit(Produit produit) {
        this.produit = produit;
        
        nomField.setText(produit.getNom());
        descriptionField.setText(produit.getDescription());
        prixAchatField.setText(String.valueOf(produit.getPrixAchat()));
        prixVenteField.setText(String.valueOf(produit.getPrixVente()));
        categorieCombo.setValue(produit.getCategorie());
        quantiteField.setText(String.valueOf(produit.getQuantiteStock()));
        
        // Configurer le seuil d'alerte s'il existe
        try {
            int seuilAlerte = produit.getSeuilAlerte();
            seuilAlerteField.setText(String.valueOf(seuilAlerte));
        } catch (Exception e) {
            seuilAlerteField.setText("10"); // Valeur par défaut
        }
        
        dateExpirationPicker.setValue(produit.getDateExpiration());
    }
    
    /**
     * Récupère le produit configuré dans la boîte de dialogue
     * @return Produit configuré
     */
    public Produit getProduit() {
        if (produit == null) {
            produit = new Produit();
        }
        
        produit.setNom(nomField.getText());
        produit.setDescription(descriptionField.getText());
        
        try {
            produit.setPrixAchat(Double.parseDouble(prixAchatField.getText()));
        } catch (NumberFormatException e) {
            produit.setPrixAchat(0.0);
        }
        
        try {
            produit.setPrixVente(Double.parseDouble(prixVenteField.getText()));
        } catch (NumberFormatException e) {
            produit.setPrixVente(0.0);
        }
        
        produit.setCategorie(categorieCombo.getValue());
        
        try {
            produit.setQuantiteStock(Integer.parseInt(quantiteField.getText()));
        } catch (NumberFormatException e) {
            produit.setQuantiteStock(0);
        }
        
        try {
            produit.setSeuilAlerte(Integer.parseInt(seuilAlerteField.getText()));
        } catch (NumberFormatException e) {
            produit.setSeuilAlerte(10); // Valeur par défaut
        }
        
        produit.setDateExpiration(dateExpirationPicker.getValue());
        
        return produit;
    }
    
    /**
     * Valide le formulaire et désactive le bouton OK si le formulaire n'est pas valide
     * @param dialog La boîte de dialogue à valider
     */
    public void configurerValidation(Dialog<?> dialog) {
        Button okButton = (Button) dialog.getDialogPane().lookupButton(ButtonType.OK);
        
        // Désactiver le bouton OK si les champs obligatoires sont vides
        okButton.setDisable(true);
        
        // Ajouter un écouteur pour vérifier la validité du formulaire
        nomField.textProperty().addListener((observable, oldValue, newValue) -> {
            okButton.setDisable(newValue.trim().isEmpty());
        });
    }
}
                