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

// Compter le nombre de produits sans image
$countStmt = $pdo->query("SELECT COUNT(*) FROM produits WHERE image IS NULL OR image = ''");
$countWithoutImage = $countStmt->fetchColumn();

echo "Nombre de produits sans image: " . $countWithoutImage . "\n";

// Mettre à jour tous les produits qui n'ont pas d'image
if ($countWithoutImage > 0) {
    $updateStmt = $pdo->prepare("UPDATE produits SET image = NULL WHERE image IS NULL OR image = ''");
    $updateStmt->execute();
    
    echo "Les produits sans image ont été mis à jour pour utiliser l'image par défaut.\n";
} else {
    echo "Tous les produits ont déjà une image définie.\n";
}

// Vérifier que l'image par défaut existe
$defaultImagePath = ROOT_PATH . '/img/imgDefault.jpg';
if (file_exists($defaultImagePath)) {
    echo "L'image par défaut existe à l'emplacement: " . $defaultImagePath . "\n";
} else {
    echo "ATTENTION: L'image par défaut n'existe pas à l'emplacement: " . $defaultImagePath . "\n";
}

echo "Terminé!\n";
