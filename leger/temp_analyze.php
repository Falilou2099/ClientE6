<?php
// Connexion Ã  la base de donnÃ©es
try {
    $pdo = new PDO("mysql:host=localhost;dbname=clientlegerlourd", "root", "");
    $pdo->setAttribute(PDO::ATTR_ERRMODE, PDO::ERRMODE_EXCEPTION);
} catch (PDOException $e) {
    die("Erreur de connexion Ã  la base de donnÃ©es: " . $e->getMessage());
}

// RÃ©cupÃ©rer la liste des tables
$stmt = $pdo->query("SHOW TABLES");
$tables = $stmt->fetchAll(PDO::FETCH_COLUMN);

echo "Tables dans la base de donnÃ©es clientlegerlourd:\n";
echo "==============================================\n\n";

foreach ($tables as $table) {
    echo "Table: " . $table . "\n";
    echo "--------------------\n";
    
    // RÃ©cupÃ©rer la structure de la table
    $stmt = $pdo->query("DESCRIBE " . $table);
    $columns = $stmt->fetchAll(PDO::FETCH_ASSOC);
    
    echo "Colonnes:\n";
    foreach ($columns as $column) {
        echo "- " . $column['Field'] . " (" . $column['Type'] . ")";
        if ($column['Key'] == 'PRI') {
            echo " [ClÃ© primaire]";
        }
        if ($column['Key'] == 'MUL') {
            echo " [ClÃ© Ã©trangÃ¨re]";
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

// RÃ©cupÃ©rer les contraintes de clÃ©s Ã©trangÃ¨res
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
    echo "Aucune relation de clÃ© Ã©trangÃ¨re formelle n'a Ã©tÃ© trouvÃ©e.\n";
    
    // Rechercher des relations potentielles basÃ©es sur les noms de colonnes
    echo "\nRelations potentielles (basÃ©es sur les noms de colonnes):\n";
    
    foreach ($tables as $table) {
        $stmt = $pdo->query("DESCRIBE " . $table);
        $columns = $stmt->fetchAll(PDO::FETCH_ASSOC);
        
        foreach ($columns as $column) {
            // Rechercher les colonnes qui se terminent par _id
            if (preg_match('/_id$/', $column['Field']) && $column['Field'] != 'id') {
                $referencedTable = str_replace('_id', '', $column['Field']);
                
                // VÃ©rifier si la table rÃ©fÃ©rencÃ©e existe
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

// VÃ©rifier les tables utilisÃ©es dans le code
$codeDir = dirname(__DIR__) . '/src';
$allFiles = new RecursiveIteratorIterator(
    new RecursiveDirectoryIterator($codeDir, RecursiveDirectoryIterator::SKIP_DOTS)
);

foreach ($allFiles as $file) {
    if ($file->isFile() && $file->getExtension() == 'php') {
        $content = file_get_contents($file->getPathname());
        
        foreach ($tables as $table) {
            // Rechercher des rÃ©fÃ©rences Ã  la table dans le code
            if (stripos($content, $table) !== false) {
                if (!in_array($table, $usedTables)) {
                    $usedTables[] = $table;
                }
            }
        }
    }
}

// Identifier les tables non utilisÃ©es
foreach ($tables as $table) {
    if (!in_array($table, $usedTables)) {
        $unusedTables[] = $table;
    }
}

echo "Tables utilisÃ©es dans le code:\n";
if (count($usedTables) > 0) {
    foreach ($usedTables as $table) {
        echo "- " . $table . "\n";
    }
} else {
    echo "Aucune table n'a Ã©tÃ© trouvÃ©e comme Ã©tant explicitement utilisÃ©e dans le code.\n";
}

echo "\nTables potentiellement non utilisÃ©es:\n";
if (count($unusedTables) > 0) {
    foreach ($unusedTables as $table) {
        echo "- " . $table . "\n";
    }
} else {
    echo "Toutes les tables semblent Ãªtre utilisÃ©es dans le code.\n";
}

echo "\nConclusion:\n";
echo "==========\n\n";

echo "Tables essentielles Ã  conserver:\n";
foreach ($tables as $table) {
    $stmt = $pdo->query("SELECT COUNT(*) FROM " . $table);
    $count = $stmt->fetchColumn();
    
    if (in_array($table, $usedTables) || $count > 0) {
        echo "- " . $table . " (" . $count . " enregistrements)";
        if (in_array($table, $usedTables)) {
            echo " [UtilisÃ©e dans le code]";
        }
        echo "\n";
    }
}

echo "\nTables qui pourraient Ãªtre supprimÃ©es (vides et non utilisÃ©es):\n";
$emptyUnusedTables = [];
foreach ($tables as $table) {
    $stmt = $pdo->query("SELECT COUNT(*) FROM " . $table);
    $count = $stmt->fetchColumn();
    
    if (!in_array($table, $usedTables) && $count == 0) {
        $emptyUnusedTables[] = $table;
        echo "- " . $table . " (0 enregistrements, non utilisÃ©e dans le code)\n";
    }
}

if (count($emptyUnusedTables) == 0) {
    echo "Aucune table n'est Ã  la fois vide et non utilisÃ©e.\n";
}
