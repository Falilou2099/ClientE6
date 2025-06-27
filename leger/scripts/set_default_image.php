<?php
/**
 * Script pour mettre à jour tous les produits pour utiliser l'image par défaut
 */

// Chemins absolus pour les inclusions
define('ROOT_PATH', dirname(__DIR__));
define('CONFIG_PATH', ROOT_PATH . '/config');

// Connexion à la base de données
require_once CONFIG_PATH . '/database.php';

echo "Début de la mise à jour des images de produits...\n";

// Nom de l'image par défaut
$defaultImage = 'imgDefault.jpg';

try {
    // Mettre à jour tous les produits avec des chemins d'images incorrects ou vides
    $stmt = $pdo->prepare("
        UPDATE produits 
        SET image = :default_image 
        WHERE image IS NULL 
           OR image = '' 
           OR image LIKE '/assets/img/%' 
           OR image NOT LIKE '%.jpg' 
           OR image NOT LIKE '%.png' 
           OR image NOT LIKE '%.jpeg' 
           OR image NOT LIKE '%.gif'
    ");
    
    $stmt->execute([':default_image' => $defaultImage]);
    
    $affectedRows = $stmt->rowCount();
    echo "Nombre de produits mis à jour : $affectedRows\n";
    
    echo "Mise à jour terminée avec succès.\n";
} catch (PDOException $e) {
    echo "Erreur lors de la mise à jour des images : " . $e->getMessage() . "\n";
}
?>
