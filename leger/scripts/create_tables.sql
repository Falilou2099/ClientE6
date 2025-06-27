-- Création de la base de données
CREATE DATABASE IF NOT EXISTS clientlegerlourd;
USE clientlegerlourd;

-- Désactiver temporairement la vérification des clés étrangères
SET FOREIGN_KEY_CHECKS = 0;

-- Suppression des tables existantes
DROP TABLE IF EXISTS details_ventes;
DROP TABLE IF EXISTS ventes;
DROP TABLE IF EXISTS details_commandes;
DROP TABLE IF EXISTS commandes;
DROP TABLE IF EXISTS stocks;
DROP TABLE IF EXISTS produits;
DROP TABLE IF EXISTS fournisseurs;
DROP TABLE IF EXISTS clients;
DROP TABLE IF EXISTS admin;
DROP TABLE IF EXISTS pharmacies;

-- Réactiver la vérification des clés étrangères
SET FOREIGN_KEY_CHECKS = 1;

-- Table des pharmacies
CREATE TABLE pharmacies (
    id INT AUTO_INCREMENT PRIMARY KEY,
    nom VARCHAR(255) NOT NULL,
    adresse TEXT NOT NULL,
    telephone VARCHAR(20) NOT NULL,
    email VARCHAR(255) NOT NULL UNIQUE,
    date_creation TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- Table des administrateurs
CREATE TABLE admin (
    id INT AUTO_INCREMENT PRIMARY KEY,
    username VARCHAR(50) NOT NULL UNIQUE,
    password VARCHAR(255) NOT NULL,
    nom VARCHAR(100),
    prenom VARCHAR(100),
    email VARCHAR(255) UNIQUE,
    pharmacie_id INT NOT NULL,
    date_creation TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    dernier_login TIMESTAMP NULL,
    FOREIGN KEY (pharmacie_id) REFERENCES pharmacies(id)
);

-- Table des produits
CREATE TABLE produits (
    id INT AUTO_INCREMENT PRIMARY KEY,
    nom VARCHAR(255) NOT NULL,
    description TEXT,
    prix DECIMAL(10, 2) NOT NULL,
    quantite_stock INT NOT NULL DEFAULT 0,
    categorie VARCHAR(100),
    date_peremption DATE,
    pharmacie_id INT NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    FOREIGN KEY (pharmacie_id) REFERENCES pharmacies(id)
);

-- Insertion des données de test
INSERT INTO pharmacies (nom, adresse, telephone, email) VALUES 
('Pharmacie Centrale', '15 rue de la Paix, 75001 Paris', '0123456789', 'contact@pharmaciecentrale.fr');

INSERT INTO admin (username, password, nom, prenom, email, pharmacie_id) VALUES 
('admin', 'Admin123!', 'Dupont', 'Jean', 'admin@pharmaciecentrale.fr', 
(SELECT id FROM pharmacies WHERE nom = 'Pharmacie Centrale'));
