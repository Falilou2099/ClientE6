-- Désactiver temporairement la vérification des clés étrangères
SET FOREIGN_KEY_CHECKS = 0;

-- Suppression des tables existantes
DROP TABLE IF EXISTS details_ventes;
DROP TABLE IF EXISTS ventes;
DROP TABLE IF EXISTS details_commandes;
DROP TABLE IF EXISTS commandes;
DROP TABLE IF EXISTS stocks;
DROP TABLE IF EXISTS produits;
DROP TABLE IF EXISTS fournisseurs;
DROP TABLE IF EXISTS clients;
DROP TABLE IF EXISTS admin;
DROP TABLE IF EXISTS pharmacies;

-- Réactiver la vérification des clés étrangères
SET FOREIGN_KEY_CHECKS = 1;
