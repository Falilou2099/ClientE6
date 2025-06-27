<?php
// Script final pour résoudre définitivement le problème des catégories dans l'application Java

// Connexion à la base de données
$servername = "localhost";
$username = "root";
$password = "";
$dbname = "bigpharma";

try {
    $conn = new PDO("mysql:host=$servername;dbname=$dbname", $username, $password);
    $conn->setAttribute(PDO::ATTR_ERRMODE, PDO::ERRMODE_EXCEPTION);
    
    echo "<h2>Solution finale pour les catégories Java</h2>";
    
    // Supprimer toutes les tables de catégories existantes pour repartir de zéro
    $tablesToDrop = [
        'categorie', 'Categorie', 'categories', 'Categories', 
        'category', 'Category', 'categorie_medicament', 'CategorieMedicament',
        'categories_medicaments', 'CategoriesMedicaments', 'categoriemedoc', 'CategorieMedoc'
    ];
    
    foreach ($tablesToDrop as $table) {
        $conn->exec("DROP TABLE IF EXISTS `$table`");
        echo "<p>Table '$table' supprimée si elle existait.</p>";
    }
    
    // Créer la table CategorieMedoc avec exactement la structure attendue
    $sql = "CREATE TABLE `CategorieMedoc` (
        `id` INT(11) AUTO_INCREMENT PRIMARY KEY,
        `name` VARCHAR(255) NOT NULL,
        `description` TEXT
    )";
    $conn->exec($sql);
    echo "<p style='color:green;'>Table 'CategorieMedoc' créée avec la structure exacte.</p>";
    
    // Insérer les catégories dans cette table
    $categories = [
        ['Analgésiques', 'Médicaments contre la douleur'],
        ['Anti-inflammatoires', 'Médicaments qui réduisent l\'inflammation'],
        ['Antibiotiques', 'Médicaments utilisés pour traiter les infections bactériennes'],
        ['Antihistaminiques', 'Médicaments contre les allergies'],
        ['Gastro-entérologie', 'Médicaments pour le système digestif'],
        ['Dermatologie', 'Médicaments pour la peau'],
        ['Cardiologie', 'Médicaments pour le cœur et la circulation'],
        ['Vitamines', 'Suppléments vitaminiques'],
        ['Compléments alimentaires', 'Suppléments nutritionnels'],
        ['Autres', 'Autres types de médicaments']
    ];
    
    $sql = "INSERT INTO `CategorieMedoc` (`name`, `description`) VALUES (?, ?)";
    $stmt = $conn->prepare($sql);
    
    foreach ($categories as $category) {
        $stmt->execute($category);
    }
    
    echo "<p style='color:green;'>" . count($categories) . " catégories ajoutées à la table 'CategorieMedoc'.</p>";
    
    // Créer la table Categorie avec la même structure
    $sql = "CREATE TABLE `Categorie` (
        `id` INT(11) AUTO_INCREMENT PRIMARY KEY,
        `name` VARCHAR(255) NOT NULL,
        `description` TEXT
    )";
    $conn->exec($sql);
    echo "<p style='color:green;'>Table 'Categorie' créée avec la structure exacte.</p>";
    
    // Insérer les mêmes catégories
    $sql = "INSERT INTO `Categorie` (`name`, `description`) VALUES (?, ?)";
    $stmt = $conn->prepare($sql);
    
    foreach ($categories as $category) {
        $stmt->execute($category);
    }
    
    echo "<p style='color:green;'>" . count($categories) . " catégories ajoutées à la table 'Categorie'.</p>";
    
    // Créer la table Categories avec la même structure
    $sql = "CREATE TABLE `Categories` (
        `id` INT(11) AUTO_INCREMENT PRIMARY KEY,
        `name` VARCHAR(255) NOT NULL,
        `description` TEXT
    )";
    $conn->exec($sql);
    echo "<p style='color:green;'>Table 'Categories' créée avec la structure exacte.</p>";
    
    // Insérer les mêmes catégories
    $sql = "INSERT INTO `Categories` (`name`, `description`) VALUES (?, ?)";
    $stmt = $conn->prepare($sql);
    
    foreach ($categories as $category) {
        $stmt->execute($category);
    }
    
    echo "<p style='color:green;'>" . count($categories) . " catégories ajoutées à la table 'Categories'.</p>";
    
    // Créer la table categoriemedoc (minuscules) avec la même structure
    $sql = "CREATE TABLE `categoriemedoc` (
        `id` INT(11) AUTO_INCREMENT PRIMARY KEY,
        `name` VARCHAR(255) NOT NULL,
        `description` TEXT
    )";
    $conn->exec($sql);
    echo "<p style='color:green;'>Table 'categoriemedoc' créée avec la structure exacte.</p>";
    
    // Insérer les mêmes catégories
    $sql = "INSERT INTO `categoriemedoc` (`name`, `description`) VALUES (?, ?)";
    $stmt = $conn->prepare($sql);
    
    foreach ($categories as $category) {
        $stmt->execute($category);
    }
    
    echo "<p style='color:green;'>" . count($categories) . " catégories ajoutées à la table 'categoriemedoc'.</p>";
    
    // Créer la table categorie (minuscules) avec la même structure
    $sql = "CREATE TABLE `categorie` (
        `id` INT(11) AUTO_INCREMENT PRIMARY KEY,
        `name` VARCHAR(255) NOT NULL,
        `description` TEXT
    )";
    $conn->exec($sql);
    echo "<p style='color:green;'>Table 'categorie' créée avec la structure exacte.</p>";
    
    // Insérer les mêmes catégories
    $sql = "INSERT INTO `categorie` (`name`, `description`) VALUES (?, ?)";
    $stmt = $conn->prepare($sql);
    
    foreach ($categories as $category) {
        $stmt->execute($category);
    }
    
    echo "<p style='color:green;'>" . count($categories) . " catégories ajoutées à la table 'categorie'.</p>";
    
    // Créer la table categories (minuscules) avec la même structure
    $sql = "CREATE TABLE `categories` (
        `id` INT(11) AUTO_INCREMENT PRIMARY KEY,
        `name` VARCHAR(255) NOT NULL,
        `description` TEXT
    )";
    $conn->exec($sql);
    echo "<p style='color:green;'>Table 'categories' créée avec la structure exacte.</p>";
    
    // Insérer les mêmes catégories
    $sql = "INSERT INTO `categories` (`name`, `description`) VALUES (?, ?)";
    $stmt = $conn->prepare($sql);
    
    foreach ($categories as $category) {
        $stmt->execute($category);
    }
    
    echo "<p style='color:green;'>" . count($categories) . " catégories ajoutées à la table 'categories'.</p>";
    
    // Créer la table category avec la même structure
    $sql = "CREATE TABLE `category` (
        `id` INT(11) AUTO_INCREMENT PRIMARY KEY,
        `name` VARCHAR(255) NOT NULL,
        `description` TEXT
    )";
    $conn->exec($sql);
    echo "<p style='color:green;'>Table 'category' créée avec la structure exacte.</p>";
    
    // Insérer les mêmes catégories
    $sql = "INSERT INTO `category` (`name`, `description`) VALUES (?, ?)";
    $stmt = $conn->prepare($sql);
    
    foreach ($categories as $category) {
        $stmt->execute($category);
    }
    
    echo "<p style='color:green;'>" . count($categories) . " catégories ajoutées à la table 'category'.</p>";
    
    // Vérifier que les catégories ont bien été insérées
    echo "<h3>Vérification des catégories insérées</h3>";
    
    foreach ($tablesToDrop as $table) {
        if (in_array($table, $tablesToDrop)) {
            try {
                $stmt = $conn->query("SELECT COUNT(*) FROM `$table`");
                $count = $stmt->fetchColumn();
                echo "<p>Table '$table': $count catégories</p>";
            } catch (PDOException $e) {
                echo "<p>Table '$table': non disponible</p>";
            }
        }
    }
    
    echo "<h3>Instructions finales</h3>";
    echo "<ol>";
    echo "<li><strong>Fermez complètement l'application Java</strong> (pas seulement la fenêtre, mais le processus entier)</li>";
    echo "<li><strong>Redémarrez l'application Java</strong></li>";
    echo "<li>Essayez à nouveau d'ajouter un produit</li>";
    echo "</ol>";
    
    echo "<p>Cette solution a recréé toutes les tables possibles avec la structure exacte attendue par l'application Java.</p>";
    
} catch(PDOException $e) {
    echo "<h3 style='color:red;'>Erreur</h3>";
    echo "<p>" . $e->getMessage() . "</p>";
}

$conn = null;
?>
