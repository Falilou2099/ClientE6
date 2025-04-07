-- Script pour ajouter la colonne date_expiration à la table produits
USE bigpharma;

-- Vérifier si la colonne existe déjà
SET @exists = 0;
SELECT COUNT(*) INTO @exists FROM information_schema.columns 
WHERE table_schema = 'bigpharma' AND table_name = 'produits' AND column_name = 'date_expiration';

-- Si la colonne n'existe pas, l'ajouter
SET @query = IF(@exists = 0, 
    'ALTER TABLE produits ADD COLUMN date_expiration DATE NULL',
    'SELECT "La colonne date_expiration existe déjà."');

PREPARE stmt FROM @query;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

-- Vérifier si la colonne prix_achat existe
SET @prix_achat_exists = 0;
SELECT COUNT(*) INTO @prix_achat_exists FROM information_schema.columns 
WHERE table_schema = 'bigpharma' AND table_name = 'produits' AND column_name = 'prix_achat';

-- Si la colonne prix_achat n'existe pas, l'ajouter
SET @query2 = IF(@prix_achat_exists = 0, 
    'ALTER TABLE produits ADD COLUMN prix_achat DECIMAL(10, 2) NOT NULL DEFAULT 0',
    'SELECT "La colonne prix_achat existe déjà."');

PREPARE stmt FROM @query2;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

-- Vérifier si la colonne prix_vente existe
SET @prix_vente_exists = 0;
SELECT COUNT(*) INTO @prix_vente_exists FROM information_schema.columns 
WHERE table_schema = 'bigpharma' AND table_name = 'produits' AND column_name = 'prix_vente';

-- Si la colonne prix_vente n'existe pas, l'ajouter (et renommer prix en prix_vente si nécessaire)
SET @prix_exists = 0;
SELECT COUNT(*) INTO @prix_exists FROM information_schema.columns 
WHERE table_schema = 'bigpharma' AND table_name = 'produits' AND column_name = 'prix';

SET @query3 = IF(@prix_vente_exists = 0 AND @prix_exists = 1, 
    'ALTER TABLE produits CHANGE COLUMN prix prix_vente DECIMAL(10, 2) NOT NULL',
    IF(@prix_vente_exists = 0 AND @prix_exists = 0,
        'ALTER TABLE produits ADD COLUMN prix_vente DECIMAL(10, 2) NOT NULL DEFAULT 0',
        'SELECT "La colonne prix_vente existe déjà."'));

PREPARE stmt FROM @query3;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

-- Afficher un message de confirmation
SELECT 'Mise à jour de la structure de la table produits terminée.' AS Message;
