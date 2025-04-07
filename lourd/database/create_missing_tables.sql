-- Script pour créer les tables manquantes dans la base de données bigpharma
-- Assurez-vous que la base de données bigpharma existe
CREATE DATABASE IF NOT EXISTS bigpharma;
USE bigpharma;

-- Table des pharmacies (si elle n'existe pas déjà)
CREATE TABLE IF NOT EXISTS pharmacies (
    id INT AUTO_INCREMENT PRIMARY KEY,
    nom VARCHAR(255) NOT NULL,
    adresse TEXT NOT NULL,
    telephone VARCHAR(20) NOT NULL,
    email VARCHAR(255) NOT NULL UNIQUE,
    date_creation TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    statut ENUM('actif', 'inactif') DEFAULT 'actif'
);

-- Table des administrateurs (si elle n'existe pas déjà)
CREATE TABLE IF NOT EXISTS administrateurs (
    id INT AUTO_INCREMENT PRIMARY KEY,
    username VARCHAR(50) NOT NULL UNIQUE,
    password VARCHAR(255) NOT NULL,
    nom VARCHAR(100),
    prenom VARCHAR(100),
    email VARCHAR(255) UNIQUE,
    pharmacie_id INT NOT NULL,
    date_creation TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    dernier_login TIMESTAMP NULL,
    statut ENUM('actif', 'inactif') DEFAULT 'actif',
    FOREIGN KEY (pharmacie_id) REFERENCES pharmacies(id)
);

-- Table des produits (si elle n'existe pas déjà)
CREATE TABLE IF NOT EXISTS produits (
    id INT AUTO_INCREMENT PRIMARY KEY,
    nom VARCHAR(255) NOT NULL,
    description TEXT,
    prix_vente DECIMAL(10, 2) NOT NULL,
    prix_achat DECIMAL(10, 2) NOT NULL,
    quantite_stock INT NOT NULL DEFAULT 0,
    seuil_alerte INT NOT NULL DEFAULT 10,
    categorie VARCHAR(100),
    date_expiration DATE NULL,
    pharmacie_id INT NOT NULL,
    FOREIGN KEY (pharmacie_id) REFERENCES pharmacies(id)
);

-- Table des fournisseurs
CREATE TABLE IF NOT EXISTS fournisseurs (
    id INT AUTO_INCREMENT PRIMARY KEY,
    nom VARCHAR(255) NOT NULL,
    adresse TEXT,
    telephone VARCHAR(20),
    email VARCHAR(255),
    siret VARCHAR(14),
    pharmacie_id INT NOT NULL,
    FOREIGN KEY (pharmacie_id) REFERENCES pharmacies(id)
);

-- Table des stocks (manquante)
CREATE TABLE IF NOT EXISTS stocks (
    id INT AUTO_INCREMENT PRIMARY KEY,
    produit_id INT NOT NULL,
    pharmacie_id INT NOT NULL,
    quantite INT NOT NULL DEFAULT 0,
    seuil_minimum INT NOT NULL DEFAULT 10,
    date_expiration DATE NULL,
    dernier_mouvement TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (produit_id) REFERENCES produits(id),
    FOREIGN KEY (pharmacie_id) REFERENCES pharmacies(id)
);

-- Table des commandes
CREATE TABLE IF NOT EXISTS commandes (
    id INT AUTO_INCREMENT PRIMARY KEY,
    date_commande TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    fournisseur_id INT NOT NULL,
    pharmacie_id INT NOT NULL,
    statut ENUM('en attente', 'validée', 'expédiée', 'livrée', 'annulée') DEFAULT 'en attente',
    montant_total DECIMAL(10, 2) NOT NULL DEFAULT 0,
    date_livraison DATE NULL,
    FOREIGN KEY (fournisseur_id) REFERENCES fournisseurs(id),
    FOREIGN KEY (pharmacie_id) REFERENCES pharmacies(id)
);

-- Table des détails de commandes
CREATE TABLE IF NOT EXISTS details_commandes (
    id INT AUTO_INCREMENT PRIMARY KEY,
    commande_id INT NOT NULL,
    produit_id INT NOT NULL,
    quantite INT NOT NULL,
    prix_unitaire DECIMAL(10, 2) NOT NULL,
    FOREIGN KEY (commande_id) REFERENCES commandes(id),
    FOREIGN KEY (produit_id) REFERENCES produits(id)
);

-- Table des activités (manquante)
CREATE TABLE IF NOT EXISTS activites (
    id INT AUTO_INCREMENT PRIMARY KEY,
    date TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    type VARCHAR(50) NOT NULL,
    description TEXT NOT NULL,
    utilisateur VARCHAR(100),
    pharmacie_id INT NOT NULL,
    FOREIGN KEY (pharmacie_id) REFERENCES pharmacies(id)
);

-- Insertion d'une pharmacie de test si aucune n'existe
INSERT INTO pharmacies (nom, adresse, telephone, email)
SELECT 'Pharmacie Centrale', '15 rue de la Paix, 75001 Paris', '0123456789', 'contact@pharmaciecentrale.fr'
WHERE NOT EXISTS (SELECT 1 FROM pharmacies LIMIT 1);

-- Insertion d'un administrateur de test si aucun n'existe (mot de passe : Admin123!)
INSERT INTO administrateurs (username, password, nom, prenom, email, pharmacie_id)
SELECT 'admin', '$2a$10$8KzaNdKwZ8P54UzQvh6dWeGd0GBKzwxnHG0ZMXWNOk3RJULt3zKi2', 'Dupont', 'Jean', 'admin@pharmaciecentrale.fr', 
(SELECT id FROM pharmacies WHERE nom = 'Pharmacie Centrale')
WHERE NOT EXISTS (SELECT 1 FROM administrateurs LIMIT 1);
