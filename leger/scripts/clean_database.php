<?php
// Connexion à la base de données
try {
    $pdo = new PDO("mysql:host=localhost;dbname=clientlegerlourd", "root", "");
    $pdo->setAttribute(PDO::ATTR_ERRMODE, PDO::ERRMODE_EXCEPTION);
    echo "Connexion à la base de données réussie.\n\n";
} catch (PDOException $e) {
    die("Erreur de connexion à la base de données: " . $e->getMessage());
}

// Tables à supprimer (vides et non utilisées dans le code)
$tablesToDrop = [
    'livraisons',
    'reset_tokens'
];

// Tables à conserver
$tablesToKeep = [
    'categories',
    'clients',
    'commandes',
    'fournisseurs',
    'lignes_commande',
    'pharmacies',
    'produits',
    'stocks',
    'users',
    'utilisateurs'
];

echo "Analyse des tables de la base de données:\n";
echo "=======================================\n\n";

// Récupérer la liste des tables
$stmt = $pdo->query("SHOW TABLES");
$tables = $stmt->fetchAll(PDO::FETCH_COLUMN);

echo "Tables existantes:\n";
foreach ($tables as $table) {
    $stmt = $pdo->query("SELECT COUNT(*) FROM " . $table);
    $count = $stmt->fetchColumn();
    echo "- " . $table . " (" . $count . " enregistrements)\n";
}

echo "\nTables à supprimer (vides et non utilisées):\n";
foreach ($tablesToDrop as $table) {
    if (in_array($table, $tables)) {
        $stmt = $pdo->query("SELECT COUNT(*) FROM " . $table);
        $count = $stmt->fetchColumn();
        echo "- " . $table . " (" . $count . " enregistrements)\n";
    } else {
        echo "- " . $table . " (n'existe pas)\n";
    }
}

echo "\nTables à conserver:\n";
foreach ($tablesToKeep as $table) {
    if (in_array($table, $tables)) {
        $stmt = $pdo->query("SELECT COUNT(*) FROM " . $table);
        $count = $stmt->fetchColumn();
        echo "- " . $table . " (" . $count . " enregistrements)\n";
    } else {
        echo "- " . $table . " (n'existe pas)\n";
    }
}

// Demander confirmation avant de supprimer les tables
echo "\nVoulez-vous supprimer les tables vides et non utilisées? (Exécutez ce script avec l'argument 'confirm' pour confirmer)\n";
echo "Exemple: php scripts/clean_database.php confirm\n";

// Vérifier si l'argument 'confirm' est présent
if (isset($argv[1]) && $argv[1] === 'confirm') {
    echo "\nSuppression des tables vides et non utilisées...\n";
    
    // Désactiver les contraintes de clés étrangères temporairement
    $pdo->exec("SET FOREIGN_KEY_CHECKS = 0");
    
    foreach ($tablesToDrop as $table) {
        if (in_array($table, $tables)) {
            try {
                $pdo->exec("DROP TABLE " . $table);
                echo "- Table " . $table . " supprimée avec succès.\n";
            } catch (PDOException $e) {
                echo "- Erreur lors de la suppression de la table " . $table . ": " . $e->getMessage() . "\n";
            }
        }
    }
    
    // Réactiver les contraintes de clés étrangères
    $pdo->exec("SET FOREIGN_KEY_CHECKS = 1");
    
    echo "\nNettoyage de la base de données terminé.\n";
} else {
    echo "\nAucune table n'a été supprimée. Exécutez ce script avec l'argument 'confirm' pour confirmer la suppression.\n";
}

// Analyser les colonnes inutilisées dans les tables à conserver
echo "\nAnalyse des colonnes potentiellement inutilisées dans les tables conservées:\n";
echo "=================================================================\n\n";

foreach ($tablesToKeep as $table) {
    if (in_array($table, $tables)) {
        $stmt = $pdo->query("DESCRIBE " . $table);
        $columns = $stmt->fetchAll(PDO::FETCH_ASSOC);
        
        $unusedColumns = [];
        
        foreach ($columns as $column) {
            // Vérifier si la colonne contient uniquement des valeurs NULL ou vides
            $stmt = $pdo->query("SELECT COUNT(*) FROM " . $table . " WHERE " . $column['Field'] . " IS NOT NULL AND " . $column['Field'] . " != ''");
            $nonEmptyCount = $stmt->fetchColumn();
            
            if ($nonEmptyCount == 0 && $column['Field'] != 'id' && !preg_match('/_id$/', $column['Field'])) {
                $unusedColumns[] = $column['Field'];
            }
        }
        
        if (count($unusedColumns) > 0) {
            echo "Table " . $table . " - Colonnes potentiellement inutilisées:\n";
            foreach ($unusedColumns as $column) {
                echo "- " . $column . " (ne contient que des valeurs NULL ou vides)\n";
            }
            echo "\n";
        }
    }
}

echo "\nRecommandations finales:\n";
echo "======================\n\n";

echo "1. Supprimer les tables vides et non utilisées: " . implode(", ", $tablesToDrop) . "\n";
echo "2. Conserver les tables essentielles: " . implode(", ", $tablesToKeep) . "\n";
echo "3. Vérifier les colonnes inutilisées mentionnées ci-dessus et envisager de les supprimer si elles ne sont pas nécessaires.\n";
echo "4. Assurez-vous que toutes les relations entre les tables sont correctement définies avec des clés étrangères.\n";

echo "\nPour supprimer les tables inutiles, exécutez: php scripts/clean_database.php confirm\n";
