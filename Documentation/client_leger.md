# Documentation Client Léger BigPharma

## 1. Présentation

Le client léger est une application web PHP destinée à la gestion quotidienne des pharmacies. Elle permet aux pharmaciens de gérer leurs ventes, leurs clients et leurs stocks de manière efficace et intuitive.

## 2. Architecture technique

### 2.1 Technologies utilisées
- PHP 7.4+
- MySQL 8.0+
- HTML5/CSS3
- JavaScript/jQuery
- Bootstrap 5
- DataTables
- Font Awesome

### 2.2 Structure MVC
```
bigpharma/
├── config/
│   ├── config.php
│   └── database.php
├── public/
│   ├── css/
│   ├── js/
│   └── images/
├── src/
│   ├── Controllers/
│   │   ├── AuthController.php
│   │   ├── VenteController.php
│   │   ├── ClientController.php
│   │   └── StockController.php
│   ├── Models/
│   │   ├── User.php
│   │   ├── Vente.php
│   │   ├── Client.php
│   │   └── Stock.php
│   └── Views/
│       ├── auth/
│       │   ├── login.php
│       │   └── reset-password.php
│       ├── ventes/
│       │   ├── index.php
│       │   ├── nouvelle.php
│       │   └── details.php
│       ├── clients/
│       │   ├── index.php
│       │   ├── ajouter.php
│       │   └── details.php
│       └── stocks/
│           ├── index.php
│           └── mouvements.php
└── templates/
    ├── header.php
    └── footer.php
```

## 3. Fonctionnalités détaillées

### 3.1 Authentification
- Connexion sécurisée avec email/mot de passe
- Réinitialisation de mot de passe par email
- Session persistante avec token
- Protection contre les attaques par force brute
- Déconnexion automatique après inactivité

### 3.2 Gestion des ventes
- Interface intuitive de création de vente
- Recherche rapide de produits
- Calcul automatique des totaux
- Gestion des remises
- Historique détaillé
- Statistiques de vente
- Export PDF des factures
- Filtres et recherche avancée

### 3.3 Gestion des clients
- Fiches clients complètes
- Historique des achats
- Statistiques par client
- Système de fidélité
- Export des données
- Recherche multicritères
- Mise à jour en masse

### 3.4 Gestion des stocks
- Vue d'ensemble du stock
- Alertes de stock bas
- Historique des mouvements
- Inventaire simplifié
- Export des données
- Filtres par catégorie
- Suggestions de réapprovisionnement

## 4. Interfaces utilisateur

### 4.1 Page de connexion
```
┌─────────────────────────────────┐
│        BigPharma Login         │
├─────────────────────────────────┤
│                                 │
│  ┌─────────────────────────┐   │
│  │     Email               │   │
│  └─────────────────────────┘   │
│                                 │
│  ┌─────────────────────────┐   │
│  │     Mot de passe        │   │
│  └─────────────────────────┘   │
│                                 │
│  [      Se connecter      ]    │
│                                 │
│  Mot de passe oublié ?         │
│                                 │
└─────────────────────────────────┘
```

### 4.2 Interface de vente
```
┌─────────────────────────────────────────┐
│ Nouvelle vente                  [Retour] │
├─────────────────────────────────────────┤
│ Client : [Rechercher▼]                  │
│                                         │
│ Produits                                │
│ ┌─────────────────────────────────────┐ │
│ │ Nom    │ Prix │ Qté │ Total        │ │
│ │ ─────────────────────────────────── │ │
│ │        │      │     │              │ │
│ │        │      │     │              │ │
│ └─────────────────────────────────────┘ │
│                                         │
│ Total : 0.00 €                         │
│                                         │
│ [    Valider    ]  [    Annuler    ]   │
└─────────────────────────────────────────┘
```

## 5. Sécurité

### 5.1 Mesures implémentées
- Authentification sécurisée
- Protection contre les injections SQL
- Validation des données
- Protection CSRF
- Sessions sécurisées
- Logs d'activité
- Contrôle d'accès
- Chiffrement des données sensibles

### 5.2 Bonnes pratiques
- Utilisation de requêtes préparées
- Validation des entrées utilisateur
- Gestion des erreurs
- Journalisation des actions
- Sauvegarde régulière
- Mises à jour de sécurité

## 6. Performance

### 6.1 Optimisations
- Cache des requêtes
- Minimisation des assets
- Pagination des résultats
- Indexation de la base
- Compression des données
- Lazy loading des images
- Requêtes optimisées

### 6.2 Monitoring
- Logs d'erreurs
- Temps de réponse
- Utilisation mémoire
- Charge serveur
- Statistiques d'accès
- Points de blocage

## 7. Maintenance

### 7.1 Tâches régulières
- Sauvegarde des données
- Nettoyage des logs
- Mise à jour des dépendances
- Vérification des performances
- Test des fonctionnalités
- Archivage des données

### 7.2 Documentation technique
- Code commenté
- API documentée
- Procédures de déploiement
- Guide de dépannage
- Scripts de maintenance
- Documentation utilisateur
