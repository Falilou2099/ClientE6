<?php
// Script pour vérifier la structure des tables users et pharmacies
require_once 'config/database.php';

try {
    $pdo = $GLOBALS['pdo'];
    
    echo "<h1>Vérification des tables users et pharmacies</h1>";
    
    // Vérifier la structure de la table users
    $stmt = $pdo->query("DESCRIBE users");
    $userColumns = $stmt->fetchAll(PDO::FETCH_ASSOC);
    
    echo "<h2>Structure de la table 'users'</h2>";
    echo "<table border='1' cellpadding='5'>";
    echo "<tr><th>Field</th><th>Type</th><th>Null</th><th>Key</th><th>Default</th><th>Extra</th></tr>";
    
    foreach ($userColumns as $column) {
        echo "<tr>";
        foreach ($column as $key => $value) {
            echo "<td>" . htmlspecialchars($value ?? 'NULL') . "</td>";
        }
        echo "</tr>";
    }
    
    echo "</table>";
    
    // Vérifier si la colonne pharmacy_id existe dans la table users
    $hasPharmacyIdColumn = false;
    foreach ($userColumns as $column) {
        if ($column['Field'] === 'pharmacy_id') {
            $hasPharmacyIdColumn = true;
            break;
        }
    }
    
    echo "<p>La colonne 'pharmacy_id' " . ($hasPharmacyIdColumn ? "existe" : "n'existe pas") . " dans la table 'users'.</p>";
    
    // Vérifier la structure de la table pharmacies
    $stmt = $pdo->query("DESCRIBE pharmacies");
    $pharmacyColumns = $stmt->fetchAll(PDO::FETCH_ASSOC);
    
    echo "<h2>Structure de la table 'pharmacies'</h2>";
    echo "<table border='1' cellpadding='5'>";
    echo "<tr><th>Field</th><th>Type</th><th>Null</th><th>Key</th><th>Default</th><th>Extra</th></tr>";
    
    foreach ($pharmacyColumns as $column) {
        echo "<tr>";
        foreach ($column as $key => $value) {
            echo "<td>" . htmlspecialchars($value ?? 'NULL') . "</td>";
        }
        echo "</tr>";
    }
    
    echo "</table>";
    
    // Vérifier les données existantes dans la table users
    $stmt = $pdo->query("SELECT * FROM users LIMIT 5");
    $users = $stmt->fetchAll(PDO::FETCH_ASSOC);
    
    echo "<h2>Données de la table 'users' (max 5 lignes)</h2>";
    if (count($users) > 0) {
        echo "<table border='1' cellpadding='5'>";
        
        // En-têtes de colonnes
        echo "<tr>";
        foreach (array_keys($users[0]) as $header) {
            echo "<th>" . htmlspecialchars($header) . "</th>";
        }
        echo "</tr>";
        
        // Données
        foreach ($users as $user) {
            echo "<tr>";
            foreach ($user as $key => $value) {
                if ($key === 'password') {
                    echo "<td>[MASQUÉ]</td>";
                } else {
                    echo "<td>" . htmlspecialchars($value ?? 'NULL') . "</td>";
                }
            }
            echo "</tr>";
        }
        
        echo "</table>";
    } else {
        echo "<p>Aucune donnée dans la table 'users'</p>";
    }
    
    // Vérifier les données existantes dans la table pharmacies
    $stmt = $pdo->query("SELECT * FROM pharmacies LIMIT 5");
    $pharmacies = $stmt->fetchAll(PDO::FETCH_ASSOC);
    
    echo "<h2>Données de la table 'pharmacies' (max 5 lignes)</h2>";
    if (count($pharmacies) > 0) {
        echo "<table border='1' cellpadding='5'>";
        
        // En-têtes de colonnes
        echo "<tr>";
        foreach (array_keys($pharmacies[0]) as $header) {
            echo "<th>" . htmlspecialchars($header) . "</th>";
        }
        echo "</tr>";
        
        // Données
        foreach ($pharmacies as $pharmacy) {
            echo "<tr>";
            foreach ($pharmacy as $value) {
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

<style>
    body {
        font-family: Arial, sans-serif;
        margin: 20px;
        line-height: 1.6;
    }
    h1, h2 {
        color: #333;
    }
    table {
        border-collapse: collapse;
        margin-bottom: 20px;
        width: 100%;
    }
    th {
        background-color: #f2f2f2;
        text-align: left;
    }
    td, th {
        padding: 8px;
        border: 1px solid #ddd;
    }
</style>
