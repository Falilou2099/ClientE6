-- Suppression de la base de données si elle existe
DROP DATABASE IF EXISTS bigpharma;

-- Création de la nouvelle base de données
CREATE DATABASE bigpharma CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
USE bigpharma;

-- Table des pharmacies
CREATE TABLE pharmacies (
    id INT AUTO_INCREMENT PRIMARY KEY,
    nom VARCHAR(255) NOT NULL,
    adresse TEXT NOT NULL,
    telephone VARCHAR(20) NOT NULL,
    email VARCHAR(255) NOT NULL UNIQUE,
    siret VARCHAR(14),
    date_creation TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    statut ENUM('actif', 'inactif') DEFAULT 'actif'
);

-- Table des administrateurs (pour le client lourd Java)
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

-- Table des utilisateurs web (pour le client léger PHP)
CREATE TABLE utilisateurs_web (
    id INT AUTO_INCREMENT PRIMARY KEY,
    username VARCHAR(50) NOT NULL UNIQUE,
    password VARCHAR(255) NOT NULL,
    nom VARCHAR(100),
    prenom VARCHAR(100),
    email VARCHAR(255) UNIQUE,
    pharmacie_id INT NOT NULL,
    role ENUM('pharmacien', 'assistant') DEFAULT 'assistant',
    date_creation TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    dernier_login TIMESTAMP NULL,
    statut ENUM('actif', 'inactif') DEFAULT 'actif',
    FOREIGN KEY (pharmacie_id) REFERENCES pharmacies(id)
);

-- Table des fournisseurs
CREATE TABLE fournisseurs (
    id INT AUTO_INCREMENT PRIMARY KEY,
    nom VARCHAR(255) NOT NULL,
    adresse TEXT,
    telephone VARCHAR(20),
    email VARCHAR(255) UNIQUE,
    siret VARCHAR(14),
    pharmacie_id INT NOT NULL,
    date_creation TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (pharmacie_id) REFERENCES pharmacies(id)
);

-- Table des produits
CREATE TABLE produits (
    id INT AUTO_INCREMENT PRIMARY KEY,
    code_barre VARCHAR(13) UNIQUE,
    nom VARCHAR(255) NOT NULL,
    description TEXT,
    prix_achat DECIMAL(10, 2) NOT NULL,
    prix_vente DECIMAL(10, 2) NOT NULL,
    quantite_stock INT NOT NULL DEFAULT 0,
    stock_minimum INT DEFAULT 5,
    categorie VARCHAR(100),
    date_peremption DATE,
    pharmacie_id INT NOT NULL,
    fournisseur_id INT,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    FOREIGN KEY (pharmacie_id) REFERENCES pharmacies(id),
    FOREIGN KEY (fournisseur_id) REFERENCES fournisseurs(id)
);

-- Table des clients
CREATE TABLE clients (
    id INT AUTO_INCREMENT PRIMARY KEY,
    nom VARCHAR(100) NOT NULL,
    prenom VARCHAR(100) NOT NULL,
    date_naissance DATE,
    adresse TEXT,
    telephone VARCHAR(20),
    email VARCHAR(255),
    numero_secu VARCHAR(15),
    pharmacie_id INT NOT NULL,
    date_creation TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (pharmacie_id) REFERENCES pharmacies(id)
);

-- Table des ventes
CREATE TABLE ventes (
    id INT AUTO_INCREMENT PRIMARY KEY,
    pharmacie_id INT NOT NULL,
    client_id INT,
    utilisateur_id INT NOT NULL,
    date_vente TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    total_ht DECIMAL(10, 2) NOT NULL,
    total_ttc DECIMAL(10, 2) NOT NULL,
    tva DECIMAL(10, 2) NOT NULL,
    mode_paiement ENUM('especes', 'carte', 'cheque') DEFAULT 'especes',
    FOREIGN KEY (pharmacie_id) REFERENCES pharmacies(id),
    FOREIGN KEY (client_id) REFERENCES clients(id),
    FOREIGN KEY (utilisateur_id) REFERENCES utilisateurs_web(id)
);

