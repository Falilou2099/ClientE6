<?php
// Script pour résoudre le problème de connexion

// Connexion à la base de données
$servername = "localhost";
$username = "root";
$password = "";
$dbname = "bigpharma";

// Email de l'utilisateur à corriger
$email = "tourefaliloumbacke12345@gmail.com";
$plainPassword = "Mrjejecool06!";

try {
    $conn = new PDO("mysql:host=$servername;dbname=$dbname", $username, $password);
    $conn->setAttribute(PDO::ATTR_ERRMODE, PDO::ERRMODE_EXCEPTION);
    
    echo "<h2>Correction du problème de connexion</h2>";
    
    // 1. Vérifier si l'utilisateur existe dans la table users de l'application PHP
    $stmt = $conn->prepare("SELECT * FROM users WHERE email = ?");
    $stmt->execute([$email]);
    $user = $stmt->fetch(PDO::FETCH_ASSOC);
    
    if ($user) {
        echo "<p style='color:green;'>✓ L'utilisateur existe dans la table users</p>";
        
        // 2. Vérifier si l'utilisateur existe déjà dans la table administrateurs
        $stmt = $conn->prepare("SELECT * FROM administrateurs WHERE email = ?");
        $stmt->execute([$email]);
        $admin = $stmt->fetch(PDO::FETCH_ASSOC);
        
        if ($admin) {
            echo "<p style='color:orange;'>⚠ L'utilisateur existe déjà dans la table administrateurs</p>";
            
            // Mettre à jour le mot de passe et la pharmacie_id pour s'assurer qu'ils correspondent
            $pharmacyId = $user['pharmacy_id'] ?? 1;
            $userPassword = $user['password'];
            
            $sql = "UPDATE administrateurs SET password = ?, pharmacie_id = ? WHERE email = ?";
            $stmt = $conn->prepare($sql);
            $stmt->execute([$userPassword, $pharmacyId, $email]);
            
            echo "<p style='color:green;'>✓ Informations de l'administrateur mises à jour</p>";
        } else {
            echo "<p style='color:orange;'>⚠ L'utilisateur n'existe pas dans la table administrateurs</p>";
            
            // Générer un nom d'utilisateur unique
            $baseUsername = explode('@', $email)[0];
            $username = $baseUsername;
            $counter = 1;
            
            // Vérifier si le nom d'utilisateur existe déjà
            $stmt = $conn->prepare("SELECT * FROM administrateurs WHERE username = ?");
            $stmt->execute([$username]);
            
            while ($stmt->rowCount() > 0) {
                $username = $baseUsername . $counter;
                $counter++;
                $stmt = $conn->prepare("SELECT * FROM administrateurs WHERE username = ?");
                $stmt->execute([$username]);
            }
            
            // Récupérer la pharmacie_id
            $pharmacyId = $user['pharmacy_id'] ?? null;
            
            // Si l'utilisateur n'a pas de pharmacie, en créer une
            if (empty($pharmacyId)) {
                $sql = "INSERT INTO pharmacies (nom) VALUES ('Pharmacie de $email')";
                $conn->exec($sql);
                $pharmacyId = $conn->lastInsertId();
                
                // Mettre à jour l'utilisateur avec la nouvelle pharmacie
                $sql = "UPDATE users SET pharmacy_id = ? WHERE email = ?";
                $stmt = $conn->prepare($sql);
                $stmt->execute([$pharmacyId, $email]);
                
                echo "<p style='color:green;'>✓ Nouvelle pharmacie créée (ID: $pharmacyId) et associée à l'utilisateur</p>";
            } else {
                // Vérifier si la pharmacie existe
                $stmt = $conn->prepare("SELECT * FROM pharmacies WHERE id = ?");
                $stmt->execute([$pharmacyId]);
                
                if ($stmt->rowCount() == 0) {
                    $sql = "INSERT INTO pharmacies (id, nom) VALUES (?, 'Pharmacie de $email')";
                    $stmt = $conn->prepare($sql);
                    $stmt->execute([$pharmacyId]);
                    
                    echo "<p style='color:green;'>✓ Pharmacie créée avec l'ID: $pharmacyId</p>";
                }
            }
            
            // Créer l'administrateur avec le nom d'utilisateur unique
            $userPassword = $user['password'];
            
            $sql = "INSERT INTO administrateurs (username, password, email, pharmacie_id) VALUES (?, ?, ?, ?)";
            $stmt = $conn->prepare($sql);
            $stmt->execute([$username, $userPassword, $email, $pharmacyId]);
            
            echo "<p style='color:green;'>✓ Administrateur créé avec le nom d'utilisateur: $username</p>";
        }
    } else {
        echo "<p style='color:red;'>✗ L'utilisateur n'existe pas dans la table users</p>";
        
        // Vérifier la structure de la table users
        $stmt = $conn->query("DESCRIBE users");
        $columns = $stmt->fetchAll(PDO::FETCH_COLUMN);
        
        echo "<p>Colonnes de la table users: " . implode(", ", $columns) . "</p>";
        
        // Afficher tous les utilisateurs
        $stmt = $conn->query("SELECT email FROM users");
        $users = $stmt->fetchAll(PDO::FETCH_COLUMN);
        
        echo "<p>Utilisateurs existants:</p>";
        echo "<ul>";
        foreach ($users as $userEmail) {
            echo "<li>$userEmail</li>";
        }
        echo "</ul>";
        
        // Créer l'utilisateur dans la table users
        $hashedPassword = password_hash($plainPassword, PASSWORD_DEFAULT);
        
        // Créer une pharmacie
        $sql = "INSERT INTO pharmacies (nom) VALUES ('Pharmacie de $email')";
        $conn->exec($sql);
        $pharmacyId = $conn->lastInsertId();
        
        // Ajouter l'utilisateur à la table users
        $sql = "INSERT INTO users (email, password, pharmacy_id) VALUES (?, ?, ?)";
        $stmt = $conn->prepare($sql);
        $stmt->execute([$email, $hashedPassword, $pharmacyId]);
        
        echo "<p style='color:green;'>✓ Utilisateur créé dans la table users</p>";
        
        // Générer un nom d'utilisateur unique
        $baseUsername = explode('@', $email)[0];
        $username = $baseUsername;
        $counter = 1;
        
        // Vérifier si le nom d'utilisateur existe déjà
        $stmt = $conn->prepare("SELECT * FROM administrateurs WHERE username = ?");
        $stmt->execute([$username]);
        
        while ($stmt->rowCount() > 0) {
            $username = $baseUsername . $counter;
            $counter++;
            $stmt = $conn->prepare("SELECT * FROM administrateurs WHERE username = ?");
            $stmt->execute([$username]);
        }
        
        // Ajouter l'utilisateur à la table administrateurs
        $sql = "INSERT INTO administrateurs (username, password, email, pharmacie_id) VALUES (?, ?, ?, ?)";
        $stmt = $conn->prepare($sql);
        $stmt->execute([$username, $hashedPassword, $email, $pharmacyId]);
        
        echo "<p style='color:green;'>✓ Administrateur créé avec le nom d'utilisateur: $username</p>";
    }
    
    echo "<h3>Opération terminée</h3>";
    echo "<p>Vous pouvez maintenant vous connecter à l'application Java avec les identifiants suivants:</p>";
    echo "<ul>";
    echo "<li>Email: $email</li>";
    echo "<li>Mot de passe: $plainPassword</li>";
    echo "</ul>";
    
} catch(PDOException $e) {
    echo "<h3 style='color:red;'>Erreur</h3>";
    echo "<p>" . $e->getMessage() . "</p>";
    
    // Afficher la trace de l'erreur pour le débogage
    echo "<pre>";
    print_r($e->getTrace());
    echo "</pre>";
}

$conn = null;
?>
