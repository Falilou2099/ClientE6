<?php
// Script de diagnostic et de configuration pour la table ventes
error_reporting(E_ALL);
ini_set('display_errors', 1);

// Chemins absolus pour les inclusions
define('ROOT_PATH', dirname(__DIR__));
define('CONFIG_PATH', ROOT_PATH . '/config');
define('SRC_PATH', ROOT_PATH . '/src');

// Autoloader personnalisé
spl_autoload_register(function($class) {
    $prefixes = [
        'Controllers\\' => SRC_PATH . '/Controllers/',
        'Models\\' => SRC_PATH . '/Models/',
        'Middleware\\' => SRC_PATH . '/Middleware/',
        'Config\\' => SRC_PATH . '/Config/'
    ];

    foreach ($prefixes as $prefix => $base_dir) {
        $len = strlen($prefix);
        if (strncmp($prefix, $class, $len) === 0) {
            $relative_class = substr($class, $len);
            $file = $base_dir . str_replace('\\', '/', $relative_class) . '.php';
            
            if (file_exists($file)) {
                require $file;
                return true;
            }
        }
    }
    return false;
});

// Connexion à la base de données
try {
    // Utiliser directement les valeurs de connexion
    $host = 'localhost';
    $dbname = 'clientlegerlourd';
    $username = 'root';
    $password = '';
    
    $pdo = new PDO(
        "mysql:host={$host};dbname={$dbname}",
        $username,
        $password
    );
    $pdo->setAttribute(PDO::ATTR_ERRMODE, PDO::ERRMODE_EXCEPTION);
    $pdo->setAttribute(PDO::ATTR_DEFAULT_FETCH_MODE, PDO::FETCH_ASSOC);
    $pdo->exec("SET NAMES utf8");
    
    echo "Connexion à la base de données réussie.\n";
} catch(PDOException $e) {
    die("Erreur de connexion à la base de données : " . $e->getMessage() . "\n");
}

// Vérifier si la table ventes existe
$tableExists = false;
try {
    $stmt = $pdo->prepare("SHOW TABLES LIKE 'ventes'");
    $stmt->execute();
    $tableExists = $stmt->rowCount() > 0;
    
    echo "Vérification de l'existence de la table 'ventes': " . ($tableExists ? "Existe" : "N'existe pas") . "\n";
} catch(PDOException $e) {
    die("Erreur lors de la vérification de la table: " . $e->getMessage() . "\n");
}

// Créer la table ventes si elle n'existe pas
if (!$tableExists) {
    try {
        $query = "
            CREATE TABLE IF NOT EXISTS ventes (
                id INT AUTO_INCREMENT PRIMARY KEY,
                product_id INT NOT NULL,
                client_id INT NOT NULL,
                pharmacy_id INT NOT NULL,
                quantite INT NOT NULL DEFAULT 1,
                prix_unitaire DECIMAL(10, 2) NOT NULL,
                prix_total DECIMAL(10, 2) NOT NULL,
                date_vente DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
                FOREIGN KEY (product_id) REFERENCES produits(id) ON DELETE CASCADE,
                FOREIGN KEY (client_id) REFERENCES clients(id) ON DELETE CASCADE,
                INDEX idx_product (product_id),
                INDEX idx_client (client_id),
                INDEX idx_pharmacy (pharmacy_id),
                INDEX idx_date (date_vente)
            ) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci
        ";
        
        $pdo->exec($query);
        echo "Table 'ventes' créée avec succès.\n";
    } catch(PDOException $e) {
        echo "Erreur lors de la création de la table 'ventes': " . $e->getMessage() . "\n";
        
        // Vérifier si l'erreur est due à des contraintes de clé étrangère
        if (strpos($e->getMessage(), 'foreign key constraint') !== false) {
            echo "Tentative de création de la table sans contraintes de clé étrangère...\n";
            
            try {
                $query = "
                    CREATE TABLE IF NOT EXISTS ventes (
                        id INT AUTO_INCREMENT PRIMARY KEY,
                        product_id INT NOT NULL,
                        client_id INT NOT NULL,
                        pharmacy_id INT NOT NULL,
                        quantite INT NOT NULL DEFAULT 1,
                        prix_unitaire DECIMAL(10, 2) NOT NULL,
                        prix_total DECIMAL(10, 2) NOT NULL,
                        date_vente DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
                        INDEX idx_product (product_id),
                        INDEX idx_client (client_id),
                        INDEX idx_pharmacy (pharmacy_id),
                        INDEX idx_date (date_vente)
                    ) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci
                ";
                
                $pdo->exec($query);
                echo "Table 'ventes' créée avec succès (sans contraintes de clé étrangère).\n";
            } catch(PDOException $e2) {
                die("Échec de la création de la table 'ventes' sans contraintes: " . $e2->getMessage() . "\n");
            }
        }
    }
}

// Vérifier la structure de la table ventes
try {
    $stmt = $pdo->prepare("DESCRIBE ventes");
    $stmt->execute();
    $columns = $stmt->fetchAll();
    
    echo "Structure de la table 'ventes':\n";
    foreach ($columns as $column) {
        echo "- {$column['Field']}: {$column['Type']} {$column['Null']} {$column['Key']} {$column['Default']}\n";
    }
} catch(PDOException $e) {
    echo "Erreur lors de la vérification de la structure de la table: " . $e->getMessage() . "\n";
}

// Vérifier les tables liées
try {
    echo "\nVérification des tables liées:\n";
    
    // Vérifier la table produits
    $stmt = $pdo->prepare("SHOW TABLES LIKE 'produits'");
    $stmt->execute();
    $produitsExists = $stmt->rowCount() > 0;
    echo "- Table 'produits': " . ($produitsExists ? "Existe" : "N'existe pas") . "\n";
    
    if ($produitsExists) {
        $stmt = $pdo->prepare("SELECT COUNT(*) as count FROM produits");
        $stmt->execute();
        $count = $stmt->fetch()['count'];
        echo "  Nombre d'enregistrements: $count\n";
    }
    
    // Vérifier la table clients
    $stmt = $pdo->prepare("SHOW TABLES LIKE 'clients'");
    $stmt->execute();
    $clientsExists = $stmt->rowCount() > 0;
    echo "- Table 'clients': " . ($clientsExists ? "Existe" : "N'existe pas") . "\n";
    
    if ($clientsExists) {
        $stmt = $pdo->prepare("SELECT COUNT(*) as count FROM clients");
        $stmt->execute();
        $count = $stmt->fetch()['count'];
        echo "  Nombre d'enregistrements: $count\n";
    }
    
    // Vérifier la table pharmacies
    $stmt = $pdo->prepare("SHOW TABLES LIKE 'pharmacies'");
    $stmt->execute();
    $pharmaciesExists = $stmt->rowCount() > 0;
    echo "- Table 'pharmacies': " . ($pharmaciesExists ? "Existe" : "N'existe pas") . "\n";
    
    if ($pharmaciesExists) {
        $stmt = $pdo->prepare("SELECT COUNT(*) as count FROM pharmacies");
        $stmt->execute();
        $count = $stmt->fetch()['count'];
        echo "  Nombre d'enregistrements: $count\n";
    }
} catch(PDOException $e) {
    echo "Erreur lors de la vérification des tables liées: " . $e->getMessage() . "\n";
}

echo "\nDiagnostic terminé.\n";
