<?php
// Script pour créer un utilisateur spécifique dans la base de données

// Connexion à la base de données
$servername = "localhost";
$username = "root";
$password = "";
$dbname = "bigpharma";

// Informations de l'utilisateur à créer
$email = "tourefaliloumbacke12345@gmail.com";
$plainPassword = "Mrjejecool06!";
$hashedPassword = password_hash($plainPassword, PASSWORD_DEFAULT);

try {
    $conn = new PDO("mysql:host=$servername;dbname=$dbname", $username, $password);
    $conn->setAttribute(PDO::ATTR_ERRMODE, PDO::ERRMODE_EXCEPTION);
    
    echo "<h2>Création d'un utilisateur spécifique</h2>";
    
    // 1. Vérifier si la table users existe
    $stmt = $conn->query("SHOW TABLES LIKE 'users'");
    if ($stmt->rowCount() == 0) {
        // Créer la table users si elle n'existe pas
        $sql = "CREATE TABLE users (
            id INT(11) AUTO_INCREMENT PRIMARY KEY,
            email VARCHAR(255) NOT NULL UNIQUE,
            password VARCHAR(255) NOT NULL,
            pharmacy_id INT(11),
            created_at DATETIME DEFAULT CURRENT_TIMESTAMP
        )";
        $conn->exec($sql);
        echo "<p>Table users créée.</p>";
    }
    
    // 2. Vérifier si la table pharmacies existe
    $stmt = $conn->query("SHOW TABLES LIKE 'pharmacies'");
    if ($stmt->rowCount() == 0) {
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
        echo "<p>Table pharmacies créée.</p>";
    }
    
    // 3. Créer une pharmacie par défaut si aucune n'existe
    $stmt = $conn->query("SELECT * FROM pharmacies LIMIT 1");
    $pharmacyId = 0;
    
    if ($stmt->rowCount() == 0) {
        // Aucune pharmacie n'existe, en créer une
        $sql = "INSERT INTO pharmacies (nom, adresse) VALUES ('Pharmacie par défaut', 'Adresse par défaut')";
        $conn->exec($sql);
        $pharmacyId = $conn->lastInsertId();
        echo "<p>Pharmacie par défaut créée avec ID: $pharmacyId</p>";
    } else {
        // Utiliser la première pharmacie existante
        $row = $stmt->fetch(PDO::FETCH_ASSOC);
        $pharmacyId = $row['id'];
        echo "<p>Utilisation de la pharmacie existante avec ID: $pharmacyId</p>";
    }
    
    // 4. Vérifier si l'utilisateur existe déjà
    $stmt = $conn->prepare("SELECT * FROM users WHERE email = ?");
    $stmt->execute([$email]);
    
    if ($stmt->rowCount() > 0) {
        // L'utilisateur existe déjà, mettre à jour son mot de passe et sa pharmacie
        $sql = "UPDATE users SET password = ?, pharmacy_id = ? WHERE email = ?";
        $stmt = $conn->prepare($sql);
        $stmt->execute([$hashedPassword, $pharmacyId, $email]);
        echo "<p>Utilisateur existant mis à jour.</p>";
    } else {
        // Créer le nouvel utilisateur
        $sql = "INSERT INTO users (email, password, pharmacy_id) VALUES (?, ?, ?)";
        $stmt = $conn->prepare($sql);
        $stmt->execute([$email, $hashedPassword, $pharmacyId]);
        echo "<p>Nouvel utilisateur créé.</p>";
    }
    
    // 5. Vérifier si la colonne app_access existe dans la table users
    $stmt = $conn->query("SHOW COLUMNS FROM users LIKE 'app_access'");
    if ($stmt->rowCount() == 0) {
        // Ajouter la colonne app_access
        $sql = "ALTER TABLE users ADD COLUMN app_access ENUM('web', 'heavy', 'both') NOT NULL DEFAULT 'both'";
        $conn->exec($sql);
        echo "<p>Colonne app_access ajoutée à la table users.</p>";
    } else {
        // Mettre à jour la valeur de app_access pour cet utilisateur
        $sql = "UPDATE users SET app_access = 'both' WHERE email = ?";
        $stmt = $conn->prepare($sql);
        $stmt->execute([$email]);
        echo "<p>Valeur app_access mise à jour pour l'utilisateur.</p>";
    }
    
    echo "<h3>Opération terminée avec succès</h3>";
    echo "<p>Vous pouvez maintenant vous connecter à l'application Java avec:</p>";
    echo "<ul>";
    echo "<li>Email: $email</li>";
    echo "<li>Mot de passe: $plainPassword</li>";
    echo "</ul>";
    
    // Afficher les informations de l'utilisateur créé
    $stmt = $conn->prepare("SELECT * FROM users WHERE email = ?");
    $stmt->execute([$email]);
    $user = $stmt->fetch(PDO::FETCH_ASSOC);
    
    echo "<h3>Informations de l'utilisateur créé</h3>";
    echo "<table border='1'>";
    echo "<tr><th>ID</th><th>Email</th><th>Pharmacy ID</th><th>App Access</th></tr>";
    echo "<tr>";
    echo "<td>" . $user['id'] . "</td>";
    echo "<td>" . $user['email'] . "</td>";
    echo "<td>" . $user['pharmacy_id'] . "</td>";
    echo "<td>" . ($user['app_access'] ?? 'both') . "</td>";
    echo "</tr>";
    echo "</table>";
    
} catch(PDOException $e) {
    echo "<h3 style='color:red;'>Erreur</h3>";
    echo "<p>" . $e->getMessage() . "</p>";
}

$conn = null;
?>
