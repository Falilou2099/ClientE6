<?php
// Script pour créer un utilisateur de test avec une pharmacie associée

// Connexion à la base de données
$servername = "localhost";
$username = "root";
$password = "";
$dbname = "bigpharma";

try {
    $conn = new PDO("mysql:host=$servername;dbname=$dbname", $username, $password);
    $conn->setAttribute(PDO::ATTR_ERRMODE, PDO::ERRMODE_EXCEPTION);
    
    echo "<h2>Création d'un utilisateur de test</h2>";
    
    // Vérifier si la table pharmacies existe
    $stmt = $conn->query("SHOW TABLES LIKE 'pharmacies'");
    $pharmaciesExist = $stmt->rowCount() > 0;
    
    if (!$pharmaciesExist) {
        // Créer la table pharmacies si elle n'existe pas
        $sql = "CREATE TABLE pharmacies (
            id INT(11) AUTO_INCREMENT PRIMARY KEY,
            nom VARCHAR(255) NOT NULL,
            adresse TEXT,
            telephone VARCHAR(20),
            email VARCHAR(255),
            date_creation DATETIME DEFAULT CURRENT_TIMESTAMP
        )";
        $conn->exec($sql);
        echo "Table pharmacies créée.<br>";
    }
    
    // Vérifier si une pharmacie existe déjà
    $stmt = $conn->query("SELECT * FROM pharmacies LIMIT 1");
    $pharmacyId = 0;
    
    if ($row = $stmt->fetch(PDO::FETCH_ASSOC)) {
        $pharmacyId = $row['id'];
        echo "Pharmacie existante utilisée (ID: $pharmacyId).<br>";
    } else {
        // Créer une pharmacie
        $sql = "INSERT INTO pharmacies (nom, adresse, telephone, email) 
                VALUES ('Pharmacie Test', '123 Rue de Test', '0123456789', 'contact@pharmacietest.com')";
        $conn->exec($sql);
        $pharmacyId = $conn->lastInsertId();
        echo "Nouvelle pharmacie créée (ID: $pharmacyId).<br>";
    }
    
    // Vérifier si l'utilisateur existe déjà
    $email = "tourefaliloumbacke12345@gmail.com";
    $stmt = $conn->prepare("SELECT * FROM users WHERE email = ?");
    $stmt->execute([$email]);
    
    if ($row = $stmt->fetch(PDO::FETCH_ASSOC)) {
        // Mettre à jour l'utilisateur existant
        $sql = "UPDATE users SET pharmacy_id = ? WHERE email = ?";
        $stmt = $conn->prepare($sql);
        $stmt->execute([$pharmacyId, $email]);
        echo "Utilisateur existant mis à jour avec pharmacy_id = $pharmacyId.<br>";
    } else {
        // Créer un nouvel utilisateur
        $password = password_hash("Mrjejecool06!", PASSWORD_DEFAULT);
        $sql = "INSERT INTO users (email, password, pharmacy_id) 
                VALUES (?, ?, ?)";
        $stmt = $conn->prepare($sql);
        $stmt->execute([$email, $password, $pharmacyId]);
        echo "Nouvel utilisateur créé avec email: $email et pharmacy_id = $pharmacyId.<br>";
    }
    
    // Vérifier si la colonne app_access existe
    $stmt = $conn->query("SHOW COLUMNS FROM users LIKE 'app_access'");
    $hasAppAccess = $stmt->rowCount() > 0;
    
    if (!$hasAppAccess) {
        // Ajouter la colonne app_access
        $sql = "ALTER TABLE users ADD COLUMN app_access ENUM('web', 'heavy', 'both') NOT NULL DEFAULT 'both'";
        $conn->exec($sql);
        echo "Colonne app_access ajoutée à la table users.<br>";
    } else {
        // Mettre à jour la valeur de app_access pour cet utilisateur
        $sql = "UPDATE users SET app_access = 'both' WHERE email = ?";
        $stmt = $conn->prepare($sql);
        $stmt->execute([$email]);
        echo "Valeur app_access mise à jour pour l'utilisateur.<br>";
    }
    
    echo "<h3>Opération terminée avec succès</h3>";
    echo "Vous pouvez maintenant essayer de vous connecter à l'application Java avec:<br>";
    echo "Email: $email<br>";
    echo "Mot de passe: Mrjejecool06!<br>";
    
} catch(PDOException $e) {
    echo "Erreur : " . $e->getMessage();
}

$conn = null;
?>
