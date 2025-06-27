<?php
// Script pour vérifier et corriger un utilisateur spécifique

// Connexion à la base de données
$servername = "localhost";
$username = "root";
$password = "";
$dbname = "bigpharma";

// Email de l'utilisateur à vérifier
$email = "tourefaliloumbacke12345@gmail.com";

try {
    $conn = new PDO("mysql:host=$servername;dbname=$dbname", $username, $password);
    $conn->setAttribute(PDO::ATTR_ERRMODE, PDO::ERRMODE_EXCEPTION);
    
    echo "<h2>Vérification et correction de l'utilisateur</h2>";
    
    // 1. Afficher toutes les tables de la base de données
    $tables = $conn->query("SHOW TABLES")->fetchAll(PDO::FETCH_COLUMN);
    
    echo "<h3>Tables dans la base de données</h3>";
    echo "<ul>";
    foreach ($tables as $table) {
        echo "<li>$table</li>";
    }
    echo "</ul>";
    
    // 2. Vérifier si la table users existe
    if (!in_array('users', $tables)) {
        echo "<p style='color:red;'>La table users n'existe pas dans la base de données!</p>";
    } else {
        // 3. Vérifier la structure de la table users
        $columns = $conn->query("DESCRIBE users")->fetchAll(PDO::FETCH_COLUMN);
        
        echo "<h3>Structure de la table users</h3>";
        echo "<ul>";
        foreach ($columns as $column) {
            echo "<li>$column</li>";
        }
        echo "</ul>";
        
        // 4. Vérifier si l'utilisateur existe dans la table users
        $stmt = $conn->prepare("SELECT * FROM users WHERE email = ?");
        $stmt->execute([$email]);
        $user = $stmt->fetch(PDO::FETCH_ASSOC);
        
        if ($user) {
            echo "<h3 style='color:green;'>L'utilisateur existe dans la table users</h3>";
            echo "<table border='1'>";
            echo "<tr>";
            foreach ($user as $key => $value) {
                echo "<th>$key</th>";
            }
            echo "</tr>";
            echo "<tr>";
            foreach ($user as $key => $value) {
                if ($key == 'password') {
                    echo "<td>[Mot de passe hashé]</td>";
                } else {
                    echo "<td>$value</td>";
                }
            }
            echo "</tr>";
            echo "</table>";
            
            // 5. Vérifier si l'utilisateur a une pharmacie associée
            $pharmacyId = $user['pharmacy_id'] ?? null;
            
            if (empty($pharmacyId)) {
                echo "<p style='color:orange;'>L'utilisateur n'a pas de pharmacie associée.</p>";
                
                // Créer une pharmacie pour cet utilisateur
                $sql = "INSERT INTO pharmacies (nom) VALUES ('Pharmacie de $email')";
                $conn->exec($sql);
                $pharmacyId = $conn->lastInsertId();
                
                // Mettre à jour l'utilisateur avec la nouvelle pharmacie
                $sql = "UPDATE users SET pharmacy_id = ? WHERE email = ?";
                $stmt = $conn->prepare($sql);
                $stmt->execute([$pharmacyId, $email]);
                
                echo "<p style='color:green;'>Nouvelle pharmacie créée (ID: $pharmacyId) et associée à l'utilisateur.</p>";
            } else {
                echo "<p style='color:green;'>L'utilisateur est associé à la pharmacie ID: $pharmacyId</p>";
                
                // Vérifier si la pharmacie existe
                $stmt = $conn->prepare("SELECT * FROM pharmacies WHERE id = ?");
                $stmt->execute([$pharmacyId]);
                
                if ($stmt->rowCount() == 0) {
                    echo "<p style='color:orange;'>La pharmacie associée n'existe pas.</p>";
                    
                    // Créer la pharmacie
                    $sql = "INSERT INTO pharmacies (id, nom) VALUES (?, 'Pharmacie de $email')";
                    $stmt = $conn->prepare($sql);
                    $stmt->execute([$pharmacyId]);
                    
                    echo "<p style='color:green;'>Pharmacie créée avec l'ID: $pharmacyId</p>";
                } else {
                    echo "<p style='color:green;'>La pharmacie associée existe.</p>";
                }
            }
            
            // 6. Vérifier si l'utilisateur existe dans la table administrateurs
            if (!in_array('administrateurs', $tables)) {
                echo "<p style='color:red;'>La table administrateurs n'existe pas!</p>";
                
                // Créer la table administrateurs
                $sql = "CREATE TABLE administrateurs (
                    id INT(11) AUTO_INCREMENT PRIMARY KEY,
                    username VARCHAR(255) NOT NULL,
                    password VARCHAR(255) NOT NULL,
                    nom VARCHAR(255),
                    prenom VARCHAR(255),
                    email VARCHAR(255) NOT NULL UNIQUE,
                    pharmacie_id INT(11),
                    created_at DATETIME DEFAULT CURRENT_TIMESTAMP
                )";
                $conn->exec($sql);
                echo "<p style='color:green;'>Table administrateurs créée.</p>";
            }
            
            // Vérifier si l'utilisateur existe dans la table administrateurs
            $stmt = $conn->prepare("SELECT * FROM administrateurs WHERE email = ?");
            $stmt->execute([$email]);
            
            if ($stmt->rowCount() == 0) {
                echo "<p style='color:orange;'>L'utilisateur n'existe pas dans la table administrateurs.</p>";
                
                // Créer l'administrateur
                $username = explode('@', $email)[0];
                $password = $user['password'];
                
                $sql = "INSERT INTO administrateurs (username, password, email, pharmacie_id) VALUES (?, ?, ?, ?)";
                $stmt = $conn->prepare($sql);
                $stmt->execute([$username, $password, $email, $pharmacyId]);
                
                echo "<p style='color:green;'>Utilisateur ajouté à la table administrateurs avec succès.</p>";
            } else {
                echo "<p style='color:green;'>L'utilisateur existe déjà dans la table administrateurs.</p>";
                
                // Mettre à jour le mot de passe et la pharmacie pour s'assurer qu'ils sont synchronisés
                $password = $user['password'];
                
                $sql = "UPDATE administrateurs SET password = ?, pharmacie_id = ? WHERE email = ?";
                $stmt = $conn->prepare($sql);
                $stmt->execute([$password, $pharmacyId, $email]);
                
                echo "<p style='color:green;'>Informations de l'administrateur mises à jour.</p>";
            }
        } else {
            echo "<h3 style='color:red;'>L'utilisateur n'existe pas dans la table users!</h3>";
            
            // Afficher tous les utilisateurs de la table users
            $stmt = $conn->query("SELECT email FROM users");
            $users = $stmt->fetchAll(PDO::FETCH_COLUMN);
            
            echo "<h4>Utilisateurs existants dans la table users</h4>";
            if (count($users) > 0) {
                echo "<ul>";
                foreach ($users as $userEmail) {
                    echo "<li>$userEmail</li>";
                }
                echo "</ul>";
            } else {
                echo "<p>Aucun utilisateur trouvé dans la table users.</p>";
            }
            
            // Vérifier si l'utilisateur existe dans une autre table (par exemple, utilisateurs)
            if (in_array('utilisateurs', $tables)) {
                $stmt = $conn->prepare("SELECT * FROM utilisateurs WHERE email = ?");
                $stmt->execute([$email]);
                $oldUser = $stmt->fetch(PDO::FETCH_ASSOC);
                
                if ($oldUser) {
                    echo "<p style='color:green;'>L'utilisateur a été trouvé dans l'ancienne table utilisateurs.</p>";
                    
                    // Migrer l'utilisateur vers la table users
                    $sql = "INSERT INTO users (email, password) VALUES (?, ?)";
                    $stmt = $conn->prepare($sql);
                    $stmt->execute([$email, $oldUser['password']]);
                    $userId = $conn->lastInsertId();
                    
                    echo "<p style='color:green;'>Utilisateur migré vers la table users avec ID: $userId</p>";
                    
                    // Rafraîchir la page pour voir les résultats de la migration
                    echo "<script>window.location.reload();</script>";
                } else {
                    echo "<p style='color:red;'>L'utilisateur n'a pas été trouvé dans l'ancienne table utilisateurs non plus.</p>";
                }
            }
        }
    }
    
    echo "<h3>Que faire maintenant?</h3>";
    echo "<p>1. Si l'utilisateur a été correctement ajouté à la table administrateurs, essayez de vous connecter à l'application Java.</p>";
    echo "<p>2. Si l'utilisateur n'existe pas dans la table users, vous devez d'abord l'ajouter à cette table.</p>";
    
    // Formulaire pour ajouter manuellement l'utilisateur
    echo "<h3>Ajouter manuellement l'utilisateur</h3>";
    echo "<form method='post'>";
    echo "<input type='hidden' name='action' value='add_user'>";
    echo "<label>Email: </label><input type='email' name='email' value='$email'><br>";
    echo "<label>Mot de passe: </label><input type='password' name='password' value='Mrjejecool06!'><br>";
    echo "<input type='submit' value='Ajouter l'utilisateur'>";
    echo "</form>";
    
    // Traiter le formulaire
    if ($_SERVER['REQUEST_METHOD'] === 'POST' && isset($_POST['action']) && $_POST['action'] === 'add_user') {
        $email = $_POST['email'];
        $password = password_hash($_POST['password'], PASSWORD_DEFAULT);
        
        // Créer une pharmacie
        $sql = "INSERT INTO pharmacies (nom) VALUES ('Pharmacie de $email')";
        $conn->exec($sql);
        $pharmacyId = $conn->lastInsertId();
        
        // Ajouter l'utilisateur à la table users
        $sql = "INSERT INTO users (email, password, pharmacy_id) VALUES (?, ?, ?)";
        $stmt = $conn->prepare($sql);
        $stmt->execute([$email, $password, $pharmacyId]);
        $userId = $conn->lastInsertId();
        
        // Ajouter l'utilisateur à la table administrateurs
        $username = explode('@', $email)[0];
        $sql = "INSERT INTO administrateurs (username, password, email, pharmacie_id) VALUES (?, ?, ?, ?)";
        $stmt = $conn->prepare($sql);
        $stmt->execute([$username, $password, $email, $pharmacyId]);
        
        echo "<p style='color:green;'>Utilisateur ajouté avec succès aux tables users et administrateurs.</p>";
        
        // Rafraîchir la page pour voir les résultats
        echo "<script>window.location.reload();</script>";
    }
    
} catch(PDOException $e) {
    echo "<h3 style='color:red;'>Erreur</h3>";
    echo "<p>" . $e->getMessage() . "</p>";
}

$conn = null;
?>
