# BigPharma - Panneau d'Administration Java

Application Java pour l'administration des pharmacies BigPharma. Cette application permet aux administrateurs de gérer les produits, les stocks et les commandes pour chaque pharmacie.

## Fonctionnalités

- Authentification avec les mêmes identifiants que l'application web
- Gestion des produits (ajout, modification, suppression)
- Gestion des stocks
- Gestion des commandes
- Importation d'images pour les produits
- Interface moderne et esthétique

## Configuration requise

- Java 11 ou supérieur
- Eclipse IDE
- MySQL (via XAMPP)

## Installation

1. Importez le projet dans Eclipse
2. Assurez-vous que les bibliothèques requises sont dans le classpath
3. Exécutez la classe `Main.java`

## Structure du projet

- `src/` - Code source principal
  - `com/bigpharma/admin/` - Package principal
    - `controllers/` - Contrôleurs pour les différentes vues
    - `models/` - Modèles de données
    - `views/` - Interfaces utilisateur
    - `utils/` - Classes utilitaires
    - `dao/` - Couche d'accès aux données
