<?php
// Script de remplissage de la base de données BigPharma

// Inclure la configuration de la base de données
require_once '../config/database.php';

// Vérifier la connexion
if (!isset($pdo)) {
    die("Erreur : Connexion à la base de données non établie.");
}

// Tableau de produits pharmaceutiques
$produits = [
    [
        'nom' => 'Doliprane 500mg',
        'description' => 'Médicament antalgique et antipyrétique à base de paracétamol',
        'prix' => 4.50,
        'quantite_stock' => 150,
        'categorie' => 'Antalgique',
        'date_peremption' => '2025-12-31'
    ],
    [
        'nom' => 'Advil 200mg',
        'description' => 'Anti-inflammatoire et antidouleur à base d\'ibuprofène',
        'prix' => 5.20,
        'quantite_stock' => 80,
        'categorie' => 'Anti-inflammatoire',
        'date_peremption' => '2024-11-30'
    ],
    [
        'nom' => 'Spasfon Lyoc 80mg',
        'description' => 'Médicament antispasmodique pour soulager les douleurs abdominales',
        'prix' => 6.30,
        'quantite_stock' => 50,
        'categorie' => 'Antispasmodique',
        'date_peremption' => '2025-06-15'
    ],
    [
        'nom' => 'Efferalgan 1000mg',
        'description' => 'Comprimés effervescents de paracétamol pour traiter la douleur et la fièvre',
        'prix' => 7.80,
        'quantite_stock' => 100,
        'categorie' => 'Antalgique',
        'date_peremption' => '2025-09-20'
    ],
    [
        'nom' => 'Xanax 0.5mg',
        'description' => 'Médicament anxiolytique pour traiter l\'anxiété',
        'prix' => 12.50,
        'quantite_stock' => 30,
        'categorie' => 'Anxiolytique',
        'date_peremption' => '2024-08-10'
    ],
    [
        'nom' => 'Ventoline 100µg',
        'description' => 'Spray pour le traitement de l\'asthme et des bronchospasmes',
        'prix' => 9.20,
        'quantite_stock' => 60,
        'categorie' => 'Bronchodilatateur',
        'date_peremption' => '2025-03-25'
    ],
    [
        'nom' => 'Kardegic 75mg',
        'description' => 'Traitement préventif des maladies cardiovasculaires',
        'prix' => 8.70,
        'quantite_stock' => 40,
        'categorie' => 'Anticoagulant',
        'date_peremption' => '2024-12-15'
    ],
    [
        'nom' => 'Zithromax 250mg',
        'description' => 'Antibiotique à large spectre pour traiter diverses infections bactériennes',
        'prix' => 15.60,
        'quantite_stock' => 25,
        'categorie' => 'Antibiotique',
        'date_peremption' => '2024-10-05'
    ],
    [
        'nom' => 'Maalox',
        'description' => 'Suspension buvable pour traiter les troubles digestifs et les brûlures d\'estomac',
        'prix' => 5.90,
        'quantite_stock' => 70,
        'categorie' => 'Antiacide',
        'date_peremption' => '2025-01-20'
    ],
    [
        'nom' => 'Previscan 20mg',
        'description' => 'Anticoagulant oral pour prévenir les thromboses',
        'prix' => 18.40,
        'quantite_stock' => 35,
        'categorie' => 'Anticoagulant',
        'date_peremption' => '2024-07-30'
    ]
];

// Supprimer les données existantes
$pdo->exec("DELETE FROM produits");

// Préparer la requête d'insertion
$stmt = $pdo->prepare("
    INSERT INTO produits 
    (nom, description, prix, quantite_stock, categorie, date_peremption) 
    VALUES 
    (:nom, :description, :prix, :quantite_stock, :categorie, :date_peremption)
");

// Insérer les produits
foreach ($produits as $produit) {
    $stmt->execute($produit);
}

echo "Base de données remplie avec succès ! " . count($produits) . " produits ajoutés.\n";
?>