-- Table des détails des ventes
CREATE TABLE details_ventes (
    id INT AUTO_INCREMENT PRIMARY KEY,
    vente_id INT NOT NULL,
    produit_id INT NOT NULL,
    quantite INT NOT NULL,
    prix_unitaire DECIMAL(10, 2) NOT NULL,
    total_ligne DECIMAL(10, 2) NOT NULL,
    FOREIGN KEY (vente_id) REFERENCES ventes(id),
    FOREIGN KEY (produit_id) REFERENCES produits(id)
);

-- Table des commandes fournisseurs
CREATE TABLE commandes (
    id INT AUTO_INCREMENT PRIMARY KEY,
    pharmacie_id INT NOT NULL,
    fournisseur_id INT NOT NULL,
    date_commande TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    date_livraison_prevue DATE,
    date_livraison_reelle DATE,
    statut ENUM('en_attente', 'validee', 'expediee', 'recue', 'annulee') DEFAULT 'en_attente',
    total_ht DECIMAL(10, 2) NOT NULL,
    total_ttc DECIMAL(10, 2) NOT NULL,
    FOREIGN KEY (pharmacie_id) REFERENCES pharmacies(id),
    FOREIGN KEY (fournisseur_id) REFERENCES fournisseurs(id)
);

-- Table des détails des commandes
CREATE TABLE details_commandes (
    id INT AUTO_INCREMENT PRIMARY KEY,
    commande_id INT NOT NULL,
    produit_id INT NOT NULL,
    quantite INT NOT NULL,
    prix_unitaire DECIMAL(10, 2) NOT NULL,
    total_ligne DECIMAL(10, 2) NOT NULL,
    FOREIGN KEY (commande_id) REFERENCES commandes(id),
    FOREIGN KEY (produit_id) REFERENCES produits(id)
);

-- Table des mouvements de stock
CREATE TABLE mouvements_stock (
    id INT AUTO_INCREMENT PRIMARY KEY,
    produit_id INT NOT NULL,
    type_mouvement ENUM('entree', 'sortie', 'ajustement') NOT NULL,
    quantite INT NOT NULL,
    date_mouvement TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    reference_source VARCHAR(50),
    commentaire TEXT,
    FOREIGN KEY (produit_id) REFERENCES produits(id)
);

-- Insertion des données de test
INSERT INTO pharmacies (nom, adresse, telephone, email, siret) VALUES 
('Pharmacie Centrale', '15 rue de la Paix, 75001 Paris', '0123456789', 'contact@pharmaciecentrale.fr', '12345678901234');

-- Création d'un compte administrateur pour le client lourd
INSERT INTO administrateurs (username, password, nom, prenom, email, pharmacie_id) VALUES 
('admin', 'Admin123!', 'Dupont', 'Jean', 'admin@pharmaciecentrale.fr', 
(SELECT id FROM pharmacies WHERE nom = 'Pharmacie Centrale'));

-- Création d'un compte utilisateur web pour le client léger
INSERT INTO utilisateurs_web (username, password, nom, prenom, email, pharmacie_id, role) VALUES 
('pharmacien', 'Pharmacien123!', 'Martin', 'Sophie', 'pharmacien@pharmaciecentrale.fr',
(SELECT id FROM pharmacies WHERE nom = 'Pharmacie Centrale'), 'pharmacien');

-- Ajout d'un assistant pour la pharmacie
INSERT INTO utilisateurs_web (username, password, nom, prenom, email, pharmacie_id, role) VALUES 
('assistant', 'Assistant123!', 'Dubois', 'Marie', 'assistant@pharmaciecentrale.fr',
(SELECT id FROM pharmacies WHERE nom = 'Pharmacie Centrale'), 'assistant');

