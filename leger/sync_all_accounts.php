<?php
// Script pour synchroniser tous les comptes entre l'application PHP et l'application Java

// Connexion à la base de données
$servername = "localhost";
$username = "root";
$password = "";
$dbname = "bigpharma";

try {
    $conn = new PDO("mysql:host=$servername;dbname=$dbname", $username, $password);
    $conn->setAttribute(PDO::ATTR_ERRMODE, PDO::ERRMODE_EXCEPTION);
    
    echo "<h2>Synchronisation des comptes PHP et Java</h2>";
    
    // 1. Vérifier si les tables nécessaires existent
    $tables = ['users', 'pharmacies', 'administrateurs'];
    $missingTables = [];
    
    foreach ($tables as $table) {
        $stmt = $conn->query("SHOW TABLES LIKE '$table'");
        if ($stmt->rowCount() == 0) {
            $missingTables[] = $table;
        }
    }
    
    if (!empty($missingTables)) {
        echo "<h3 style='color:red;'>Tables manquantes</h3>";
        echo "<p>Les tables suivantes sont manquantes et doivent être créées :</p>";
        echo "<ul>";
        foreach ($missingTables as $table) {
            echo "<li>$table</li>";
        }
        echo "</ul>";
        
        // Créer les tables manquantes
        if (in_array('pharmacies', $missingTables)) {
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
        
        if (in_array('users', $missingTables)) {
            $sql = "CREATE TABLE users (
                id INT(11) AUTO_INCREMENT PRIMARY KEY,
                email VARCHAR(255) NOT NULL UNIQUE,
                password VARCHAR(255) NOT NULL,
                pharmacy_id INT(11),
                app_access ENUM('web', 'heavy', 'both') NOT NULL DEFAULT 'both',
                created_at DATETIME DEFAULT CURRENT_TIMESTAMP
            )";
            $conn->exec($sql);
            echo "<p>Table users créée.</p>";
        }
        
        if (in_array('administrateurs', $missingTables)) {
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
            echo "<p>Table administrateurs créée.</p>";
        }
    } else {
        echo "<p style='color:green;'>Toutes les tables nécessaires existent.</p>";
    }
    
    // 2. Vérifier si la colonne app_access existe dans la table users
    $stmt = $conn->query("SHOW COLUMNS FROM users LIKE 'app_access'");
    if ($stmt->rowCount() == 0) {
        // Ajouter la colonne app_access
        $sql = "ALTER TABLE users ADD COLUMN app_access ENUM('web', 'heavy', 'both') NOT NULL DEFAULT 'both'";
        $conn->exec($sql);
        echo "<p>Colonne app_access ajoutée à la table users.</p>";
    } else {
        echo "<p style='color:green;'>La colonne app_access existe déjà dans la table users.</p>";
    }
    
    // 3. Vérifier si la colonne pharmacy_id existe dans la table users
    $stmt = $conn->query("SHOW COLUMNS FROM users LIKE 'pharmacy_id'");
    if ($stmt->rowCount() == 0) {
        // Ajouter la colonne pharmacy_id
        $sql = "ALTER TABLE users ADD COLUMN pharmacy_id INT(11)";
        $conn->exec($sql);
        echo "<p>Colonne pharmacy_id ajoutée à la table users.</p>";
    } else {
        echo "<p style='color:green;'>La colonne pharmacy_id existe déjà dans la table users.</p>";
    }
    
    // 4. Récupérer tous les utilisateurs de la table users
    $stmt = $conn->query("SELECT * FROM users");
    $users = $stmt->fetchAll(PDO::FETCH_ASSOC);
    
    echo "<h3>Synchronisation des comptes</h3>";
    echo "<p>Nombre d'utilisateurs trouvés dans la table users : " . count($users) . "</p>";
    
    $syncCount = 0;
    $errorCount = 0;
    
    foreach ($users as $user) {
        $email = $user['email'];
        $userId = $user['id'];
        $pharmacyId = $user['pharmacy_id'] ?? null;
        
        echo "<h4>Traitement de l'utilisateur : $email</h4>";
        
        // 4.1 Vérifier si l'utilisateur a une pharmacie associée
        if (empty($pharmacyId)) {
            echo "<p style='color:orange;'>L'utilisateur n'a pas de pharmacie associée.</p>";
            
            // Créer une pharmacie pour cet utilisateur
            $sql = "INSERT INTO pharmacies (nom) VALUES ('Pharmacie de $email')";
            $conn->exec($sql);
            $pharmacyId = $conn->lastInsertId();
            
            // Mettre à jour l'utilisateur avec la nouvelle pharmacie
            $sql = "UPDATE users SET pharmacy_id = ? WHERE id = ?";
            $stmt = $conn->prepare($sql);
            $stmt->execute([$pharmacyId, $userId]);
            
            echo "<p style='color:green;'>Nouvelle pharmacie créée (ID: $pharmacyId) et associée à l'utilisateur.</p>";
        } else {
            // Vérifier si la pharmacie existe
            $stmt = $conn->prepare("SELECT * FROM pharmacies WHERE id = ?");
            $stmt->execute([$pharmacyId]);
            
            if ($stmt->rowCount() == 0) {
                echo "<p style='color:orange;'>La pharmacie associée (ID: $pharmacyId) n'existe pas.</p>";
                
                // Créer la pharmacie manquante
                $sql = "INSERT INTO pharmacies (id, nom) VALUES (?, 'Pharmacie de $email')";
                $stmt = $conn->prepare($sql);
                $stmt->execute([$pharmacyId]);
                
                echo "<p style='color:green;'>Pharmacie créée avec l'ID spécifié: $pharmacyId</p>";
            } else {
                echo "<p style='color:green;'>La pharmacie associée (ID: $pharmacyId) existe.</p>";
            }
        }
        
        // 4.2 Vérifier si l'utilisateur existe dans la table administrateurs
        $stmt = $conn->prepare("SELECT * FROM administrateurs WHERE email = ?");
        $stmt->execute([$email]);
        
        if ($stmt->rowCount() == 0) {
            echo "<p>L'utilisateur n'existe pas dans la table administrateurs. Création en cours...</p>";
            
            // Générer un nom d'utilisateur basé sur l'email
            $username = explode('@', $email)[0];
            
            // Récupérer le mot de passe de l'utilisateur
            $password = $user['password'];
            
            // Insérer l'utilisateur dans la table administrateurs
            $sql = "INSERT INTO administrateurs (username, password, email, pharmacie_id) VALUES (?, ?, ?, ?)";
            $stmt = $conn->prepare($sql);
            
            try {
                $stmt->execute([$username, $password, $email, $pharmacyId]);
                $syncCount++;
                echo "<p style='color:green;'>Utilisateur ajouté à la table administrateurs avec succès.</p>";
            } catch (PDOException $e) {
                $errorCount++;
                echo "<p style='color:red;'>Erreur lors de l'ajout de l'utilisateur à la table administrateurs : " . $e->getMessage() . "</p>";
            }
        } else {
            echo "<p style='color:green;'>L'utilisateur existe déjà dans la table administrateurs.</p>";
            
            // Mettre à jour le mot de passe et la pharmacie pour s'assurer qu'ils sont synchronisés
            $password = $user['password'];
            
            $sql = "UPDATE administrateurs SET password = ?, pharmacie_id = ? WHERE email = ?";
            $stmt = $conn->prepare($sql);
            
            try {
                $stmt->execute([$password, $pharmacyId, $email]);
                $syncCount++;
                echo "<p style='color:green;'>Informations de l'administrateur mises à jour avec succès.</p>";
            } catch (PDOException $e) {
                $errorCount++;
                echo "<p style='color:red;'>Erreur lors de la mise à jour de l'administrateur : " . $e->getMessage() . "</p>";
            }
        }
    }
    
    echo "<h3>Résumé de la synchronisation</h3>";
    echo "<p>Comptes synchronisés avec succès : $syncCount</p>";
    echo "<p>Erreurs rencontrées : $errorCount</p>";
    
    // 5. Créer un trigger pour synchroniser automatiquement les futurs comptes
    echo "<h3>Configuration de la synchronisation automatique</h3>";
    
    // Supprimer les triggers existants s'ils existent
    try {
        $conn->exec("DROP TRIGGER IF EXISTS after_user_insert");
        $conn->exec("DROP TRIGGER IF EXISTS after_user_update");
        echo "<p>Anciens triggers supprimés.</p>";
    } catch (PDOException $e) {
        echo "<p style='color:orange;'>Erreur lors de la suppression des anciens triggers : " . $e->getMessage() . "</p>";
    }
    
    // Créer le trigger pour les insertions
    $triggerInsert = "
    CREATE TRIGGER after_user_insert AFTER INSERT ON users
    FOR EACH ROW
    BEGIN
        DECLARE username VARCHAR(255);
        SET username = SUBSTRING_INDEX(NEW.email, '@', 1);
        
        -- Vérifier si une pharmacie existe pour cet utilisateur
        IF NEW.pharmacy_id IS NULL THEN
            -- Créer une nouvelle pharmacie
            INSERT INTO pharmacies (nom) VALUES (CONCAT('Pharmacie de ', NEW.email));
            
            -- Mettre à jour l'utilisateur avec la nouvelle pharmacie
            UPDATE users SET pharmacy_id = LAST_INSERT_ID() WHERE id = NEW.id;
            
            -- Utiliser la nouvelle pharmacie pour l'administrateur
            INSERT INTO administrateurs (username, password, email, pharmacie_id)
            VALUES (username, NEW.password, NEW.email, LAST_INSERT_ID());
        ELSE
            -- Vérifier si la pharmacie existe
            IF NOT EXISTS (SELECT 1 FROM pharmacies WHERE id = NEW.pharmacy_id) THEN
                -- Créer la pharmacie avec l'ID spécifié
                INSERT INTO pharmacies (id, nom) VALUES (NEW.pharmacy_id, CONCAT('Pharmacie de ', NEW.email));
            END IF;
            
            -- Créer l'administrateur avec la pharmacie existante
            INSERT INTO administrateurs (username, password, email, pharmacie_id)
            VALUES (username, NEW.password, NEW.email, NEW.pharmacy_id);
        END IF;
    END;
    ";
    
    // Créer le trigger pour les mises à jour
    $triggerUpdate = "
    CREATE TRIGGER after_user_update AFTER UPDATE ON users
    FOR EACH ROW
    BEGIN
        -- Mettre à jour l'administrateur correspondant
        UPDATE administrateurs SET password = NEW.password, pharmacie_id = NEW.pharmacy_id
        WHERE email = NEW.email;
    END;
    ";
    
    try {
        $conn->exec($triggerInsert);
        echo "<p style='color:green;'>Trigger pour les nouvelles inscriptions créé avec succès.</p>";
    } catch (PDOException $e) {
        echo "<p style='color:red;'>Erreur lors de la création du trigger d'insertion : " . $e->getMessage() . "</p>";
    }
    
    try {
        $conn->exec($triggerUpdate);
        echo "<p style='color:green;'>Trigger pour les mises à jour créé avec succès.</p>";
    } catch (PDOException $e) {
        echo "<p style='color:red;'>Erreur lors de la création du trigger de mise à jour : " . $e->getMessage() . "</p>";
    }
    
    echo "<h3>Opération terminée</h3>";
    echo "<p>Tous les comptes existants ont été synchronisés et les futurs comptes seront automatiquement synchronisés.</p>";
    echo "<p>Vous pouvez maintenant vous connecter à l'application Java avec les mêmes identifiants que l'application PHP.</p>";
    
} catch(PDOException $e) {
    echo "<h3 style='color:red;'>Erreur</h3>";
    echo "<p>" . $e->getMessage() . "</p>";
}

$conn = null;
?>
