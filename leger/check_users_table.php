<?php
// Script pour vérifier la structure de la table users

// Connexion à la base de données
$servername = "localhost";
$username = "root";
$password = "";
$dbname = "bigpharma";

try {
    $conn = new PDO("mysql:host=$servername;dbname=$dbname", $username, $password);
    $conn->setAttribute(PDO::ATTR_ERRMODE, PDO::ERRMODE_EXCEPTION);
    
    echo "<h2>Structure de la table users</h2>";
    
    // Vérifier si la table users existe
    $stmt = $conn->query("SHOW TABLES LIKE 'users'");
    if ($stmt->rowCount() > 0) {
        echo "La table users existe.<br>";
        
        // Afficher la structure de la table
        $stmt = $conn->query("DESCRIBE users");
        echo "<h3>Colonnes de la table users</h3>";
        echo "<table border='1'>";
        echo "<tr><th>Nom</th><th>Type</th><th>Null</th><th>Clé</th><th>Défaut</th><th>Extra</th></tr>";
        while ($row = $stmt->fetch(PDO::FETCH_ASSOC)) {
            echo "<tr>";
            foreach ($row as $key => $value) {
                echo "<td>" . htmlspecialchars($value ?? 'NULL') . "</td>";
            }
            echo "</tr>";
        }
        echo "</table>";
        
        // Afficher les données de la table
        $stmt = $conn->query("SELECT * FROM users");
        echo "<h3>Données de la table users</h3>";
        if ($stmt->rowCount() > 0) {
            echo "<table border='1'>";
            // Entêtes de colonnes
            $first = true;
            while ($row = $stmt->fetch(PDO::FETCH_ASSOC)) {
                if ($first) {
                    echo "<tr>";
                    foreach ($row as $key => $value) {
                        echo "<th>" . htmlspecialchars($key) . "</th>";
                    }
                    echo "</tr>";
                    $first = false;
                }
                
                echo "<tr>";
                foreach ($row as $value) {
                    // Masquer les mots de passe pour la sécurité
                    if (strpos($value, '$2y$') === 0) {
                        echo "<td>[Mot de passe hashé]</td>";
                    } else {
                        echo "<td>" . htmlspecialchars($value ?? 'NULL') . "</td>";
                    }
                }
                echo "</tr>";
            }
            echo "</table>";
        } else {
            echo "Aucune donnée dans la table users.";
        }
    } else {
        echo "La table users n'existe pas.";
    }
    
    // Vérifier si la table pharmacies existe
    $stmt = $conn->query("SHOW TABLES LIKE 'pharmacies'");
    if ($stmt->rowCount() > 0) {
        echo "<h2>Structure de la table pharmacies</h2>";
        
        // Afficher la structure de la table
        $stmt = $conn->query("DESCRIBE pharmacies");
        echo "<h3>Colonnes de la table pharmacies</h3>";
        echo "<table border='1'>";
        echo "<tr><th>Nom</th><th>Type</th><th>Null</th><th>Clé</th><th>Défaut</th><th>Extra</th></tr>";
        while ($row = $stmt->fetch(PDO::FETCH_ASSOC)) {
            echo "<tr>";
            foreach ($row as $key => $value) {
                echo "<td>" . htmlspecialchars($value ?? 'NULL') . "</td>";
            }
            echo "</tr>";
        }
        echo "</table>";
        
        // Afficher les données de la table
        $stmt = $conn->query("SELECT * FROM pharmacies");
        echo "<h3>Données de la table pharmacies</h3>";
        if ($stmt->rowCount() > 0) {
            echo "<table border='1'>";
            // Entêtes de colonnes
            $first = true;
            while ($row = $stmt->fetch(PDO::FETCH_ASSOC)) {
                if ($first) {
                    echo "<tr>";
                    foreach ($row as $key => $value) {
                        echo "<th>" . htmlspecialchars($key) . "</th>";
                    }
                    echo "</tr>";
                    $first = false;
                }
                
                echo "<tr>";
                foreach ($row as $value) {
                    echo "<td>" . htmlspecialchars($value ?? 'NULL') . "</td>";
                }
                echo "</tr>";
            }
            echo "</table>";
        } else {
            echo "Aucune donnée dans la table pharmacies.";
        }
    } else {
        echo "La table pharmacies n'existe pas.";
    }
    
} catch(PDOException $e) {
    echo "Erreur : " . $e->getMessage();
}

$conn = null;
?>
