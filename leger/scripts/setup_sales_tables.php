<?php
// Script pour créer les tables nécessaires pour la fonctionnalité de vente

// Charger la configuration de la base de données
require_once __DIR__ . '/../config/database.php';

try {
    // Vérifier si la table clients existe
    $checkTableQuery = "SHOW TABLES LIKE 'clients'"; 
    $stmt = $pdo->prepare($checkTableQuery);
    $stmt->execute();
    
    if ($stmt->rowCount() == 0) {
        // La table n'existe pas, on la crée
        $createTableQuery = "CREATE TABLE clients (
            id INT AUTO_INCREMENT PRIMARY KEY,
            nom VARCHAR(100) NOT NULL,
            prenom VARCHAR(100) NOT NULL,
            email VARCHAR(255),
            telephone VARCHAR(20),
            adresse TEXT,
            pharmacie_id INT,
            date_creation DATETIME NOT NULL,
            FOREIGN KEY (pharmacie_id) REFERENCES pharmacies(id) ON DELETE SET NULL
        ) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci";
        
        $pdo->exec($createTableQuery);
        echo "La table clients a été créée avec succès.\n";
        
        // Ajouter quelques clients de test
        $insertClientsQuery = "INSERT INTO clients (nom, prenom, email, telephone, adresse, pharmacie_id, date_creation) VALUES 
            ('Dupont', 'Jean', 'jean.dupont@email.com', '0612345678', '123 Rue de Paris, 75001 Paris', 1, NOW()),
            ('Martin', 'Sophie', 'sophie.martin@email.com', '0687654321', '456 Avenue des Champs-Élysées, 75008 Paris', 1, NOW())";
        
        $pdo->exec($insertClientsQuery);
        echo "Des clients de test ont été ajoutés.\n";
    } else {
        echo "La table clients existe déjà.\n";
    }
    
    // Vérifier si la table ventes existe
    $checkTableQuery = "SHOW TABLES LIKE 'ventes'"; 
    $stmt = $pdo->prepare($checkTableQuery);
    $stmt->execute();
    
    if ($stmt->rowCount() == 0) {
        // La table n'existe pas, on la crée
        $createTableQuery = "CREATE TABLE ventes (
            id INT AUTO_INCREMENT PRIMARY KEY,
            product_id INT NOT NULL,
            client_id INT NOT NULL,
            pharmacy_id INT NOT NULL,
            quantite INT NOT NULL DEFAULT 1,
            prix_unitaire DECIMAL(10,2) NOT NULL,
            prix_total DECIMAL(10,2) NOT NULL,
            date_vente DATETIME NOT NULL,
            FOREIGN KEY (product_id) REFERENCES produits(id),
            FOREIGN KEY (client_id) REFERENCES clients(id),
            FOREIGN KEY (pharmacy_id) REFERENCES pharmacies(id)
        ) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci";
        
        $pdo->exec($createTableQuery);
        echo "La table ventes a été créée avec succès.\n";
    } else {
        echo "La table ventes existe déjà.\n";
    }
    
    echo "Mise à jour des tables terminée avec succès.\n";
    
} catch (PDOException $e) {
    echo "Erreur lors de la mise à jour des tables : " . $e->getMessage() . "\n";
}
?>
