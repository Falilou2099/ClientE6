<?php
// Script pour vérifier la structure de la table pharmacies
require_once 'config/database.php';

try {
    // Utiliser la connexion PDO globale
    $pdo = $GLOBALS['pdo'];
    
    // Vérifier la structure de la table pharmacies
    $stmt = $pdo->query('DESCRIBE pharmacies');
    $columns = $stmt->fetchAll(PDO::FETCH_ASSOC);
    
    echo "<h2>Structure de la table 'pharmacies'</h2>";
    echo "<pre>";
    print_r($columns);
    echo "</pre>";
    
    // Vérifier les données existantes
    $stmt = $pdo->query('SELECT * FROM pharmacies LIMIT 5');
    $pharmacies = $stmt->fetchAll(PDO::FETCH_ASSOC);
    
    echo "<h2>Données de la table 'pharmacies' (5 premières lignes)</h2>";
    echo "<pre>";
    print_r($pharmacies);
    echo "</pre>";
    
} catch (PDOException $e) {
    echo "Erreur : " . $e->getMessage();
}
?>
