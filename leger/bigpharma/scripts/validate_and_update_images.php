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
$stmt = $pdo->query("SELECT id, nom, image FROM produits");
$products = $stmt->fetchAll(PDO::FETCH_ASSOC);

echo "Vérification et mise à jour des images pour " . count($products) . " produits...\n";

$updatedCount = 0;
$alreadyNullCount = 0;
$validImageCount = 0;

foreach ($products as $product) {
    $id = $product['id'];
    $nom = $product['nom'];
    $image = $product['image'];
    
    // Vérifier si l'image est NULL ou vide
    if ($image === NULL || $image === '') {
        $alreadyNullCount++;
        echo "Produit ID: " . $id . " (" . $nom . ") - Déjà configuré pour utiliser l'image par défaut\n";
        continue;
    }
    
    // Vérifier si l'image est une URL externe
    if (preg_match('/^https?:\/\//i', $image)) {
        // Pour les URLs externes, on les conserve telles quelles
        $validImageCount++;
        echo "Produit ID: " . $id . " (" . $nom . ") - URL d'image externe valide\n";
        continue;
    }
    
    // Pour les images locales, vérifier si le fichier existe
    $localImagePath = ROOT_PATH . '/public/images/products/' . $image;
    if (file_exists($localImagePath)) {
        $validImageCount++;
        echo "Produit ID: " . $id . " (" . $nom . ") - Image locale valide\n";
        continue;
    }
    
    // Si on arrive ici, l'image n'est pas valide, mettre à NULL pour utiliser l'image par défaut
    $updateStmt = $pdo->prepare("UPDATE produits SET image = NULL WHERE id = :id");
    $updateStmt->execute([':id' => $id]);
    $updatedCount++;
    echo "Produit ID: " . $id . " (" . $nom . ") - Image non valide, mise à jour pour utiliser l'image par défaut\n";
}

echo "\nRésumé:\n";
echo "- " . $validImageCount . " produits avec des images valides\n";
echo "- " . $alreadyNullCount . " produits déjà configurés pour utiliser l'image par défaut\n";
echo "- " . $updatedCount . " produits mis à jour pour utiliser l'image par défaut\n";

// Vérifier que l'image par défaut existe
$defaultImagePath = ROOT_PATH . '/img/imgDefault.jpg';
if (file_exists($defaultImagePath)) {
    echo "\nL'image par défaut existe à l'emplacement: " . $defaultImagePath . "\n";
    echo "Taille du fichier: " . round(filesize($defaultImagePath) / 1024, 2) . " Ko\n";
} else {
    echo "\nATTENTION: L'image par défaut n'existe pas à l'emplacement: " . $defaultImagePath . "\n";
}

echo "\nTerminé!\n";
