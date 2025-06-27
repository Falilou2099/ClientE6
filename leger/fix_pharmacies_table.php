<?php
// Script pour corriger la structure de la table pharmacies
require_once 'config/database.php';

try {
    $pdo = $GLOBALS['pdo'];
    
    echo "<h1>Correction de la table 'pharmacies'</h1>";
    
    // Vérifier si la table existe
    $stmt = $pdo->query("SHOW TABLES LIKE 'pharmacies'");
    $tableExists = $stmt->rowCount() > 0;
    
    if (!$tableExists) {
        // Créer la table si elle n'existe pas
        $pdo->exec("CREATE TABLE pharmacies (
            id INT AUTO_INCREMENT PRIMARY KEY,
            nom VARCHAR(255) NOT NULL,
            adresse VARCHAR(255) NOT NULL,
            telephone VARCHAR(20) NOT NULL,
            email VARCHAR(255) NOT NULL,
            numero_enregistrement VARCHAR(50) NOT NULL,
            statut ENUM('pending', 'active', 'suspended') DEFAULT 'pending',
            date_creation DATETIME DEFAULT CURRENT_TIMESTAMP
        ) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;");
        
        echo "<p class='text-success'>La table 'pharmacies' a été créée avec succès.</p>";
    } else {
        // La table existe, vérifier les colonnes
        $stmt = $pdo->query("DESCRIBE pharmacies");
        $existingColumns = $stmt->fetchAll(PDO::FETCH_ASSOC);
        
        // Convertir en tableau associatif pour faciliter la vérification
        $columnMap = [];
        foreach ($existingColumns as $column) {
            $columnMap[$column['Field']] = $column;
        }
        
        echo "<h2>Colonnes existantes</h2>";
        echo "<ul>";
        foreach ($columnMap as $field => $details) {
            echo "<li>" . htmlspecialchars($field) . " (" . htmlspecialchars($details['Type']) . ")</li>";
        }
        echo "</ul>";
        
        // Définir les colonnes requises et leurs équivalents possibles
        $requiredColumns = [
            'id' => ['id'],
            'nom' => ['nom', 'name', 'pharmacy_name'],
            'adresse' => ['adresse', 'address'],
            'telephone' => ['telephone', 'phone', 'phone_number'],
            'email' => ['email'],
            'numero_enregistrement' => ['numero_enregistrement', 'registration_number'],
            'statut' => ['statut', 'status'],
            'date_creation' => ['date_creation', 'created_at']
        ];
        
        // Vérifier et corriger chaque colonne requise
        echo "<h2>Modifications apportées</h2>";
        $modificationsApplied = false;
        
        foreach ($requiredColumns as $standardName => $alternativeNames) {
            // Vérifier si la colonne standard existe
            $standardExists = isset($columnMap[$standardName]);
            
            // Vérifier si une colonne alternative existe
            $alternativeFound = null;
            foreach ($alternativeNames as $altName) {
                if ($altName !== $standardName && isset($columnMap[$altName])) {
                    $alternativeFound = $altName;
                    break;
                }
            }
            
            if (!$standardExists && $alternativeFound) {
                // Renommer la colonne alternative en colonne standard
                $nullValue = ($columnMap[$alternativeFound]['Null'] == 'YES') ? 'NULL' : 'NOT NULL';
                $pdo->exec("ALTER TABLE pharmacies CHANGE `{$alternativeFound}` `{$standardName}` {$columnMap[$alternativeFound]['Type']} {$nullValue}");
                echo "<p class='text-success'>La colonne '{$alternativeFound}' a été renommée en '{$standardName}'.</p>";
                $modificationsApplied = true;
            } elseif (!$standardExists && !$alternativeFound) {
                // Ajouter la colonne manquante
                $columnDefinition = '';
                switch ($standardName) {
                    case 'id':
                        $columnDefinition = "INT AUTO_INCREMENT PRIMARY KEY";
                        break;
                    case 'nom':
                    case 'adresse':
                    case 'email':
                        $columnDefinition = "VARCHAR(255) NOT NULL";
                        break;
                    case 'telephone':
                        $columnDefinition = "VARCHAR(20) NOT NULL";
                        break;
                    case 'numero_enregistrement':
                        $columnDefinition = "VARCHAR(50) NOT NULL";
                        break;
                    case 'statut':
                        $columnDefinition = "ENUM('pending', 'active', 'suspended') DEFAULT 'pending'";
                        break;
                    case 'date_creation':
                        $columnDefinition = "DATETIME DEFAULT CURRENT_TIMESTAMP";
                        break;
                }
                
                if (!empty($columnDefinition)) {
                    $pdo->exec("ALTER TABLE pharmacies ADD COLUMN `{$standardName}` {$columnDefinition}");
                    echo "<p class='text-success'>La colonne '{$standardName}' a été ajoutée.</p>";
                    $modificationsApplied = true;
                }
            }
        }
        
        if (!$modificationsApplied) {
            echo "<p class='text-info'>Aucune modification nécessaire, toutes les colonnes requises existent déjà.</p>";
        }
    }
    
    // Vérifier la structure finale
    $stmt = $pdo->query("DESCRIBE pharmacies");
    $finalColumns = $stmt->fetchAll(PDO::FETCH_ASSOC);
    
    echo "<h2>Structure finale de la table 'pharmacies'</h2>";
    echo "<table border='1' cellpadding='5'>";
    echo "<tr><th>Field</th><th>Type</th><th>Null</th><th>Key</th><th>Default</th><th>Extra</th></tr>";
    
    foreach ($finalColumns as $column) {
        echo "<tr>";
        foreach ($column as $key => $value) {
            echo "<td>" . htmlspecialchars($value ?? 'NULL') . "</td>";
        }
        echo "</tr>";
    }
    
    echo "</table>";
    
    // Mettre à jour le modèle Pharmacy
    $modelPath = __DIR__ . '/src/Models/Pharmacy.php';
    if (file_exists($modelPath)) {
        $modelContent = file_get_contents($modelPath);
        
        // Remplacer les références aux anciennes colonnes par les nouvelles
        $replacements = [
            'name' => 'nom',
            'pharmacy_name' => 'nom',
            'address' => 'adresse',
            'phone_number' => 'telephone',
            'registration_number' => 'numero_enregistrement',
            'status' => 'statut',
            'created_at' => 'date_creation'
        ];
        
        $updated = false;
        foreach ($replacements as $old => $new) {
            if (strpos($modelContent, $old) !== false) {
                $modelContent = str_replace($old, $new, $modelContent);
                $updated = true;
            }
        }
        
        if ($updated) {
            file_put_contents($modelPath, $modelContent);
            echo "<p class='text-success'>Le modèle Pharmacy a été mis à jour pour utiliser les nouveaux noms de colonnes.</p>";
        }
    }
    
    echo "<p>La structure de la table 'pharmacies' a été corrigée. Vous pouvez maintenant <a href='/bigpharma/register'>vous inscrire</a>.</p>";
    
} catch (PDOException $e) {
    echo "<h1>Erreur</h1>";
    echo "<p>Une erreur s'est produite : " . htmlspecialchars($e->getMessage()) . "</p>";
}
?>

<style>
    body {
        font-family: Arial, sans-serif;
        margin: 20px;
        line-height: 1.6;
    }
    h1, h2 {
        color: #333;
    }
    table {
        border-collapse: collapse;
        margin-bottom: 20px;
    }
    th {
        background-color: #f2f2f2;
    }
    .text-success {
        color: green;
        font-weight: bold;
    }
    .text-info {
        color: blue;
        font-weight: bold;
    }
</style>
