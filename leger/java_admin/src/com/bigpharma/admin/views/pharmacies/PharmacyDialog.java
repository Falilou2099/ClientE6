package com.bigpharma.admin.views.pharmacies;

import com.bigpharma.admin.models.Pharmacy;
import com.bigpharma.admin.utils.AlertUtils;
import com.bigpharma.admin.utils.SecurityUtils;
import javafx.geometry.Insets;
import javafx.scene.control.*;
import javafx.scene.layout.GridPane;

/**
 * Boîte de dialogue pour ajouter ou modifier une pharmacie
 */
public class PharmacyDialog extends Dialog<Pharmacy> {
    
    private final Pharmacy pharmacy;
    private final boolean isNewPharmacy;
    
    // Champs du formulaire
    private TextField nameField;
    private TextArea addressArea;
    private TextField phoneField;
    private TextField emailField;
    private TextArea notesArea;
    
    /**
     * Constructeur
     * @param pharmacy La pharmacie à éditer ou une nouvelle pharmacie
     * @param isNewPharmacy true si c'est une nouvelle pharmacie, false sinon
     */
    public PharmacyDialog(Pharmacy pharmacy, boolean isNewPharmacy) {
        this.pharmacy = pharmacy;
        this.isNewPharmacy = isNewPharmacy;
        
        // Configurer la boîte de dialogue
        setTitle(isNewPharmacy ? "Ajouter une pharmacie" : "Modifier la pharmacie");
        setHeaderText(isNewPharmacy ? "Créer une nouvelle pharmacie" : "Modifier la pharmacie: " + pharmacy.getNom());
        
        // Créer le contenu
        createContent();
        
        // Configurer les boutons
        ButtonType saveButtonType = new ButtonType("Enregistrer", ButtonBar.ButtonData.OK_DONE);
        ButtonType cancelButtonType = new ButtonType("Annuler", ButtonBar.ButtonData.CANCEL_CLOSE);
        getDialogPane().getButtonTypes().addAll(saveButtonType, cancelButtonType);
        
        // Définir le convertisseur de résultat
        setResultConverter(buttonType -> {
            if (buttonType == saveButtonType) {
                return savePharmacy();
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
        
        // Nom de la pharmacie
        grid.add(new Label("Nom:"), 0, 0);
        nameField = new TextField();
        nameField.setPromptText("Nom de la pharmacie");
        nameField.setText(pharmacy.getNom() != null ? pharmacy.getNom() : "");
        grid.add(nameField, 1, 0);
        
        // Adresse
        grid.add(new Label("Adresse:"), 0, 1);
        addressArea = new TextArea();
        addressArea.setPromptText("Adresse de la pharmacie");
        addressArea.setText(pharmacy.getAdresse() != null ? pharmacy.getAdresse() : "");
        addressArea.setPrefRowCount(3);
        grid.add(addressArea, 1, 1);
        
        // Téléphone
        grid.add(new Label("Téléphone:"), 0, 2);
        phoneField = new TextField();
        phoneField.setPromptText("Numéro de téléphone");
        phoneField.setText(pharmacy.getTelephone() != null ? pharmacy.getTelephone() : "");
        grid.add(phoneField, 1, 2);
        
        // Email
        grid.add(new Label("Email:"), 0, 3);
        emailField = new TextField();
        emailField.setPromptText("Adresse email");
        emailField.setText(pharmacy.getEmail() != null ? pharmacy.getEmail() : "");
        grid.add(emailField, 1, 3);
        
        // Notes
        grid.add(new Label("Notes:"), 0, 4);
        notesArea = new TextArea();
        notesArea.setPromptText("Notes supplémentaires");
        notesArea.setText(pharmacy.getNotes() != null ? pharmacy.getNotes() : "");
        notesArea.setPrefRowCount(3);
        grid.add(notesArea, 1, 4);
        
        // Ajouter la grille au panneau de la boîte de dialogue
        getDialogPane().setContent(grid);
    }
    
    /**
     * Enregistre la pharmacie avec les valeurs du formulaire
     * @return La pharmacie enregistrée
     */
    private Pharmacy savePharmacy() {
        // Valider les champs obligatoires
        if (nameField.getText().isEmpty()) {
            AlertUtils.showWarning("Champ obligatoire", "Le nom de la pharmacie est obligatoire.");
            return null;
        }
        
        if (addressArea.getText().isEmpty()) {
            AlertUtils.showWarning("Champ obligatoire", "L'adresse de la pharmacie est obligatoire.");
            return null;
        }
        
        if (phoneField.getText().isEmpty()) {
            AlertUtils.showWarning("Champ obligatoire", "Le numéro de téléphone est obligatoire.");
            return null;
        }
        
        // Valider l'email s'il est renseigné
        if (!emailField.getText().isEmpty() && !SecurityUtils.isValidEmail(emailField.getText())) {
            AlertUtils.showWarning("Email invalide", "Veuillez saisir une adresse email valide.");
            return null;
        }
        
        try {
            // Mettre à jour la pharmacie avec les valeurs du formulaire
            pharmacy.setNom(nameField.getText());
            pharmacy.setAdresse(addressArea.getText());
            pharmacy.setTelephone(phoneField.getText());
            pharmacy.setEmail(emailField.getText());
            pharmacy.setNotes(notesArea.getText());
            
            return pharmacy;
            
        } catch (Exception e) {
            AlertUtils.showError("Erreur", 
                    "Une erreur est survenue lors de l'enregistrement de la pharmacie: " + e.getMessage());
            return null;
        }
    }
}
