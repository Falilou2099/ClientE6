<?php
// Script pour synchroniser les catégories entre l'application PHP et Java

// Connexion à la base de données
$servername = "localhost";
$username = "root";
$password = "";
$dbname = "bigpharma";

try {
    $conn = new PDO("mysql:host=$servername;dbname=$dbname", $username, $password);
    $conn->setAttribute(PDO::ATTR_ERRMODE, PDO::ERRMODE_EXCEPTION);
    
    echo "<h2>Synchronisation des catégories de médicaments</h2>";
    
    // 1. Vérifier si la table categories existe
    $tables = $conn->query("SHOW TABLES")->fetchAll(PDO::FETCH_COLUMN);
    
    if (!in_array('categories', $tables)) {
        echo "<p style='color:red;'>La table categories n'existe pas. Création de la table...</p>";
        
        // Créer la table categories
        $sql = "CREATE TABLE categories (
            id INT(11) AUTO_INCREMENT PRIMARY KEY,
            nom VARCHAR(255) NOT NULL,
            description TEXT,
            created_at DATETIME DEFAULT CURRENT_TIMESTAMP
        )";
        $conn->exec($sql);
        
        echo "<p style='color:green;'>Table categories créée avec succès.</p>";
    } else {
        echo "<p style='color:green;'>La table categories existe déjà.</p>";
    }
    
    // 2. Vérifier si la table categorie_medicament existe (table Java)
    if (!in_array('categorie_medicament', $tables)) {
        echo "<p style='color:orange;'>La table categorie_medicament n'existe pas. Création de la table...</p>";
        
        // Créer la table categorie_medicament pour l'application Java
        $sql = "CREATE TABLE categorie_medicament (
            id INT(11) AUTO_INCREMENT PRIMARY KEY,
            nom VARCHAR(255) NOT NULL,
            description TEXT,
            created_at DATETIME DEFAULT CURRENT_TIMESTAMP
        )";
        $conn->exec($sql);
        
        echo "<p style='color:green;'>Table categorie_medicament créée avec succès.</p>";
    } else {
        echo "<p style='color:green;'>La table categorie_medicament existe déjà.</p>";
    }
    
    // 3. Vérifier le nombre de catégories existantes
    $stmt = $conn->query("SELECT COUNT(*) FROM categories");
    $categoryCount = $stmt->fetchColumn();
    
    echo "<p>Nombre de catégories dans la table categories: $categoryCount</p>";
    
    // 4. Ajouter des catégories par défaut si aucune n'existe
    if ($categoryCount == 0) {
        echo "<p style='color:orange;'>Aucune catégorie trouvée. Ajout de catégories par défaut...</p>";
        
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
        
        $sql = "INSERT INTO categories (nom, description) VALUES (?, ?)";
        $stmt = $conn->prepare($sql);
        
        foreach ($defaultCategories as $category) {
            $stmt->execute($category);
        }
        
        echo "<p style='color:green;'>" . count($defaultCategories) . " catégories ajoutées à la table categories.</p>";
    }
    
    // 5. Synchroniser les catégories entre les tables
    echo "<h3>Synchronisation des catégories entre les tables</h3>";
    
    // Vider la table categorie_medicament
    $conn->exec("TRUNCATE TABLE categorie_medicament");
    echo "<p>Table categorie_medicament vidée.</p>";
    
    // Copier toutes les catégories de categories vers categorie_medicament
    $sql = "INSERT INTO categorie_medicament (id, nom, description) 
            SELECT id, nom, description FROM categories";
    $conn->exec($sql);
    
    // Compter le nombre de catégories synchronisées
    $stmt = $conn->query("SELECT COUNT(*) FROM categorie_medicament");
    $syncedCount = $stmt->fetchColumn();
    
    echo "<p style='color:green;'>$syncedCount catégories synchronisées avec succès.</p>";
    
    // 6. Afficher les catégories disponibles
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
    
    // 7. Créer un trigger pour maintenir la synchronisation
    echo "<h3>Création des triggers pour la synchronisation automatique</h3>";
    
    // Supprimer les triggers existants s'ils existent
    $conn->exec("DROP TRIGGER IF EXISTS after_category_insert");
    $conn->exec("DROP TRIGGER IF EXISTS after_category_update");
    $conn->exec("DROP TRIGGER IF EXISTS after_category_delete");
    
    // Créer les triggers
    $conn->exec("
        CREATE TRIGGER after_category_insert
        AFTER INSERT ON categories
        FOR EACH ROW
        BEGIN
            INSERT INTO categorie_medicament (id, nom, description)
            VALUES (NEW.id, NEW.nom, NEW.description);
        END
    ");
    
    $conn->exec("
        CREATE TRIGGER after_category_update
        AFTER UPDATE ON categories
        FOR EACH ROW
        BEGIN
            UPDATE categorie_medicament
            SET nom = NEW.nom, description = NEW.description
            WHERE id = NEW.id;
        END
    ");
    
    $conn->exec("
        CREATE TRIGGER after_category_delete
        AFTER DELETE ON categories
        FOR EACH ROW
        BEGIN
            DELETE FROM categorie_medicament
            WHERE id = OLD.id;
        END
    ");
    
    echo "<p style='color:green;'>Triggers créés avec succès pour maintenir la synchronisation automatique.</p>";
    
    echo "<h3>Opération terminée</h3>";
    echo "<p>Les catégories de médicaments ont été synchronisées entre l'application PHP et Java.</p>";
    echo "<p>Vous pouvez maintenant ajouter des médicaments en sélectionnant une catégorie dans l'application Java.</p>";
    
    // Formulaire pour ajouter une nouvelle catégorie
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
        
        echo "<p style='color:green;'>Catégorie '$nom' ajoutée avec succès.</p>";
        
        // Rafraîchir la page pour voir les résultats
        echo "<script>window.location.reload();</script>";
    }
    
} catch(PDOException $e) {
    echo "<h3 style='color:red;'>Erreur</h3>";
    echo "<p>" . $e->getMessage() . "</p>";
}

$conn = null;
?>
