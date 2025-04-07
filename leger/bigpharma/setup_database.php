<?php
// Script pour configurer la base de données
require_once __DIR__ . '/config/database.php';

// Fonction pour vérifier si une table existe
function tableExists($tableName, $pdo) {
    try {
        $result = $pdo->query("SHOW TABLES LIKE '{$tableName}'");
        return $result->rowCount() > 0;
    } catch (Exception $e) {
        return false;
    }
}

// Création de la table des pharmacies si elle n'existe pas
if (!tableExists('pharmacies', $pdo)) {
    $pdo->exec("
        CREATE TABLE pharmacies (
            id INT AUTO_INCREMENT PRIMARY KEY,
            name VARCHAR(100) NOT NULL,
            address VARCHAR(255) NOT NULL,
            phone_number VARCHAR(20) NOT NULL,
            email VARCHAR(100) NOT NULL UNIQUE,
            registration_number VARCHAR(50) NOT NULL UNIQUE,
            status ENUM('pending', 'active', 'suspended') DEFAULT 'pending',
            created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
            updated_at DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
        ) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
    ");
    echo "Table 'pharmacies' créée avec succès.<br>";
}

// Création de la table des utilisateurs si elle n'existe pas
if (!tableExists('users', $pdo)) {
    $pdo->exec("
        CREATE TABLE users (
            id INT AUTO_INCREMENT PRIMARY KEY,
            email VARCHAR(100) NOT NULL UNIQUE,
            password VARCHAR(255) NOT NULL,
            pharmacy_id INT,
            role ENUM('admin', 'employee', 'superadmin') NOT NULL DEFAULT 'employee',
            status ENUM('active', 'inactive', 'blocked') DEFAULT 'active',
            last_login DATETIME NULL,
            created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
            updated_at DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
            FOREIGN KEY (pharmacy_id) REFERENCES pharmacies(id) ON DELETE SET NULL
        ) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
    ");
    echo "Table 'users' créée avec succès.<br>";
}

// Création de la table des produits si elle n'existe pas
if (!tableExists('produits', $pdo)) {
    $pdo->exec("
        CREATE TABLE produits (
            id INT AUTO_INCREMENT PRIMARY KEY,
            nom VARCHAR(100) NOT NULL,
            description TEXT,
            prix DECIMAL(10,2) NOT NULL,
            quantite_stock INT NOT NULL DEFAULT 0,
            categorie VARCHAR(50) NOT NULL,
            image VARCHAR(255),
            est_ordonnance BOOLEAN DEFAULT FALSE,
            date_ajout DATETIME DEFAULT CURRENT_TIMESTAMP,
            date_modification DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
        ) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
    ");
    echo "Table 'produits' créée avec succès.<br>";
    
    // Ajouter quelques produits de test
    $pdo->exec("
        INSERT INTO produits (nom, description, prix, quantite_stock, categorie, est_ordonnance) VALUES
        ('Doliprane 1000mg', 'Boîte de 8 comprimés - Paracétamol', 2.95, 50, 'Médicaments', 0),
        ('Advil 200mg', 'Boîte de 20 comprimés - Ibuprofène', 4.50, 30, 'Médicaments', 0),
        ('Amoxicilline 500mg', 'Boîte de 12 gélules - Antibiotique', 8.75, 15, 'Médicaments', 1),
        ('Crème hydratante', 'Tube 50ml - Peaux sèches', 6.99, 25, 'Soins du corps', 0),
        ('Thermomètre digital', 'Mesure précise de la température', 12.50, 10, 'Matériel médical', 0),
        ('Pansements', 'Boîte de 30 pansements assortis', 3.25, 40, 'Premiers soins', 0),
        ('Vitamine C 1000mg', 'Tube de 20 comprimés effervescents', 5.80, 35, 'Compléments alimentaires', 0),
        ('Sérum physiologique', 'Lot de 5 doses de 5ml', 2.15, 60, 'Soins du corps', 0),
        ('Masques chirurgicaux', 'Boîte de 50 masques jetables', 9.99, 20, 'Protection', 0),
        ('Gel hydroalcoolique', 'Flacon 100ml', 3.50, 45, 'Hygiène', 0)
    ");
    echo "Produits de test ajoutés avec succès.<br>";
}

// Création de la table des tokens de réinitialisation si elle n'existe pas
if (!tableExists('reset_tokens', $pdo)) {
    $pdo->exec("
        CREATE TABLE reset_tokens (
            id INT AUTO_INCREMENT PRIMARY KEY,
            user_id INT NOT NULL,
            token VARCHAR(64) NOT NULL,
            expires_at DATETIME NOT NULL,
            created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
            FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE
        ) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
    ");
    echo "Table 'reset_tokens' créée avec succès.<br>";
}

echo "<br>Configuration de la base de données terminée.";
echo "<br><a href='/bigpharma/'>Retour à l'accueil</a>";
?>
