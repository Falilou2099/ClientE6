<?php
// Script pour ajouter les colonnes manquantes à la table produits
require_once __DIR__ . '/config/database.php';

try {
    // Vérifier si la colonne est_ordonnance existe déjà
    $stmt = $pdo->query("SHOW COLUMNS FROM produits LIKE 'est_ordonnance'");
    $columnExists = $stmt->rowCount() > 0;
    
    if (!$columnExists) {
        // Ajouter la colonne est_ordonnance
        $pdo->exec("ALTER TABLE produits ADD COLUMN est_ordonnance BOOLEAN DEFAULT FALSE");
        echo "La colonne 'est_ordonnance' a été ajoutée à la table 'produits'.<br>";
    } else {
        echo "La colonne 'est_ordonnance' existe déjà dans la table 'produits'.<br>";
    }
    
    // Vérifier si la colonne image existe déjà
    $stmt = $pdo->query("SHOW COLUMNS FROM produits LIKE 'image'");
    $columnExists = $stmt->rowCount() > 0;
    
    if (!$columnExists) {
        // Ajouter la colonne image
        $pdo->exec("ALTER TABLE produits ADD COLUMN image VARCHAR(255) DEFAULT NULL");
        echo "La colonne 'image' a été ajoutée à la table 'produits'.<br>";
    } else {
        echo "La colonne 'image' existe déjà dans la table 'produits'.<br>";
    }
    
    // Vérifier si les colonnes date_ajout et date_modification existent déjà
    $stmt = $pdo->query("SHOW COLUMNS FROM produits LIKE 'date_ajout'");
    $columnExists = $stmt->rowCount() > 0;
    
    if (!$columnExists) {
        // Ajouter les colonnes date_ajout et date_modification
        $pdo->exec("ALTER TABLE produits ADD COLUMN date_ajout DATETIME DEFAULT CURRENT_TIMESTAMP");
        echo "La colonne 'date_ajout' a été ajoutée à la table 'produits'.<br>";
    } else {
        echo "La colonne 'date_ajout' existe déjà dans la table 'produits'.<br>";
    }
    
    $stmt = $pdo->query("SHOW COLUMNS FROM produits LIKE 'date_modification'");
    $columnExists = $stmt->rowCount() > 0;
    
    if (!$columnExists) {
        $pdo->exec("ALTER TABLE produits ADD COLUMN date_modification DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP");
        echo "La colonne 'date_modification' a été ajoutée à la table 'produits'.<br>";
    } else {
        echo "La colonne 'date_modification' existe déjà dans la table 'produits'.<br>";
    }
    
    // Mettre à jour quelques produits pour qu'ils soient sur ordonnance
    $pdo->exec("UPDATE produits SET est_ordonnance = 1 WHERE nom LIKE '%Amoxicilline%' OR nom LIKE '%Morphine%' OR nom LIKE '%Codéine%'");
    echo "Les produits sur ordonnance ont été mis à jour.<br>";
    
    echo "Toutes les modifications ont été effectuées avec succès.";
} catch (PDOException $e) {
    echo "Erreur : " . $e->getMessage();
}
