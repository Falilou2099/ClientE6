-- Script pour créer la table des catégories de produits
-- Utiliser la base de données
USE bigpharma;

-- Créer la table des catégories si elle n'existe pas déjà
CREATE TABLE IF NOT EXISTS categories (
    id INT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(100) NOT NULL UNIQUE,
    description TEXT,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- Supprimer les catégories existantes pour éviter les doublons
TRUNCATE TABLE categories;

-- Insérer les catégories de produits utilisées dans l'application PHP
INSERT INTO categories (name) VALUES
('Analgésiques'),
('Anti-inflammatoires'),
('Antibiotiques'),
('Antihistaminiques'),
('Gastro-entérologie'),
('Dermatologie'),
('Cardiologie'),
('Vitamines'),
('Compléments alimentaires'),
('Homéopathie'),
('Hygiène'),
('Premiers soins'),
('Ophtalmologie'),
('ORL'),
('Contraception'),
('Nutrition'),
('Autres');

-- Vérification que les catégories ont été créées
SELECT 'Catégories créées avec succès !' AS Message;
SELECT COUNT(*) AS 'Nombre de catégories' FROM categories;
