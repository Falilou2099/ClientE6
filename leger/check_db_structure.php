<?php
try {
    $conn = new PDO("mysql:host=localhost;dbname=clientlegerlourd", "root", "");
    $conn->setAttribute(PDO::ATTR_ERRMODE, PDO::ERRMODE_EXCEPTION);

    // Récupérer la liste des tables
    $stmt = $conn->query("SHOW TABLES");
    $tables = $stmt->fetchAll(PDO::FETCH_COLUMN);
    
    echo "Tables dans la base de données :\n";
    print_r($tables);

    // Afficher la structure de la table 'produits'
    $stmt = $conn->query("DESCRIBE produits");
    $columns = $stmt->fetchAll(PDO::FETCH_ASSOC);
    
    echo "\nStructure de la table 'produits' :\n";
    print_r($columns);
} catch(PDOException $e) {
    echo "Erreur de connexion : " . $e->getMessage();
}
?>
