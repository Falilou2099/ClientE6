<?php
// Script pour vérifier un utilisateur spécifique

// Connexion à la base de données
$servername = "localhost";
$username = "root";
$password = "";
$dbname = "bigpharma";

// Email à vérifier
$emailToCheck = "tourefaliloumbacke12345@gmail.com";

try {
    $conn = new PDO("mysql:host=$servername;dbname=$dbname", $username, $password);
    $conn->setAttribute(PDO::ATTR_ERRMODE, PDO::ERRMODE_EXCEPTION);
    
    echo "<h2>Vérification de l'utilisateur</h2>";
    echo "Email recherché: " . htmlspecialchars($emailToCheck) . "<br><br>";
    
    // Vérifier dans la table users
    $stmt = $conn->prepare("SELECT * FROM users WHERE email = ?");
    $stmt->execute([$emailToCheck]);
    
    if ($row = $stmt->fetch(PDO::FETCH_ASSOC)) {
        echo "<h3>Utilisateur trouvé dans la table users</h3>";
        echo "<table border='1'>";
        echo "<tr>";
        foreach ($row as $key => $value) {
            echo "<th>" . htmlspecialchars($key) . "</th>";
        }
        echo "</tr>";
        
        echo "<tr>";
        foreach ($row as $key => $value) {
            if ($key == 'password') {
                echo "<td>[Mot de passe hashé]</td>";
            } else {
                echo "<td>" . htmlspecialchars($value ?? 'NULL') . "</td>";
            }
        }
        echo "</tr>";
        echo "</table>";
        
        // Vérifier si la colonne pharmacy_id existe et a une valeur
        if (isset($row['pharmacy_id'])) {
            $pharmacyId = $row['pharmacy_id'];
            echo "<br>Pharmacy ID: " . $pharmacyId . "<br>";
            
            // Vérifier si cette pharmacie existe dans la table pharmacies
            $stmt = $conn->prepare("SELECT * FROM pharmacies WHERE id = ?");
            $stmt->execute([$pharmacyId]);
            
            if ($pharmacy = $stmt->fetch(PDO::FETCH_ASSOC)) {
                echo "<h3>Pharmacie associée trouvée</h3>";
                echo "<table border='1'>";
                echo "<tr>";
                foreach ($pharmacy as $key => $value) {
                    echo "<th>" . htmlspecialchars($key) . "</th>";
                }
                echo "</tr>";
                
                echo "<tr>";
                foreach ($pharmacy as $value) {
                    echo "<td>" . htmlspecialchars($value ?? 'NULL') . "</td>";
                }
                echo "</tr>";
                echo "</table>";
            } else {
                echo "<h3 style='color:red;'>Aucune pharmacie trouvée avec l'ID: " . $pharmacyId . "</h3>";
                echo "Cela peut causer des problèmes lors de la connexion à l'application Java.";
            }
        } else {
            echo "<h3 style='color:red;'>La colonne pharmacy_id n'existe pas ou est NULL</h3>";
            echo "Cela peut causer des problèmes lors de la connexion à l'application Java.";
        }
    } else {
        echo "<h3 style='color:red;'>Aucun utilisateur trouvé avec cet email dans la table users</h3>";
    }
    
    // Vérifier dans la table administrateurs
    $stmt = $conn->prepare("SELECT * FROM administrateurs WHERE email = ?");
    $stmt->execute([$emailToCheck]);
    
    if ($row = $stmt->fetch(PDO::FETCH_ASSOC)) {
        echo "<h3>Utilisateur trouvé dans la table administrateurs</h3>";
        echo "<table border='1'>";
        echo "<tr>";
        foreach ($row as $key => $value) {
            echo "<th>" . htmlspecialchars($key) . "</th>";
        }
        echo "</tr>";
        
        echo "<tr>";
        foreach ($row as $key => $value) {
            if ($key == 'password') {
                echo "<td>[Mot de passe hashé]</td>";
            } else {
                echo "<td>" . htmlspecialchars($value ?? 'NULL') . "</td>";
            }
        }
        echo "</tr>";
        echo "</table>";
    } else {
        echo "<h3>Aucun utilisateur trouvé avec cet email dans la table administrateurs</h3>";
    }
    
} catch(PDOException $e) {
    echo "Erreur : " . $e->getMessage();
}

$conn = null;
?>
