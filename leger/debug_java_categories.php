<?php
// Script pour déboguer et corriger les problèmes de catégories dans l'application Java

// Connexion à la base de données
$servername = "localhost";
$username = "root";
$password = "";
$dbname = "bigpharma";

try {
    $conn = new PDO("mysql:host=$servername;dbname=$dbname", $username, $password);
    $conn->setAttribute(PDO::ATTR_ERRMODE, PDO::ERRMODE_EXCEPTION);
    
    echo "<h2>Débogage avancé des catégories pour l'application Java</h2>";
    
    // 1. Analyser la structure exacte de la base de données
    $tables = $conn->query("SHOW TABLES")->fetchAll(PDO::FETCH_COLUMN);
    
    echo "<h3>Tables dans la base de données</h3>";
    echo "<ul>";
    foreach ($tables as $table) {
        echo "<li>$table</li>";
    }
    echo "</ul>";
    
    // 2. Créer la table avec le nom exact utilisé par Java
    $javaTable = "categorie";
    
    if (!in_array($javaTable, $tables)) {
        $sql = "CREATE TABLE $javaTable (
            id INT(11) AUTO_INCREMENT PRIMARY KEY,
            nom VARCHAR(255) NOT NULL,
            description TEXT
        )";
        $conn->exec($sql);
        echo "<p style='color:green;'>Table '$javaTable' créée avec succès.</p>";
    } else {
        echo "<p>La table '$javaTable' existe déjà.</p>";
    }
    
    // 3. Vérifier et corriger la structure de la table categorie
    $columns = $conn->query("DESCRIBE $javaTable")->fetchAll(PDO::FETCH_COLUMN);
    
    echo "<h3>Structure de la table '$javaTable'</h3>";
    echo "<ul>";
    foreach ($columns as $column) {
        echo "<li>$column</li>";
    }
    echo "</ul>";
    
    // Vérifier si les colonnes nécessaires existent
    $requiredColumns = ['id', 'nom', 'description'];
    foreach ($requiredColumns as $column) {
        if (!in_array($column, $columns)) {
            if ($column == 'id') {
                $conn->exec("ALTER TABLE $javaTable ADD COLUMN id INT(11) AUTO_INCREMENT PRIMARY KEY");
            } elseif ($column == 'nom') {
                $conn->exec("ALTER TABLE $javaTable ADD COLUMN nom VARCHAR(255) NOT NULL");
            } elseif ($column == 'description') {
                $conn->exec("ALTER TABLE $javaTable ADD COLUMN description TEXT");
            }
            echo "<p style='color:green;'>Colonne '$column' ajoutée à la table '$javaTable'.</p>";
        }
    }
    
    // 4. Remplir la table avec des catégories
    $stmt = $conn->query("SELECT COUNT(*) FROM $javaTable");
    $count = $stmt->fetchColumn();
    
    if ($count == 0) {
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
            ['Corticostéroïdes', 'Médicaments anti-inflammatoires stéroïdiens']
        ];
        
        $sql = "INSERT INTO $javaTable (nom, description) VALUES (?, ?)";
        $stmt = $conn->prepare($sql);
        
        foreach ($defaultCategories as $category) {
            $stmt->execute($category);
        }
        
        echo "<p style='color:green;'>" . count($defaultCategories) . " catégories ajoutées à la table '$javaTable'.</p>";
    } else {
        echo "<p>La table '$javaTable' contient déjà $count catégories.</p>";
    }
    
    // 5. Créer toutes les variations possibles de tables de catégories
    $possibleTables = ['categorie', 'categories', 'category', 'categorie_medicament', 'categories_medicaments'];
    
    foreach ($possibleTables as $table) {
        if ($table != $javaTable && !in_array($table, $tables)) {
            $sql = "CREATE TABLE $table (
                id INT(11) AUTO_INCREMENT PRIMARY KEY,
                nom VARCHAR(255) NOT NULL,
                description TEXT
            )";
            $conn->exec($sql);
            echo "<p style='color:green;'>Table '$table' créée avec succès.</p>";
        }
        
        if ($table != $javaTable && in_array($table, $tables)) {
            // Vider la table
            $conn->exec("TRUNCATE TABLE $table");
            
            // Copier les données de la table principale
            $sql = "INSERT INTO $table (id, nom, description) 
                    SELECT id, nom, description FROM $javaTable";
            $conn->exec($sql);
            
            echo "<p style='color:green;'>Données copiées de '$javaTable' vers '$table'.</p>";
        }
    }
    
    // 6. Vérifier si la table produits existe et a la bonne structure
    if (in_array('produits', $tables)) {
        $columns = $conn->query("DESCRIBE produits")->fetchAll(PDO::FETCH_COLUMN);
        
        echo "<h3>Structure de la table 'produits'</h3>";
        echo "<ul>";
        foreach ($columns as $column) {
            echo "<li>$column</li>";
        }
        echo "</ul>";
        
        // Vérifier si la colonne categorie_id existe
        if (!in_array('categorie_id', $columns)) {
            $conn->exec("ALTER TABLE produits ADD COLUMN categorie_id INT(11)");
            echo "<p style='color:green;'>Colonne 'categorie_id' ajoutée à la table 'produits'.</p>";
        }
    }
    
    // 7. Créer un script Java de test pour afficher la requête SQL utilisée
    echo "<h3>Script de débogage Java</h3>";
    echo "<p>Voici un extrait de code Java à ajouter temporairement dans votre application pour déboguer le problème :</p>";
    
    echo "<pre style='background-color: #f5f5f5; padding: 10px; border-radius: 5px;'>
