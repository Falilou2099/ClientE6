<?php
// Script pour forcer l'utilisation de catégories codées en dur dans l'application Java
// Cette approche est brutale mais efficace

// Connexion à la base de données
$servername = "localhost";
$username = "root";
$password = "";
$dbname = "bigpharma";

try {
    $conn = new PDO("mysql:host=$servername;dbname=$dbname", $username, $password);
    $conn->setAttribute(PDO::ATTR_ERRMODE, PDO::ERRMODE_EXCEPTION);
    
    echo "<h2>Solution finale pour les catégories Java (approche directe)</h2>";
    
    // Localiser le fichier Java
    $javaFile = "C:/Users/toure/Desktop/BTS SIO/BTS-SIO-SLAM-1/Client leger lourd/lourd/src/main/java/com/gestionpharma/controllers/ProduitDialogController.java";
    
    if (file_exists($javaFile)) {
        // Lire le fichier
        $javaContent = file_get_contents($javaFile);
        
        // Créer une sauvegarde
        file_put_contents($javaFile . ".backup", $javaContent);
        echo "<p style='color:green;'>Sauvegarde du fichier Java créée: " . $javaFile . ".backup</p>";
        
        // Remplacer la méthode initialize par une version qui utilise des catégories codées en dur
        $initializePattern = '/public void initialize\(\) \{.*?categorieCombo\.setItems\(.*?\);/s';
        $initializeReplacement = 'public void initialize() {
        // Utiliser des catégories codées en dur (solution directe)
        List<String> categories = Arrays.asList(
            "Analgésiques", "Anti-inflammatoires", "Antibiotiques", 
            "Antihistaminiques", "Gastro-entérologie", "Dermatologie",
            "Cardiologie", "Vitamines", "Compléments alimentaires", "Autres"
        );
        categorieCombo.setItems(FXCollections.observableArrayList(categories));';
        
        $newJavaContent = preg_replace($initializePattern, $initializeReplacement, $javaContent);
        
        // Écrire le nouveau contenu dans le fichier
        if ($newJavaContent != $javaContent) {
            file_put_contents($javaFile, $newJavaContent);
            echo "<p style='color:green;'>Fichier Java modifié avec succès pour utiliser des catégories codées en dur.</p>";
        } else {
            echo "<p style='color:red;'>Erreur: Impossible de modifier le fichier Java. Le pattern n'a pas été trouvé.</p>";
            
            // Tentative plus directe
            $importPattern = '/import java\.util\.List;/';
            $importReplacement = 'import java.util.List;
import java.util.Arrays;';
            
            $newJavaContent = preg_replace($importPattern, $importReplacement, $javaContent);
            
            // Rechercher la méthode initialize
            if (preg_match('/@FXML\s+public void initialize\(\) \{/s', $newJavaContent, $matches, PREG_OFFSET_CAPTURE)) {
                $position = $matches[0][1] + strlen($matches[0][0]);
                
                // Insérer le code pour charger les catégories codées en dur
                $categoryCode = '
        // Utiliser des catégories codées en dur (solution directe)
        List<String> categories = Arrays.asList(
            "Analgésiques", "Anti-inflammatoires", "Antibiotiques", 
            "Antihistaminiques", "Gastro-entérologie", "Dermatologie",
            "Cardiologie", "Vitamines", "Compléments alimentaires", "Autres"
        );
        categorieCombo.setItems(FXCollections.observableArrayList(categories));';
                
                $newJavaContent = substr_replace($newJavaContent, $categoryCode, $position, 0);
                
                file_put_contents($javaFile, $newJavaContent);
                echo "<p style='color:green;'>Fichier Java modifié avec succès (méthode alternative).</p>";
            } else {
                echo "<p style='color:red;'>Erreur: Impossible de localiser la méthode initialize.</p>";
                
                // Dernière tentative: réécrire complètement le fichier
                $completeJavaFile = '
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
 * Contrôleur pour la boîte de dialogue d\'ajout/modification de produit
 */
public class ProduitDialogController {
    
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
        // Configurer les catégories par défaut
        List<String> categories = Arrays.asList(
            "Analgésiques", "Anti-inflammatoires", "Antibiotiques", 
            "Antihistaminiques", "Gastro-entérologie", "Dermatologie",
            "Cardiologie", "Vitamines", "Compléments alimentaires", "Autres"
        );
        categorieCombo.setItems(FXCollections.observableArrayList(categories));
        
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
        
        // Configurer les champs numériques pour n\'accepter que des nombres
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
        
        // Configurer le seuil d\'alerte s\'il existe
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
     * Valide le formulaire et désactive le bouton OK si le formulaire n\'est pas valide
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
                ';
                
                file_put_contents($javaFile, $completeJavaFile);
                echo "<p style='color:green;'>Réécriture complète du fichier Java réalisée avec succès.</p>";
            }
        }
    } else {
        echo "<p style='color:red;'>Erreur: Le fichier Java n'a pas été trouvé à l'emplacement spécifié.</p>";
        echo "<p>Emplacement recherché: $javaFile</p>";
    }
    
    echo "<h3>Instructions finales</h3>";
    echo "<ol>";
    echo "<li><strong>Recompilez l'application Java</strong> avec la commande suivante (nécessite Maven):";
    echo "<pre>cd \"C:/Users/toure/Desktop/BTS SIO/BTS-SIO-SLAM-1/Client leger lourd/lourd\"<br>mvn clean install</pre></li>";
    echo "<li><strong>Redémarrez l'application Java</strong></li>";
    echo "<li>Essayez à nouveau d'ajouter un produit - les catégories devraient maintenant apparaître</li>";
    echo "</ol>";
    
} catch(PDOException $e) {
    echo "<h3 style='color:red;'>Erreur</h3>";
    echo "<p>" . $e->getMessage() . "</p>";
}

$conn = null;
?>
