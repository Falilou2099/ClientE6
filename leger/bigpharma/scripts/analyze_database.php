<?php
// Connexion à la base de données
try {
    $pdo = new PDO("mysql:host=localhost;dbname=clientlegerlourd", "root", "");
    $pdo->setAttribute(PDO::ATTR_ERRMODE, PDO::ERRMODE_EXCEPTION);
} catch (PDOException $e) {
    die("Erreur de connexion à la base de données: " . $e->getMessage());
}

// Récupérer la liste des tables
$stmt = $pdo->query("SHOW TABLES");
$tables = $stmt->fetchAll(PDO::FETCH_COLUMN);

echo "Tables dans la base de données clientlegerlourd:\n";
echo "==============================================\n\n";

foreach ($tables as $table) {
    echo "Table: " . $table . "\n";
    echo "--------------------\n";
    
    // Récupérer la structure de la table
    $stmt = $pdo->query("DESCRIBE " . $table);
    $columns = $stmt->fetchAll(PDO::FETCH_ASSOC);
    
    echo "Colonnes:\n";
    foreach ($columns as $column) {
        echo "- " . $column['Field'] . " (" . $column['Type'] . ")";
        if ($column['Key'] == 'PRI') {
            echo " [Clé primaire]";
        }
        if ($column['Key'] == 'MUL') {
            echo " [Clé étrangère]";
        }
        echo "\n";
    }
    
    // Compter le nombre d'enregistrements
    $stmt = $pdo->query("SELECT COUNT(*) FROM " . $table);
    $count = $stmt->fetchColumn();
    
    echo "Nombre d'enregistrements: " . $count . "\n\n";
}

// Analyser les relations entre les tables
echo "Relations entre les tables:\n";
echo "=========================\n\n";

// Récupérer les contraintes de clés étrangères
$stmt = $pdo->query("
    SELECT 
        TABLE_NAME, 
        COLUMN_NAME, 
        REFERENCED_TABLE_NAME, 
        REFERENCED_COLUMN_NAME 
    FROM 
        INFORMATION_SCHEMA.KEY_COLUMN_USAGE 
    WHERE 
        REFERENCED_TABLE_SCHEMA = 'clientlegerlourd' 
        AND REFERENCED_TABLE_NAME IS NOT NULL
");

$foreignKeys = $stmt->fetchAll(PDO::FETCH_ASSOC);

if (count($foreignKeys) > 0) {
    foreach ($foreignKeys as $fk) {
        echo "- Table " . $fk['TABLE_NAME'] . " (colonne " . $fk['COLUMN_NAME'] . ") -> Table " . $fk['REFERENCED_TABLE_NAME'] . " (colonne " . $fk['REFERENCED_COLUMN_NAME'] . ")\n";
    }
} else {
    echo "Aucune relation de clé étrangère formelle n'a été trouvée.\n";
    
    // Rechercher des relations potentielles basées sur les noms de colonnes
    echo "\nRelations potentielles (basées sur les noms de colonnes):\n";
    
    foreach ($tables as $table) {
        $stmt = $pdo->query("DESCRIBE " . $table);
        $columns = $stmt->fetchAll(PDO::FETCH_ASSOC);
        
        foreach ($columns as $column) {
            // Rechercher les colonnes qui se terminent par _id
            if (preg_match('/_id$/', $column['Field']) && $column['Field'] != 'id') {
                $referencedTable = str_replace('_id', '', $column['Field']);
                
                // Vérifier si la table référencée existe
                if (in_array($referencedTable, $tables) || in_array($referencedTable . 's', $tables)) {
                    $actualReferencedTable = in_array($referencedTable, $tables) ? $referencedTable : $referencedTable . 's';
                    echo "- Table " . $table . " (colonne " . $column['Field'] . ") -> Table " . $actualReferencedTable . " (colonne id) [Relation potentielle]\n";
                }
            }
        }
    }
}

echo "\nRecommandations:\n";
echo "===============\n\n";

$usedTables = [];
$unusedTables = [];

// Vérifier les tables utilisées dans le code
$codeDir = dirname(__DIR__) . '/src';
$allFiles = new RecursiveIteratorIterator(
    new RecursiveDirectoryIterator($codeDir, RecursiveDirectoryIterator::SKIP_DOTS)
);

foreach ($allFiles as $file) {
    if ($file->isFile() && $file->getExtension() == 'php') {
        $content = file_get_contents($file->getPathname());
        
        foreach ($tables as $table) {
            // Rechercher des références à la table dans le code
            if (stripos($content, $table) !== false) {
                if (!in_array($table, $usedTables)) {
                    $usedTables[] = $table;
                }
            }
        }
    }
}

// Identifier les tables non utilisées
foreach ($tables as $table) {
    if (!in_array($table, $usedTables)) {
        $unusedTables[] = $table;
    }
}

echo "Tables utilisées dans le code:\n";
if (count($usedTables) > 0) {
    foreach ($usedTables as $table) {
        echo "- " . $table . "\n";
    }
} else {
    echo "Aucune table n'a été trouvée comme étant explicitement utilisée dans le code.\n";
}

echo "\nTables potentiellement non utilisées:\n";
if (count($unusedTables) > 0) {
    foreach ($unusedTables as $table) {
        echo "- " . $table . "\n";
    }
} else {
    echo "Toutes les tables semblent être utilisées dans le code.\n";
}

echo "\nConclusion:\n";
echo "==========\n\n";

echo "Tables essentielles à conserver:\n";
foreach ($tables as $table) {
    $stmt = $pdo->query("SELECT COUNT(*) FROM " . $table);
    $count = $stmt->fetchColumn();
    
    if (in_array($table, $usedTables) || $count > 0) {
        echo "- " . $table . " (" . $count . " enregistrements)";
        if (in_array($table, $usedTables)) {
            echo " [Utilisée dans le code]";
        }
        echo "\n";
    }
}

echo "\nTables qui pourraient être supprimées (vides et non utilisées):\n";
$emptyUnusedTables = [];
foreach ($tables as $table) {
    $stmt = $pdo->query("SELECT COUNT(*) FROM " . $table);
    $count = $stmt->fetchColumn();
    
    if (!in_array($table, $usedTables) && $count == 0) {
        $emptyUnusedTables[] = $table;
        echo "- " . $table . " (0 enregistrements, non utilisée dans le code)\n";
    }
}

if (count($emptyUnusedTables) == 0) {
    echo "Aucune table n'est à la fois vide et non utilisée.\n";
}
