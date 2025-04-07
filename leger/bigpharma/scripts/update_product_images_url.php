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

// URLs d'images externes pour différentes catégories de médicaments
$externalImageUrls = [
    // Antalgiques
    'https://www.pharmashopi.com/images/Image/DOLIPRANE-1000-mg-Paracetamol-8-comprimes-Sanofi-1.jpg',
    'https://www.pharmashopi.com/images/Image/EFFERALGAN-500-mg-Paracetamol-16-comprimes-effervescents-UPSA-1.jpg',
    'https://www.pharmashopi.com/images/Image/DAFALGAN-1000-mg-Paracetamol-8-comprimes-effervescents-UPSA-1.jpg',
    
    // Antibiotiques
    'https://www.pharmashopi.com/images/Image/AMOXICILLINE-BIOGARAN-1-g-14-comprimes-dispersibles-Biogaran-1.jpg',
    'https://www.pharmashopi.com/images/Image/AUGMENTIN-1-g-500-mg-Amoxicilline-acide-clavulanique-16-comprimes-pellicules-GSK-1.jpg',
    
    // Anti-inflammatoires
    'https://www.pharmashopi.com/images/Image/VOLTARENE-EMULGEL-1-pour-cent-Diclofenac-de-diethylamine-gel-100-g-Novartis-1.jpg',
    'https://www.pharmashopi.com/images/Image/NUROFEN-400-mg-Ibuprofene-12-comprimes-enrobes-Reckitt-Benckiser-1.jpg',
    
    // Vaccins
    'https://www.pharmashopi.com/images/Image/INFLUVAC-TETRA-Vaccin-grippal-inactivee-a-antigenes-de-surface-0-5-ml-suspension-injectable-en-seringue-preremplie-Mylan-1.jpg'
];

// Récupérer tous les produits
$stmt = $pdo->query("SELECT id, categorie FROM produits ORDER BY id ASC");
$products = $stmt->fetchAll(PDO::FETCH_ASSOC);

echo "Mise à jour des images de produits avec des URLs externes...\n";

// Mettre à jour quelques produits avec des URLs externes (pas tous)
$updateCount = min(count($products), count($externalImageUrls));
for ($i = 0; $i < $updateCount; $i++) {
    $product = $products[$i];
    $imageUrl = $externalImageUrls[$i];
    
    // Mettre à jour la base de données avec l'URL externe
    $updateStmt = $pdo->prepare("UPDATE produits SET image = :image WHERE id = :id");
    $updateStmt->execute([
        ':image' => $imageUrl,
        ':id' => $product['id']
    ]);
    
    echo "Produit ID: " . $product['id'] . " mis à jour avec l'URL d'image: " . $imageUrl . "\n";
}

// Laisser quelques produits sans image pour tester l'image par défaut
if (count($products) > $updateCount) {
    $updateStmt = $pdo->prepare("UPDATE produits SET image = NULL WHERE id = :id");
    $updateStmt->execute([
        ':id' => $products[$updateCount]['id']
    ]);
    echo "Produit ID: " . $products[$updateCount]['id'] . " mis à jour sans image (utilisera l'image par défaut)\n";
}

echo "Terminé!\n";
