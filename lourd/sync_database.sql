-- Script de synchronisation BigPharma Java ↔ PHP
-- Création des tables et données pour le compte tourefaliloumbacke12345@gmail.com

USE bigpharma;

-- Création de la table pharmacies si elle n'existe pas
CREATE TABLE IF NOT EXISTS pharmacies (
    id INT PRIMARY KEY AUTO_INCREMENT,
    nom VARCHAR(255) NOT NULL,
    adresse TEXT,
    telephone VARCHAR(20),
    email VARCHAR(100),
    siret VARCHAR(14),
    date_creation TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- Création de la table categories si elle n'existe pas
CREATE TABLE IF NOT EXISTS categories (
    id INT PRIMARY KEY AUTO_INCREMENT,
    nom VARCHAR(100) NOT NULL UNIQUE,
    description TEXT,
    date_creation TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- Création de la table fournisseurs si elle n'existe pas
CREATE TABLE IF NOT EXISTS fournisseurs (
    id INT PRIMARY KEY AUTO_INCREMENT,
    nom VARCHAR(255) NOT NULL,
    adresse TEXT,
    telephone VARCHAR(20),
    email VARCHAR(100),
    siret VARCHAR(14),
    pharmacie_id INT DEFAULT 1,
    date_creation TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (pharmacie_id) REFERENCES pharmacies(id)
);

-- Mise à jour de la table produits pour ajouter les colonnes manquantes
ALTER TABLE produits 
ADD COLUMN IF NOT EXISTS prix_achat DECIMAL(10,2) DEFAULT 0.00,
ADD COLUMN IF NOT EXISTS stock_minimum INT DEFAULT 10,
ADD COLUMN IF NOT EXISTS categorie_id INT DEFAULT 1,
ADD COLUMN IF NOT EXISTS pharmacie_id INT DEFAULT 1,
ADD COLUMN IF NOT EXISTS date_creation TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
ADD COLUMN IF NOT EXISTS date_modification TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP;

-- Mise à jour de la table utilisateurs pour ajouter pharmacie_id
ALTER TABLE utilisateurs 
ADD COLUMN IF NOT EXISTS pharmacie_id INT DEFAULT 1;

-- Insertion de la pharmacie par défaut
INSERT IGNORE INTO pharmacies (id, nom, adresse, telephone, email, siret) VALUES 
(1, 'Pharmacie BigPharma', '123 Rue de la Santé, 75000 Paris', '01.23.45.67.89', 'contact@bigpharma.com', '12345678901234');

-- Insertion des catégories
INSERT IGNORE INTO categories (nom, description) VALUES 
('Analgésiques', 'Médicaments contre la douleur'),
('Anti-inflammatoires', 'Médicaments anti-inflammatoires'),
('Antibiotiques', 'Médicaments antibactériens'),
('Antihistaminiques', 'Médicaments contre les allergies'),
('Antispasmodiques', 'Médicaments contre les spasmes'),
('Cardiovasculaires', 'Médicaments pour le cœur'),
('Dermatologiques', 'Médicaments pour la peau'),
('Digestifs', 'Médicaments pour la digestion'),
('Endocrinologiques', 'Médicaments hormonaux'),
('Gynécologiques', 'Médicaments gynécologiques'),
('Neurologiques', 'Médicaments neurologiques'),
('Ophtalmologiques', 'Médicaments pour les yeux'),
('ORL', 'Médicaments ORL'),
('Pneumologiques', 'Médicaments respiratoires'),
('Psychiatriques', 'Médicaments psychiatriques'),
('Urologiques', 'Médicaments urologiques'),
('Vitamines', 'Vitamines et compléments');

-- Insertion des fournisseurs
INSERT IGNORE INTO fournisseurs (nom, adresse, telephone, email, siret, pharmacie_id) VALUES 
('Laboratoires Sanofi', '54 Rue La Boétie, 75008 Paris', '01.53.77.40.00', 'contact@sanofi.fr', '12345678901234', 1),
('Pfizer France', '23-25 Avenue du Dr Lannelongue, 75014 Paris', '01.58.07.34.40', 'info@pfizer.fr', '23456789012345', 1),
('Novartis Pharma', '2-4 Rue Lionel Terray, 92500 Rueil-Malmaison', '01.55.47.60.00', 'contact@novartis.fr', '34567890123456', 1),
('Roche France', '30 Cours de l\'Île Seguin, 92100 Boulogne-Billancourt', '01.46.40.50.00', 'info@roche.fr', '45678901234567', 1),
('Merck France', '37 Rue Saint-Romain, 69008 Lyon', '04.72.78.09.00', 'contact@merck.fr', '56789012345678', 1);

-- Vérification/création de l'utilisateur tourefaliloumbacke12345@gmail.com
INSERT IGNORE INTO utilisateurs (nom, email, mot_de_passe, pharmacie_id) VALUES 
('Toure Falilou Mbacke', 'tourefaliloumbacke12345@gmail.com', '5e884898da28047151d0e56f8dc6292773603d0d6aabbdd62a11ef721d1542d8', 1);

-- Mise à jour de l'utilisateur existant pour s'assurer qu'il a la bonne pharmacie_id
UPDATE utilisateurs SET pharmacie_id = 1 WHERE email = 'tourefaliloumbacke12345@gmail.com';

-- Insertion des produits pour la pharmacie_id = 1
INSERT IGNORE INTO produits (nom, description, prix_achat, prix_vente, stock_actuel, stock_minimum, categorie_id, pharmacie_id) VALUES 
('Doliprane 1000mg', 'Paracétamol 1000mg - Boîte de 8 comprimés', 3.50, 5.20, 100, 10, 1, 1),
('Advil 400mg', 'Ibuprofène 400mg - Boîte de 20 comprimés', 4.80, 7.20, 75, 10, 2, 1),
('Amoxicilline 500mg', 'Amoxicilline 500mg - Boîte de 12 gélules', 8.90, 12.50, 50, 5, 3, 1),
('Cetirizine 10mg', 'Cetirizine 10mg - Boîte de 15 comprimés', 3.20, 4.80, 80, 10, 4, 1),
('Spasfon 80mg', 'Phloroglucinol 80mg - Boîte de 30 comprimés', 6.40, 9.60, 60, 10, 5, 1),
('Kardegic 75mg', 'Aspirine 75mg - Boîte de 30 comprimés', 2.90, 4.35, 90, 15, 6, 1),
('Biafine', 'Émulsion pour application cutanée - Tube 93g', 7.20, 10.80, 40, 5, 7, 1),
('Smecta', 'Diosmectite - Boîte de 30 sachets', 5.60, 8.40, 70, 10, 8, 1),
('Levothyrox 50µg', 'Lévothyroxine 50µg - Boîte de 30 comprimés', 4.20, 6.30, 85, 10, 9, 1),
('Gynéfam', 'Complément alimentaire - Boîte de 30 gélules', 12.50, 18.75, 30, 5, 10, 1),
('Laroxyl 25mg', 'Amitriptyline 25mg - Boîte de 28 comprimés', 8.40, 12.60, 25, 5, 11, 1),
('Maxidex', 'Dexaméthasone - Flacon 5ml', 6.80, 10.20, 35, 5, 12, 1),
('Rhinofluimucil', 'Spray nasal - Flacon 10ml', 4.90, 7.35, 55, 10, 13, 1),
('Ventoline', 'Salbutamol - Aérosol 100µg', 3.80, 5.70, 65, 10, 14, 1),
('Lexomil 6mg', 'Bromazépam 6mg - Boîte de 30 comprimés', 2.40, 3.60, 45, 10, 15, 1),
('Monuril 3g', 'Fosfomycine 3g - Boîte de 1 sachet', 7.60, 11.40, 20, 5, 16, 1),
('Supradyn', 'Multivitamines - Boîte de 30 comprimés', 9.20, 13.80, 50, 10, 17, 1),
('Efferalgan 500mg', 'Paracétamol 500mg - Boîte de 16 comprimés', 2.80, 4.20, 120, 20, 1, 1),
('Nurofen 200mg', 'Ibuprofène 200mg - Boîte de 30 comprimés', 3.90, 5.85, 95, 15, 2, 1),
('Augmentin 1g', 'Amoxicilline/Acide clavulanique 1g - Boîte de 8 comprimés', 12.40, 18.60, 35, 5, 3, 1);

-- Nettoyage des anciens tokens de réinitialisation (plus de 24h)
DELETE FROM password_resets WHERE created_at < DATE_SUB(NOW(), INTERVAL 24 HOUR);

-- Affichage des statistiques finales
SELECT 'STATISTIQUES DE SYNCHRONISATION' as Info;
SELECT COUNT(*) as 'Nombre de produits', pharmacie_id FROM produits GROUP BY pharmacie_id;
SELECT COUNT(*) as 'Nombre de fournisseurs', pharmacie_id FROM fournisseurs GROUP BY pharmacie_id;
SELECT COUNT(*) as 'Nombre de catégories' FROM categories;
SELECT nom, email, pharmacie_id FROM utilisateurs WHERE email = 'tourefaliloumbacke12345@gmail.com';

-- Vérification des données pour l'utilisateur spécifique
SELECT 
    'VERIFICATION COMPTE UTILISATEUR' as Info,
    u.nom as 'Nom Utilisateur',
    u.email as 'Email',
    u.pharmacie_id as 'ID Pharmacie',
    p.nom as 'Nom Pharmacie',
    (SELECT COUNT(*) FROM produits WHERE pharmacie_id = u.pharmacie_id) as 'Nb Produits',
    (SELECT COUNT(*) FROM fournisseurs WHERE pharmacie_id = u.pharmacie_id) as 'Nb Fournisseurs'
FROM utilisateurs u
LEFT JOIN pharmacies p ON u.pharmacie_id = p.id
WHERE u.email = 'tourefaliloumbacke12345@gmail.com';

COMMIT;
