<?php
// Script pour ajouter directement des catégories à la table categoriemedoc

// Connexion à la base de données
$servername = "localhost";
$username = "root";
$password = "";
$dbname = "bigpharma";

try {
    $conn = new PDO("mysql:host=$servername;dbname=$dbname", $username, $password);
    $conn->setAttribute(PDO::ATTR_ERRMODE, PDO::ERRMODE_EXCEPTION);
    
    echo "<h2>Ajout direct des catégories à la table categoriemedoc</h2>";
    
    // Vérifier si la table categoriemedoc existe
    $stmt = $conn->query("SHOW TABLES LIKE 'categoriemedoc'");
    if ($stmt->rowCount() == 0) {
        // Créer la table si elle n'existe pas
        $sql = "CREATE TABLE categoriemedoc (
            id INT(11) AUTO_INCREMENT PRIMARY KEY,
            nom VARCHAR(255) NOT NULL,
            description TEXT,
            libelle VARCHAR(255)
        )";
        $conn->exec($sql);
        echo "<p style='color:green;'>Table 'categoriemedoc' créée avec succès.</p>";
    } else {
        echo "<p>La table 'categoriemedoc' existe déjà.</p>";
    }
    
    // Vider la table
    $conn->exec("TRUNCATE TABLE categoriemedoc");
    echo "<p>Table 'categoriemedoc' vidée.</p>";
    
    // Insérer les catégories une par une avec des requêtes SQL directes
    $conn->exec("INSERT INTO categoriemedoc (id, nom, description, libelle) VALUES (1, 'Antibiotiques', 'Médicaments utilisés pour traiter les infections bactériennes', 'Antibiotiques')");
    $conn->exec("INSERT INTO categoriemedoc (id, nom, description, libelle) VALUES (2, 'Analgésiques', 'Médicaments contre la douleur', 'Analgésiques')");
    $conn->exec("INSERT INTO categoriemedoc (id, nom, description, libelle) VALUES (3, 'Anti-inflammatoires', 'Médicaments qui réduisent l\'inflammation', 'Anti-inflammatoires')");
    $conn->exec("INSERT INTO categoriemedoc (id, nom, description, libelle) VALUES (4, 'Antihistaminiques', 'Médicaments contre les allergies', 'Antihistaminiques')");
    $conn->exec("INSERT INTO categoriemedoc (id, nom, description, libelle) VALUES (5, 'Antidépresseurs', 'Médicaments pour traiter la dépression', 'Antidépresseurs')");
    
    echo "<p style='color:green;'>5 catégories ajoutées directement à la table 'categoriemedoc'.</p>";
    
    // Vérifier le contenu de la table
    $stmt = $conn->query("SELECT * FROM categoriemedoc");
    $categories = $stmt->fetchAll(PDO::FETCH_ASSOC);
    
    echo "<h3>Catégories dans la table 'categoriemedoc'</h3>";
    
    if (count($categories) > 0) {
        echo "<table border='1' style='border-collapse: collapse; width: 100%;'>";
        echo "<tr style='background-color: #f2f2f2;'><th>ID</th><th>Nom</th><th>Description</th><th>Libellé</th></tr>";
        
        foreach ($categories as $category) {
            echo "<tr>";
            echo "<td>" . $category['id'] . "</td>";
            echo "<td>" . $category['nom'] . "</td>";
            echo "<td>" . $category['description'] . "</td>";
            echo "<td>" . $category['libelle'] . "</td>";
            echo "</tr>";
        }
        
        echo "</table>";
    } else {
        echo "<p>Aucune catégorie disponible.</p>";
    }
    
    // Faire la même chose pour la table categorie
    $stmt = $conn->query("SHOW TABLES LIKE 'categorie'");
    if ($stmt->rowCount() == 0) {
        // Créer la table si elle n'existe pas
        $sql = "CREATE TABLE categorie (
            id INT(11) AUTO_INCREMENT PRIMARY KEY,
            nom VARCHAR(255) NOT NULL,
            description TEXT,
            libelle VARCHAR(255)
        )";
        $conn->exec($sql);
        echo "<p style='color:green;'>Table 'categorie' créée avec succès.</p>";
    } else {
        echo "<p>La table 'categorie' existe déjà.</p>";
    }
    
    // Vider la table
    $conn->exec("TRUNCATE TABLE categorie");
    echo "<p>Table 'categorie' vidée.</p>";
    
    // Insérer les mêmes catégories
    $conn->exec("INSERT INTO categorie (id, nom, description, libelle) VALUES (1, 'Antibiotiques', 'Médicaments utilisés pour traiter les infections bactériennes', 'Antibiotiques')");
    $conn->exec("INSERT INTO categorie (id, nom, description, libelle) VALUES (2, 'Analgésiques', 'Médicaments contre la douleur', 'Analgésiques')");
    $conn->exec("INSERT INTO categorie (id, nom, description, libelle) VALUES (3, 'Anti-inflammatoires', 'Médicaments qui réduisent l\'inflammation', 'Anti-inflammatoires')");
    $conn->exec("INSERT INTO categorie (id, nom, description, libelle) VALUES (4, 'Antihistaminiques', 'Médicaments contre les allergies', 'Antihistaminiques')");
    $conn->exec("INSERT INTO categorie (id, nom, description, libelle) VALUES (5, 'Antidépresseurs', 'Médicaments pour traiter la dépression', 'Antidépresseurs')");
    
    echo "<p style='color:green;'>5 catégories ajoutées directement à la table 'categorie'.</p>";
    
    // Faire la même chose pour la table categories
    $stmt = $conn->query("SHOW TABLES LIKE 'categories'");
    if ($stmt->rowCount() == 0) {
        // Créer la table si elle n'existe pas
        $sql = "CREATE TABLE categories (
            id INT(11) AUTO_INCREMENT PRIMARY KEY,
            nom VARCHAR(255) NOT NULL,
            description TEXT,
            libelle VARCHAR(255)
        )";
        $conn->exec($sql);
        echo "<p style='color:green;'>Table 'categories' créée avec succès.</p>";
    } else {
        echo "<p>La table 'categories' existe déjà.</p>";
    }
    
    // Vider la table
    $conn->exec("TRUNCATE TABLE categories");
    echo "<p>Table 'categories' vidée.</p>";
    
    // Insérer les mêmes catégories
    $conn->exec("INSERT INTO categories (id, nom, description, libelle) VALUES (1, 'Antibiotiques', 'Médicaments utilisés pour traiter les infections bactériennes', 'Antibiotiques')");
    $conn->exec("INSERT INTO categories (id, nom, description, libelle) VALUES (2, 'Analgésiques', 'Médicaments contre la douleur', 'Analgésiques')");
    $conn->exec("INSERT INTO categories (id, nom, description, libelle) VALUES (3, 'Anti-inflammatoires', 'Médicaments qui réduisent l\'inflammation', 'Anti-inflammatoires')");
    $conn->exec("INSERT INTO categories (id, nom, description, libelle) VALUES (4, 'Antihistaminiques', 'Médicaments contre les allergies', 'Antihistaminiques')");
    $conn->exec("INSERT INTO categories (id, nom, description, libelle) VALUES (5, 'Antidépresseurs', 'Médicaments pour traiter la dépression', 'Antidépresseurs')");
    
    echo "<p style='color:green;'>5 catégories ajoutées directement à la table 'categories'.</p>";
    
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
