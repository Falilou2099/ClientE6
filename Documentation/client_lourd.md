# Documentation Client Lourd BigPharma

## 1. Présentation

Le client lourd est une application Java destinée à l'administration des pharmacies. Elle permet aux administrateurs de gérer le catalogue des produits, les stocks, les fournisseurs et d'avoir une vue d'ensemble sur l'activité des pharmacies.

## 2. Architecture technique

### 2.1 Technologies utilisées
- Java 11+
- Swing/AWT
- MySQL JDBC
- JUnit
- Maven/Gradle
- Log4j

### 2.2 Structure du projet
```
BigPharma/
├── src/
│   └── main/
│       ├── java/
│       │   └── com/
│       │       └── gestionpharma/
│       │           ├── models/
│       │           │   ├── Produit.java
│       │           │   ├── Stock.java
│       │           │   ├── Fournisseur.java
│       │           │   └── Commande.java
│       │           ├── views/
│       │           │   ├── LoginFrame.java
│       │           │   ├── MenuPrincipalFrame.java
│       │           │   ├── GestionProduitFrame.java
│       │           │   └── DashboardFrame.java
│       │           ├── controllers/
│       │           │   ├── ProduitController.java
│       │           │   ├── StockController.java
│       │           │   └── CommandeController.java
│       │           └── utils/
│       │               ├── DatabaseConnection.java
│       │               └── Config.java
│       └── resources/
│           ├── config.properties
│           └── images/
└── lib/
    └── mysql-connector-java.jar
```

## 3. Fonctionnalités détaillées

### 3.1 Administration
- Création de comptes pharmacie
- Gestion des administrateurs
- Configuration système
- Logs d'activité
- Sauvegarde/restauration
- Paramètres globaux

### 3.2 Gestion des produits
- Catalogue complet
- Fiches produits
- Catégorisation
- Prix et marges
- Images produits
- Import/export
- Historique des modifications

### 3.3 Gestion des stocks
- Niveau des stocks
- Seuils d'alerte
- Mouvements de stock
- Inventaire
- Prévisions
- Statistiques
- Rapports

### 3.4 Gestion des fournisseurs
- Base fournisseurs
- Catalogues
- Commandes
- Livraisons
- Factures
- Historique
- Performance

### 3.5 Tableau de bord
- Vue d'ensemble
- Statistiques
- Graphiques
- Alertes
- KPIs
- Exports

## 4. Interfaces utilisateur

### 4.1 Fenêtre de connexion
```
┌─────────────────────────────────┐
│     BigPharma Administration    │
├─────────────────────────────────┤
│                                 │
│  Identifiant :                  │
│  ┌─────────────────────────┐   │
│  │                         │   │
│  └─────────────────────────┘   │
│                                 │
│  Mot de passe :                 │
│  ┌─────────────────────────┐   │
│  │                         │   │
│  └─────────────────────────┘   │
│                                 │
│  [      Connexion         ]    │
│                                 │
└─────────────────────────────────┘
```

### 4.2 Menu principal
```
┌─────────────────────────────────────────┐
│ BigPharma - Menu principal             │
├─────────────────────────────────────────┤
│                                         │
│  ┌──────────────┐    ┌──────────────┐  │
│  │   Produits   │    │    Stocks    │  │
│  └──────────────┘    └──────────────┘  │
│                                         │
│  ┌──────────────┐    ┌──────────────┐  │
│  │ Fournisseurs │    │  Tableau de  │  │
│  │              │    │     bord     │  │
│  └──────────────┘    └──────────────┘  │
│                                         │
│  [      Déconnexion        ]           │
└─────────────────────────────────────────┘
```

## 5. Sécurité

### 5.1 Authentification
- Login/mot de passe
- Cryptage des mots de passe
- Sessions sécurisées
- Timeout d'inactivité
- Historique des connexions
- Blocage après échecs

### 5.2 Autorisations
- Profils utilisateurs
- Droits d'accès
- Restrictions par module
- Audit des actions
- Validation des données
- Traçabilité

## 6. Performance

### 6.1 Optimisations
- Cache mémoire
- Requêtes optimisées
- Chargement différé
- Pagination
- Indexation
- Threading

### 6.2 Ressources
- Utilisation CPU
- Consommation mémoire
- Accès disque
- Réseau
- Base de données
- Temps de réponse

## 7. Installation

### 7.1 Prérequis
- JDK 11+
- MySQL 8.0+
- 4GB RAM minimum
- Résolution 1366x768+
- Droits administrateur
- Accès réseau

### 7.2 Configuration
- Fichier config.properties
- Variables d'environnement
- Paramètres JVM
- Connexion base de données
- Logs
- Sauvegardes

## 8. Maintenance

### 8.1 Mises à jour
- Versions du logiciel
- Correctifs de sécurité
- Nouvelles fonctionnalités
- Migration des données
- Tests de régression
- Documentation

### 8.2 Support
- Guide utilisateur
- FAQ
- Dépannage
- Contact support
- Base de connaissances
- Formation
