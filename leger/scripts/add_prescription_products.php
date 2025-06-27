<?php
/**
 * Script pour ajouter des produits sous ordonnance à stock limité
 */

// Chemins absolus pour les inclusions
define('ROOT_PATH', dirname(__DIR__));
define('CONFIG_PATH', ROOT_PATH . '/config');

// Connexion à la base de données
require_once CONFIG_PATH . '/database.php';

echo "Début de l'ajout des produits sous ordonnance à stock limité...\n";

// Liste des produits sous ordonnance à ajouter
$prescriptionProducts = [
    [
        'nom' => 'Tramadol 50mg',
        'description' => 'Analgésique opioïde pour les douleurs modérées à sévères. Délivré uniquement sur ordonnance médicale.',
        'prix' => 15.75,
        'quantite_stock' => 8,
        'categorie' => 'Analgésiques',
        'categorie_id' => 1,
        'fournisseur_id' => 1,
        'est_ordonnance' => 1,
        'image' => '/assets/img/products/tramadol.jpg',
        'date_ajout' => date('Y-m-d H:i:s'),
        'date_modification' => date('Y-m-d H:i:s')
    ],
    [
        'nom' => 'Morphine 10mg',
        'description' => 'Analgésique opioïde puissant pour les douleurs intenses. Strictement contrôlé et délivré uniquement sur ordonnance médicale.',
        'prix' => 28.90,
        'quantite_stock' => 5,
        'categorie' => 'Analgésiques',
        'categorie_id' => 1,
        'fournisseur_id' => 1,
        'est_ordonnance' => 1,
        'image' => '/assets/img/products/morphine.jpg',
        'date_ajout' => date('Y-m-d H:i:s'),
        'date_modification' => date('Y-m-d H:i:s')
    ],
    [
        'nom' => 'Diazépam 5mg',
        'description' => 'Benzodiazépine utilisée pour traiter l\'anxiété, les spasmes musculaires et les convulsions. Délivré uniquement sur ordonnance médicale.',
        'prix' => 12.50,
        'quantite_stock' => 10,
        'categorie' => 'Anxiolytiques',
        'categorie_id' => 2,
        'fournisseur_id' => 2,
        'est_ordonnance' => 1,
        'image' => '/assets/img/products/diazepam.jpg',
        'date_ajout' => date('Y-m-d H:i:s'),
        'date_modification' => date('Y-m-d H:i:s')
    ],
    [
        'nom' => 'Lévothyroxine 100µg',
        'description' => 'Hormone thyroïdienne de synthèse utilisée pour traiter l\'hypothyroïdie. Délivré uniquement sur ordonnance médicale.',
        'prix' => 8.90,
        'quantite_stock' => 7,
        'categorie' => 'Hormones',
        'categorie_id' => 3,
        'fournisseur_id' => 1,
        'est_ordonnance' => 1,
        'image' => '/assets/img/products/levothyroxine.jpg',
        'date_ajout' => date('Y-m-d H:i:s'),
        'date_modification' => date('Y-m-d H:i:s')
    ],
    [
        'nom' => 'Insuline Lantus',
        'description' => 'Insuline à action prolongée utilisée pour traiter le diabète de type 1 et 2. Délivré uniquement sur ordonnance médicale.',
        'prix' => 42.30,
        'quantite_stock' => 6,
        'categorie' => 'Hormones',
        'categorie_id' => 3,
        'fournisseur_id' => 2,
        'est_ordonnance' => 1,
        'image' => '/assets/img/products/insuline.jpg',
        'date_ajout' => date('Y-m-d H:i:s'),
        'date_modification' => date('Y-m-d H:i:s')
    ],
    [
        'nom' => 'Méthotrexate 2.5mg',
        'description' => 'Immunosuppresseur utilisé pour traiter certains cancers et maladies auto-immunes. Délivré uniquement sur ordonnance médicale.',
        'prix' => 35.60,
        'quantite_stock' => 4,
        'categorie' => 'Immunosuppresseurs',
        'categorie_id' => 4,
        'fournisseur_id' => 1,
        'est_ordonnance' => 1,
        'image' => '/assets/img/products/methotrexate.jpg',
        'date_ajout' => date('Y-m-d H:i:s'),
        'date_modification' => date('Y-m-d H:i:s')
    ],
    [
        'nom' => 'Warfarine 5mg',
        'description' => 'Anticoagulant utilisé pour prévenir la formation de caillots sanguins. Délivré uniquement sur ordonnance médicale.',
        'prix' => 18.25,
        'quantite_stock' => 9,
        'categorie' => 'Anticoagulants',
        'categorie_id' => 5,
        'fournisseur_id' => 2,
        'est_ordonnance' => 1,
        'image' => '/assets/img/products/warfarine.jpg',
        'date_ajout' => date('Y-m-d H:i:s'),
        'date_modification' => date('Y-m-d H:i:s')
    ],
    [
        'nom' => 'Méthylphénidate 10mg',
        'description' => 'Psychostimulant utilisé pour traiter le TDAH. Strictement contrôlé et délivré uniquement sur ordonnance médicale.',
        'prix' => 24.80,
        'quantite_stock' => 3,
        'categorie' => 'Psychostimulants',
        'categorie_id' => 6,
        'fournisseur_id' => 1,
        'est_ordonnance' => 1,
        'image' => '/assets/img/products/methylphenidate.jpg',
        'date_ajout' => date('Y-m-d H:i:s'),
        'date_modification' => date('Y-m-d H:i:s')
    ]
];