-- Ajout de fournisseurs
INSERT INTO fournisseurs (nom, adresse, telephone, email, siret, pharmacie_id) VALUES
('Pharma Wholesale', '25 avenue des Sciences, 69100 Villeurbanne', '0456789012', 'contact@pharmawholesale.fr', '98765432109876', 
(SELECT id FROM pharmacies WHERE nom = 'Pharmacie Centrale')),
('MediSupply', '8 rue de l\'Industrie, 69200 Vénissieux', '0478901234', 'info@medisupply.fr', '65432109876543', 
(SELECT id FROM pharmacies WHERE nom = 'Pharmacie Centrale')),
('BioPharm', '42 boulevard des Médicaments, 69003 Lyon', '0490123456', 'service@biopharm.fr', '54321098765432', 
(SELECT id FROM pharmacies WHERE nom = 'Pharmacie Centrale'));

-- Ajout de produits avec stock initial
INSERT INTO produits (code_barre, nom, description, prix_achat, prix_vente, quantite_stock, stock_minimum, categorie, date_peremption, pharmacie_id, fournisseur_id) VALUES
-- Médicaments
('3400930000000', 'Doliprane 1000mg', 'Paracétamol 1000mg - Boîte de 8 comprimés', 1.80, 2.95, 150, 30, 'Antalgique', '2025-12-31', 
(SELECT id FROM pharmacies WHERE nom = 'Pharmacie Centrale'), (SELECT id FROM fournisseurs WHERE nom = 'Pharma Wholesale')),
('3400931111111', 'Advil 200mg', 'Ibuprofène 200mg - Boîte de 20 comprimés', 2.45, 4.20, 80, 20, 'Anti-inflammatoire', '2025-10-15', 
(SELECT id FROM pharmacies WHERE nom = 'Pharmacie Centrale'), (SELECT id FROM fournisseurs WHERE nom = 'Pharma Wholesale')),
('3400932222222', 'Smecta', 'Poudre pour suspension buvable - Boîte de 30 sachets', 4.10, 6.90, 45, 10, 'Anti-diarrhéique', '2025-08-20', 
(SELECT id FROM pharmacies WHERE nom = 'Pharmacie Centrale'), (SELECT id FROM fournisseurs WHERE nom = 'MediSupply')),
('3400933333333', 'Amoxicilline 500mg', 'Antibiotique - Boîte de 12 gélules', 3.75, 6.50, 30, 10, 'Antibiotique', '2024-11-15', 
(SELECT id FROM pharmacies WHERE nom = 'Pharmacie Centrale'), (SELECT id FROM fournisseurs WHERE nom = 'BioPharm')),

-- Compléments alimentaires
('3400934444444', 'Vitamine C 1000mg', 'Complément alimentaire - Tube de 20 comprimés effervescents', 3.20, 5.90, 60, 15, 'Complément alimentaire', '2026-05-10', 
(SELECT id FROM pharmacies WHERE nom = 'Pharmacie Centrale'), (SELECT id FROM fournisseurs WHERE nom = 'MediSupply')),
('3400935555555', 'Magnésium Marin', 'Complément alimentaire - Boîte de 60 gélules', 6.30, 11.50, 40, 10, 'Complément alimentaire', '2026-03-25', 
(SELECT id FROM pharmacies WHERE nom = 'Pharmacie Centrale'), (SELECT id FROM fournisseurs WHERE nom = 'BioPharm')),

-- Produits dermatologiques
('3400936666666', 'Crème hydratante', 'Soin visage - Tube de 50ml', 4.90, 9.80, 35, 8, 'Dermatologie', '2026-01-20', 
(SELECT id FROM pharmacies WHERE nom = 'Pharmacie Centrale'), (SELECT id FROM fournisseurs WHERE nom = 'MediSupply')),
('3400937777777', 'Écran solaire SPF50', 'Protection solaire - Flacon de 200ml', 8.50, 16.90, 25, 5, 'Dermatologie', '2025-04-30', 
(SELECT id FROM pharmacies WHERE nom = 'Pharmacie Centrale'), (SELECT id FROM fournisseurs WHERE nom = 'BioPharm')),

