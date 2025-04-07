-- Script de création de la base de données BigPharma
CREATE DATABASE IF NOT EXISTS clientlegerlourd;
USE clientlegerlourd;

-- Table des pharmacies
CREATE TABLE IF NOT EXISTS pharmacies (
    id INT PRIMARY KEY AUTO_INCREMENT,
    nom VARCHAR(100) NOT NULL,
    adresse TEXT NOT NULL,
    telephone VARCHAR(20) NOT NULL,
    email VARCHAR(100) NOT NULL UNIQUE,
    date_creation DATETIME DEFAULT CURRENT_TIMESTAMP
);

-- Table des administrateurs (client lourd)
CREATE TABLE IF NOT EXISTS administrateurs (
    id INT PRIMARY KEY AUTO_INCREMENT,
    pharmacie_id INT NOT NULL,
    nom_utilisateur VARCHAR(50) NOT NULL UNIQUE,
    mot_de_passe VARCHAR(255) NOT NULL,
    nom VARCHAR(100) NOT NULL,
    prenom VARCHAR(100) NOT NULL,
    email VARCHAR(100) NOT NULL UNIQUE,
    date_creation DATETIME DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (pharmacie_id) REFERENCES pharmacies(id)
);

-- Table des clients
CREATE TABLE IF NOT EXISTS clients (
    id INT PRIMARY KEY AUTO_INCREMENT,
    pharmacie_id INT NOT NULL,
    nom VARCHAR(100) NOT NULL,
    prenom VARCHAR(100) NOT NULL,
    email VARCHAR(100),
    telephone VARCHAR(20),
    date_creation DATETIME DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (pharmacie_id) REFERENCES pharmacies(id)
);

-- Table des fournisseurs
CREATE TABLE IF NOT EXISTS fournisseurs (
    id INT PRIMARY KEY AUTO_INCREMENT,
    nom VARCHAR(100) NOT NULL,
    adresse TEXT NOT NULL,
    telephone VARCHAR(20) NOT NULL,
    email VARCHAR(100) NOT NULL UNIQUE,
    date_creation DATETIME DEFAULT CURRENT_TIMESTAMP
);

-- Table des produits
CREATE TABLE IF NOT EXISTS produits (
    id INT PRIMARY KEY AUTO_INCREMENT,
    nom VARCHAR(100) NOT NULL,
    description TEXT,
    prix_unitaire DECIMAL(10,2) NOT NULL,
    image_url VARCHAR(255),
    fournisseur_id INT,
    date_creation DATETIME DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (fournisseur_id) REFERENCES fournisseurs(id)
);

-- Table des stocks
CREATE TABLE IF NOT EXISTS stocks (
    id INT PRIMARY KEY AUTO_INCREMENT,
    pharmacie_id INT NOT NULL,
    produit_id INT NOT NULL,
    quantite INT NOT NULL DEFAULT 0,
    seuil_alerte INT NOT NULL DEFAULT 10,
    FOREIGN KEY (pharmacie_id) REFERENCES pharmacies(id),
    FOREIGN KEY (produit_id) REFERENCES produits(id),
    UNIQUE KEY unique_stock (pharmacie_id, produit_id)
);

-- Table des ventes
CREATE TABLE IF NOT EXISTS ventes (
    id INT PRIMARY KEY AUTO_INCREMENT,
    pharmacie_id INT NOT NULL,
    client_id INT NOT NULL,
    date_vente DATETIME DEFAULT CURRENT_TIMESTAMP,
    total DECIMAL(10,2) NOT NULL,
    FOREIGN KEY (pharmacie_id) REFERENCES pharmacies(id),
    FOREIGN KEY (client_id) REFERENCES clients(id)
);

-- Table des détails des ventes
CREATE TABLE IF NOT EXISTS details_ventes (
    id INT PRIMARY KEY AUTO_INCREMENT,
    vente_id INT NOT NULL,
    produit_id INT NOT NULL,
    quantite INT NOT NULL,
    prix_unitaire DECIMAL(10,2) NOT NULL,
    FOREIGN KEY (vente_id) REFERENCES ventes(id),
    FOREIGN KEY (produit_id) REFERENCES produits(id)
);

-- Table des commandes fournisseurs
CREATE TABLE IF NOT EXISTS commandes (
    id INT PRIMARY KEY AUTO_INCREMENT,
    pharmacie_id INT NOT NULL,
    fournisseur_id INT NOT NULL,
    date_commande DATETIME DEFAULT CURRENT_TIMESTAMP,
    statut ENUM('en_attente', 'validee', 'livree', 'annulee') NOT NULL DEFAULT 'en_attente',
    FOREIGN KEY (pharmacie_id) REFERENCES pharmacies(id),
    FOREIGN KEY (fournisseur_id) REFERENCES fournisseurs(id)
);

-- Table des détails des commandes
CREATE TABLE IF NOT EXISTS details_commandes (
    id INT PRIMARY KEY AUTO_INCREMENT,
    commande_id INT NOT NULL,
    produit_id INT NOT NULL,
    quantite INT NOT NULL,
    prix_unitaire DECIMAL(10,2) NOT NULL,
    FOREIGN KEY (commande_id) REFERENCES commandes(id),
    FOREIGN KEY (produit_id) REFERENCES produits(id)
);
