<?php
// Script pour ajouter la colonne app_access à la table users

// Connexion à la base de données
$servername = "localhost";
$username = "root";
$password = "";
$dbname = "bigpharma";

try {
    $conn = new PDO("mysql:host=$servername;dbname=$dbname", $username, $password);
    $conn->setAttribute(PDO::ATTR_ERRMODE, PDO::ERRMODE_EXCEPTION);
    
    // Vérifier si la colonne existe déjà
    $stmt = $conn->query("SHOW COLUMNS FROM users LIKE 'app_access'");
    $column_exists = $stmt->rowCount() > 0;
    
    if (!$column_exists) {
        // Ajouter la colonne app_access
        $sql = "ALTER TABLE users ADD COLUMN app_access ENUM('web', 'heavy', 'both') NOT NULL DEFAULT 'both'";
        $conn->exec($sql);
        echo "La colonne app_access a été ajoutée avec succès à la table users.<br>";
        
        // Mettre à jour tous les utilisateurs existants pour leur donner accès à l'application lourde
        $sql = "UPDATE users SET app_access = 'both'";
        $conn->exec($sql);
        echo "Tous les utilisateurs ont maintenant accès aux deux applications (web et lourde).<br>";
    } else {
        echo "La colonne app_access existe déjà dans la table users.<br>";
    }
    
    echo "Opération terminée avec succès.";
} catch(PDOException $e) {
    echo "Erreur : " . $e->getMessage();
}

$conn = null;
?>
