-- ========================================
-- CORRECTION FINALE DE LA SYNCHRONISATION
-- BigPharma Java ↔ PHP
-- ========================================

-- 1. Utiliser la base bigpharma (Java)
USE bigpharma;

-- 2. Corriger la structure des tables pour la synchronisation
-- Table fournisseurs - Ajouter pharmacie_id si manquant
ALTER TABLE fournisseurs 
ADD COLUMN IF NOT EXISTS pharmacie_id INT DEFAULT 1;

-- Table produits - Vérifier les colonnes nécessaires
ALTER TABLE produits 
ADD COLUMN IF NOT EXISTS pharmacie_id INT DEFAULT 1,
ADD COLUMN IF NOT EXISTS categorie VARCHAR(100),
ADD COLUMN IF NOT EXISTS quantite_stock INT DEFAULT 0,
ADD COLUMN IF NOT EXISTS prix_achat DECIMAL(10,2) DEFAULT 0.00,
ADD COLUMN IF NOT EXISTS image_url VARCHAR(500);

-- Table categories - Créer si n'existe pas
CREATE TABLE IF NOT EXISTS categories (
    id INT PRIMARY KEY AUTO_INCREMENT,
    nom VARCHAR(100) NOT NULL,
    description TEXT,
    pharmacie_id INT DEFAULT 1,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- 3. Insérer les données de base pour la synchronisation
-- Pharmacie par défaut
INSERT IGNORE INTO pharmacies (id, nom, adresse, telephone, email) 
VALUES (1, 'Pharmacie BigPharma', '123 Rue de la Santé, 75000 Paris', '01.23.45.67.89', 'contact@bigpharma.fr');

-- Utilisateur cible avec mot de passe correct
DELETE FROM utilisateurs WHERE email = 'tourefaliloumbacke12345@gmail.com';
INSERT INTO utilisateurs (nom, prenom, email, mot_de_passe, role, pharmacie_id, actif) 
VALUES ('Mbacke', 'Toure Falilou', 'tourefaliloumbacke12345@gmail.com', 
        SHA2('password', 256), 'admin', 1, 1);

-- 4. Insérer les catégories de base
INSERT IGNORE INTO categories (id, nom, description, pharmacie_id) VALUES
(1, 'Analgésiques', 'Médicaments contre la douleur', 1),
(2, 'Anti-inflammatoires', 'Médicaments anti-inflammatoires', 1),
(3, 'Antibiotiques', 'Médicaments antibactériens', 1),
(4, 'Antihistaminiques', 'Médicaments contre les allergies', 1),
(5, 'Vitamines', 'Vitamines et compléments alimentaires', 1),
(6, 'Antispasmodiques', 'Médicaments contre les spasmes', 1),
(7, 'Antiseptiques', 'Produits désinfectants', 1),
(8, 'Cardiovasculaires', 'Médicaments pour le cœur', 1),
(9, 'Dermatologiques', 'Produits pour la peau', 1),
(10, 'Digestifs', 'Médicaments pour la digestion', 1),
(11, 'Neurologiques', 'Médicaments pour le système nerveux', 1),
(12, 'Ophtalmologiques', 'Produits pour les yeux', 1),
(13, 'ORL', 'Produits pour nez, gorge, oreilles', 1),
(14, 'Respiratoires', 'Médicaments pour la respiration', 1),
(15, 'Urologiques', 'Médicaments pour les voies urinaires', 1),
(16, 'Gynécologiques', 'Produits pour la santé féminine', 1),
(17, 'Pédiatriques', 'Médicaments pour enfants', 1);

-- 5. Insérer les fournisseurs de base
INSERT IGNORE INTO fournisseurs (id, nom, adresse, telephone, email, siret, pharmacie_id) VALUES
(1, 'Laboratoires Sanofi', '54 Rue La Boétie, 75008 Paris', '01.53.77.40.00', 'contact@sanofi.com', '12345678901234', 1),
(2, 'Pfizer France', '23-25 Avenue du Docteur Lannelongue, 75014 Paris', '01.58.07.34.40', 'contact@pfizer.fr', '23456789012345', 1),
(3, 'Laboratoires Novartis', '8-10 Rue Henri Sainte-Claire Deville, 92500 Rueil-Malmaison', '01.55.47.60.00', 'contact@novartis.fr', '34567890123456', 1),
(4, 'Roche France', '30 Cours de l\'Île Seguin, 92100 Boulogne-Billancourt', '01.46.40.50.00', 'contact@roche.fr', '45678901234567', 1),
(5, 'Merck France', '37 Rue Saint-Romain, 69008 Lyon', '04.72.78.09.00', 'contact@merck.fr', '56789012345678', 1);

-- 6. Insérer les produits de base avec toutes les colonnes nécessaires
INSERT IGNORE INTO produits (id, nom, description, prix_achat, prix_vente, categorie, quantite_stock, seuil_alerte, date_expiration, pharmacie_id, image_url, fournisseur_id, sur_ordonnance) VALUES
(1, 'Doliprane 1000mg', 'Paracétamol 1000mg - Boîte de 8 comprimés', 2.50, 3.95, 'Analgésiques', 150, 20, '2025-12-31', 1, 'images/doliprane.jpg', 1, 0),
(2, 'Advil 400mg', 'Ibuprofène 400mg - Boîte de 14 comprimés', 3.20, 4.85, 'Anti-inflammatoires', 120, 15, '2025-11-30', 1, 'images/advil.jpg', 2, 0),
(3, 'Amoxicilline 500mg', 'Antibiotique - Boîte de 16 gélules', 4.80, 7.25, 'Antibiotiques', 80, 10, '2025-10-15', 1, 'images/amoxicilline.jpg', 1, 1),
(4, 'Cetirizine 10mg', 'Antihistaminique - Boîte de 15 comprimés', 2.10, 3.45, 'Antihistaminiques', 90, 12, '2026-01-20', 1, 'images/cetirizine.jpg', 3, 0),
(5, 'Supradyn', 'Multivitamines - Boîte de 30 comprimés', 6.20, 9.50, 'Vitamines', 65, 10, '2026-01-10', 1, 'images/supradyn.jpg', 4, 0),
(6, 'Spasfon', 'Antispasmodique - Boîte de 30 comprimés', 3.80, 5.75, 'Antispasmodiques', 75, 15, '2025-09-20', 1, 'images/spasfon.jpg', 1, 0),
(7, 'Bétadine', 'Antiseptique - Flacon 125ml', 4.20, 6.30, 'Antiseptiques', 45, 8, '2026-03-15', 1, 'images/betadine.jpg', 2, 0),
(8, 'Kardégic 75mg', 'Antiagrégant plaquettaire - Boîte de 30 sachets', 2.90, 4.40, 'Cardiovasculaires', 55, 12, '2025-08-10', 1, 'images/kardegic.jpg', 1, 1),
(9, 'Biafine', 'Émulsion pour brûlures - Tube 93g', 5.60, 8.45, 'Dermatologiques', 35, 8, '2026-02-28', 1, 'images/biafine.jpg', 3, 0),
(10, 'Smecta', 'Antidiarrhéique - Boîte de 30 sachets', 3.40, 5.15, 'Digestifs', 85, 15, '2025-07-25', 1, 'images/smecta.jpg', 2, 0),
(11, 'Lexomil 6mg', 'Anxiolytique - Boîte de 30 comprimés', 7.80, 11.70, 'Neurologiques', 25, 5, '2025-06-30', 1, 'images/lexomil.jpg', 4, 1),
(12, 'Collyre Bleu', 'Collyre antiseptique - Flacon 10ml', 2.80, 4.20, 'Ophtalmologiques', 40, 10, '2025-12-15', 1, 'images/collyre.jpg', 1, 0),
(13, 'Rhinofluimucil', 'Spray nasal décongestionnant - 10ml', 4.50, 6.75, 'ORL', 60, 12, '2025-11-05', 1, 'images/rhinofluimucil.jpg', 2, 0),
(14, 'Ventoline', 'Bronchodilatateur - Aérosol 100 doses', 8.90, 13.35, 'Respiratoires', 30, 8, '2025-10-20', 1, 'images/ventoline.jpg', 3, 1),
(15, 'Monuril 3g', 'Antibiotique urinaire - Sachet', 6.70, 10.05, 'Urologiques', 20, 5, '2025-09-12', 1, 'images/monuril.jpg', 1, 1),
(16, 'Gynéfam', 'Complément alimentaire féminin - 30 gélules', 12.50, 18.75, 'Gynécologiques', 15, 5, '2026-04-18', 1, 'images/gynefam.jpg', 4, 0),
(17, 'Doliprane Pédiatrique', 'Paracétamol enfant - Flacon 90ml', 3.20, 4.80, 'Pédiatriques', 70, 15, '2025-08-30', 1, 'images/doliprane_pediatrique.jpg', 1, 0),
(18, 'Aspégic 1000mg', 'Acide acétylsalicylique - Boîte de 20 sachets', 2.60, 3.90, 'Analgésiques', 95, 20, '2025-07-15', 1, 'images/aspegic.jpg', 2, 0),
(19, 'Voltarène Gel', 'Anti-inflammatoire topique - Tube 50g', 5.40, 8.10, 'Anti-inflammatoires', 50, 10, '2025-12-08', 1, 'images/voltarene.jpg', 3, 0),
(20, 'Augmentin 500mg', 'Antibiotique - Boîte de 16 comprimés', 8.20, 12.30, 'Antibiotiques', 40, 8, '2025-06-22', 1, 'images/augmentin.jpg', 1, 1);

-- 7. Mettre à jour tous les fournisseurs pour avoir pharmacie_id = 1
UPDATE fournisseurs SET pharmacie_id = 1 WHERE pharmacie_id IS NULL OR pharmacie_id = 0;

-- 8. Mettre à jour tous les produits pour avoir pharmacie_id = 1
UPDATE produits SET pharmacie_id = 1 WHERE pharmacie_id IS NULL OR pharmacie_id = 0;

-- 9. Synchroniser avec la base clientlegerlourd (PHP)
-- Créer la base si elle n'existe pas
CREATE DATABASE IF NOT EXISTS clientlegerlourd CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;

-- Utiliser la base clientlegerlourd
USE clientlegerlourd;

-- Créer les tables dans clientlegerlourd
CREATE TABLE IF NOT EXISTS pharmacies (
    id INT PRIMARY KEY AUTO_INCREMENT,
    nom VARCHAR(255) NOT NULL,
    adresse TEXT,
    telephone VARCHAR(20),
    email VARCHAR(255),
    siret VARCHAR(14),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
);

CREATE TABLE IF NOT EXISTS utilisateurs (
    id INT PRIMARY KEY AUTO_INCREMENT,
    nom VARCHAR(255) NOT NULL,
    prenom VARCHAR(255),
    email VARCHAR(255) UNIQUE NOT NULL,
    mot_de_passe VARCHAR(255) NOT NULL,
    role ENUM('admin','pharmacien','vendeur') DEFAULT 'vendeur',
    pharmacie_id INT DEFAULT 1,
    actif TINYINT(1) DEFAULT 1,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
);

CREATE TABLE IF NOT EXISTS categories (
    id INT PRIMARY KEY AUTO_INCREMENT,
    nom VARCHAR(255) NOT NULL,
    description TEXT,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE IF NOT EXISTS fournisseurs (
    id INT PRIMARY KEY AUTO_INCREMENT,
    nom VARCHAR(255) NOT NULL,
    adresse TEXT,
    telephone VARCHAR(20),
    email VARCHAR(255),
    siret VARCHAR(14),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
);

CREATE TABLE IF NOT EXISTS produits (
    id INT PRIMARY KEY AUTO_INCREMENT,
    nom VARCHAR(255) NOT NULL,
    description TEXT,
    prix_achat DECIMAL(10,2) DEFAULT 0.00,
    prix_vente DECIMAL(10,2) NOT NULL,
    stock INT DEFAULT 0,
    seuil_alerte INT DEFAULT 10,
    code_barre VARCHAR(50),
    categorie_id INT,
    fournisseur_id INT,
    pharmacie_id INT DEFAULT 1,
    date_expiration DATE,
    image_url VARCHAR(500),
    sur_ordonnance TINYINT(1) DEFAULT 0,
    actif TINYINT(1) DEFAULT 1,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
);

CREATE TABLE IF NOT EXISTS password_reset_tokens (
    id INT PRIMARY KEY AUTO_INCREMENT,
    email VARCHAR(255) NOT NULL,
    token VARCHAR(255) NOT NULL,
    expires_at TIMESTAMP NOT NULL,
    used TINYINT(1) DEFAULT 0,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    KEY idx_email (email),
    KEY idx_token (token),
    KEY idx_expires (expires_at)
);

-- 10. Copier les données de bigpharma vers clientlegerlourd
-- Pharmacies
INSERT IGNORE INTO pharmacies SELECT * FROM bigpharma.pharmacies;

-- Utilisateurs
INSERT IGNORE INTO utilisateurs SELECT * FROM bigpharma.utilisateurs;

-- Catégories
INSERT IGNORE INTO categories (id, nom, description) 
SELECT id, nom, description FROM bigpharma.categories;

-- Fournisseurs
INSERT IGNORE INTO fournisseurs (id, nom, adresse, telephone, email, siret) 
SELECT id, nom, adresse, telephone, email, siret FROM bigpharma.fournisseurs;

-- Produits (adapter les noms de colonnes)
INSERT IGNORE INTO produits (id, nom, description, prix_achat, prix_vente, stock, seuil_alerte, 
                            categorie_id, fournisseur_id, pharmacie_id, date_expiration, image_url, sur_ordonnance, actif)
SELECT id, nom, description, prix_achat, prix_vente, quantite_stock, seuil_alerte,
       (SELECT c.id FROM categories c WHERE c.nom = p.categorie LIMIT 1) as categorie_id,
       fournisseur_id, pharmacie_id, date_expiration, image_url, sur_ordonnance, 1
FROM bigpharma.produits p;

-- 11. Vérifications finales
SELECT 'VÉRIFICATIONS FINALES' as status;

-- Compter les données dans bigpharma
USE bigpharma;
SELECT 'bigpharma - Utilisateurs' as table_name, COUNT(*) as count FROM utilisateurs WHERE email = 'tourefaliloumbacke12345@gmail.com'
UNION ALL
SELECT 'bigpharma - Produits', COUNT(*) FROM produits WHERE pharmacie_id = 1
UNION ALL
SELECT 'bigpharma - Fournisseurs', COUNT(*) FROM fournisseurs WHERE pharmacie_id = 1
UNION ALL
SELECT 'bigpharma - Catégories', COUNT(*) FROM categories WHERE pharmacie_id = 1;

-- Compter les données dans clientlegerlourd
USE clientlegerlourd;
SELECT 'clientlegerlourd - Utilisateurs' as table_name, COUNT(*) as count FROM utilisateurs WHERE email = 'tourefaliloumbacke12345@gmail.com'
UNION ALL
SELECT 'clientlegerlourd - Produits', COUNT(*) FROM produits WHERE pharmacie_id = 1
UNION ALL
SELECT 'clientlegerlourd - Fournisseurs', COUNT(*) FROM fournisseurs
UNION ALL
SELECT 'clientlegerlourd - Catégories', COUNT(*) FROM categories;

-- Afficher les informations de l'utilisateur cible
SELECT 'UTILISATEUR CIBLE' as info;
USE bigpharma;
SELECT 'bigpharma' as base_donnees, nom, prenom, email, role, pharmacie_id, actif 
FROM utilisateurs WHERE email = 'tourefaliloumbacke12345@gmail.com';

USE clientlegerlourd;
SELECT 'clientlegerlourd' as base_donnees, nom, prenom, email, role, pharmacie_id, actif 
FROM utilisateurs WHERE email = 'tourefaliloumbacke12345@gmail.com';

SELECT '✅ SYNCHRONISATION TERMINÉE AVEC SUCCÈS !' as resultat;
