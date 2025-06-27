-- Script d'harmonisation de la base de données pour le client léger
-- Ce script doit être identique à celui du client lourd pour assurer la cohérence

-- Désactiver les contraintes de clés étrangères
SET FOREIGN_KEY_CHECKS = 0;

-- Supprimer les tables existantes
DROP TABLE IF EXISTS lignes_commande;
DROP TABLE IF EXISTS commandes;
DROP TABLE IF EXISTS administrateurs;
DROP TABLE IF EXISTS pharmacies;
DROP TABLE IF EXISTS fournisseurs;
DROP TABLE IF EXISTS produits;
DROP TABLE IF EXISTS clients;

-- Création de la table produits harmonisée
CREATE TABLE produits (
    id INT AUTO_INCREMENT PRIMARY KEY,
    nom VARCHAR(255) NOT NULL,
    description TEXT,
    prix DECIMAL(10, 2) NOT NULL,
    quantite_stock INT NOT NULL DEFAULT 0,
    categorie VARCHAR(100),
    date_peremption DATE,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    CONSTRAINT chk_prix_positif CHECK (prix >= 0),
    CONSTRAINT chk_stock_positif CHECK (quantite_stock >= 0)
);

-- Création de la table clients
CREATE TABLE clients (
    id INT AUTO_INCREMENT PRIMARY KEY,
    nom VARCHAR(100) NOT NULL,
    prenom VARCHAR(100),
    adresse TEXT,
    telephone VARCHAR(20),
    email VARCHAR(100) UNIQUE,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
);

-- Création de la table fournisseurs
CREATE TABLE fournisseurs (
    id INT AUTO_INCREMENT PRIMARY KEY,
    nom VARCHAR(100) NOT NULL,
    contact VARCHAR(100),
    telephone VARCHAR(20),
    email VARCHAR(100) UNIQUE,
    types_medicaments TEXT,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
);

-- Création de la table pharmacies
CREATE TABLE pharmacies (
    id INT AUTO_INCREMENT PRIMARY KEY,
    nom VARCHAR(100) NOT NULL,
    adresse TEXT NOT NULL,
    telephone VARCHAR(20),
    email VARCHAR(100) NOT NULL UNIQUE,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
);

-- Création de la table administrateurs
CREATE TABLE administrateurs (
    id INT AUTO_INCREMENT PRIMARY KEY,
    pharmacie_id INT NOT NULL,
    identifiant VARCHAR(50) NOT NULL UNIQUE,
    mot_de_passe VARCHAR(255) NOT NULL,
    email VARCHAR(100) NOT NULL UNIQUE,
    est_super_admin BOOLEAN DEFAULT FALSE,
    premiere_connexion BOOLEAN DEFAULT TRUE,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    FOREIGN KEY (pharmacie_id) REFERENCES pharmacies(id)
);

-- Création de la table commandes
CREATE TABLE commandes (
    id INT AUTO_INCREMENT PRIMARY KEY,
    client_id INT,
    fournisseur_id INT NOT NULL,
    pharmacie_id INT,
    date_commande TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    statut VARCHAR(50) NOT NULL DEFAULT 'En attente',
    total_prix DECIMAL(10, 2) NOT NULL DEFAULT 0,
    duree_livraison INT,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    FOREIGN KEY (client_id) REFERENCES clients(id),
    FOREIGN KEY (fournisseur_id) REFERENCES fournisseurs(id),
    FOREIGN KEY (pharmacie_id) REFERENCES pharmacies(id)
);

-- Création de la table lignes_commande
CREATE TABLE lignes_commande (
    id INT AUTO_INCREMENT PRIMARY KEY,
    commande_id INT NOT NULL,
    produit_id INT NOT NULL,
    quantite INT NOT NULL,
    prix_unitaire DECIMAL(10, 2) NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    FOREIGN KEY (commande_id) REFERENCES commandes(id),
    FOREIGN KEY (produit_id) REFERENCES produits(id)
);

-- Création des index pour améliorer les performances
CREATE INDEX idx_categorie ON produits(categorie);
CREATE INDEX idx_pharmacie_admin ON administrateurs(pharmacie_id);
CREATE INDEX idx_commande_relations ON commandes(client_id, fournisseur_id, pharmacie_id);
CREATE INDEX idx_ligne_commande_relations ON lignes_commande(commande_id, produit_id);

-- Insérer les données de test pour les produits
INSERT INTO produits (nom, description, prix, quantite_stock, categorie, date_peremption) VALUES
('Doliprane 500mg', 'Médicament antalgique et antipyrétique à base de paracétamol', 4.50, 150, 'Antalgique', '2025-12-31'),
('Advil 200mg', 'Anti-inflammatoire et antidouleur à base d\'ibuprofène', 5.20, 80, 'Anti-inflammatoire', '2024-11-30'),
('Spasfon Lyoc 80mg', 'Médicament antispasmodique pour soulager les douleurs abdominales', 6.30, 50, 'Antispasmodique', '2025-06-15'),
('Efferalgan 1000mg', 'Comprimés effervescents de paracétamol', 7.80, 100, 'Antalgique', '2025-09-20'),
('Xanax 0.5mg', 'Médicament anxiolytique pour traiter l\'anxiété', 12.50, 30, 'Anxiolytique', '2024-08-10'),
('Ventoline 100µg', 'Spray pour le traitement de l\'asthme', 9.20, 60, 'Bronchodilatateur', '2025-03-25'),
('Kardegic 75mg', 'Traitement préventif cardiovasculaire', 8.70, 40, 'Anticoagulant', '2024-12-15'),
('Zithromax 250mg', 'Antibiotique à large spectre', 15.60, 25, 'Antibiotique', '2024-10-05'),
('Maalox', 'Suspension buvable pour troubles digestifs', 5.90, 70, 'Antiacide', '2025-01-20'),
('Previscan 20mg', 'Anticoagulant oral préventif', 18.40, 35, 'Anticoagulant', '2024-07-30');

-- Procédure stockée pour créer une pharmacie avec son administrateur
DELIMITER //
CREATE PROCEDURE creer_pharmacie_avec_admin(
    IN p_nom_pharmacie VARCHAR(100),
    IN p_adresse TEXT,
    IN p_telephone VARCHAR(20),
    IN p_email VARCHAR(100),
    IN p_identifiant_admin VARCHAR(50),
    IN p_mot_de_passe_admin VARCHAR(255)
)
BEGIN
    DECLARE v_pharmacie_id INT;
    
    -- Insertion de la pharmacie
    INSERT INTO pharmacies (nom, adresse, telephone, email)
    VALUES (p_nom_pharmacie, p_adresse, p_telephone, p_email);
    
    SET v_pharmacie_id = LAST_INSERT_ID();
    
    -- Création de l'administrateur associé
    INSERT INTO administrateurs (pharmacie_id, identifiant, mot_de_passe, email)
    VALUES (v_pharmacie_id, p_identifiant_admin, p_mot_de_passe_admin, p_email);
END //
DELIMITER ;

-- Réactiver les contraintes de clés étrangères
SET FOREIGN_KEY_CHECKS = 1;
