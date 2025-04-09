-- Ajouter la colonne horaires à la table pharmacies
ALTER TABLE pharmacies ADD COLUMN horaires TEXT AFTER siret;

-- Mettre à jour les enregistrements existants avec une valeur par défaut
UPDATE pharmacies SET horaires = 'Lundi-Vendredi: 9h-19h, Samedi: 9h-12h';
