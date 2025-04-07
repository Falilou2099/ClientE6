package com.gestionpharma.controllers;

import com.gestionpharma.models.Fournisseur;
import javafx.fxml.FXML;
import javafx.scene.control.*;

/**
 * Contrôleur pour la boîte de dialogue d'ajout/modification de fournisseur
 */
public class FournisseurDialogController {
    
    @FXML private TextField nomField;
    @FXML private TextArea adresseField;
    @FXML private TextField telephoneField;
    @FXML private TextField emailField;
    @FXML private TextField siretField;
    
    private Fournisseur fournisseur;
    
    /**
     * Initialise la boîte de dialogue
     */
    @FXML
    public void initialize() {
        // Configurer la validation pour le champ téléphone
        telephoneField.textProperty().addListener((observable, oldValue, newValue) -> {
            if (!newValue.matches("\\d*")) {
                telephoneField.setText(newValue.replaceAll("[^\\d]", ""));
            }
        });
        
        // Configurer la validation pour le champ SIRET
        siretField.textProperty().addListener((observable, oldValue, newValue) -> {
            if (!newValue.matches("\\d*") || newValue.length() > 14) {
                if (newValue.length() > 14) {
                    siretField.setText(oldValue);
                } else {
                    siretField.setText(newValue.replaceAll("[^\\d]", ""));
                }
            }
        });
    }
    
    /**
     * Configure le fournisseur à modifier
     * @param fournisseur Fournisseur à modifier
     */
    public void setFournisseur(Fournisseur fournisseur) {
        this.fournisseur = fournisseur;
        
        nomField.setText(fournisseur.getNom());
        adresseField.setText(fournisseur.getAdresse());
        telephoneField.setText(fournisseur.getTelephone());
        emailField.setText(fournisseur.getEmail());
        siretField.setText(fournisseur.getSiret());
    }
    
    /**
     * Récupère le fournisseur configuré dans la boîte de dialogue
     * @return Fournisseur configuré
     */
    public Fournisseur getFournisseur() {
        if (fournisseur == null) {
            fournisseur = new Fournisseur();
        }
        
        fournisseur.setNom(nomField.getText());
        fournisseur.setAdresse(adresseField.getText());
        fournisseur.setTelephone(telephoneField.getText());
        fournisseur.setEmail(emailField.getText());
        fournisseur.setSiret(siretField.getText());
        
        return fournisseur;
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
