-- Script simplifié pour créer et remplir la table des catégories
USE bigpharma;

-- Supprimer la table si elle existe déjà pour repartir de zéro
DROP TABLE IF EXISTS categories;

-- Créer la table des catégories avec une structure simple
CREATE TABLE categories (
    id INT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(100) NOT NULL UNIQUE
);

-- Insérer les catégories de produits pharmaceutiques
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

-- Afficher les catégories pour vérification
SELECT * FROM categories;
