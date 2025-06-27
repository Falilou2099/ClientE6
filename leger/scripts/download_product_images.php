<?php
// Définir le chemin racine
define('ROOT_PATH', dirname(__DIR__));

// Inclure les fichiers nécessaires
require_once ROOT_PATH . '/src/Models/Product.php';

// Initialiser la connexion à la base de données
$dbConfig = require_once ROOT_PATH . '/config/database.php';

// Liste d'images de médicaments génériques
$imageUrls = [
    'https://www.pharma-gdd.com/media/cache/resolve/product_show/rc/YTCzNGMxYT/uploads/product/5e8c5a3c7a55d_doliprane-1000-mg-8-comprimes.jpg',
    'https://www.pharma-gdd.com/media/cache/resolve/product_show/rc/ZmNkMjk1ZW/uploads/product/5e8c5a3e0f9f5_doliprane-500-mg-16-comprimes.jpg',
    'https://www.pharma-gdd.com/media/cache/resolve/product_show/rc/MzI1MzBiMD/uploads/product/5e8c5a3c8c7f3_doliprane-paracetamol-500-mg-16-gelules.jpg',
    'https://www.pharma-gdd.com/media/cache/resolve/product_show/rc/OTRkOTRkZj/uploads/product/5e8c5a3c3d5a5_advil-200-mg-20-comprimes-enrobes.jpg',
    'https://www.pharma-gdd.com/media/cache/resolve/product_show/rc/ZjM3MzZiMj/uploads/product/5e8c5a3c9f1c6_efferalgan-500-mg-16-comprimes-effervescents.jpg',
    'https://www.pharma-gdd.com/media/cache/resolve/product_show/rc/MWFkZDNhMT/uploads/product/5e8c5a3c9f1c6_efferalgan-500-mg-16-comprimes-effervescents.jpg',
    'https://www.pharma-gdd.com/media/cache/resolve/product_show/rc/ZDFmYzZmZj/uploads/product/5e8c5a3d8c7f3_nurofen-200-mg-20-comprimes-enrobes.jpg',
    'https://www.pharma-gdd.com/media/cache/resolve/product_show/rc/MjZmZWNjNT/uploads/product/5e8c5a3d9f1c6_spasfon-80-mg-30-comprimes-enrobes.jpg',
    'https://www.pharma-gdd.com/media/cache/resolve/product_show/rc/YzUxMzhhMz/uploads/product/5e8c5a3d7a55d_smecta-3-g-30-sachets-de-poudre-pour-suspension-buvable-vanille.jpg',
    'https://www.pharma-gdd.com/media/cache/resolve/product_show/rc/YWVkYzc0Nj/uploads/product/5e8c5a3d0f9f5_gaviscon-menthe-24-comprimes-a-croquer.jpg'
];

// Créer le dossier de destination s'il n'existe pas
$targetDir = ROOT_PATH . '/public/images/products/';
if (!file_exists($targetDir)) {
    mkdir($targetDir, 0777, true);
}

// Télécharger les images et mettre à jour la base de données
$pdo = new PDO("mysql:host=localhost;dbname=clientlegerlourd", "root", "");
$pdo->setAttribute(PDO::ATTR_ERRMODE, PDO::ERRMODE_EXCEPTION);

// Récupérer tous les produits
$stmt = $pdo->query("SELECT id FROM produits ORDER BY id ASC");
$products = $stmt->fetchAll(PDO::FETCH_ASSOC);

// Mettre à jour chaque produit avec une image
echo "Mise à jour des images de produits...\n";
foreach ($products as $index => $product) {
    // Sélectionner une image aléatoire ou utiliser l'index si disponible
    $imageIndex = $index % count($imageUrls);
    $imageUrl = $imageUrls[$imageIndex];
    
    // Générer un nom de fichier unique
    $imageName = 'product_' . $product['id'] . '_' . time() . '.jpg';
    $targetFile = $targetDir . $imageName;
    
    // Télécharger l'image
    try {
        $imageContent = @file_get_contents($imageUrl);
        if ($imageContent !== false) {
            file_put_contents($targetFile, $imageContent);
            echo "Image téléchargée: $imageName\n";
            
            // Mettre à jour la base de données
            $updateStmt = $pdo->prepare("UPDATE produits SET image = :image WHERE id = :id");
            $updateStmt->execute([
                ':image' => $imageName,
                ':id' => $product['id']
            ]);
            echo "Base de données mise à jour pour le produit ID: " . $product['id'] . "\n";
        } else {
            echo "Impossible de télécharger l'image: $imageUrl\n";
        }
    } catch (Exception $e) {
        echo "Erreur lors du téléchargement de l'image: " . $e->getMessage() . "\n";
    }
}

echo "Terminé!\n";
