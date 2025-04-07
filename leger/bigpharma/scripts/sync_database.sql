-- Script de synchronisation de la base de données avec le client lourd

-- Supprimer la table existante si elle existe
DROP TABLE IF EXISTS produits;

-- Créer la table produits exactement comme dans le client lourd
CREATE TABLE produits (
    id INT AUTO_INCREMENT PRIMARY KEY,
    nom VARCHAR(255) NOT NULL,
    description TEXT,
    prix DECIMAL(10, 2) NOT NULL,
    quantite_stock INT NOT NULL DEFAULT 0,
    categorie VARCHAR(100),
    date_peremption DATE
);

-- Copier les données de la table produits du client lourd
INSERT INTO produits (nom, description, prix, quantite_stock, categorie, date_peremption)
SELECT nom, description, prix, quantite_stock, categorie, date_peremption
FROM clientlegerlourd.produits;
