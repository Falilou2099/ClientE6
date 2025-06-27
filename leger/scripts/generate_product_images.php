<?php
// Définir le chemin racine
define('ROOT_PATH', dirname(__DIR__));

// Créer le dossier de destination s'il n'existe pas
$targetDir = ROOT_PATH . '/public/images/products/';
if (!file_exists($targetDir)) {
    mkdir($targetDir, 0777, true);
}

// Connexion à la base de données
try {
    $pdo = new PDO("mysql:host=localhost;dbname=clientlegerlourd", "root", "");
    $pdo->setAttribute(PDO::ATTR_ERRMODE, PDO::ERRMODE_EXCEPTION);
} catch (PDOException $e) {
    die("Erreur de connexion à la base de données: " . $e->getMessage());
}

// Récupérer tous les produits
$stmt = $pdo->query("SELECT id, nom, categorie, est_ordonnance FROM produits ORDER BY id ASC");
$products = $stmt->fetchAll(PDO::FETCH_ASSOC);

// Couleurs de fond pour différentes catégories
$categoryColors = [
    'Antalgiques' => [220, 230, 255],
    'Antibiotiques' => [255, 220, 220],
    'Anti-inflammatoires' => [220, 255, 220],
    'Vaccins' => [255, 255, 220],
    'default' => [240, 240, 240]
];

// Générer une image pour chaque produit
echo "Génération des images de produits...\n";
foreach ($products as $product) {
    // Créer une image de 400x300 pixels
    $img = imagecreatetruecolor(400, 300);
    
    // Couleur de fond basée sur la catégorie
    $category = $product['categorie'] ?? 'default';
    $bgColor = $categoryColors[$category] ?? $categoryColors['default'];
    $background = imagecolorallocate($img, $bgColor[0], $bgColor[1], $bgColor[2]);
    imagefill($img, 0, 0, $background);
    
    // Couleurs pour le texte et les bordures
    $textColor = imagecolorallocate($img, 50, 50, 50);
    $borderColor = imagecolorallocate($img, 200, 200, 200);
    $prescriptionColor = imagecolorallocate($img, 255, 50, 50);
    
    // Dessiner un cadre
    imagerectangle($img, 0, 0, 399, 299, $borderColor);
    
    // Ajouter le nom du produit
    $productName = $product['nom'];
    // Centrer le texte
    $fontSize = 5;
    $textBox = imagettfbbox($fontSize, 0, __DIR__ . '/arial.ttf', $productName);
    if ($textBox) {
        $textWidth = $textBox[2] - $textBox[0];
        $textHeight = $textBox[7] - $textBox[1];
        $x = (400 - $textWidth) / 2;
        $y = 150 + $textHeight;
        imagettftext($img, $fontSize, 0, $x, $y, $textColor, __DIR__ . '/arial.ttf', $productName);
    } else {
        // Fallback si imagettfbbox ne fonctionne pas
        $x = (400 - strlen($productName) * 5) / 2;
        imagestring($img, 5, $x, 140, $productName, $textColor);
    }
    
    // Ajouter la catégorie
    $categoryText = "Catégorie: " . $category;
    imagestring($img, 3, 10, 200, $categoryText, $textColor);
    
    // Ajouter un indicateur d'ordonnance si nécessaire
    if ($product['est_ordonnance']) {
        imagestring($img, 4, 10, 10, "ORDONNANCE REQUISE", $prescriptionColor);
    }
    
    // Générer un nom de fichier unique
    $imageName = 'product_' . $product['id'] . '.png';
    $targetFile = $targetDir . $imageName;
    
    // Sauvegarder l'image
    imagepng($img, $targetFile);
    imagedestroy($img);
    
    echo "Image générée: $imageName\n";
    
    // Mettre à jour la base de données
    $updateStmt = $pdo->prepare("UPDATE produits SET image = :image WHERE id = :id");
    $updateStmt->execute([
        ':image' => $imageName,
        ':id' => $product['id']
    ]);
    echo "Base de données mise à jour pour le produit ID: " . $product['id'] . "\n";
}

echo "Terminé!\n";
