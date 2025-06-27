<?php
// Connexion à la base de données
try {
    $pdo = new PDO("mysql:host=localhost;dbname=clientlegerlourd", "root", "");
    $pdo->setAttribute(PDO::ATTR_ERRMODE, PDO::ERRMODE_EXCEPTION);
    echo "Connexion à la base de données réussie.\n\n";
} catch (PDOException $e) {
    die("Erreur de connexion à la base de données: " . $e->getMessage());
}

// Fonction pour afficher la structure d'une table
function showTableStructure($pdo, $tableName) {
    try {
        $stmt = $pdo->query("DESCRIBE " . $tableName);
        $columns = $stmt->fetchAll(PDO::FETCH_ASSOC);
        
        echo "Structure de la table " . $tableName . ":\n";
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
        $stmt = $pdo->query("SELECT COUNT(*) FROM " . $tableName);
        $count = $stmt->fetchColumn();
        
        echo "Nombre d'enregistrements: " . $count . "\n\n";
        
        // Si la table a des enregistrements, afficher un exemple
        if ($count > 0) {
            $stmt = $pdo->query("SELECT * FROM " . $tableName . " LIMIT 1");
            $row = $stmt->fetch(PDO::FETCH_ASSOC);
            
            echo "Exemple d'enregistrement:\n";
            foreach ($row as $key => $value) {
                echo "- " . $key . ": " . (is_null($value) ? "NULL" : $value) . "\n";
            }
            echo "\n";
        }
        
        return true;
    } catch (PDOException $e) {
        echo "Erreur lors de l'analyse de la table " . $tableName . ": " . $e->getMessage() . "\n\n";
        return false;
    }
}

// Analyser les références aux tables dans le code
function findTableReferencesInCode($directory, $tableName) {
    $references = [];
    
    $iterator = new RecursiveIteratorIterator(
        new RecursiveDirectoryIterator($directory, RecursiveDirectoryIterator::SKIP_DOTS)
    );
    
    foreach ($iterator as $file) {
        if ($file->isFile() && $file->getExtension() == 'php') {
            $content = file_get_contents($file->getPathname());
            
            // Rechercher les requêtes SQL qui font référence à la table
            preg_match_all('/\b(FROM|JOIN|INTO|UPDATE)\s+' . $tableName . '\b/i', $content, $matches);
            
            if (!empty($matches[0])) {
                $references[] = [
                    'file' => $file->getPathname(),
                    'matches' => count($matches[0])
                ];
            }
        }
    }
    
    return $references;
}

echo "Analyse des tables users et utilisateurs:\n";
echo "======================================\n\n";

// Analyser la table users
$usersExists = showTableStructure($pdo, 'users');

// Analyser la table utilisateurs
$utilisateursExists = showTableStructure($pdo, 'utilisateurs');

// Rechercher les références aux tables dans le code
echo "Références aux tables dans le code:\n";
echo "=================================\n\n";

$rootDir = dirname(__DIR__);

if ($usersExists) {
    $usersReferences = findTableReferencesInCode($rootDir, 'users');
    
    echo "Références à la table 'users':\n";
    if (!empty($usersReferences)) {
        foreach ($usersReferences as $reference) {
            echo "- " . $reference['file'] . " (" . $reference['matches'] . " références)\n";
        }
    } else {
        echo "- Aucune référence trouvée\n";
    }
    echo "\n";
}

if ($utilisateursExists) {
    $utilisateursReferences = findTableReferencesInCode($rootDir, 'utilisateurs');
    
    echo "Références à la table 'utilisateurs':\n";
    if (!empty($utilisateursReferences)) {
        foreach ($utilisateursReferences as $reference) {
            echo "- " . $reference['file'] . " (" . $reference['matches'] . " références)\n";
        }
    } else {
        echo "- Aucune référence trouvée\n";
    }
    echo "\n";
}

// Recommandations
echo "Recommandations:\n";
echo "==============\n\n";