// Vérifier si la table categories existe, sinon la créer
try {
    $pdo->query("SELECT 1 FROM categories LIMIT 1");
    echo "La table categories existe déjà.\n";
} catch (PDOException $e) {
    echo "La table categories n'existe pas. Création de la table...\n";
    
    $pdo->exec("CREATE TABLE categories (
        id INT AUTO_INCREMENT PRIMARY KEY,
        nom VARCHAR(100) NOT NULL,
        description TEXT,
        date_creation TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
        date_modification TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
    ) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci");
    
    echo "Table categories créée avec succès.\n";
}

// Vérifier si les catégories existent, sinon les créer
$categories = ['Analgésiques', 'Anxiolytiques', 'Hormones', 'Immunosuppresseurs', 'Anticoagulants', 'Psychostimulants'];
$categoryIds = [];

foreach ($categories as $index => $category) {
    // Vérifier si la catégorie existe déjà
    $stmt = $pdo->prepare("SELECT id FROM categories WHERE nom = ?");
    $stmt->execute([$category]);
    $existingCategory = $stmt->fetch();
    
    if ($existingCategory) {
        $categoryIds[$category] = $existingCategory['id'];
        echo "Catégorie existante: {$category} (ID: {$existingCategory['id']})\n";
    } else {
        // Créer la catégorie
        $stmt = $pdo->prepare("INSERT INTO categories (nom, description) VALUES (?, ?)");
        $stmt->execute([$category, "Catégorie pour les produits {$category}"]);
        $categoryId = $pdo->lastInsertId();
        $categoryIds[$category] = $categoryId;
        echo "Nouvelle catégorie créée: {$category} (ID: {$categoryId})\n";
    }
}

// Vérifier si les colonnes categorie_id et fournisseur_id existent dans la table produits
try {
    $stmt = $pdo->prepare("SHOW COLUMNS FROM produits LIKE 'categorie_id'");
    $stmt->execute();
    $hasCategorie_id = $stmt->rowCount() > 0;
    
    if (!$hasCategorie_id) {
        echo "La colonne categorie_id n'existe pas dans la table produits. Ajout de la colonne...\n";
        $pdo->exec("ALTER TABLE produits ADD COLUMN categorie_id INT AFTER categorie, ADD INDEX idx_categorie_id (categorie_id)");
        echo "Colonne categorie_id ajoutée avec succès.\n";
    }
    
    $stmt = $pdo->prepare("SHOW COLUMNS FROM produits LIKE 'fournisseur_id'");
    $stmt->execute();
    $hasFournisseur_id = $stmt->rowCount() > 0;
    
    if (!$hasFournisseur_id) {
        echo "La colonne fournisseur_id n'existe pas dans la table produits. Ajout de la colonne...\n";
        $pdo->exec("ALTER TABLE produits ADD COLUMN fournisseur_id INT AFTER categorie_id, ADD INDEX idx_fournisseur_id (fournisseur_id)");
        echo "Colonne fournisseur_id ajoutée avec succès.\n";
    }
} catch (PDOException $e) {
    echo "Erreur lors de la vérification ou de l'ajout des colonnes: " . $e->getMessage() . "\n";
}

// Ajouter les produits
$addedCount = 0;
$updatedCount = 0;

foreach ($prescriptionProducts as $product) {
    // Mettre à jour l'ID de la catégorie si nécessaire
    if (isset($categoryIds[$product['categorie']])) {
        $product['categorie_id'] = $categoryIds[$product['categorie']];
    }
    
    // Vérifier si le produit existe déjà
    $stmt = $pdo->prepare("SELECT id FROM produits WHERE nom = ?");
    $stmt->execute([$product['nom']]);
    $existingProduct = $stmt->fetch();
    
    if ($existingProduct) {
        // Mettre à jour le produit existant
        $stmt = $pdo->prepare("
            UPDATE produits SET 
                description = ?,
                prix = ?,
                quantite_stock = ?,
                categorie = ?,
                categorie_id = ?,
                fournisseur_id = ?,
                est_ordonnance = ?,
                image = ?
            WHERE id = ?
        ");
        
        $stmt->execute([
            $product['description'],
            $product['prix'],
            $product['quantite_stock'],
            $product['categorie'],
            $product['categorie_id'],
            $product['fournisseur_id'],
            $product['est_ordonnance'],
            $product['image'],
            $existingProduct['id']
        ]);
        
        echo "Produit mis à jour: {$product['nom']} (ID: {$existingProduct['id']})\n";
        $updatedCount++;
    } else {
        // Ajouter un nouveau produit
        $stmt = $pdo->prepare("
            INSERT INTO produits (
                nom, description, prix, quantite_stock, categorie, categorie_id,
                fournisseur_id, est_ordonnance, image
            ) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)
        ");
        
        $stmt->execute([
            $product['nom'],
            $product['description'],
            $product['prix'],
            $product['quantite_stock'],
            $product['categorie'],
            $product['categorie_id'],
            $product['fournisseur_id'],
            $product['est_ordonnance'],
            $product['image']
        ]);
        
        $newProductId = $pdo->lastInsertId();
        echo "Nouveau produit ajouté: {$product['nom']} (ID: {$newProductId})\n";
        $addedCount++;
    }
}

echo "\nRésumé:\n";
echo "- {$addedCount} nouveaux produits ajoutés\n";
echo "- {$updatedCount} produits mis à jour\n";
echo "Opération terminée avec succès.\n";
