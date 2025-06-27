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

// Images prédéfinies pour différentes catégories
$categoryImages = [
    'Antalgiques' => 'antalgique.jpg',
    'Antibiotiques' => 'antibiotique.jpg',
    'Anti-inflammatoires' => 'anti_inflammatoire.jpg',
    'Vaccins' => 'vaccin.jpg',
    'default' => 'medicament.jpg'
];

// Créer des images par défaut pour chaque catégorie
$targetDir = ROOT_PATH . '/public/images/products/';
if (!file_exists($targetDir)) {
    mkdir($targetDir, 0777, true);
}

// Récupérer tous les produits
$stmt = $pdo->query("SELECT id, nom, categorie, est_ordonnance FROM produits ORDER BY id ASC");
$products = $stmt->fetchAll(PDO::FETCH_ASSOC);

echo "Mise à jour des images de produits...\n";
foreach ($products as $product) {
    $category = $product['categorie'] ?? 'default';
    $imageFile = $categoryImages[$category] ?? $categoryImages['default'];
    
    // Générer un nom de fichier unique pour ce produit
    $imageName = 'product_' . $product['id'] . '.jpg';
    
    // Mettre à jour la base de données
    $updateStmt = $pdo->prepare("UPDATE produits SET image = :image WHERE id = :id");
    $updateStmt->execute([
        ':image' => $imageName,
        ':id' => $product['id']
    ]);
    echo "Base de données mise à jour pour le produit ID: " . $product['id'] . " avec l'image: " . $imageName . "\n";
}

echo "Terminé!\n";