-- Produits en stock limité - pour tester les alertes de stock
('3400938888888', 'Spray nasal', 'Solution physiologique - Flacon de 100ml', 2.80, 5.50, 6, 10, 'ORL', '2025-09-22', 
(SELECT id FROM pharmacies WHERE nom = 'Pharmacie Centrale'), (SELECT id FROM fournisseurs WHERE nom = 'Pharma Wholesale')),
('3400939999999', 'Bande élastique', 'Bandage auto-adhésif - 6cm x 4m', 3.40, 6.80, 3, 8, 'Premiers soins', '2027-07-15', 
(SELECT id FROM pharmacies WHERE nom = 'Pharmacie Centrale'), (SELECT id FROM fournisseurs WHERE nom = 'MediSupply'));

-- Création de la table activites pour suivre les actions des utilisateurs
CREATE TABLE IF NOT EXISTS activites (
    id INT AUTO_INCREMENT PRIMARY KEY,
    type VARCHAR(50) NOT NULL,
    description TEXT NOT NULL,
    utilisateur VARCHAR(100) NOT NULL,
    pharmacie_id INT NOT NULL,
    date_creation TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (pharmacie_id) REFERENCES pharmacies(id)
);

-- Ajout d'activités récentes
INSERT INTO activites (type, description, utilisateur, pharmacie_id) VALUES
('Connexion', 'Connexion au système', 'admin', (SELECT id FROM pharmacies WHERE nom = 'Pharmacie Centrale')),
('Produit', 'Ajout du produit: Doliprane 1000mg', 'admin', (SELECT id FROM pharmacies WHERE nom = 'Pharmacie Centrale')),
('Produit', 'Ajout du produit: Advil 200mg', 'admin', (SELECT id FROM pharmacies WHERE nom = 'Pharmacie Centrale')),
('Stock', 'Entrée de stock: 150 unités de Doliprane 1000mg', 'admin', (SELECT id FROM pharmacies WHERE nom = 'Pharmacie Centrale')),
('Stock', 'Entrée de stock: 80 unités de Advil 200mg', 'admin', (SELECT id FROM pharmacies WHERE nom = 'Pharmacie Centrale')),
('Fournisseur', 'Ajout du fournisseur: Pharma Wholesale', 'admin', (SELECT id FROM pharmacies WHERE nom = 'Pharmacie Centrale')),
('Stock', 'Sortie de stock: 5 unités de Spray nasal (Raison: Vente)', 'admin', (SELECT id FROM pharmacies WHERE nom = 'Pharmacie Centrale'));

-- Création de la table stocks si elle n'existe pas déjà
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

-- Synchronisation des stocks avec les produits
INSERT INTO stocks (produit_id, pharmacie_id, quantite, seuil_minimum, date_expiration)
SELECT id, pharmacie_id, quantite_stock, stock_minimum, date_peremption 
FROM produits 
WHERE id NOT IN (SELECT produit_id FROM stocks);

-- Création de quelques clients
INSERT INTO clients (nom, prenom, date_naissance, adresse, telephone, email, numero_secu, pharmacie_id) VALUES
('Leroy', 'Thomas', '1985-05-12', '10 rue des Lilas, 69006 Lyon', '0612345678', 'thomas.leroy@email.com', '185051269123456', 
(SELECT id FROM pharmacies WHERE nom = 'Pharmacie Centrale')),
('Richard', 'Emilie', '1990-11-23', '25 rue Victor Hugo, 69002 Lyon', '0698765432', 'emilie.richard@email.com', '290112369123456', 
(SELECT id FROM pharmacies WHERE nom = 'Pharmacie Centrale')),
('Bertrand', 'Philippe', '1978-03-30', '5 avenue Jean Jaurès, 69007 Lyon', '0676543210', 'philippe.bertrand@email.com', '178033069123456', 
(SELECT id FROM pharmacies WHERE nom = 'Pharmacie Centrale')),
('Petit', 'Isabelle', '1982-09-15', '42 rue de la République, 69001 Lyon', '0654321098', 'isabelle.petit@email.com', '282091569123456', 
(SELECT id FROM pharmacies WHERE nom = 'Pharmacie Centrale'));

-- Création de ventes
INSERT INTO ventes (pharmacie_id, client_id, utilisateur_id, date_vente, total_ht, total_ttc, tva, mode_paiement) VALUES
((SELECT id FROM pharmacies WHERE nom = 'Pharmacie Centrale'), 
(SELECT id FROM clients WHERE nom = 'Leroy' AND prenom = 'Thomas'), 
(SELECT id FROM utilisateurs_web WHERE username = 'pharmacien'), 
'2024-04-05 10:15:00', 5.90, 6.20, 0.30, 'carte'),

