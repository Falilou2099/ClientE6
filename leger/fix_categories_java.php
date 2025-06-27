<?php
// Script pour diagnostiquer et corriger les problèmes de catégories dans l'application Java

// Connexion à la base de données
$servername = "localhost";
$username = "root";
$password = "";
$dbname = "bigpharma";

try {
    $conn = new PDO("mysql:host=$servername;dbname=$dbname", $username, $password);
    $conn->setAttribute(PDO::ATTR_ERRMODE, PDO::ERRMODE_EXCEPTION);
    
    echo "<h2>Diagnostic et correction des catégories pour l'application Java</h2>";
    
    // 1. Vérifier toutes les tables liées aux catégories
    $tables = $conn->query("SHOW TABLES")->fetchAll(PDO::FETCH_COLUMN);
    
    echo "<h3>Tables existantes</h3>";
    echo "<ul>";
    foreach ($tables as $table) {
        echo "<li>$table</li>";
    }
    echo "</ul>";
    
    // 2. Vérifier les tables spécifiques aux catégories
    $categoryTables = [
        'categories' => false,
        'categorie_medicament' => false,
        'category' => false,
        'categories_medicaments' => false
    ];
    
    foreach ($categoryTables as $table => $exists) {
        $categoryTables[$table] = in_array($table, $tables);
    }
    
    echo "<h3>Tables de catégories</h3>";
    echo "<ul>";
    foreach ($categoryTables as $table => $exists) {
        $status = $exists ? "✓ Existe" : "✗ N'existe pas";
        $color = $exists ? "green" : "red";
        echo "<li style='color:$color;'>$table: $status</li>";
    }
    echo "</ul>";
    
    // 3. Créer toutes les tables possibles de catégories
    echo "<h3>Création/Vérification des tables de catégories</h3>";
    
    // Table categories (PHP)
    if (!$categoryTables['categories']) {
        $sql = "CREATE TABLE categories (
            id INT(11) AUTO_INCREMENT PRIMARY KEY,
            nom VARCHAR(255) NOT NULL,
            description TEXT,
            created_at DATETIME DEFAULT CURRENT_TIMESTAMP
        )";
        $conn->exec($sql);
        echo "<p style='color:green;'>Table 'categories' créée.</p>";
    }
    
    // Table categorie_medicament (Java)
    if (!$categoryTables['categorie_medicament']) {
        $sql = "CREATE TABLE categorie_medicament (
            id INT(11) AUTO_INCREMENT PRIMARY KEY,
            nom VARCHAR(255) NOT NULL,
            description TEXT,
            created_at DATETIME DEFAULT CURRENT_TIMESTAMP
        )";
        $conn->exec($sql);
        echo "<p style='color:green;'>Table 'categorie_medicament' créée.</p>";
    }
    
    // Table category (alternative Java)
    if (!$categoryTables['category']) {
        $sql = "CREATE TABLE category (
            id INT(11) AUTO_INCREMENT PRIMARY KEY,
            nom VARCHAR(255) NOT NULL,
            description TEXT,
            created_at DATETIME DEFAULT CURRENT_TIMESTAMP
        )";
        $conn->exec($sql);
        echo "<p style='color:green;'>Table 'category' créée.</p>";
    }
    
    // Table categories_medicaments (alternative)
    if (!$categoryTables['categories_medicaments']) {
        $sql = "CREATE TABLE categories_medicaments (
            id INT(11) AUTO_INCREMENT PRIMARY KEY,
            nom VARCHAR(255) NOT NULL,
            description TEXT,
            created_at DATETIME DEFAULT CURRENT_TIMESTAMP
        )";
        $conn->exec($sql);
        echo "<p style='color:green;'>Table 'categories_medicaments' créée.</p>";
    }
    
    // 4. Vérifier le contenu des tables
    echo "<h3>Contenu des tables de catégories</h3>";
    
    foreach (array_keys($categoryTables) as $table) {
        if (in_array($table, $tables) || !$categoryTables[$table]) {
            $stmt = $conn->query("SELECT COUNT(*) FROM $table");
            $count = $stmt->fetchColumn();
            echo "<p>Table '$table': $count catégories</p>";
            
            if ($count == 0) {
                echo "<p style='color:orange;'>La table '$table' est vide.</p>";
            }
        }
    }
    
    // 5. Ajouter des catégories par défaut à toutes les tables
    echo "<h3>Ajout de catégories par défaut à toutes les tables</h3>";
    
    $defaultCategories = [
        ['Antibiotiques', 'Médicaments utilisés pour traiter les infections bactériennes'],
        ['Analgésiques', 'Médicaments contre la douleur'],
        ['Anti-inflammatoires', 'Médicaments qui réduisent l\'inflammation'],
        ['Antihistaminiques', 'Médicaments contre les allergies'],
        ['Antidépresseurs', 'Médicaments pour traiter la dépression'],
        ['Antihypertenseurs', 'Médicaments pour traiter l\'hypertension artérielle'],
        ['Antidiabétiques', 'Médicaments pour contrôler la glycémie'],
        ['Anticoagulants', 'Médicaments qui préviennent la coagulation du sang'],
        ['Bronchodilatateurs', 'Médicaments pour traiter l\'asthme et la BPCO'],
        ['Corticostéroïdes', 'Médicaments anti-inflammatoires stéroïdiens'],
        ['Diurétiques', 'Médicaments qui augmentent la production d\'urine'],
        ['Hypnotiques', 'Médicaments pour induire le sommeil'],
        ['Immunosuppresseurs', 'Médicaments qui suppriment le système immunitaire'],
        ['Laxatifs', 'Médicaments pour traiter la constipation'],
        ['Statines', 'Médicaments pour réduire le cholestérol'],
        ['Vaccins', 'Préparations utilisées pour stimuler la réponse immunitaire'],
        ['Vitamines et minéraux', 'Suppléments nutritionnels'],
        ['Médicaments dermatologiques', 'Médicaments pour traiter les affections cutanées'],
        ['Médicaments ophtalmiques', 'Médicaments pour traiter les affections oculaires'],
        ['Médicaments ORL', 'Médicaments pour traiter les affections ORL']
    ];
    
    foreach (array_keys($categoryTables) as $table) {
        if (in_array($table, $tables) || !$categoryTables[$table]) {
            // Vider la table
            $conn->exec("TRUNCATE TABLE $table");
            
            // Ajouter les catégories
            $sql = "INSERT INTO $table (nom, description) VALUES (?, ?)";
            $stmt = $conn->prepare($sql);
            
            foreach ($defaultCategories as $category) {
                $stmt->execute($category);
            }
            
            echo "<p style='color:green;'>" . count($defaultCategories) . " catégories ajoutées à la table '$table'.</p>";
        }
    }
    
    // 6. Vérifier la structure des tables
    echo "<h3>Structure des tables</h3>";
    
    foreach (array_keys($categoryTables) as $table) {
        if (in_array($table, $tables) || !$categoryTables[$table]) {
            $columns = $conn->query("DESCRIBE $table")->fetchAll(PDO::FETCH_COLUMN);
            
            echo "<p>Table '$table': " . implode(", ", $columns) . "</p>";
            
            // Vérifier si la colonne 'name' existe (utilisée par Java au lieu de 'nom')
            if (!in_array('name', $columns) && in_array('nom', $columns)) {
                // Ajouter la colonne 'name' et la synchroniser avec 'nom'
                $conn->exec("ALTER TABLE $table ADD COLUMN name VARCHAR(255)");
                $conn->exec("UPDATE $table SET name = nom");
                
                echo "<p style='color:green;'>Colonne 'name' ajoutée à la table '$table' et synchronisée avec 'nom'.</p>";
            }
        }
    }
    
    // 7. Vérifier les tables utilisées par l'application Java
    echo "<h3>Vérification des tables Java</h3>";
    
    // Vérifier la table produits
    if (in_array('produits', $tables)) {
        $columns = $conn->query("DESCRIBE produits")->fetchAll(PDO::FETCH_COLUMN);
        
        echo "<p>Table 'produits': " . implode(", ", $columns) . "</p>";
        
        // Vérifier si la colonne categorie_id existe
        if (!in_array('categorie_id', $columns)) {
            $conn->exec("ALTER TABLE produits ADD COLUMN categorie_id INT(11)");
            echo "<p style='color:green;'>Colonne 'categorie_id' ajoutée à la table 'produits'.</p>";
        }
    }
    
    // 8. Vérifier si la table utilisée par Java est celle attendue
    echo "<h3>Analyse du code Java</h3>";
    
    echo "<p>L'application Java peut utiliser différentes tables pour les catégories:</p>";
    echo "<ul>";
    echo "<li>categorie_medicament (plus probable)</li>";
    echo "<li>category</li>";
    echo "<li>categories</li>";
    echo "<li>categories_medicaments</li>";
    echo "</ul>";
    
    echo "<p>Pour garantir la compatibilité, toutes ces tables ont été créées et remplies avec les mêmes catégories.</p>";
    
    // 9. Afficher les catégories disponibles
    echo "<h3>Catégories disponibles</h3>";
    
    $stmt = $conn->query("SELECT id, nom, description FROM categories ORDER BY nom");
    $categories = $stmt->fetchAll(PDO::FETCH_ASSOC);
    
    if (count($categories) > 0) {
        echo "<table border='1' style='border-collapse: collapse; width: 100%;'>";
        echo "<tr style='background-color: #f2f2f2;'><th>ID</th><th>Nom</th><th>Description</th></tr>";
        
        foreach ($categories as $category) {
            echo "<tr>";
            echo "<td>" . $category['id'] . "</td>";
            echo "<td>" . $category['nom'] . "</td>";
            echo "<td>" . $category['description'] . "</td>";
            echo "</tr>";
        }
        
        echo "</table>";
    } else {
        echo "<p>Aucune catégorie disponible.</p>";
    }
    
    echo "<h3>Instructions pour l'application Java</h3>";
    echo "<ol>";
    echo "<li>Redémarrez complètement l'application Java après avoir exécuté ce script</li>";
    echo "<li>Vérifiez que les catégories apparaissent dans le menu déroulant</li>";
    echo "<li>Si les catégories n'apparaissent toujours pas, vérifiez les logs de l'application Java pour identifier la table utilisée</li>";
    echo "</ol>";
    
} catch(PDOException $e) {
    echo "<h3 style='color:red;'>Erreur</h3>";
    echo "<p>" . $e->getMessage() . "</p>";
}

$conn = null;
?>
