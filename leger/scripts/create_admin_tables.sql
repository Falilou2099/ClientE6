-- Création des tables pour la gestion des administrateurs et des pharmacies

-- Table des pharmacies
CREATE TABLE IF NOT EXISTS pharmacie (
    id INT AUTO_INCREMENT PRIMARY KEY,
    nom VARCHAR(255) NOT NULL,
    adresse TEXT NOT NULL,
    telephone VARCHAR(20) NOT NULL,
    email VARCHAR(255) NOT NULL UNIQUE,
    date_creation TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    statut ENUM('actif', 'inactif') DEFAULT 'actif'
);

-- Table des administrateurs
CREATE TABLE IF NOT EXISTS admin (
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
    FOREIGN KEY (pharmacie_id) REFERENCES pharmacie(id)
);

-- Insertion d'une pharmacie de test
INSERT INTO pharmacie (nom, adresse, telephone, email) VALUES 
('Pharmacie Centrale', '15 rue de la Paix, 75001 Paris', '0123456789', 'contact@pharmaciecentrale.fr');

-- Insertion d'un administrateur de test (mot de passe : Admin123!)
INSERT INTO admin (username, password, nom, prenom, email, pharmacie_id) VALUES 
('admin', 'Admin123!', 'Dupont', 'Jean', 'admin@pharmaciecentrale.fr', 
(SELECT id FROM pharmacie WHERE nom = 'Pharmacie Centrale'));
