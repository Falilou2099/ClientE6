<?php
// Connexion à la base de données
require_once 'config/database.php';

// Vérifier la structure de la table pharmacies
try {
    $pdo = $GLOBALS['pdo'];
    
    // Afficher les informations de connexion (sans le mot de passe)
    echo "<h1>Informations de connexion</h1>";
    echo "<p>Base de données: " . $GLOBALS['db_name'] . "</p>";
    
    // Lister toutes les tables de la base de données
    $stmt = $pdo->query("SHOW TABLES");
    $tables = $stmt->fetchAll(PDO::FETCH_COLUMN);
    
    echo "<h1>Tables dans la base de données</h1>";
    echo "<ul>";
    foreach ($tables as $table) {
        echo "<li>" . htmlspecialchars($table) . "</li>";
    }
    echo "</ul>";
    
    // Récupérer la structure de la table pharmacies
    $stmt = $pdo->query("DESCRIBE pharmacies");
    $columns = $stmt->fetchAll(PDO::FETCH_ASSOC);
    
    echo "<h1>Structure de la table 'pharmacies'</h1>";
    echo "<table border='1' cellpadding='5'>";
    echo "<tr><th>Field</th><th>Type</th><th>Null</th><th>Key</th><th>Default</th><th>Extra</th></tr>";
    
    foreach ($columns as $column) {
        echo "<tr>";
        foreach ($column as $key => $value) {
            echo "<td>" . htmlspecialchars($value ?? 'NULL') . "</td>";
        }
        echo "</tr>";
    }
    
    echo "</table>";
    
    // Afficher la requête CREATE TABLE
    $stmt = $pdo->query("SHOW CREATE TABLE pharmacies");
    $createTable = $stmt->fetch(PDO::FETCH_ASSOC);
    
    echo "<h1>Requête CREATE TABLE pour 'pharmacies'</h1>";
    echo "<pre>" . htmlspecialchars($createTable['Create Table'] ?? '') . "</pre>";
    
    // Vérifier les données existantes
    $stmt = $pdo->query("SELECT * FROM pharmacies LIMIT 5");
    $data = $stmt->fetchAll(PDO::FETCH_ASSOC);
    
    if (count($data) > 0) {
        echo "<h1>Données existantes (max 5 lignes)</h1>";
        echo "<table border='1' cellpadding='5'>";
        
        // En-têtes de colonnes
        echo "<tr>";
        foreach (array_keys($data[0]) as $header) {
            echo "<th>" . htmlspecialchars($header) . "</th>";
        }
        echo "</tr>";
        
        // Données
        foreach ($data as $row) {
            echo "<tr>";
            foreach ($row as $value) {
                echo "<td>" . htmlspecialchars($value ?? 'NULL') . "</td>";
            }
            echo "</tr>";
        }
        
        echo "</table>";
    } else {
        echo "<p>Aucune donnée dans la table 'pharmacies'</p>";
    }
    
} catch (PDOException $e) {
    echo "<h1>Erreur</h1>";
    echo "<p>Une erreur s'est produite : " . htmlspecialchars($e->getMessage()) . "</p>";
}
?>
