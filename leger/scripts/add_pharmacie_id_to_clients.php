<?php
// Script pour ajouter la colonne pharmacie_id à la table clients
error_reporting(E_ALL);
ini_set('display_errors', 1);

// Connexion à la base de données
try {
    // Utiliser directement les valeurs de connexion
    $host = 'localhost';
    $dbname = 'clientlegerlourd';
    $username = 'root';
    $password = '';
    
    $pdo = new PDO(
        "mysql:host={$host};dbname={$dbname}",
        $username,
        $password
    );
    $pdo->setAttribute(PDO::ATTR_ERRMODE, PDO::ERRMODE_EXCEPTION);
    $pdo->setAttribute(PDO::ATTR_DEFAULT_FETCH_MODE, PDO::FETCH_ASSOC);
    $pdo->exec("SET NAMES utf8");
    
    echo "Connexion à la base de données réussie.\n";
} catch(PDOException $e) {
    die("Erreur de connexion à la base de données : " . $e->getMessage() . "\n");
}

// Vérifier si la colonne pharmacie_id existe déjà dans la table clients
try {
    $stmt = $pdo->prepare("SHOW COLUMNS FROM clients LIKE 'pharmacie_id'");
    $stmt->execute();
    $columnExists = $stmt->rowCount() > 0;
    
    echo "Vérification de l'existence de la colonne 'pharmacie_id': " . ($columnExists ? "Existe déjà" : "N'existe pas") . "\n";
    
    if (!$columnExists) {
        // Ajouter la colonne pharmacie_id
        $stmt = $pdo->prepare("ALTER TABLE clients ADD COLUMN pharmacie_id INT NOT NULL DEFAULT 1 AFTER adresse");
        $stmt->execute();
        echo "Colonne 'pharmacie_id' ajoutée avec succès à la table clients.\n";
        
        // Ajouter un index sur la colonne pharmacie_id
        $stmt = $pdo->prepare("ALTER TABLE clients ADD INDEX idx_pharmacie_id (pharmacie_id)");
        $stmt->execute();
        echo "Index ajouté sur la colonne 'pharmacie_id'.\n";
    } else {
        echo "La colonne 'pharmacie_id' existe déjà dans la table clients.\n";
    }
    
    // Vérifier la structure mise à jour de la table clients
    $stmt = $pdo->prepare("DESCRIBE clients");
    $stmt->execute();
    $columns = $stmt->fetchAll();
    
    echo "\nStructure actuelle de la table 'clients':\n";
    foreach ($columns as $column) {
        echo "- {$column['Field']}: {$column['Type']} {$column['Null']} {$column['Key']} {$column['Default']}\n";
    }
} catch(PDOException $e) {
    echo "Erreur lors de la modification de la table: " . $e->getMessage() . "\n";
}

echo "\nOpération terminée.\n";
