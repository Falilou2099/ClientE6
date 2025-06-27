<?php
// Script pour insérer automatiquement des catégories dans toutes les tables possibles
// Exécution automatique sans interaction utilisateur

// Connexion à la base de données
$servername = "localhost";
$username = "root";
$password = "";
$dbname = "bigpharma";

// Liste complète des catégories de médicaments
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
    [10, 'Corticostéroïdes', 'Médicaments anti-inflammatoires stéroïdiens'],
    [11, 'Diurétiques', 'Médicaments qui augmentent la production d\'urine'],
    [12, 'Hypnotiques', 'Médicaments pour induire le sommeil'],
    [13, 'Immunosuppresseurs', 'Médicaments qui suppriment le système immunitaire'],
    [14, 'Laxatifs', 'Médicaments pour traiter la constipation'],
    [15, 'Statines', 'Médicaments pour réduire le cholestérol']
];

// Exécution automatique
try {
    $conn = new PDO("mysql:host=$servername;dbname=$dbname", $username, $password);
    $conn->setAttribute(PDO::ATTR_ERRMODE, PDO::ERRMODE_EXCEPTION);
    
    // Liste des tables possibles pour les catégories
    $possibleTables = [
        'categorie',
        'categories',
        'category',
        'categorie_medicament',
        'categories_medicaments',
        'categoriemedoc'
    ];
    
    // Créer toutes les tables si elles n'existent pas
    foreach ($possibleTables as $table) {
        $stmt = $conn->query("SHOW TABLES LIKE '$table'");
        if ($stmt->rowCount() == 0) {
            $sql = "CREATE TABLE $table (
                id INT(11) AUTO_INCREMENT PRIMARY KEY,
                nom VARCHAR(255) NOT NULL,
                description TEXT,
                libelle VARCHAR(255)
            )";
            $conn->exec($sql);
        }
        
        // Vider la table
        $conn->exec("TRUNCATE TABLE $table");
        
        // Insérer les catégories
        foreach ($categories as $category) {
            $id = $category[0];
            $nom = $category[1];
            $description = $category[2];
            
            $sql = "INSERT INTO $table (id, nom, description, libelle) VALUES ($id, '$nom', '$description', '$nom')";
            $conn->exec($sql);
        }
    }
    
    // Vérifier que les catégories ont bien été insérées
    $results = [];
    foreach ($possibleTables as $table) {
        $stmt = $conn->query("SELECT COUNT(*) FROM $table");
        $count = $stmt->fetchColumn();
        $results[$table] = $count;
    }
    
    // Afficher les résultats
    echo json_encode([
        'success' => true,
        'message' => 'Catégories insérées avec succès',
        'counts' => $results
    ]);
    
} catch(PDOException $e) {
    echo json_encode([
        'success' => false,
        'message' => 'Erreur: ' . $e->getMessage()
    ]);
}

$conn = null;
?>
