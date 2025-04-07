-- Création de la base de données si elle n'existe pas
CREATE DATABASE IF NOT EXISTS clientlegerlourd;
USE clientlegerlourd;

-- Table des pharmacies
CREATE TABLE IF NOT EXISTS pharmacies (
    id INT AUTO_INCREMENT PRIMARY KEY,
    nom VARCHAR(255) NOT NULL,
    adresse TEXT NOT NULL,
    telephone VARCHAR(20) NOT NULL,
    email VARCHAR(255) NOT NULL UNIQUE,
    date_creation TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    statut ENUM('actif', 'inactif') DEFAULT 'actif'
);

-- Table des administrateurs
CREATE TABLE IF NOT EXISTS administrateurs (
    id INT AUTO_INCREMENT PRIMARY KEY,
    username VARCHAR(50) NOT NULL UNIQUE,
    password VARCHAR(255) NOT NULL,
    nom VARCHAR(100),
    prenom VARCHAR(100),
    email VARCHAR(255) UNIQUE,
    pharmacie_id INT NOT NULL,
    date_creation TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    dernier_login TIMESTAMP,
    statut ENUM('actif', 'inactif') DEFAULT 'actif',
    FOREIGN KEY (pharmacie_id) REFERENCES pharmacies(id)
);

-- Table des produits
CREATE TABLE IF NOT EXISTS produits (
    id INT AUTO_INCREMENT PRIMARY KEY,
    nom VARCHAR(255) NOT NULL,
    description TEXT,
    prix DECIMAL(10, 2) NOT NULL,
    quantite_stock INT NOT NULL,
    categorie VARCHAR(100),
    pharmacie_id INT NOT NULL,
    FOREIGN KEY (pharmacie_id) REFERENCES pharmacies(id)
);

-- Insertion d'une pharmacie de test
INSERT INTO pharmacies (nom, adresse, telephone, email) VALUES 
('Pharmacie Centrale', '15 rue de la Paix, 75001 Paris', '0123456789', 'contact@pharmaciecentrale.fr');

-- Insertion d'un administrateur de test (mot de passe : Admin123!)
INSERT INTO administrateurs (username, password, nom, prenom, email, pharmacie_id) VALUES 
('admin', '$2a$10$8KzaNdKwZ8P54UzQvh6dWeGd0GBKzwxnHG0ZMXWNOk3RJULt3zKi2', 'Dupont', 'Jean', 'admin@pharmaciecentrale.fr', 
(SELECT id FROM pharmacies WHERE nom = 'Pharmacie Centrale'));
