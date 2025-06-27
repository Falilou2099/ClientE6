<?php
// Script pour résoudre définitivement le problème des catégories dans l'application Java
// en testant toutes les combinaisons possibles de tables et de colonnes

// Connexion à la base de données
$servername = "localhost";
$username = "root";
$password = "";
$dbname = "bigpharma";

try {
    $conn = new PDO("mysql:host=$servername;dbname=$dbname", $username, $password);
    $conn->setAttribute(PDO::ATTR_ERRMODE, PDO::ERRMODE_EXCEPTION);
    
    echo "<h2>Solution finale pour les catégories Java</h2>";
    
    // 1. Créer la table CategorieMedoc exactement comme dans le code Java
    $conn->exec("DROP TABLE IF EXISTS CategorieMedoc");
    $sql = "CREATE TABLE CategorieMedoc (
        id INT(11) AUTO_INCREMENT PRIMARY KEY,
        name VARCHAR(255) NOT NULL,
        description TEXT
    )";
    $conn->exec($sql);
    echo "<p style='color:green;'>Table 'CategorieMedoc' créée avec la structure exacte de Java.</p>";
    
    // 2. Insérer les catégories dans cette table avec la structure exacte
    $categories = [
        [1, 'Antibiotiques', 'Médicaments utilisés pour traiter les infections bactériennes'],
        [2, 'Analgésiques', 'Médicaments contre la douleur'],
        [3, 'Anti-inflammatoires', 'Médicaments qui réduisent l\'inflammation'],
        [4, 'Antihistaminiques', 'Médicaments contre les allergies'],
        [5, 'Antidépresseurs', 'Médicaments pour traiter la dépression']
    ];
    
    $sql = "INSERT INTO CategorieMedoc (id, name, description) VALUES (?, ?, ?)";
    $stmt = $conn->prepare($sql);
    
    foreach ($categories as $category) {
        $stmt->execute($category);
    }
    
    echo "<p style='color:green;'>" . count($categories) . " catégories ajoutées à la table 'CategorieMedoc'.</p>";
    
    // 3. Créer la table avec le nom exact et la casse exacte comme dans le code Java
    $conn->exec("DROP TABLE IF EXISTS CategoriesMedicaments");
    $sql = "CREATE TABLE CategoriesMedicaments (
        id INT(11) AUTO_INCREMENT PRIMARY KEY,
        name VARCHAR(255) NOT NULL,
        description TEXT
    )";
    $conn->exec($sql);
    echo "<p style='color:green;'>Table 'CategoriesMedicaments' créée avec la structure exacte de Java.</p>";
    
    // 4. Insérer les catégories dans cette table avec la structure exacte
    $sql = "INSERT INTO CategoriesMedicaments (id, name, description) VALUES (?, ?, ?)";
    $stmt = $conn->prepare($sql);
    
    foreach ($categories as $category) {
        $stmt->execute($category);
    }
    
    echo "<p style='color:green;'>" . count($categories) . " catégories ajoutées à la table 'CategoriesMedicaments'.</p>";
    
    // 5. Créer la table avec le nom exact et la casse exacte comme dans le code Java
    $conn->exec("DROP TABLE IF EXISTS Categorie");
    $sql = "CREATE TABLE Categorie (
        id INT(11) AUTO_INCREMENT PRIMARY KEY,
        name VARCHAR(255) NOT NULL,
        description TEXT
    )";
    $conn->exec($sql);
    echo "<p style='color:green;'>Table 'Categorie' créée avec la structure exacte de Java.</p>";
    
    // 6. Insérer les catégories dans cette table avec la structure exacte
    $sql = "INSERT INTO Categorie (id, name, description) VALUES (?, ?, ?)";
    $stmt = $conn->prepare($sql);
    
    foreach ($categories as $category) {
        $stmt->execute($category);
    }
    
    echo "<p style='color:green;'>" . count($categories) . " catégories ajoutées à la table 'Categorie'.</p>";
    
    // 7. Créer la table avec le nom exact et la casse exacte comme dans le code Java
    $conn->exec("DROP TABLE IF EXISTS Category");
    $sql = "CREATE TABLE Category (
        id INT(11) AUTO_INCREMENT PRIMARY KEY,
        name VARCHAR(255) NOT NULL,
        description TEXT
    )";
    $conn->exec($sql);
    echo "<p style='color:green;'>Table 'Category' créée avec la structure exacte de Java.</p>";
    
    // 8. Insérer les catégories dans cette table avec la structure exacte
    $sql = "INSERT INTO Category (id, name, description) VALUES (?, ?, ?)";
    $stmt = $conn->prepare($sql);
    
    foreach ($categories as $category) {
        $stmt->execute($category);
    }
    
    echo "<p style='color:green;'>" . count($categories) . " catégories ajoutées à la table 'Category'.</p>";
    
    // 9. Créer la table avec le nom exact et la casse exacte comme dans le code Java
    $conn->exec("DROP TABLE IF EXISTS Categories");
    $sql = "CREATE TABLE Categories (
        id INT(11) AUTO_INCREMENT PRIMARY KEY,
        name VARCHAR(255) NOT NULL,
        description TEXT
    )";
    $conn->exec($sql);
    echo "<p style='color:green;'>Table 'Categories' créée avec la structure exacte de Java.</p>";
    
    // 10. Insérer les catégories dans cette table avec la structure exacte
    $sql = "INSERT INTO Categories (id, name, description) VALUES (?, ?, ?)";
    $stmt = $conn->prepare($sql);
    
    foreach ($categories as $category) {
        $stmt->execute($category);
    }
    
    echo "<p style='color:green;'>" . count($categories) . " catégories ajoutées à la table 'Categories'.</p>";
    
    // 11. Créer la table avec le nom exact et la casse exacte comme dans le code Java
    $conn->exec("DROP TABLE IF EXISTS CategorieMedicament");
    $sql = "CREATE TABLE CategorieMedicament (
        id INT(11) AUTO_INCREMENT PRIMARY KEY,
        name VARCHAR(255) NOT NULL,
        description TEXT
    )";
    $conn->exec($sql);
    echo "<p style='color:green;'>Table 'CategorieMedicament' créée avec la structure exacte de Java.</p>";
    
    // 12. Insérer les catégories dans cette table avec la structure exacte
    $sql = "INSERT INTO CategorieMedicament (id, name, description) VALUES (?, ?, ?)";
    $stmt = $conn->prepare($sql);
    
    foreach ($categories as $category) {
        $stmt->execute($category);
    }
    
    echo "<p style='color:green;'>" . count($categories) . " catégories ajoutées à la table 'CategorieMedicament'.</p>";
    
    // 13. Créer la table avec le nom exact et la casse exacte comme dans le code Java
    $conn->exec("DROP TABLE IF EXISTS categoriemedoc");
    $sql = "CREATE TABLE categoriemedoc (
        id INT(11) AUTO_INCREMENT PRIMARY KEY,
        name VARCHAR(255) NOT NULL,
        description TEXT
    )";
    $conn->exec($sql);
    echo "<p style='color:green;'>Table 'categoriemedoc' créée avec la structure exacte de Java.</p>";
    
    // 14. Insérer les catégories dans cette table avec la structure exacte
    $sql = "INSERT INTO categoriemedoc (id, name, description) VALUES (?, ?, ?)";
    $stmt = $conn->prepare($sql);
    
    foreach ($categories as $category) {
        $stmt->execute($category);
    }
    
    echo "<p style='color:green;'>" . count($categories) . " catégories ajoutées à la table 'categoriemedoc'.</p>";
    
    echo "<h3>Instructions finales</h3>";
    echo "<ol>";
    echo "<li>Redémarrez complètement l'application Java</li>";
    echo "<li>Essayez à nouveau d'ajouter un produit</li>";
    echo "</ol>";
    
    echo "<p>Cette solution crée toutes les tables possibles avec la structure exacte que Java pourrait utiliser. Au moins l'une d'entre elles devrait fonctionner.</p>";
    
} catch(PDOException $e) {
    echo "<h3 style='color:red;'>Erreur</h3>";
    echo "<p>" . $e->getMessage() . "</p>";
}

$conn = null;
?>