// Ajouter ce code dans la méthode qui charge les catégories
try {
    Connection conn = DatabaseConfig.getConnection();
    String sql = \"SELECT * FROM categorie\"; // Essayez de changer 'categorie' par d'autres noms de tables
    
    System.out.println(\"Exécution de la requête SQL: \" + sql);
    
    Statement stmt = conn.createStatement();
    ResultSet rs = stmt.executeQuery(sql);
    
    int count = 0;
    while (rs.next()) {
        count++;
        int id = rs.getInt(\"id\");
        String nom = rs.getString(\"nom\");
        System.out.println(\"Catégorie trouvée: ID=\" + id + \", Nom=\" + nom);
    }
    
    System.out.println(\"Total des catégories trouvées: \" + count);
    
} catch (SQLException e) {
    System.err.println(\"Erreur SQL: \" + e.getMessage());
    e.printStackTrace();
}
</pre>";
    
    // 8. Afficher les catégories disponibles dans la table principale
    echo "<h3>Catégories disponibles dans la table '$javaTable'</h3>";
    
    $stmt = $conn->query("SELECT id, nom, description FROM $javaTable ORDER BY nom");
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
    
    // 9. Créer un script pour corriger le code Java
    echo "<h3>Solution pour l'application Java</h3>";
    echo "<p>Si après avoir exécuté ce script et redémarré l'application Java, les catégories ne s'affichent toujours pas, il est probable que le problème soit dans le code Java lui-même.</p>";
    
    echo "<p>Voici les étapes à suivre :</p>";
    echo "<ol>";
    echo "<li>Redémarrez l'application Java</li>";
    echo "<li>Si les catégories n'apparaissent toujours pas, vérifiez les logs de l'application Java pour identifier la table utilisée</li>";
    echo "<li>Si nécessaire, modifiez le code Java pour utiliser la table 'categorie' au lieu d'une autre table</li>";
    echo "</ol>";
    
    // 10. Créer une vue pour unifier toutes les tables de catégories
    echo "<h3>Création d'une vue unifiée</h3>";
    
    // Supprimer la vue si elle existe
    $conn->exec("DROP VIEW IF EXISTS v_categories");
    
    // Créer la vue
    $sql = "CREATE VIEW v_categories AS SELECT * FROM $javaTable";
    $conn->exec($sql);
    
    echo "<p style='color:green;'>Vue 'v_categories' créée avec succès.</p>";
    
    echo "<p>Cette vue peut être utilisée dans l'application Java pour accéder aux catégories, quelle que soit la table sous-jacente.</p>";
    
} catch(PDOException $e) {
    echo "<h3 style='color:red;'>Erreur</h3>";
    echo "<p>" . $e->getMessage() . "</p>";
}

$conn = null;
?>
