<?php
// Script pour créer toutes les variations possibles de tables de catégories
// avec toutes les structures possibles

// Connexion à la base de données
$servername = "localhost";
$username = "root";
$password = "";
$dbname = "bigpharma";

try {
    $conn = new PDO("mysql:host=$servername;dbname=$dbname", $username, $password);
    $conn->setAttribute(PDO::ATTR_ERRMODE, PDO::ERRMODE_EXCEPTION);
    
    echo "<h2>Création de toutes les tables possibles de catégories</h2>";
    
    // Liste des noms de tables possibles
    $tableNames = [
        'categorie',
        'Categorie',
        'categories',
        'Categories',
        'category',
        'Category',
        'categorie_medicament',
        'CategorieMedicament',
        'categories_medicaments',
        'CategoriesMedicaments',
        'categoriemedoc',
        'CategorieMedoc'
    ];
    
    // Liste des structures possibles
    $structures = [
        // Structure 1: id, nom, description
        [
            "id INT(11) AUTO_INCREMENT PRIMARY KEY",
            "nom VARCHAR(255) NOT NULL",
            "description TEXT"
        ],
        // Structure 2: id, name, description
        [
            "id INT(11) AUTO_INCREMENT PRIMARY KEY",
            "name VARCHAR(255) NOT NULL",
            "description TEXT"
        ],
        // Structure 3: id, libelle, description
        [
            "id INT(11) AUTO_INCREMENT PRIMARY KEY",
            "libelle VARCHAR(255) NOT NULL",
            "description TEXT"
        ],
        // Structure 4: id, label, description
        [
            "id INT(11) AUTO_INCREMENT PRIMARY KEY",
            "label VARCHAR(255) NOT NULL",
            "description TEXT"
        ]
    ];
    
    // Catégories à insérer
    $categories = [
        [1, 'Antibiotiques', 'Médicaments utilisés pour traiter les infections bactériennes'],
        [2, 'Analgésiques', 'Médicaments contre la douleur'],
        [3, 'Anti-inflammatoires', 'Médicaments qui réduisent l\'inflammation'],
        [4, 'Antihistaminiques', 'Médicaments contre les allergies'],
        [5, 'Antidépresseurs', 'Médicaments pour traiter la dépression'],
        [6, 'Antihypertenseurs', 'Médicaments pour traiter l\'hypertension artérielle'],
        [7, 'Antidiabétiques', 'Médicaments pour contrôler la glycémie'],
        [8, 'Anticoagulants', 'Médicaments qui préviennent la coagulation du sang'],
        [9, 'Bronchodilatateurs', 'Médicaments pour traiter l\'asthme et la BPCO'],
        [10, 'Corticostéroïdes', 'Médicaments anti-inflammatoires stéroïdiens']
    ];
    
    // Créer et remplir toutes les combinaisons possibles
    foreach ($tableNames as $tableName) {
        foreach ($structures as $index => $structure) {
            // Nom de la colonne pour le nom de la catégorie
            $nameColumn = ($index == 0) ? "nom" : (($index == 1) ? "name" : (($index == 2) ? "libelle" : "label"));
            
            // Créer la table
            $conn->exec("DROP TABLE IF EXISTS $tableName");
            $sql = "CREATE TABLE $tableName (" . implode(", ", $structure) . ")";
            $conn->exec($sql);
            
            // Insérer les catégories
            $sql = "INSERT INTO $tableName (id, $nameColumn, description) VALUES (?, ?, ?)";
            $stmt = $conn->prepare($sql);
            
            foreach ($categories as $category) {
                $stmt->execute($category);
            }
            
            echo "<p style='color:green;'>Table '$tableName' créée avec la structure $index et " . count($categories) . " catégories ajoutées.</p>";
            
            // Ne garder qu'une seule structure par nom de table
            break;
        }
    }
    
    echo "<h3>Toutes les tables possibles ont été créées</h3>";
    echo "<p>Redémarrez l'application Java et essayez à nouveau d'ajouter un produit.</p>";
    
} catch(PDOException $e) {
    echo "<h3 style='color:red;'>Erreur</h3>";
    echo "<p>" . $e->getMessage() . "</p>";
}

$conn = null;
?>