((SELECT id FROM pharmacies WHERE nom = 'Pharmacie Centrale'), 
(SELECT id FROM clients WHERE nom = 'Richard' AND prenom = 'Emilie'), 
(SELECT id FROM utilisateurs_web WHERE username = 'pharmacien'), 
'2024-04-06 14:30:00', 11.10, 11.70, 0.60, 'especes'),

((SELECT id FROM pharmacies WHERE nom = 'Pharmacie Centrale'), 
(SELECT id FROM clients WHERE nom = 'Bertrand' AND prenom = 'Philippe'), 
(SELECT id FROM utilisateurs_web WHERE username = 'assistant'), 
'2024-04-07 09:45:00', 16.90, 17.80, 0.90, 'carte'),

((SELECT id FROM pharmacies WHERE nom = 'Pharmacie Centrale'), 
(SELECT id FROM clients WHERE nom = 'Petit' AND prenom = 'Isabelle'), 
(SELECT id FROM utilisateurs_web WHERE username = 'pharmacien'), 
'2024-04-07 16:20:00', 9.40, 9.90, 0.50, 'cheque');

-- Ajout de détails de ventes
INSERT INTO details_ventes (vente_id, produit_id, quantite, prix_unitaire, total_ligne) VALUES
(1, (SELECT id FROM produits WHERE nom = 'Doliprane 1000mg'), 2, 2.95, 5.90),

(2, (SELECT id FROM produits WHERE nom = 'Advil 200mg'), 1, 4.20, 4.20),
(2, (SELECT id FROM produits WHERE nom = 'Spray nasal'), 1, 5.50, 5.50),
(2, (SELECT id FROM produits WHERE nom = 'Magnésium Marin'), 1, 11.50, 11.50),

(3, (SELECT id FROM produits WHERE nom = 'Écran solaire SPF50'), 1, 16.90, 16.90),

(4, (SELECT id FROM produits WHERE nom = 'Crème hydratante'), 1, 9.80, 9.80);

-- Création d'une commande
INSERT INTO commandes (pharmacie_id, fournisseur_id, date_commande, date_livraison_prevue, statut, total_ht, total_ttc) VALUES
((SELECT id FROM pharmacies WHERE nom = 'Pharmacie Centrale'), 
(SELECT id FROM fournisseurs WHERE nom = 'Pharma Wholesale'), 
'2024-04-03 11:00:00', '2024-04-10', 'validee', 340.00, 357.00);

-- Détails de la commande
INSERT INTO details_commandes (commande_id, produit_id, quantite, prix_unitaire, total_ligne) VALUES
(1, (SELECT id FROM produits WHERE nom = 'Doliprane 1000mg'), 100, 1.80, 180.00),
(1, (SELECT id FROM produits WHERE nom = 'Advil 200mg'), 50, 2.45, 122.50),
(1, (SELECT id FROM produits WHERE nom = 'Spray nasal'), 15, 2.50, 37.50);

-- Création de mouvements de stock pour les produits
INSERT INTO mouvements_stock (produit_id, type_mouvement, quantite, reference_source, commentaire) VALUES
((SELECT id FROM produits WHERE nom = 'Doliprane 1000mg'), 'entree', 150, 'INIT-001', 'Stock initial'),
((SELECT id FROM produits WHERE nom = 'Advil 200mg'), 'entree', 80, 'INIT-002', 'Stock initial'),
((SELECT id FROM produits WHERE nom = 'Spray nasal'), 'entree', 15, 'INIT-003', 'Stock initial'),
((SELECT id FROM produits WHERE nom = 'Spray nasal'), 'sortie', 5, 'VENTE-001', 'Vente en pharmacie'),
((SELECT id FROM produits WHERE nom = 'Doliprane 1000mg'), 'sortie', 2, 'VENTE-002', 'Vente au client Leroy Thomas');
