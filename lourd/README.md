# Gestion des Produits Pharmaceutiques

## Prérequis
- Java 17 ou supérieur
- MySQL 8.0 ou supérieur
- Maven

## Configuration de la base de données

### Étapes d'installation

1. Ouvrez MySQL Workbench
2. Connectez-vous à votre serveur MySQL local
3. Exécutez le script SQL suivant pour configurer la base de données :

```sql
-- Supprimer la base de données si elle existe
DROP DATABASE IF EXISTS clientlegerlourd;

-- Créer une nouvelle base de données
CREATE DATABASE clientlegerlourd CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci;

-- Utiliser la nouvelle base de données
USE clientlegerlourd;

-- Table des catégories
CREATE TABLE categories (
    id INT AUTO_INCREMENT PRIMARY KEY,
    nom VARCHAR(100) NOT NULL UNIQUE,
    description TEXT
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

-- Insérer des catégories par défaut
INSERT INTO categories (nom, description) VALUES 
('Médicaments', 'Produits pharmaceutiques avec ordonnance'),
('Parapharmacie', 'Produits de santé sans ordonnance'),
('Cosmétique', 'Produits de beauté et de soin');

-- Table des produits
CREATE TABLE produits (
    id INT AUTO_INCREMENT PRIMARY KEY,
    nom VARCHAR(255) NOT NULL,
    description TEXT,
    prix DECIMAL(10, 2) NOT NULL,
    quantite_stock INT NOT NULL,
    categorie_id INT,
    date_creation TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    date_derniere_maj TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    FOREIGN KEY (categorie_id) REFERENCES categories(id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

-- Créer un utilisateur pour l'application
CREATE USER IF NOT EXISTS 'appuser'@'localhost' IDENTIFIED BY 'password123';
GRANT ALL PRIVILEGES ON clientlegerlourd.* TO 'appuser'@'localhost';
FLUSH PRIVILEGES;
```

### Configuration de la connexion

Modifiez le fichier `DatabaseConnection.java` si nécessaire :
- Vérifiez l'URL de connexion
- Confirmez le nom d'utilisateur et le mot de passe

## Fonctionnalités de l'application

- Interface graphique moderne
- Gestion des produits pharmaceutiques
- Catégorisation des produits
- Opérations CRUD (Créer, Lire, Mettre à jour, Supprimer)

## Exécution de l'application

1. Importez le projet dans votre IDE (Eclipse, IntelliJ, etc.)
2. Assurez-vous que Maven télécharge toutes les dépendances
3. Exécutez la classe `GestionProduitFrame`

## Dépendances
- MySQL Connector Java 8.0.33
- Java Swing (inclus dans le JDK)

## Résolution des problèmes
- Vérifiez que MySQL est en cours d'exécution
- Confirmez les paramètres de connexion
- Assurez-vous que les scripts SQL ont été exécutés

## Captures d'écran
(Vous pouvez ajouter des captures d'écran de l'application ici)
