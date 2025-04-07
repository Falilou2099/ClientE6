<?php
// Définir le chemin racine
define('ROOT_PATH', dirname(__DIR__));

// Connexion à la base de données
try {
    $pdo = new PDO("mysql:host=localhost;dbname=clientlegerlourd", "root", "");
    $pdo->setAttribute(PDO::ATTR_ERRMODE, PDO::ERRMODE_EXCEPTION);
} catch (PDOException $e) {
    die("Erreur de connexion à la base de données: " . $e->getMessage());
}

// Récupérer tous les produits
$stmt = $pdo->query("SELECT id, nom, categorie FROM produits ORDER BY id ASC");
$products = $stmt->fetchAll(PDO::FETCH_ASSOC);

// Images génériques par catégorie
$categoryImages = [
    'Antalgiques' => 'antalgique.jpg',
    'Antibiotiques' => 'antibiotique.jpg',
    'Anti-inflammatoires' => 'anti_inflammatoire.jpg',
    'Vaccins' => 'vaccin.jpg'
];

// Images génériques par défaut
$defaultImages = [
    'medicament1.jpg',
    'medicament2.jpg',
    'medicament3.jpg',
    'medicament4.jpg',
    'medicament5.jpg'
];

echo "Mise à jour des images de produits dans la base de données...\n";
foreach ($products as $index => $product) {
    // Déterminer l'image à utiliser
    $category = $product['categorie'] ?? '';
    if (isset($categoryImages[$category])) {
        $imageName = $categoryImages[$category];
    } else {
        // Utiliser une image par défaut basée sur l'ID du produit
        $imageIndex = $product['id'] % count($defaultImages);
        $imageName = $defaultImages[$imageIndex];
    }
    
    // Mettre à jour la base de données
    $updateStmt = $pdo->prepare("UPDATE produits SET image = :image WHERE id = :id");
    $updateStmt->execute([
        ':image' => $imageName,
        ':id' => $product['id']
    ]);
    echo "Base de données mise à jour pour le produit ID: " . $product['id'] . " avec l'image: " . $imageName . "\n";
}

echo "Terminé!\n";
