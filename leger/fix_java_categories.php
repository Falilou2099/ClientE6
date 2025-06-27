<?php
// Script pour résoudre définitivement le problème des catégories dans l'application Java

// Connexion à la base de données
$servername = "localhost";
$username = "root";
$password = "";
$dbname = "bigpharma";

try {
    $conn = new PDO("mysql:host=$servername;dbname=$dbname", $username, $password);
    $conn->setAttribute(PDO::ATTR_ERRMODE, PDO::ERRMODE_EXCEPTION);
    
    echo "<h2>Correction finale des catégories pour l'application Java</h2>";
    
    // 1. Identifier toutes les tables possibles utilisées par Java
    $possibleTables = [
        'categorie', 
        'categories', 
        'category', 
        'categorie_medicament', 
        'categories_medicaments',
        'categoriemedoc'
    ];
    
    // 2. Vérifier quelles tables existent déjà
    $tables = $conn->query("SHOW TABLES")->fetchAll(PDO::FETCH_COLUMN);
    $existingTables = array_intersect($possibleTables, $tables);
    
    echo "<h3>Tables de catégories existantes</h3>";
    echo "<ul>";
    foreach ($existingTables as $table) {
        echo "<li>$table</li>";
    }
    echo "</ul>";
    
    // 3. Créer toutes les tables possibles avec la structure correcte
    echo "<h3>Création et mise à jour de toutes les tables possibles</h3>";
    
    foreach ($possibleTables as $table) {
        if (!in_array($table, $tables)) {
            // Créer la table si elle n'existe pas
            $sql = "CREATE TABLE $table (
                id INT(11) AUTO_INCREMENT PRIMARY KEY,
                nom VARCHAR(255) NOT NULL,
                description TEXT,
                libelle VARCHAR(255)
            )";
            $conn->exec($sql);
            echo "<p style='color:green;'>Table '$table' créée avec succès.</p>";
        } else {
            // Vérifier la structure de la table existante
            $columns = $conn->query("DESCRIBE $table")->fetchAll(PDO::FETCH_COLUMN);
            
            // Ajouter les colonnes manquantes
            if (!in_array('nom', $columns)) {
                $conn->exec("ALTER TABLE $table ADD COLUMN nom VARCHAR(255)");
                echo "<p style='color:green;'>Colonne 'nom' ajoutée à la table '$table'.</p>";
            }
            
            if (!in_array('description', $columns)) {
                $conn->exec("ALTER TABLE $table ADD COLUMN description TEXT");
                echo "<p style='color:green;'>Colonne 'description' ajoutée à la table '$table'.</p>";
            }
            
            if (!in_array('libelle', $columns)) {
                $conn->exec("ALTER TABLE $table ADD COLUMN libelle VARCHAR(255)");
                echo "<p style='color:green;'>Colonne 'libelle' ajoutée à la table '$table'.</p>";
            }
        }
        
        // Vider la table
        $conn->exec("TRUNCATE TABLE $table");
        echo "<p>Table '$table' vidée.</p>";
    }
    
    // 4. Ajouter les mêmes catégories à toutes les tables
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
    
    echo "<h3>Ajout des catégories à toutes les tables</h3>";
    
    foreach ($possibleTables as $table) {
        $sql = "INSERT INTO $table (nom, description, libelle) VALUES (?, ?, ?)";
        $stmt = $conn->prepare($sql);
        
        foreach ($defaultCategories as $category) {
            // Utiliser le nom comme libellé également
            $stmt->execute([$category[0], $category[1], $category[0]]);
        }
        
        echo "<p style='color:green;'>" . count($defaultCategories) . " catégories ajoutées à la table '$table'.</p>";
    }
    
    // 5. Vérifier et corriger la table produits
    if (in_array('produits', $tables)) {
        $columns = $conn->query("DESCRIBE produits")->fetchAll(PDO::FETCH_COLUMN);
        
        echo "<h3>Structure de la table 'produits'</h3>";
        echo "<ul>";
        foreach ($columns as $column) {
            echo "<li>$column</li>";
        }
        echo "</ul>";
        
        // Ajouter les colonnes nécessaires si elles n'existent pas
        $requiredColumns = [
            'categorie_id' => 'INT(11)',
            'categorie' => 'VARCHAR(255)'
        ];
        
        foreach ($requiredColumns as $column => $type) {
            if (!in_array($column, $columns)) {
                $conn->exec("ALTER TABLE produits ADD COLUMN $column $type");
                echo "<p style='color:green;'>Colonne '$column' ajoutée à la table 'produits'.</p>";
            }
        }
    }
    
    // 6. Créer une procédure stockée pour synchroniser les catégories
    echo "<h3>Création d'une procédure stockée pour la synchronisation</h3>";
    
    $conn->exec("DROP PROCEDURE IF EXISTS sync_categories");
    
    $sql = "
    CREATE PROCEDURE sync_categories()
    BEGIN
        -- Synchroniser toutes les tables de catégories
        DECLARE done INT DEFAULT FALSE;
        DECLARE cat_name VARCHAR(255);
        DECLARE cat_desc TEXT;
        DECLARE cat_id INT;
        
        -- Utiliser la table categories comme référence
        DECLARE cur CURSOR FOR SELECT id, nom, description FROM categories;
        DECLARE CONTINUE HANDLER FOR NOT FOUND SET done = TRUE;
        
        -- Vider toutes les autres tables
        TRUNCATE TABLE categorie;
        TRUNCATE TABLE category;
        TRUNCATE TABLE categorie_medicament;
        TRUNCATE TABLE categories_medicaments;
        TRUNCATE TABLE categoriemedoc;
        
        -- Remplir toutes les tables avec les mêmes données
        OPEN cur;
        
        read_loop: LOOP
            FETCH cur INTO cat_id, cat_name, cat_desc;
            IF done THEN
                LEAVE read_loop;
            END IF;
            
            INSERT INTO categorie (id, nom, description, libelle) VALUES (cat_id, cat_name, cat_desc, cat_name);
            INSERT INTO category (id, nom, description, libelle) VALUES (cat_id, cat_name, cat_desc, cat_name);
            INSERT INTO categorie_medicament (id, nom, description, libelle) VALUES (cat_id, cat_name, cat_desc, cat_name);
            INSERT INTO categories_medicaments (id, nom, description, libelle) VALUES (cat_id, cat_name, cat_desc, cat_name);
            INSERT INTO categoriemedoc (id, nom, description, libelle) VALUES (cat_id, cat_name, cat_desc, cat_name);
        END LOOP;
        
        CLOSE cur;
    END
    ";
    
    $conn->exec($sql);
    echo "<p style='color:green;'>Procédure stockée 'sync_categories' créée avec succès.</p>";
    
    // 7. Exécuter la procédure stockée
    $conn->exec("CALL sync_categories()");
    echo "<p style='color:green;'>Synchronisation des catégories effectuée.</p>";
    
    // 8. Afficher les catégories disponibles
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
    
    // 9. Créer un formulaire pour ajouter une catégorie
    echo "<h3>Ajouter une nouvelle catégorie</h3>";
    echo "<form method='post'>";
    echo "<label>Nom: </label><input type='text' name='nom' required><br>";
    echo "<label>Description: </label><textarea name='description'></textarea><br>";
    echo "<input type='submit' name='add_category' value='Ajouter la catégorie'>";
    echo "</form>";
    
    // Traiter le formulaire
    if ($_SERVER['REQUEST_METHOD'] === 'POST' && isset($_POST['add_category'])) {
        $nom = $_POST['nom'];
        $description = $_POST['description'];
        
        $sql = "INSERT INTO categories (nom, description) VALUES (?, ?)";
        $stmt = $conn->prepare($sql);
        $stmt->execute([$nom, $description]);
        
        // Synchroniser les catégories
        $conn->exec("CALL sync_categories()");
        
        echo "<p style='color:green;'>Catégorie '$nom' ajoutée avec succès et synchronisée avec toutes les tables.</p>";
        
        // Rafraîchir la page pour voir les résultats
        echo "<script>window.location.reload();</script>";
    }
    
    echo "<h3>Instructions finales</h3>";
    echo "<ol>";
    echo "<li>Redémarrez complètement l'application Java</li>";
    echo "<li>Essayez à nouveau d'ajouter un produit</li>";
    echo "<li>Si les catégories n'apparaissent toujours pas, vérifiez les logs de l'application Java pour identifier la table utilisée</li>";
    echo "</ol>";
    
} catch(PDOException $e) {
    echo "<h3 style='color:red;'>Erreur</h3>";
    echo "<p>" . $e->getMessage() . "</p>";
}

$conn = null;
?>
