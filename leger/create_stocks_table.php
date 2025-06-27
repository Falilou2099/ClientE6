<?php
// Script pour créer la table stocks
require_once 'config/database.php';

try {
    $pdo = $GLOBALS['pdo'];
    
    echo "<h1>Création de la table 'stocks'</h1>";
    
    // Vérifier si la table existe déjà
    $stmt = $pdo->query("SHOW TABLES LIKE 'stocks'");
    $tableExists = $stmt->rowCount() > 0;
    
    if (!$tableExists) {
        // Créer la table stocks
        $pdo->exec("CREATE TABLE stocks (
            id INT AUTO_INCREMENT PRIMARY KEY,
            produit_id INT NOT NULL,
            pharmacy_id INT NOT NULL,
            quantite INT NOT NULL DEFAULT 0,
            seuil_alerte INT DEFAULT 5,
            date_derniere_maj DATETIME DEFAULT CURRENT_TIMESTAMP,
            FOREIGN KEY (produit_id) REFERENCES produits(id) ON DELETE CASCADE,
            FOREIGN KEY (pharmacy_id) REFERENCES pharmacies(id) ON DELETE CASCADE
        ) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;");
        
        echo "<p class='success'>La table 'stocks' a été créée avec succès.</p>";
    } else {
        // Vérifier si la colonne pharmacy_id existe
        $stmt = $pdo->query("DESCRIBE stocks");
        $columns = $stmt->fetchAll(PDO::FETCH_ASSOC);
        
        $hasPharmacyIdColumn = false;
        foreach ($columns as $column) {
            if ($column['Field'] === 'pharmacy_id') {
                $hasPharmacyIdColumn = true;
                break;
            }
        }
        
        if (!$hasPharmacyIdColumn) {
            $pdo->exec("ALTER TABLE stocks ADD COLUMN pharmacy_id INT NOT NULL AFTER produit_id");
            $pdo->exec("ALTER TABLE stocks ADD CONSTRAINT fk_stocks_pharmacy FOREIGN KEY (pharmacy_id) REFERENCES pharmacies(id) ON DELETE CASCADE");
            echo "<p class='success'>La colonne 'pharmacy_id' a été ajoutée à la table 'stocks'.</p>";
        } else {
            echo "<p class='info'>La table 'stocks' existe déjà et contient la colonne 'pharmacy_id'.</p>";
        }
    }
    
    // Vérifier la structure finale
    $stmt = $pdo->query("DESCRIBE stocks");
    $columns = $stmt->fetchAll(PDO::FETCH_ASSOC);
    
    echo "<h2>Structure de la table 'stocks'</h2>";
    echo "<table border='1' cellpadding='5'>";
    echo "<tr><th>Field</th><th>Type</th><th>Null</th><th>Key</th><th>Default</th><th>Extra</th></tr>";
    
    foreach ($columns as $column) {
        echo "<tr>";
        foreach ($column as $key => $value) {
            echo "<td>" . htmlspecialchars($value ?? 'NULL') . "</td>";
        }
        echo "</tr>";
    }
    
    echo "</table>";
    
    echo "<p>Vous pouvez maintenant utiliser la table 'stocks' pour gérer les stocks de produits par pharmacie.</p>";
    
} catch (PDOException $e) {
    echo "<h1>Erreur</h1>";
    echo "<p class='error'>Une erreur s'est produite : " . htmlspecialchars($e->getMessage()) . "</p>";
}
?>

<style>
    body {
        font-family: Arial, sans-serif;
        margin: 20px;
        line-height: 1.6;
    }
    h1, h2 {
        color: #333;
    }
    .success {
        color: green;
        font-weight: bold;
    }
    .info {
        color: blue;
    }
    .error {
        color: red;
        font-weight: bold;
    }
    table {
        border-collapse: collapse;
        margin-bottom: 20px;
        width: 100%;
    }
    th {
        background-color: #f2f2f2;
        text-align: left;
    }
    td, th {
        padding: 8px;
        border: 1px solid #ddd;
    }
</style>
