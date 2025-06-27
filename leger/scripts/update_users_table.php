<?php
// Script pour mettre à jour la table users en ajoutant le champ app_access

// Charger la configuration de la base de données
require_once __DIR__ . '/../config/database.php';

try {
    // Vérifier si la table users existe
    $checkTableQuery = "SHOW TABLES LIKE 'users'"; 
    $stmt = $pdo->prepare($checkTableQuery);
    $stmt->execute();
    
    if ($stmt->rowCount() == 0) {
        // La table n'existe pas, on la crée
        $createTableQuery = "CREATE TABLE users (
            id INT AUTO_INCREMENT PRIMARY KEY,
            email VARCHAR(255) NOT NULL UNIQUE,
            password VARCHAR(255) NOT NULL,
            pharmacy_id INT,
            role ENUM('admin', 'pharmacist', 'manager') NOT NULL DEFAULT 'pharmacist',
            status ENUM('active', 'inactive', 'pending') NOT NULL DEFAULT 'active',
            created_at DATETIME NOT NULL,
            last_login DATETIME,
            app_access ENUM('both', 'light', 'heavy') NOT NULL DEFAULT 'both'
        ) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci";
        
        $pdo->exec($createTableQuery);
        echo "La table users a été créée avec succès.\n";
    } else {
    // Vérifier si la colonne app_access existe déjà
    $checkColumnQuery = "SHOW COLUMNS FROM users LIKE 'app_access'";
    $stmt = $pdo->prepare($checkColumnQuery);
    $stmt->execute();
    
    if ($stmt->rowCount() == 0) {
        // La colonne n'existe pas, on l'ajoute
        $alterTableQuery = "ALTER TABLE users ADD COLUMN app_access ENUM('both', 'light', 'heavy') NOT NULL DEFAULT 'both' AFTER last_login";
        $pdo->exec($alterTableQuery);
        echo "La colonne app_access a été ajoutée avec succès à la table users.\n";
        
        // Mettre à jour tous les utilisateurs existants pour qu'ils aient accès aux deux applications
        $updateUsersQuery = "UPDATE users SET app_access = 'both'";
        $pdo->exec($updateUsersQuery);
        echo "Tous les utilisateurs existants ont été mis à jour pour avoir accès aux deux applications.\n";
    } else {
        echo "La colonne app_access existe déjà dans la table users.\n";
    }
    }
    
    echo "Mise à jour de la table users terminée avec succès.\n";
    
} catch (PDOException $e) {
    echo "Erreur lors de la mise à jour de la table users : " . $e->getMessage() . "\n";
}
?>