if ($usersExists && $utilisateursExists) {
    $usersReferenceCount = array_sum(array_column($usersReferences ?? [], 'matches'));
    $utilisateursReferenceCount = array_sum(array_column($utilisateursReferences ?? [], 'matches'));
    
    $usersRecordCount = $pdo->query("SELECT COUNT(*) FROM users")->fetchColumn();
    $utilisateursRecordCount = $pdo->query("SELECT COUNT(*) FROM utilisateurs")->fetchColumn();
    
    if ($usersReferenceCount > $utilisateursReferenceCount) {
        echo "La table 'users' est plus référencée dans le code (" . $usersReferenceCount . " références contre " . $utilisateursReferenceCount . " pour 'utilisateurs').\n";
        
        if ($utilisateursRecordCount > 0 && $usersRecordCount == 0) {
            echo "Cependant, la table 'utilisateurs' contient des données (" . $utilisateursRecordCount . " enregistrements) alors que 'users' est vide.\n";
            echo "Recommandation: Migrer les données de 'utilisateurs' vers 'users' avant de supprimer 'utilisateurs'.\n";
        } else {
            echo "Recommandation: Conserver 'users' et supprimer 'utilisateurs'.\n";
        }
    } else if ($utilisateursReferenceCount > $usersReferenceCount) {
        echo "La table 'utilisateurs' est plus référencée dans le code (" . $utilisateursReferenceCount . " références contre " . $usersReferenceCount . " pour 'users').\n";
        
        if ($usersRecordCount > 0 && $utilisateursRecordCount == 0) {
            echo "Cependant, la table 'users' contient des données (" . $usersRecordCount . " enregistrements) alors que 'utilisateurs' est vide.\n";
            echo "Recommandation: Migrer les données de 'users' vers 'utilisateurs' avant de supprimer 'users'.\n";
        } else {
            echo "Recommandation: Conserver 'utilisateurs' et supprimer 'users'.\n";
        }
    } else {
        echo "Les deux tables sont référencées de manière égale dans le code.\n";
        
        if ($usersRecordCount > 0 && $utilisateursRecordCount == 0) {
            echo "La table 'users' contient des données (" . $usersRecordCount . " enregistrements) alors que 'utilisateurs' est vide.\n";
            echo "Recommandation: Conserver 'users' et supprimer 'utilisateurs'.\n";
        } else if ($utilisateursRecordCount > 0 && $usersRecordCount == 0) {
            echo "La table 'utilisateurs' contient des données (" . $utilisateursRecordCount . " enregistrements) alors que 'users' est vide.\n";
            echo "Recommandation: Conserver 'utilisateurs' et supprimer 'users'.\n";
        } else if ($usersRecordCount > 0 && $utilisateursRecordCount > 0) {
            echo "Les deux tables contiennent des données.\n";
            echo "Recommandation: Fusionner les deux tables avant d'en supprimer une.\n";
        } else {
            echo "Les deux tables sont vides.\n";
            echo "Recommandation: Conserver la table qui correspond le mieux à la structure de l'application et supprimer l'autre.\n";
        }
    }
} else if ($usersExists) {
    echo "Seule la table 'users' existe. Aucune action nécessaire.\n";
} else if ($utilisateursExists) {
    echo "Seule la table 'utilisateurs' existe. Aucune action nécessaire.\n";
} else {
    echo "Aucune des deux tables n'existe. Aucune action nécessaire.\n";
}

// Script pour supprimer la table inutile
echo "\nScript pour supprimer la table inutile:\n";
echo "===================================\n\n";

if ($usersExists && $utilisateursExists) {
    $usersReferenceCount = array_sum(array_column($usersReferences ?? [], 'matches'));
    $utilisateursReferenceCount = array_sum(array_column($utilisateursReferences ?? [], 'matches'));
    
    $usersRecordCount = $pdo->query("SELECT COUNT(*) FROM users")->fetchColumn();
    $utilisateursRecordCount = $pdo->query("SELECT COUNT(*) FROM utilisateurs")->fetchColumn();
    
    $tableToKeep = null;
    $tableToRemove = null;
    
    if ($usersReferenceCount > $utilisateursReferenceCount) {
        $tableToKeep = 'users';
        $tableToRemove = 'utilisateurs';
    } else if ($utilisateursReferenceCount > $usersReferenceCount) {
        $tableToKeep = 'utilisateurs';
        $tableToRemove = 'users';
    } else {
        if ($usersRecordCount > 0 && $utilisateursRecordCount == 0) {
            $tableToKeep = 'users';
            $tableToRemove = 'utilisateurs';
        } else if ($utilisateursRecordCount > 0 && $usersRecordCount == 0) {
            $tableToKeep = 'utilisateurs';
            $tableToRemove = 'users';
        } else {
            echo "Les deux tables semblent être utilisées de manière égale et contiennent des données.\n";
            echo "Une analyse plus approfondie est nécessaire avant de supprimer l'une d'entre elles.\n";
            exit;
        }
    }
    
    echo "Table à conserver: " . $tableToKeep . "\n";
    echo "Table à supprimer: " . $tableToRemove . "\n\n";
    
    echo "Pour supprimer la table " . $tableToRemove . ", exécutez la commande SQL suivante:\n";
    echo "SET FOREIGN_KEY_CHECKS = 0;\n";
    echo "DROP TABLE " . $tableToRemove . ";\n";
    echo "SET FOREIGN_KEY_CHECKS = 1;\n\n";
    
    echo "Ou exécutez ce script avec l'argument 'confirm' pour supprimer automatiquement la table:\n";
    echo "php scripts/analyze_user_tables.php confirm\n\n";
    
    // Vérifier si l'argument 'confirm' est présent
    if (isset($argv[1]) && $argv[1] === 'confirm') {
        echo "Suppression de la table " . $tableToRemove . "...\n";
        
        try {
            // Désactiver les contraintes de clés étrangères temporairement
            $pdo->exec("SET FOREIGN_KEY_CHECKS = 0");
            
            // Supprimer la table
            $pdo->exec("DROP TABLE " . $tableToRemove);
            
            // Réactiver les contraintes de clés étrangères
            $pdo->exec("SET FOREIGN_KEY_CHECKS = 1");
            
            echo "Table " . $tableToRemove . " supprimée avec succès.\n";
        } catch (PDOException $e) {
            echo "Erreur lors de la suppression de la table " . $tableToRemove . ": " . $e->getMessage() . "\n";
        }
    }
}

echo "\nAnalyse terminée.\n";
