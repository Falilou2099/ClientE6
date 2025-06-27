-- Script de création et peuplement de la base de données BigPharma

-- Supprimer les tables existantes si elles existent
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

-- Insérer des données de test
INSERT INTO produits (nom, description, prix, quantite_stock, categorie, date_peremption) VALUES
('Doliprane 500mg', 'Médicament antalgique et antipyrétique à base de paracétamol', 4.50, 150, 'Antalgique', '2025-12-31'),
('Advil 200mg', 'Anti-inflammatoire et antidouleur à base d\'ibuprofène', 5.20, 80, 'Anti-inflammatoire', '2024-11-30'),
('Spasfon Lyoc 80mg', 'Médicament antispasmodique pour soulager les douleurs abdominales', 6.30, 50, 'Antispasmodique', '2025-06-15'),
('Efferalgan 1000mg', 'Comprimés effervescents de paracétamol pour traiter la douleur et la fièvre', 7.80, 100, 'Antalgique', '2025-09-20'),
('Xanax 0.5mg', 'Médicament anxiolytique pour traiter l\'anxiété', 12.50, 30, 'Anxiolytique', '2024-08-10'),
('Ventoline 100µg', 'Spray pour le traitement de l\'asthme et des bronchospasmes', 9.20, 60, 'Bronchodilatateur', '2025-03-25'),
('Kardegic 75mg', 'Traitement préventif des maladies cardiovasculaires', 8.70, 40, 'Anticoagulant', '2024-12-15'),
('Zithromax 250mg', 'Antibiotique à large spectre pour traiter diverses infections bactériennes', 15.60, 25, 'Antibiotique', '2024-10-05'),
('Maalox', 'Suspension buvable pour traiter les troubles digestifs et les brûlures d\'estomac', 5.90, 70, 'Antiacide', '2025-01-20'),
('Previscan 20mg', 'Anticoagulant oral pour prévenir les thromboses', 18.40, 35, 'Anticoagulant', '2024-07-30');

-- Créer un index sur la colonne de catégorie
CREATE INDEX idx_categorie ON produits(categorie);

-- Ajouter des contraintes de vérification
ALTER TABLE produits 
ADD CONSTRAINT chk_prix_positif CHECK (prix >= 0),
ADD CONSTRAINT chk_stock_positif CHECK (quantite_stock >= 0);
