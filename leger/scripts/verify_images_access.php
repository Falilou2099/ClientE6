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

// Vérifier l'existence de l'image par défaut
$defaultImagePath = ROOT_PATH . '/public/images/products/imgDefault.jpg';
if (file_exists($defaultImagePath)) {
    echo "L'image par défaut existe à l'emplacement: " . $defaultImagePath . "\n";
    echo "Taille du fichier: " . round(filesize($defaultImagePath) / 1024, 2) . " Ko\n\n";
} else {
    echo "ATTENTION: L'image par défaut n'existe pas à l'emplacement: " . $defaultImagePath . "\n";
    die("Veuillez placer une image par défaut à cet emplacement avant de continuer.\n");
}

// Récupérer tous les produits
$stmt = $pdo->query("SELECT id, nom, image FROM produits");
$products = $stmt->fetchAll(PDO::FETCH_ASSOC);

echo "Vérification de l'accès aux images pour " . count($products) . " produits...\n\n";

$updatedCount = 0;
$alreadyNullCount = 0;
$validImageCount = 0;

foreach ($products as $product) {
    $id = $product['id'];
    $nom = $product['nom'];
    $image = $product['image'];
    
    echo "Produit ID: " . $id . " (" . $nom . ") - ";
    
    // Vérifier si l'image est NULL ou vide
    if ($image === NULL || $image === '') {
        $alreadyNullCount++;
        echo "Utilise déjà l'image par défaut\n";
        continue;
    }
    
    // Vérifier si l'image est une URL externe
    if (preg_match('/^https?:\/\//i', $image)) {
        // Pour les URLs externes, on vérifie si l'image est accessible
        $headers = @get_headers($image);
        if ($headers && strpos($headers[0], '200') !== false) {
            $validImageCount++;
            echo "URL d'image externe accessible\n";
        } else {
            // L'URL n'est pas accessible, on met à jour pour utiliser l'image par défaut
            $updateStmt = $pdo->prepare("UPDATE produits SET image = NULL WHERE id = :id");
            $updateStmt->execute([':id' => $id]);
            $updatedCount++;
            echo "URL d'image externe inaccessible, mise à jour pour utiliser l'image par défaut\n";
        }
        continue;
    }
    
    // Pour les images locales, vérifier si le fichier existe
    $localImagePath = ROOT_PATH . '/public/images/products/' . $image;
    if (file_exists($localImagePath)) {
        $validImageCount++;
        echo "Image locale accessible\n";
    } else {
        // L'image locale n'existe pas, on met à jour pour utiliser l'image par défaut
        $updateStmt = $pdo->prepare("UPDATE produits SET image = NULL WHERE id = :id");
        $updateStmt->execute([':id' => $id]);
        $updatedCount++;
        echo "Image locale inaccessible, mise à jour pour utiliser l'image par défaut\n";
    }
}

echo "\nRésumé:\n";
echo "- " . $validImageCount . " produits avec des images accessibles\n";
echo "- " . $alreadyNullCount . " produits déjà configurés pour utiliser l'image par défaut\n";
echo "- " . $updatedCount . " produits mis à jour pour utiliser l'image par défaut\n";

echo "\nTerminé!\n";
