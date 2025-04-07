-- Script complet pour reconstruire entièrement la base de données
-- Ce script supprime et recrée toutes les tables nécessaires

-- Créer la base de données si elle n'existe pas
CREATE DATABASE IF NOT EXISTS bigpharma;
USE bigpharma;

-- Supprimer les tables existantes si elles existent (dans l'ordre inverse des dépendances)
DROP TABLE IF EXISTS activites;
DROP TABLE IF EXISTS details_commandes;
DROP TABLE IF EXISTS commandes;
DROP TABLE IF EXISTS stocks;
DROP TABLE IF EXISTS produits;
DROP TABLE IF EXISTS fournisseurs;
DROP TABLE IF EXISTS administrateurs;
DROP TABLE IF EXISTS utilisateurs_web;
DROP TABLE IF EXISTS pharmacies;

-- Table des pharmacies
CREATE TABLE pharmacies (
    id INT AUTO_INCREMENT PRIMARY KEY,
    nom VARCHAR(255) NOT NULL,
    adresse TEXT NOT NULL,
    telephone VARCHAR(20) NOT NULL,
    email VARCHAR(255) NOT NULL UNIQUE,
    date_creation TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    statut ENUM('actif', 'inactif') DEFAULT 'actif'
);

-- Table des administrateurs (client lourd)
CREATE TABLE administrateurs (
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

-- Table des utilisateurs web (client léger)
CREATE TABLE utilisateurs_web (
    id INT AUTO_INCREMENT PRIMARY KEY,
    username VARCHAR(50) NOT NULL UNIQUE,
    password VARCHAR(255) NOT NULL,
    nom VARCHAR(100),
    prenom VARCHAR(100),
    email VARCHAR(255) UNIQUE,
    pharmacie_id INT NOT NULL,
    role ENUM('admin', 'pharmacien', 'vendeur') DEFAULT 'pharmacien',
    date_creation TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    dernier_login TIMESTAMP NULL,
    statut ENUM('actif', 'inactif') DEFAULT 'actif',
    FOREIGN KEY (pharmacie_id) REFERENCES pharmacies(id)
);

-- Table des produits
CREATE TABLE produits (
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
    date_ajout TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (pharmacie_id) REFERENCES pharmacies(id)
);

-- Table des fournisseurs
CREATE TABLE fournisseurs (
    id INT AUTO_INCREMENT PRIMARY KEY,
    nom VARCHAR(255) NOT NULL,
    adresse TEXT,
    telephone VARCHAR(20),
    email VARCHAR(255),
    siret VARCHAR(14),
    pharmacie_id INT NOT NULL,
    FOREIGN KEY (pharmacie_id) REFERENCES pharmacies(id)
);

-- Table des stocks
CREATE TABLE stocks (
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
CREATE TABLE commandes (
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
CREATE TABLE details_commandes (
    id INT AUTO_INCREMENT PRIMARY KEY,
    commande_id INT NOT NULL,
    produit_id INT NOT NULL,
    quantite INT NOT NULL,
    prix_unitaire DECIMAL(10, 2) NOT NULL,
    FOREIGN KEY (commande_id) REFERENCES commandes(id),
    FOREIGN KEY (produit_id) REFERENCES produits(id)
);

-- Table des activités
CREATE TABLE activites (
    id INT AUTO_INCREMENT PRIMARY KEY,
    date TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    type VARCHAR(50) NOT NULL,
    description TEXT NOT NULL,
    utilisateur VARCHAR(100),
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

-- Insertion d'un utilisateur web de test (mot de passe : Pharmacien123!)
INSERT INTO utilisateurs_web (username, password, nom, prenom, email, pharmacie_id, role) VALUES 
('pharmacien', '$2a$10$QZs3Svmy.ILVc0DHM0W8q.jm6OEXF9Y.nthdtZPnL9FW2H5X7fkK2', 'Martin', 'Sophie', 'pharmacien@pharmaciecentrale.fr',
(SELECT id FROM pharmacies WHERE nom = 'Pharmacie Centrale'), 'pharmacien');

-- Insertion de fournisseurs de test
INSERT INTO fournisseurs (nom, adresse, telephone, email, siret, pharmacie_id) VALUES
('MediSupply', '25 avenue des Sciences, 69100 Villeurbanne', '0456789012', 'contact@medisupply.fr', '12345678901234', 
(SELECT id FROM pharmacies WHERE nom = 'Pharmacie Centrale')),
('PharmaPro', '8 rue de la Recherche, 75015 Paris', '0123456789', 'info@pharmapro.fr', '98765432109876', 
(SELECT id FROM pharmacies WHERE nom = 'Pharmacie Centrale'));

-- Insertion de produits de test
INSERT INTO produits (nom, description, prix_vente, prix_achat, quantite_stock, seuil_alerte, categorie, date_expiration, pharmacie_id) VALUES
('Doliprane 1000mg', 'Comprimés contre la douleur et la fièvre', 5.90, 3.20, 100, 20, 'Analgésiques', '2026-12-31', 
(SELECT id FROM pharmacies WHERE nom = 'Pharmacie Centrale')),
('Advil 200mg', 'Anti-inflammatoire non stéroïdien', 4.50, 2.50, 80, 15, 'Anti-inflammatoires', '2026-06-30', 
(SELECT id FROM pharmacies WHERE nom = 'Pharmacie Centrale')),
('Smecta', 'Traitement de la diarrhée', 6.20, 3.80, 50, 10, 'Gastro-entérologie', '2025-10-15', 
(SELECT id FROM pharmacies WHERE nom = 'Pharmacie Centrale'));

-- Synchronisation des stocks avec les produits
INSERT INTO stocks (produit_id, pharmacie_id, quantite, seuil_minimum, date_expiration)
SELECT id, pharmacie_id, quantite_stock, seuil_alerte, date_expiration FROM produits;

-- Insertion de commandes de test
INSERT INTO commandes (fournisseur_id, pharmacie_id, statut, montant_total, date_livraison) VALUES
((SELECT id FROM fournisseurs WHERE nom = 'MediSupply'), 
(SELECT id FROM pharmacies WHERE nom = 'Pharmacie Centrale'), 
'en attente', 450.00, DATE_ADD(CURRENT_DATE, INTERVAL 7 DAY));

-- Ajout de détails de commande
INSERT INTO details_commandes (commande_id, produit_id, quantite, prix_unitaire) VALUES
(1, 1, 50, 3.20),
(1, 2, 30, 2.50),
(1, 3, 40, 3.80);

-- Insertion d'activités de test
INSERT INTO activites (type, description, utilisateur, pharmacie_id) VALUES
('Connexion', 'Connexion au système', 'Jean Dupont', (SELECT id FROM pharmacies WHERE nom = 'Pharmacie Centrale')),
('Inventaire', 'Mise à jour des stocks', 'Jean Dupont', (SELECT id FROM pharmacies WHERE nom = 'Pharmacie Centrale')),
('Commande', 'Nouvelle commande créée', 'Jean Dupont', (SELECT id FROM pharmacies WHERE nom = 'Pharmacie Centrale'));

-- Confirmation de la fin du script
SELECT 'Base de données reconstruite avec succès!' AS Message;
