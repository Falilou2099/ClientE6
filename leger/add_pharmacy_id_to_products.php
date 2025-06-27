<?php
// Script pour ajouter la colonne pharmacy_id à la table produits
require_once __DIR__ . '/config/database.php';

// Fonction pour vérifier si une colonne existe dans une table
function columnExists($tableName, $columnName, $pdo) {
    try {
        $stmt = $pdo->prepare("
            SELECT COUNT(*) 
            FROM information_schema.COLUMNS 
            WHERE TABLE_SCHEMA = DATABASE() 
            AND TABLE_NAME = ? 
            AND COLUMN_NAME = ?
        ");
        $stmt->execute([$tableName, $columnName]);
        return $stmt->fetchColumn() > 0;
    } catch (Exception $e) {
        echo "Erreur lors de la vérification de la colonne: " . $e->getMessage() . "<br>";
        return false;
    }
}

// Ajouter la colonne pharmacy_id à la table produits si elle n'existe pas
if (!columnExists('produits', 'pharmacy_id', $pdo)) {
    try {
        $pdo->exec("
            ALTER TABLE produits 
            ADD COLUMN pharmacy_id INT NULL,
            ADD CONSTRAINT fk_produits_pharmacy 
            FOREIGN KEY (pharmacy_id) 
            REFERENCES pharmacies(id) 
            ON DELETE SET NULL
        ");
        echo "Colonne 'pharmacy_id' ajoutée avec succès à la table 'produits'.<br>";
        
        // Mettre à jour les produits existants pour les associer à une pharmacie par défaut
        // Récupérer la première pharmacie (si elle existe)
        $stmt = $pdo->query("SELECT id FROM pharmacies LIMIT 1");
        $pharmacy = $stmt->fetch(\PDO::FETCH_ASSOC);
        
        if ($pharmacy) {
            $pdo->exec("UPDATE produits SET pharmacy_id = {$pharmacy['id']} WHERE pharmacy_id IS NULL");
            echo "Produits existants associés à la pharmacie ID: {$pharmacy['id']}.<br>";
        } else {
            echo "Aucune pharmacie trouvée. Les produits existants n'ont pas été associés.<br>";
        }
    } catch (Exception $e) {
        echo "Erreur lors de l'ajout de la colonne 'pharmacy_id': " . $e->getMessage() . "<br>";
    }
} else {
    echo "La colonne 'pharmacy_id' existe déjà dans la table 'produits'.<br>";
}

echo "<br>Mise à jour de la base de données terminée.";
echo "<br><a href='/bigpharma/'>Retour à l'accueil</a>";
?>
