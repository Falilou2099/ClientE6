<?php
// Script pour ajouter manuellement des catégories à toutes les tables possibles

// Connexion à la base de données
$servername = "localhost";
$username = "root";
$password = "";
$dbname = "bigpharma";

try {
    $conn = new PDO("mysql:host=$servername;dbname=$dbname", $username, $password);
    $conn->setAttribute(PDO::ATTR_ERRMODE, PDO::ERRMODE_EXCEPTION);
    
    echo "<h2>Ajout manuel des catégories</h2>";
    
    // Liste des tables possibles
    $tables = [
        'categorie',
        'categories',
        'category',
        'categorie_medicament',
        'categories_medicaments',
        'categoriemedoc'
    ];
    
    // Vérifier quelles tables existent
    $existingTables = [];
    foreach ($tables as $table) {
        $stmt = $conn->query("SHOW TABLES LIKE '$table'");
        if ($stmt->rowCount() > 0) {
            $existingTables[] = $table;
        } else {
            // Créer la table si elle n'existe pas
            $sql = "CREATE TABLE $table (
                id INT(11) AUTO_INCREMENT PRIMARY KEY,
                nom VARCHAR(255) NOT NULL,
                description TEXT,
                libelle VARCHAR(255)
            )";
            $conn->exec($sql);
            $existingTables[] = $table;
            echo "<p>Table '$table' créée.</p>";
        }
    }
    
    echo "<h3>Tables existantes</h3>";
    echo "<ul>";
    foreach ($existingTables as $table) {
        echo "<li>$table</li>";
    }
    echo "</ul>";
    
    // Vider toutes les tables
    foreach ($existingTables as $table) {
        $conn->exec("TRUNCATE TABLE $table");
        echo "<p>Table '$table' vidée.</p>";
    }
    
    // Catégories à ajouter
    $categories = [
        [1, 'Antibiotiques', 'Médicaments utilisés pour traiter les infections bactériennes', 'Antibiotiques'],
        [2, 'Analgésiques', 'Médicaments contre la douleur', 'Analgésiques'],
        [3, 'Anti-inflammatoires', 'Médicaments qui réduisent l\'inflammation', 'Anti-inflammatoires'],
        [4, 'Antihistaminiques', 'Médicaments contre les allergies', 'Antihistaminiques'],
        [5, 'Antidépresseurs', 'Médicaments pour traiter la dépression', 'Antidépresseurs'],
        [6, 'Antihypertenseurs', 'Médicaments pour traiter l\'hypertension artérielle', 'Antihypertenseurs'],
        [7, 'Antidiabétiques', 'Médicaments pour contrôler la glycémie', 'Antidiabétiques'],
        [8, 'Anticoagulants', 'Médicaments qui préviennent la coagulation du sang', 'Anticoagulants'],
        [9, 'Bronchodilatateurs', 'Médicaments pour traiter l\'asthme et la BPCO', 'Bronchodilatateurs'],
        [10, 'Corticostéroïdes', 'Médicaments anti-inflammatoires stéroïdiens', 'Corticostéroïdes']
    ];
    
    // Ajouter les catégories à toutes les tables
    foreach ($existingTables as $table) {
        $sql = "INSERT INTO $table (id, nom, description, libelle) VALUES (?, ?, ?, ?)";
        $stmt = $conn->prepare($sql);
        
        foreach ($categories as $category) {
            $stmt->execute($category);
        }
        
        echo "<p style='color:green;'>" . count($categories) . " catégories ajoutées à la table '$table'.</p>";
    }
    
    // Vérifier le contenu des tables
    echo "<h3>Contenu des tables après ajout</h3>";
    
    foreach ($existingTables as $table) {
        $stmt = $conn->query("SELECT COUNT(*) FROM $table");
        $count = $stmt->fetchColumn();
        echo "<p>Table '$table': $count catégories</p>";
    }
    
    // Afficher les catégories ajoutées
    echo "<h3>Catégories ajoutées</h3>";
    
    $stmt = $conn->query("SELECT id, nom, description FROM categorie ORDER BY id");
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
    
    echo "<h3>Instructions</h3>";
    echo "<ol>";
    echo "<li>Redémarrez complètement l'application Java</li>";
    echo "<li>Essayez à nouveau d'ajouter un produit</li>";
    echo "</ol>";
    
} catch(PDOException $e) {
    echo "<h3 style='color:red;'>Erreur</h3>";
    echo "<p>" . $e->getMessage() . "</p>";
}

$conn = null;
?>
