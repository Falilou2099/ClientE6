-- Script de création de la table produits

-- Supprimer la table si elle existe déjà
DROP TABLE IF EXISTS produits;

-- Créer la table produits
CREATE TABLE produits (
    id INT AUTO_INCREMENT PRIMARY KEY,
    nom VARCHAR(255) NOT NULL,
    description TEXT,
    prix DECIMAL(10, 2) NOT NULL,
    quantite_stock INT NOT NULL DEFAULT 0,
    categorie VARCHAR(100),
    date_peremption DATE,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
);

-- Ajouter un index sur la colonne de catégorie pour améliorer les performances de recherche
CREATE INDEX idx_categorie ON produits(categorie);

-- Ajouter une contrainte de vérification pour le prix
ALTER TABLE produits 
ADD CONSTRAINT chk_prix_positif CHECK (prix >= 0);

-- Ajouter une contrainte de vérification pour la quantité de stock
ALTER TABLE produits 
ADD CONSTRAINT chk_stock_positif CHECK (quantite_stock >= 0);
