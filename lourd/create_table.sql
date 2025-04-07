-- Supprimer la table existante si elle existe
DROP TABLE IF EXISTS produits;

-- Créer la table avec les colonnes correctes
CREATE TABLE produits (
    id INT AUTO_INCREMENT PRIMARY KEY,
    nom VARCHAR(255) NOT NULL,
    description TEXT,
    prix DECIMAL(10, 2) NOT NULL,
    quantite_stock INT NOT NULL,
    categorie VARCHAR(100)
);
