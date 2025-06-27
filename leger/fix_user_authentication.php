<?php
// Script pour vérifier et corriger les problèmes d'authentification

// Connexion à la base de données
$servername = "localhost";
$username = "root";
$password = "";
$dbname = "bigpharma";

// Email et mot de passe à vérifier
$emailToCheck = "tourefaliloumbacke12345@gmail.com";
$passwordToCheck = "Mrjejecool06!";

try {
    $conn = new PDO("mysql:host=$servername;dbname=$dbname", $username, $password);
    $conn->setAttribute(PDO::ATTR_ERRMODE, PDO::ERRMODE_EXCEPTION);
    
    echo "<h2>Diagnostic et réparation de l'authentification</h2>";
    
    // 1. Vérifier si l'utilisateur existe dans la table users
    $stmt = $conn->prepare("SELECT * FROM users WHERE email = ?");
    $stmt->execute([$emailToCheck]);
    $userExists = false;
    $userId = null;
    
    if ($row = $stmt->fetch(PDO::FETCH_ASSOC)) {
        $userExists = true;
        $userId = $row['id'];
        echo "<p style='color:green;'>✓ L'utilisateur existe dans la table users (ID: {$userId})</p>";
        
        // Vérifier le format du mot de passe
        $storedPassword = $row['password'];
        if (strpos($storedPassword, '$2y$') === 0) {
            echo "<p style='color:green;'>✓ Le mot de passe est au format BCrypt</p>";
            
            // Vérifier si le mot de passe est correct
            if (password_verify($passwordToCheck, $storedPassword)) {
                echo "<p style='color:green;'>✓ Le mot de passe fourni est correct</p>";
            } else {
                echo "<p style='color:red;'>✗ Le mot de passe fourni est incorrect</p>";
                // Mettre à jour le mot de passe
                $newHash = password_hash($passwordToCheck, PASSWORD_DEFAULT);
                $stmt = $conn->prepare("UPDATE users SET password = ? WHERE id = ?");
                $stmt->execute([$newHash, $userId]);
                echo "<p style='color:green;'>✓ Mot de passe mis à jour avec succès</p>";
            }
        } else {
            echo "<p style='color:red;'>✗ Le mot de passe n'est pas au format BCrypt</p>";
            // Mettre à jour le mot de passe au format BCrypt
            $newHash = password_hash($passwordToCheck, PASSWORD_DEFAULT);
            $stmt = $conn->prepare("UPDATE users SET password = ? WHERE id = ?");
            $stmt->execute([$newHash, $userId]);
            echo "<p style='color:green;'>✓ Mot de passe mis à jour au format BCrypt</p>";
        }
        
        // Vérifier si l'utilisateur a une pharmacie associée
        if (isset($row['pharmacy_id']) && !empty($row['pharmacy_id'])) {
            $pharmacyId = $row['pharmacy_id'];
            echo "<p style='color:green;'>✓ L'utilisateur est associé à la pharmacie ID: {$pharmacyId}</p>";
            
            // Vérifier si cette pharmacie existe
            $stmt = $conn->prepare("SELECT * FROM pharmacies WHERE id = ?");
            $stmt->execute([$pharmacyId]);
            if ($pharmacy = $stmt->fetch(PDO::FETCH_ASSOC)) {
                echo "<p style='color:green;'>✓ La pharmacie associée existe</p>";
            } else {
                echo "<p style='color:red;'>✗ La pharmacie associée n'existe pas</p>";
                // Créer une pharmacie
                $stmt = $conn->prepare("INSERT INTO pharmacies (id, nom, adresse) VALUES (?, 'Pharmacie par défaut', 'Adresse par défaut')");
                $stmt->execute([$pharmacyId]);
                echo "<p style='color:green;'>✓ Pharmacie créée avec l'ID: {$pharmacyId}</p>";
            }
        } else {
            echo "<p style='color:red;'>✗ L'utilisateur n'a pas de pharmacie associée</p>";
            // Créer une pharmacie et l'associer à l'utilisateur
            $stmt = $conn->prepare("INSERT INTO pharmacies (nom, adresse) VALUES ('Pharmacie par défaut', 'Adresse par défaut')");
            $stmt->execute();
            $pharmacyId = $conn->lastInsertId();
            
            $stmt = $conn->prepare("UPDATE users SET pharmacy_id = ? WHERE id = ?");
            $stmt->execute([$pharmacyId, $userId]);
            echo "<p style='color:green;'>✓ Pharmacie créée (ID: {$pharmacyId}) et associée à l'utilisateur</p>";
        }
    } else {
        echo "<p style='color:red;'>✗ L'utilisateur n'existe pas dans la table users</p>";
        
        // Créer l'utilisateur
        // D'abord, vérifier si la table pharmacies existe
        $stmt = $conn->query("SHOW TABLES LIKE 'pharmacies'");
        if ($stmt->rowCount() == 0) {
            $sql = "CREATE TABLE IF NOT EXISTS pharmacies (
                id INT(11) AUTO_INCREMENT PRIMARY KEY,
                nom VARCHAR(255) NOT NULL,
                adresse TEXT,
                telephone VARCHAR(20),
                email VARCHAR(255),
                date_creation DATETIME DEFAULT CURRENT_TIMESTAMP
            )";
            $conn->exec($sql);
            echo "<p style='color:green;'>✓ Table pharmacies créée</p>";
        }
        
        // Créer une pharmacie
        $stmt = $conn->prepare("INSERT INTO pharmacies (nom, adresse) VALUES ('Pharmacie par défaut', 'Adresse par défaut')");
        $stmt->execute();
        $pharmacyId = $conn->lastInsertId();
        echo "<p style='color:green;'>✓ Pharmacie créée avec l'ID: {$pharmacyId}</p>";
        
        // Créer l'utilisateur
        $hashedPassword = password_hash($passwordToCheck, PASSWORD_DEFAULT);
        $stmt = $conn->prepare("INSERT INTO users (email, password, pharmacy_id) VALUES (?, ?, ?)");
        $stmt->execute([$emailToCheck, $hashedPassword, $pharmacyId]);
        $userId = $conn->lastInsertId();
        echo "<p style='color:green;'>✓ Utilisateur créé avec l'ID: {$userId}</p>";
        $userExists = true;
    }
    
    // 2. Vérifier si la colonne app_access existe dans la table users
    $stmt = $conn->query("SHOW COLUMNS FROM users LIKE 'app_access'");
    if ($stmt->rowCount() > 0) {
        echo "<p style='color:green;'>✓ La colonne app_access existe dans la table users</p>";
        
        // Mettre à jour la valeur pour cet utilisateur
        if ($userExists) {
            $stmt = $conn->prepare("UPDATE users SET app_access = 'both' WHERE id = ?");
            $stmt->execute([$userId]);
            echo "<p style='color:green;'>✓ Valeur app_access mise à jour pour l'utilisateur</p>";
        }
    } else {
        echo "<p style='color:red;'>✗ La colonne app_access n'existe pas dans la table users</p>";
        
        // Ajouter la colonne
        $sql = "ALTER TABLE users ADD COLUMN app_access ENUM('web', 'heavy', 'both') NOT NULL DEFAULT 'both'";
        $conn->exec($sql);
        echo "<p style='color:green;'>✓ Colonne app_access ajoutée à la table users</p>";
    }
    
    // 3. Vérifier si l'utilisateur existe dans la table administrateurs
    $stmt = $conn->prepare("SELECT * FROM administrateurs WHERE email = ?");
    $stmt->execute([$emailToCheck]);
    
    if ($row = $stmt->fetch(PDO::FETCH_ASSOC)) {
        echo "<p style='color:green;'>✓ L'utilisateur existe déjà dans la table administrateurs</p>";
        
        // Vérifier si le mot de passe est correct
        $adminPassword = $row['password'];
        if ($adminPassword == $passwordToCheck || (strpos($adminPassword, '$2') === 0 && password_verify($passwordToCheck, $adminPassword))) {
            echo "<p style='color:green;'>✓ Le mot de passe dans la table administrateurs est correct</p>";
        } else {
            echo "<p style='color:red;'>✗ Le mot de passe dans la table administrateurs est incorrect</p>";
            
            // Mettre à jour le mot de passe
            $stmt = $conn->prepare("UPDATE administrateurs SET password = ? WHERE email = ?");
            $stmt->execute([$passwordToCheck, $emailToCheck]);
            echo "<p style='color:green;'>✓ Mot de passe mis à jour dans la table administrateurs</p>";
        }
    } else {
        echo "<p style='color:orange;'>⚠ L'utilisateur n'existe pas dans la table administrateurs</p>";
        echo "<p>Cela n'est pas un problème, car l'application Java créera automatiquement un administrateur lors de la première connexion réussie.</p>";
    }
    
    echo "<h3>Résumé des actions</h3>";
    echo "<p>Toutes les corrections nécessaires ont été appliquées. Vous pouvez maintenant essayer de vous connecter à l'application Java avec:</p>";
    echo "<ul>";
    echo "<li>Email: $emailToCheck</li>";
    echo "<li>Mot de passe: $passwordToCheck</li>";
    echo "</ul>";
    
} catch(PDOException $e) {
    echo "<h3 style='color:red;'>Erreur</h3>";
    echo "<p>" . $e->getMessage() . "</p>";
}

$conn = null;
?>
