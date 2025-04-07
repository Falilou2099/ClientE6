# Documentation BigPharma

## Table des matières
1. [Architecture globale](#architecture-globale)
2. [Base de données](#base-de-données)
3. [Client léger (PHP)](#client-léger-php)
4. [Client lourd (Java)](#client-lourd-java)
5. [Guide d'installation](#guide-dinstallation)
6. [Guide d'utilisation](#guide-dutilisation)

## Architecture globale

Le système BigPharma est composé de trois éléments principaux :
1. Une base de données MySQL commune
2. Un client léger en PHP pour la gestion quotidienne des pharmacies
3. Un client lourd en Java pour l'administration

### Schéma d'architecture
```
┌─────────────────┐     ┌─────────────────┐
│   Client Léger  │     │   Client Lourd  │
│      (PHP)      │     │     (Java)      │
└────────┬────────┘     └────────┬────────┘
         │                       │
         │                       │
         ▼                       ▼
┌────────────────────────────────────────┐
│           Base de données              │
│              MySQL                     │
└────────────────────────────────────────┘
```

## Base de données

### Schéma de la base de données
```sql
-- Schéma relationnel
pharmacies (
    id INT PRIMARY KEY,
    nom VARCHAR(100),
    adresse TEXT,
    telephone VARCHAR(20),
    email VARCHAR(100)
)

administrateurs (
    id INT PRIMARY KEY,
    pharmacie_id INT,
    nom VARCHAR(100),
    email VARCHAR(100),
    mot_de_passe VARCHAR(255),
    FOREIGN KEY (pharmacie_id) REFERENCES pharmacies(id)
)

clients (
    id INT PRIMARY KEY,
    pharmacie_id INT,
    nom VARCHAR(100),
    prenom VARCHAR(100),
    email VARCHAR(100),
    telephone VARCHAR(20),
    date_creation DATETIME,
    FOREIGN KEY (pharmacie_id) REFERENCES pharmacies(id)
)

produits (
    id INT PRIMARY KEY,
    nom VARCHAR(100),
    description TEXT,
    prix_unitaire DECIMAL(10,2),
    fournisseur_id INT,
    FOREIGN KEY (fournisseur_id) REFERENCES fournisseurs(id)
)

stocks (
    pharmacie_id INT,
    produit_id INT,
    quantite INT,
    seuil_alerte INT,
    PRIMARY KEY (pharmacie_id, produit_id),
    FOREIGN KEY (pharmacie_id) REFERENCES pharmacies(id),
    FOREIGN KEY (produit_id) REFERENCES produits(id)
)

ventes (
    id INT PRIMARY KEY,
    pharmacie_id INT,
    client_id INT,
    date_vente DATETIME,
    total DECIMAL(10,2),
    FOREIGN KEY (pharmacie_id) REFERENCES pharmacies(id),
    FOREIGN KEY (client_id) REFERENCES clients(id)
)

details_ventes (
    vente_id INT,
    produit_id INT,
    quantite INT,
    prix_unitaire DECIMAL(10,2),
    PRIMARY KEY (vente_id, produit_id),
    FOREIGN KEY (vente_id) REFERENCES ventes(id),
    FOREIGN KEY (produit_id) REFERENCES produits(id)
)

fournisseurs (
    id INT PRIMARY KEY,
    nom VARCHAR(100),
    email VARCHAR(100),
    telephone VARCHAR(20)
)

commandes (
    id INT PRIMARY KEY,
    pharmacie_id INT,
    fournisseur_id INT,
    date_commande DATETIME,
    statut VARCHAR(20),
    FOREIGN KEY (pharmacie_id) REFERENCES pharmacies(id),
    FOREIGN KEY (fournisseur_id) REFERENCES fournisseurs(id)
)

details_commandes (
    commande_id INT,
    produit_id INT,
    quantite INT,
    prix_unitaire DECIMAL(10,2),
    PRIMARY KEY (commande_id, produit_id),
    FOREIGN KEY (commande_id) REFERENCES commandes(id),
    FOREIGN KEY (produit_id) REFERENCES produits(id)
)
```

### Diagramme entité-association
```
[Pharmacies] 1──┬──* [Administrateurs]
        │
        ├──* [Clients]
        │
        ├──* [Stocks] *──1 [Produits] *──1 [Fournisseurs]
        │
        ├──* [Ventes] *──1 [Details_Ventes]
        │
        └──* [Commandes] *──1 [Details_Commandes]
```

## Client léger (PHP)

### Fonctionnalités
1. Gestion des comptes
   - Connexion/déconnexion
   - Réinitialisation du mot de passe
   - Modification du profil

2. Gestion des ventes
   - Création de nouvelles ventes
   - Historique des ventes
   - Détails des ventes
   - Statistiques de vente

3. Gestion des clients
   - Ajout de nouveaux clients
   - Modification des informations clients
   - Historique des achats par client
   - Statistiques client

4. Gestion des stocks
   - Consultation des stocks
   - Alertes de stock bas
   - Historique des mouvements

### Architecture MVC
```
src/
├── Controllers/
│   ├── AuthController.php
│   ├── VenteController.php
│   ├── ClientController.php
│   └── StockController.php
├── Models/
│   ├── User.php
│   ├── Vente.php
│   ├── Client.php
│   └── Stock.php
└── Views/
    ├── auth/
    ├── ventes/
    ├── clients/
    └── stocks/
```

## Client lourd (Java)

### Fonctionnalités
1. Administration des pharmacies
   - Création de comptes pharmacie
   - Gestion des administrateurs
   - Configuration des paramètres

2. Gestion des produits
   - Catalogue des produits
   - Prix et descriptions
   - Association aux fournisseurs

3. Gestion avancée des stocks
   - Seuils d'alerte
   - Réapprovisionnement
   - Statistiques de stock

4. Gestion des fournisseurs
   - Base de données fournisseurs
   - Commandes
   - Suivi des livraisons

5. Tableau de bord
   - Statistiques globales
   - Alertes de stock
   - Suivi des ventes

### Architecture
```
src/
├── main/
│   └── java/
│       └── com/
│           └── gestionpharma/
│               ├── models/
│               ├── views/
│               ├── controllers/
│               └── utils/
```

## Guide d'installation

### Prérequis
- PHP 7.4 ou supérieur
- MySQL 8.0 ou supérieur
- Java JDK 11 ou supérieur
- Serveur web (Apache/XAMPP)

### Installation de la base de données
1. Créer une base de données nommée `clientlegerlourd`
2. Importer le fichier `database.sql`
3. Configurer les accès dans les fichiers de configuration

### Installation du client léger
1. Copier les fichiers dans le dossier web
2. Configurer le fichier `config.php`
3. Vérifier les permissions des dossiers

### Installation du client lourd
1. Compiler le projet Java
2. Configurer le fichier de connexion
3. Créer le raccourci de lancement

## Guide d'utilisation

### Client léger
1. Connexion
   - Utiliser les identifiants de la pharmacie
   - Possibilité de réinitialiser le mot de passe

2. Gestion des ventes
   - Créer une nouvelle vente
   - Sélectionner les produits
   - Appliquer les remises
   - Finaliser la vente

3. Gestion des clients
   - Créer une fiche client
   - Consulter l'historique
   - Mettre à jour les informations

### Client lourd
1. Administration
   - Créer des comptes pharmacie
   - Gérer les droits d'accès
   - Configurer les paramètres

2. Gestion des stocks
   - Définir les seuils d'alerte
   - Suivre les mouvements
   - Commander aux fournisseurs

3. Tableau de bord
   - Consulter les statistiques
   - Gérer les alertes
   - Suivre l'activité
