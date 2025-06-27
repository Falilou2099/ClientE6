-- ========================================
-- SCRIPT DE SYNCHRONISATION COMPLÈTE
-- BigPharma Java ↔ PHP
-- ========================================

-- 1. CRÉATION DE LA BASE CLIENTLEGERLOURD SI ELLE N'EXISTE PAS
CREATE DATABASE IF NOT EXISTS `clientlegerlourd` CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
USE `clientlegerlourd`;

-- 2. CRÉATION DES TABLES PRINCIPALES
-- Table des pharmacies
CREATE TABLE IF NOT EXISTS `pharmacies` (
    `id` int(11) NOT NULL AUTO_INCREMENT,
    `nom` varchar(255) NOT NULL,
    `adresse` text,
    `telephone` varchar(20),
    `email` varchar(255),
    `siret` varchar(14),
    `created_at` timestamp DEFAULT CURRENT_TIMESTAMP,
    `updated_at` timestamp DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- Table des catégories
CREATE TABLE IF NOT EXISTS `categories` (
    `id` int(11) NOT NULL AUTO_INCREMENT,
    `nom` varchar(255) NOT NULL,
    `description` text,
    `created_at` timestamp DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- Table des fournisseurs
CREATE TABLE IF NOT EXISTS `fournisseurs` (
    `id` int(11) NOT NULL AUTO_INCREMENT,
    `nom` varchar(255) NOT NULL,
    `adresse` text,
    `telephone` varchar(20),
    `email` varchar(255),
    `siret` varchar(14),
    `created_at` timestamp DEFAULT CURRENT_TIMESTAMP,
    `updated_at` timestamp DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- Table des utilisateurs
CREATE TABLE IF NOT EXISTS `utilisateurs` (
    `id` int(11) NOT NULL AUTO_INCREMENT,
    `nom` varchar(255) NOT NULL,
    `prenom` varchar(255),
    `email` varchar(255) UNIQUE NOT NULL,
    `mot_de_passe` varchar(255) NOT NULL,
    `role` enum('admin','pharmacien','vendeur') DEFAULT 'vendeur',
    `pharmacie_id` int(11) DEFAULT 1,
    `actif` tinyint(1) DEFAULT 1,
    `created_at` timestamp DEFAULT CURRENT_TIMESTAMP,
    `updated_at` timestamp DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    PRIMARY KEY (`id`),
    KEY `fk_pharmacie` (`pharmacie_id`),
    FOREIGN KEY (`pharmacie_id`) REFERENCES `pharmacies`(`id`) ON DELETE SET NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- Table des produits
CREATE TABLE IF NOT EXISTS `produits` (
    `id` int(11) NOT NULL AUTO_INCREMENT,
    `nom` varchar(255) NOT NULL,
    `description` text,
    `prix_achat` decimal(10,2) DEFAULT 0.00,
    `prix_vente` decimal(10,2) NOT NULL,
    `stock` int(11) DEFAULT 0,
    `seuil_alerte` int(11) DEFAULT 10,
    `code_barre` varchar(50),
    `categorie_id` int(11),
    `fournisseur_id` int(11),
    `pharmacie_id` int(11) DEFAULT 1,
    `date_expiration` date,
    `image_url` varchar(500),
    `sur_ordonnance` tinyint(1) DEFAULT 0,
    `actif` tinyint(1) DEFAULT 1,
    `created_at` timestamp DEFAULT CURRENT_TIMESTAMP,
    `updated_at` timestamp DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    PRIMARY KEY (`id`),
    KEY `fk_categorie` (`categorie_id`),
    KEY `fk_fournisseur` (`fournisseur_id`),
    KEY `fk_pharmacie_produit` (`pharmacie_id`),
    FOREIGN KEY (`categorie_id`) REFERENCES `categories`(`id`) ON DELETE SET NULL,
    FOREIGN KEY (`fournisseur_id`) REFERENCES `fournisseurs`(`id`) ON DELETE SET NULL,
    FOREIGN KEY (`pharmacie_id`) REFERENCES `pharmacies`(`id`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- Table des tentatives de connexion
CREATE TABLE IF NOT EXISTS `login_attempts` (
    `id` int(11) NOT NULL AUTO_INCREMENT,
    `email` varchar(255) NOT NULL,
    `ip_address` varchar(45),
    `attempts` int(11) DEFAULT 1,
    `last_attempt` timestamp DEFAULT CURRENT_TIMESTAMP,
    `blocked_until` timestamp NULL,
    PRIMARY KEY (`id`),
    UNIQUE KEY `unique_email_ip` (`email`, `ip_address`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- Table des tokens de réinitialisation de mot de passe
CREATE TABLE IF NOT EXISTS `password_reset_tokens` (
    `id` int(11) NOT NULL AUTO_INCREMENT,
    `email` varchar(255) NOT NULL,
    `token` varchar(255) NOT NULL,
    `expires_at` timestamp NOT NULL,
    `used` tinyint(1) DEFAULT 0,
    `created_at` timestamp DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (`id`),
    KEY `idx_email` (`email`),
    KEY `idx_token` (`token`),
    KEY `idx_expires` (`expires_at`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- 3. INSERTION DES DONNÉES DE BASE
-- Pharmacie par défaut
INSERT IGNORE INTO `pharmacies` (`id`, `nom`, `adresse`, `telephone`, `email`, `siret`) VALUES
(1, 'Pharmacie BigPharma', '123 Rue de la Santé, 75000 Paris', '01.23.45.67.89', 'contact@bigpharma.fr', '12345678901234');

-- Catégories pharmaceutiques
INSERT IGNORE INTO `categories` (`id`, `nom`, `description`) VALUES
(1, 'Analgésiques', 'Médicaments contre la douleur'),
(2, 'Anti-inflammatoires', 'Médicaments anti-inflammatoires'),
(3, 'Antibiotiques', 'Médicaments antibactériens'),
(4, 'Antihistaminiques', 'Médicaments contre les allergies'),
(5, 'Antispasmodiques', 'Médicaments contre les spasmes'),
(6, 'Cardiovasculaires', 'Médicaments pour le cœur'),
(7, 'Dermatologiques', 'Médicaments pour la peau'),
(8, 'Digestifs', 'Médicaments pour la digestion'),
(9, 'Endocrinologiques', 'Médicaments hormonaux'),
(10, 'Gynécologiques', 'Médicaments gynécologiques'),
(11, 'Neurologiques', 'Médicaments neurologiques'),
(12, 'Ophtalmologiques', 'Médicaments pour les yeux'),
(13, 'ORL', 'Médicaments ORL'),
(14, 'Pneumologiques', 'Médicaments respiratoires'),
(15, 'Psychiatriques', 'Médicaments psychiatriques'),
(16, 'Urologiques', 'Médicaments urologiques'),
(17, 'Vitamines', 'Vitamines et compléments');

-- Fournisseurs
INSERT IGNORE INTO `fournisseurs` (`id`, `nom`, `adresse`, `telephone`, `email`, `siret`) VALUES
(1, 'Laboratoires Sanofi', '54 Rue La Boétie, 75008 Paris', '01.53.77.40.00', 'contact@sanofi.com', '39542335300047'),
(2, 'Pfizer France', '23-25 Avenue du Docteur Lannelongue, 75014 Paris', '01.58.07.34.40', 'contact@pfizer.fr', '95542335300048'),
(3, 'Novartis Pharma', '2-4 Rue Lionel Terray, 92500 Rueil-Malmaison', '01.55.47.60.00', 'contact@novartis.fr', '78542335300049'),
(4, 'Roche France', '30 Cours de l\'Île Seguin, 92100 Boulogne-Billancourt', '01.46.40.50.00', 'contact@roche.fr', '61542335300050'),
(5, 'Merck France', '37 Rue Saint-Romain, 69008 Lyon', '04.72.78.09.00', 'contact@merck.fr', '44542335300051');

-- Produits pharmaceutiques
INSERT IGNORE INTO `produits` (`id`, `nom`, `description`, `prix_achat`, `prix_vente`, `stock`, `seuil_alerte`, `categorie_id`, `fournisseur_id`, `pharmacie_id`, `date_expiration`, `sur_ordonnance`) VALUES
(1, 'Doliprane 1000mg', 'Paracétamol 1000mg - Boîte de 8 comprimés', 2.50, 3.95, 150, 20, 1, 1, 1, '2025-12-31', 0),
(2, 'Advil 400mg', 'Ibuprofène 400mg - Boîte de 14 comprimés', 3.20, 4.85, 120, 15, 2, 2, 1, '2025-11-30', 0),
(3, 'Amoxicilline 500mg', 'Antibiotique - Boîte de 16 gélules', 4.80, 7.25, 80, 10, 3, 1, 1, '2025-10-15', 1),
(4, 'Cetirizine 10mg', 'Antihistaminique - Boîte de 15 comprimés', 2.10, 3.45, 90, 12, 4, 3, 1, '2026-01-20', 0),
(5, 'Spasfon 80mg', 'Antispasmodique - Boîte de 30 comprimés', 3.80, 5.95, 110, 18, 5, 1, 1, '2025-09-30', 0),
(6, 'Kardegic 75mg', 'Aspirine cardio - Boîte de 30 comprimés', 2.90, 4.20, 95, 15, 6, 1, 1, '2026-03-15', 1),
(7, 'Biafine', 'Émulsion pour brûlures - Tube de 93g', 4.50, 6.80, 75, 10, 7, 4, 1, '2025-08-20', 0),
(8, 'Smecta', 'Pansement digestif - Boîte de 30 sachets', 3.60, 5.45, 85, 12, 8, 2, 1, '2025-07-10', 0),
(9, 'Levothyrox 50µg', 'Hormone thyroïdienne - Boîte de 30 comprimés', 2.80, 4.15, 60, 8, 9, 5, 1, '2026-02-28', 1),
(10, 'Gynéfam', 'Complément gynécologique - Boîte de 30 capsules', 8.20, 12.50, 45, 8, 10, 3, 1, '2025-12-15', 0),
(11, 'Laroxyl 25mg', 'Antidépresseur - Boîte de 28 comprimés', 5.40, 8.20, 35, 5, 11, 1, 1, '2025-11-05', 1),
(12, 'Maxidex', 'Collyre anti-inflammatoire - Flacon de 5ml', 6.80, 9.95, 50, 8, 12, 4, 1, '2025-06-30', 1),
(13, 'Rhinofluimucil', 'Spray nasal décongestionnant - Flacon de 10ml', 4.20, 6.35, 70, 10, 13, 2, 1, '2025-09-15', 0),
(14, 'Ventoline', 'Bronchodilatateur - Aérosol de 100 doses', 7.50, 11.20, 40, 6, 14, 3, 1, '2025-08-25', 1),
(15, 'Lexomil 6mg', 'Anxiolytique - Boîte de 30 comprimés', 3.90, 5.85, 25, 5, 15, 5, 1, '2025-10-10', 1),
(16, 'Monuril 3g', 'Antibiotique urinaire - Boîte de 1 sachet', 8.90, 13.45, 30, 5, 16, 1, 1, '2025-07-20', 1),
(17, 'Supradyn', 'Multivitamines - Boîte de 30 comprimés', 6.20, 9.50, 65, 10, 17, 4, 1, '2026-01-10', 0),
(18, 'Efferalgan 500mg', 'Paracétamol effervescent - Boîte de 16 comprimés', 2.80, 4.25, 130, 20, 1, 1, 1, '2025-11-20', 0),
(19, 'Nurofen 200mg', 'Ibuprofène - Boîte de 20 comprimés', 3.50, 5.20, 100, 15, 2, 2, 1, '2025-10-05', 0),
(20, 'Augmentin 1g', 'Antibiotique - Boîte de 14 comprimés', 12.80, 18.95, 20, 3, 3, 3, 1, '2025-09-01', 1);

-- 4. CRÉATION/MISE À JOUR DE L'UTILISATEUR CIBLE
-- Supprimer l'ancien utilisateur s'il existe
DELETE FROM `utilisateurs` WHERE `email` = 'tourefaliloumbacke12345@gmail.com';

-- Insérer le nouvel utilisateur avec le bon hash SHA-256
INSERT INTO `utilisateurs` (`nom`, `prenom`, `email`, `mot_de_passe`, `role`, `pharmacie_id`, `actif`) VALUES
('Mbacke', 'Toure Falilou', 'tourefaliloumbacke12345@gmail.com', 
 SHA2('password', 256), 'admin', 1, 1);

-- 5. NETTOYAGE DES ANCIENNES DONNÉES
-- Nettoyer les tentatives de connexion anciennes
DELETE FROM `login_attempts` WHERE `last_attempt` < DATE_SUB(NOW(), INTERVAL 24 HOUR);

-- Nettoyer les tokens expirés
DELETE FROM `password_reset_tokens` WHERE `expires_at` < NOW() OR `used` = 1;

-- 6. SYNCHRONISATION AVEC LA BASE BIGPHARMA
-- Copier les données vers la base bigpharma si elle existe
CREATE DATABASE IF NOT EXISTS `bigpharma` CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;

-- Utiliser la base bigpharma pour la synchronisation inverse
USE `bigpharma`;

-- Créer les mêmes tables dans bigpharma
CREATE TABLE IF NOT EXISTS `pharmacies` (
    `id` int(11) NOT NULL AUTO_INCREMENT,
    `nom` varchar(255) NOT NULL,
    `adresse` text,
    `telephone` varchar(20),
    `email` varchar(255),
    `siret` varchar(14),
    `created_at` timestamp DEFAULT CURRENT_TIMESTAMP,
    `updated_at` timestamp DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE IF NOT EXISTS `categories` (
    `id` int(11) NOT NULL AUTO_INCREMENT,
    `nom` varchar(255) NOT NULL,
    `description` text,
    `created_at` timestamp DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE IF NOT EXISTS `fournisseurs` (
    `id` int(11) NOT NULL AUTO_INCREMENT,
    `nom` varchar(255) NOT NULL,
    `adresse` text,
    `telephone` varchar(20),
    `email` varchar(255),
    `siret` varchar(14),
    `created_at` timestamp DEFAULT CURRENT_TIMESTAMP,
    `updated_at` timestamp DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE IF NOT EXISTS `utilisateurs` (
    `id` int(11) NOT NULL AUTO_INCREMENT,
    `nom` varchar(255) NOT NULL,
    `prenom` varchar(255),
    `email` varchar(255) UNIQUE NOT NULL,
    `mot_de_passe` varchar(255) NOT NULL,
    `role` enum('admin','pharmacien','vendeur') DEFAULT 'vendeur',
    `pharmacie_id` int(11) DEFAULT 1,
    `actif` tinyint(1) DEFAULT 1,
    `created_at` timestamp DEFAULT CURRENT_TIMESTAMP,
    `updated_at` timestamp DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    PRIMARY KEY (`id`),
    KEY `fk_pharmacie` (`pharmacie_id`),
    FOREIGN KEY (`pharmacie_id`) REFERENCES `pharmacies`(`id`) ON DELETE SET NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE IF NOT EXISTS `produits` (
    `id` int(11) NOT NULL AUTO_INCREMENT,
    `nom` varchar(255) NOT NULL,
    `description` text,
    `prix_achat` decimal(10,2) DEFAULT 0.00,
    `prix_vente` decimal(10,2) NOT NULL,
    `stock` int(11) DEFAULT 0,
    `seuil_alerte` int(11) DEFAULT 10,
    `code_barre` varchar(50),
    `categorie_id` int(11),
    `fournisseur_id` int(11),
    `pharmacie_id` int(11) DEFAULT 1,
    `date_expiration` date,
    `image_url` varchar(500),
    `sur_ordonnance` tinyint(1) DEFAULT 0,
    `actif` tinyint(1) DEFAULT 1,
    `created_at` timestamp DEFAULT CURRENT_TIMESTAMP,
    `updated_at` timestamp DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    PRIMARY KEY (`id`),
    KEY `fk_categorie` (`categorie_id`),
    KEY `fk_fournisseur` (`fournisseur_id`),
    KEY `fk_pharmacie_produit` (`pharmacie_id`),
    FOREIGN KEY (`categorie_id`) REFERENCES `categories`(`id`) ON DELETE SET NULL,
    FOREIGN KEY (`fournisseur_id`) REFERENCES `fournisseurs`(`id`) ON DELETE SET NULL,
    FOREIGN KEY (`pharmacie_id`) REFERENCES `pharmacies`(`id`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE IF NOT EXISTS `login_attempts` (
    `id` int(11) NOT NULL AUTO_INCREMENT,
    `email` varchar(255) NOT NULL,
    `ip_address` varchar(45),
    `attempts` int(11) DEFAULT 1,
    `last_attempt` timestamp DEFAULT CURRENT_TIMESTAMP,
    `blocked_until` timestamp NULL,
    PRIMARY KEY (`id`),
    UNIQUE KEY `unique_email_ip` (`email`, `ip_address`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE IF NOT EXISTS `password_resets` (
    `id` int(11) NOT NULL AUTO_INCREMENT,
    `email` varchar(255) NOT NULL,
    `token` varchar(255) NOT NULL,
    `expires_at` timestamp NOT NULL,
    `used` tinyint(1) DEFAULT 0,
    `created_at` timestamp DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (`id`),
    KEY `idx_email` (`email`),
    KEY `idx_token` (`token`),
    KEY `idx_expires` (`expires_at`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- Copier toutes les données de clientlegerlourd vers bigpharma
INSERT IGNORE INTO `pharmacies` SELECT * FROM `clientlegerlourd`.`pharmacies`;
INSERT IGNORE INTO `categories` SELECT * FROM `clientlegerlourd`.`categories`;
INSERT IGNORE INTO `fournisseurs` SELECT * FROM `clientlegerlourd`.`fournisseurs`;
INSERT IGNORE INTO `utilisateurs` SELECT * FROM `clientlegerlourd`.`utilisateurs`;
INSERT IGNORE INTO `produits` SELECT * FROM `clientlegerlourd`.`produits`;
INSERT IGNORE INTO `login_attempts` SELECT * FROM `clientlegerlourd`.`login_attempts`;

-- 7. VÉRIFICATIONS FINALES
USE `clientlegerlourd`;

SELECT '=== VÉRIFICATION CLIENTLEGERLOURD ===' as 'RAPPORT';
SELECT COUNT(*) as 'Nombre de pharmacies' FROM `pharmacies`;
SELECT COUNT(*) as 'Nombre de catégories' FROM `categories`;
SELECT COUNT(*) as 'Nombre de fournisseurs' FROM `fournisseurs`;
SELECT COUNT(*) as 'Nombre d\'utilisateurs' FROM `utilisateurs`;
SELECT COUNT(*) as 'Nombre de produits' FROM `produits`;

SELECT '=== UTILISATEUR CIBLE ===' as 'UTILISATEUR';
SELECT `id`, `nom`, `prenom`, `email`, `role`, `pharmacie_id`, `actif` 
FROM `utilisateurs` 
WHERE `email` = 'tourefaliloumbacke12345@gmail.com';

SELECT '=== PRODUITS POUR LA PHARMACIE 1 ===' as 'PRODUITS';
SELECT COUNT(*) as 'Nombre de produits pour pharmacie 1' 
FROM `produits` 
WHERE `pharmacie_id` = 1 AND `actif` = 1;

USE `bigpharma`;
SELECT '=== VÉRIFICATION BIGPHARMA ===' as 'RAPPORT';
SELECT COUNT(*) as 'Nombre de produits bigpharma' FROM `produits`;
SELECT COUNT(*) as 'Nombre d\'utilisateurs bigpharma' FROM `utilisateurs`;

SELECT '=== SYNCHRONISATION TERMINÉE ===' as 'STATUT';
