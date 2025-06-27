# 🏥 BigPharma - Documentation Projet E6
## Système de Gestion Pharmaceutique - Version Finale

### 📋 Vue d'ensemble du projet

BigPharma est un système complet de gestion pharmaceutique développé dans le cadre du BTS SIO SLAM, comprenant :
- **Application Java Swing** (Client lourd) - Interface d'administration avancée
- **Application Web PHP** (Client léger) - Interface web accessible
- **Synchronisation bidirectionnelle** des données entre les deux plateformes

---

## 🎯 Objectifs du projet

### Objectifs pédagogiques
- Développement d'applications client lourd (Java Swing)
- Développement d'applications web (PHP/MySQL)
- Synchronisation de données entre applications
- Gestion de bases de données relationnelles
- Architecture MVC et bonnes pratiques

### Objectifs fonctionnels
- Gestion complète d'une pharmacie
- Gestion des stocks et produits
- Gestion des fournisseurs et commandes
- Interface d'administration sécurisée
- Synchronisation temps réel des données

---

## 🏗️ Architecture technique

### Technologies utilisées
- **Backend Java** : Java Swing, JDBC, MySQL Connector
- **Frontend Web** : PHP 7.4+, HTML5, CSS3, JavaScript
- **Base de données** : MySQL 8.0
- **Serveur web** : Apache (XAMPP)
- **Outils** : Git, Maven (optionnel)

### Structure du projet
```
Client leger lourd/
├── 📁 lourd/                          # Application Java Swing
│   ├── src/main/java/com/gestionpharma/
│   │   ├── models/                    # Modèles de données
│   │   ├── services/                  # Services métier
│   │   ├── controllers/               # Contrôleurs
│   │   ├── views/                     # Interfaces utilisateur
│   │   └── config/                    # Configuration
│   └── mysql-connector-java-8.0.33.jar
├── 📁 leger/                          # Application PHP Web
│   └── bigpharma/
│       ├── src/                       # Code source PHP
│       ├── public/                    # Fichiers publics
│       ├── templates/                 # Templates
│       └── config/                    # Configuration
├── 📁 Documentation/                  # Documentation projet
└── Scripts de déploiement et test
```

---

## 🔧 Installation et configuration

### Prérequis
- Java JDK 8 ou supérieur
- MySQL Server 8.0
- Apache/XAMPP
- PHP 7.4+ avec extensions MySQL

### Installation rapide
```bash
# 1. Cloner le projet
git clone [URL_DU_REPO]

# 2. Exécuter le script de test complet
TEST_FINAL_COMPLET.bat

# 3. En cas de problème, utiliser la correction
CORRIGER_SYNCHRONISATION.bat
```

### Configuration manuelle
1. **Base de données** : Exécuter `CORRECTION_SYNCHRONISATION_FINALE.sql`
2. **Application Java** : Compiler avec le driver MySQL
3. **Application PHP** : Copier dans htdocs et configurer

---

## 👤 Comptes utilisateur

### Compte administrateur principal
- **📧 Email** : `tourefaliloumbacke12345@gmail.com`
- **🔐 Mot de passe** : `password`
- **🔑 Rôle** : Administrateur
- **🏥 Pharmacie ID** : 1

### Fonctionnalités par rôle
- **Administrateur** : Accès complet, gestion utilisateurs
- **Pharmacien** : Gestion stocks, commandes, ventes
- **Vendeur** : Consultation, ventes simples

---

## 📊 Données de démonstration

### Produits pharmaceutiques (20)
- Médicaments avec prix d'achat/vente
- Stock et seuils d'alerte configurés
- Dates d'expiration et catégories
- Images et descriptions complètes

### Fournisseurs (5)
1. **Laboratoires Sanofi** - 01.53.77.40.00
2. **Pfizer France** - 01.58.07.34.40
3. **Laboratoires Novartis** - 01.55.47.60.00
4. **Roche France** - 01.46.40.50.00
5. **Merck France** - 04.72.78.09.00

### Catégories (17)
Analgésiques, Anti-inflammatoires, Antibiotiques, Antihistaminiques, Vitamines, Antispasmodiques, Antiseptiques, Cardiovasculaires, Dermatologiques, Digestifs, Neurologiques, Ophtalmologiques, ORL, Respiratoires, Urologiques, Gynécologiques, Pédiatriques

---

## 🔄 Synchronisation des données

### Principe de fonctionnement
- **Base Java** : `bigpharma` (données principales)
- **Base PHP** : `clientlegerlourd` (données web)
- **Synchronisation** : Scripts automatisés bidirectionnels

### Outils de synchronisation
- `CorrectionSynchronisation.java` - Interface graphique Java
- `correction_sync_finale.php` - Interface web PHP
- Scripts SQL automatisés
- Tests de validation intégrés

---

## 🧪 Tests et validation

### Scripts de test disponibles
- `TEST_FINAL_COMPLET.bat` - Test complet automatisé
- `CORRIGER_SYNCHRONISATION.bat` - Correction automatique
- Interface Java de test avec GUI
- Interface PHP de validation web

### Procédure de test
1. Vérification de l'environnement
2. Test des connexions bases de données
3. Compilation et test Java
4. Test des interfaces PHP
5. Validation de la synchronisation
6. Rapport final avec statistiques

---

## 📈 Fonctionnalités principales

### Application Java (Client lourd)
- 🏥 Gestion complète de la pharmacie
- 📦 Gestion des stocks et produits
- 🏭 Gestion des fournisseurs
- 📊 Rapports et statistiques
- 👥 Gestion des utilisateurs
- 🔔 Alertes de stock et expiration

### Application PHP (Client léger)
- 🌐 Interface web responsive
- 📱 Accès mobile optimisé
- 🔐 Authentification sécurisée
- 📊 Dashboard interactif
- 🔄 Synchronisation temps réel
- 📧 Système de mot de passe oublié

---

## 🛡️ Sécurité

### Authentification
- Mots de passe hashés SHA-256
- Sessions sécurisées PHP
- Tokens de réinitialisation avec expiration
- Gestion des rôles et permissions

### Base de données
- Requêtes préparées (PDO)
- Validation des entrées utilisateur
- Gestion des erreurs sécurisée
- Logs d'activité

---

## 📞 Support et maintenance

### Outils de diagnostic
- Interface Java de correction avec logs
- Interface PHP de diagnostic web
- Scripts de test automatisés
- Documentation complète

### Résolution des problèmes courants
- **Produits non affichés** : Exécuter la correction automatique
- **Erreur de connexion DB** : Vérifier MySQL et configuration
- **Synchronisation échouée** : Utiliser les outils de diagnostic
- **Interface inaccessible** : Vérifier Apache et permissions

---

## 🚀 Déploiement

### Environnement de développement
- MySQL : localhost:3306
- PHP : localhost/bigpharma/
- Java : Application desktop

### Déploiement production
1. **Serveur web** : Apache/Nginx + PHP
2. **Base de données** : MySQL Server sécurisé
3. **Application Java** : Distribution avec JRE
4. **Monitoring** : Logs et surveillance

---

## 📚 Documentation technique

### Fichiers de documentation
- `README_FINAL.md` - Guide complet utilisateur
- `client_leger.md` - Documentation PHP
- `client_lourd.md` - Documentation Java
- `specifications_techniques.md` - Spécifications détaillées
- `contexte_professionnel.md` - Contexte du projet

### Ressources additionnelles
- Diagrammes UML dans le dossier Documentation
- Scripts SQL commentés
- Code source documenté
- Tests unitaires et d'intégration

---

## 🎓 Compétences développées

### Compétences techniques
- Développement Java Swing avancé
- Développement web PHP/MySQL
- Architecture MVC et design patterns
- Synchronisation de données
- Tests et validation

### Compétences transversales
- Gestion de projet
- Documentation technique
- Résolution de problèmes
- Travail en autonomie
- Veille technologique

---

## 🏆 Résultats obtenus

### Fonctionnalités implémentées
✅ **Synchronisation parfaite** Java ↔ PHP  
✅ **Interface utilisateur moderne** et intuitive  
✅ **Gestion complète** des données pharmaceutiques  
✅ **Sécurité renforcée** et authentification  
✅ **Tests automatisés** et validation  
✅ **Documentation exhaustive**  
✅ **Outils de maintenance** intégrés  

### Métriques du projet
- **20 produits** pharmaceutiques configurés
- **5 fournisseurs** majeurs intégrés
- **17 catégories** de produits
- **100% synchronisation** entre applications
- **0 erreur** dans les tests finaux

---

## 📅 Évolutions futures

### Améliorations prévues
- Interface mobile native
- API REST pour intégrations
- Système de notifications push
- Rapports avancés et analytics
- Module de facturation intégré

### Technologies à explorer
- Framework Spring Boot pour Java
- Framework Laravel pour PHP
- Base de données NoSQL complémentaire
- Containerisation Docker
- Déploiement cloud

---

*Projet développé dans le cadre du BTS SIO SLAM - Spécialisation Solutions Logicielles et Applications Métiers*

**Développé avec ❤️ pour la gestion pharmaceutique moderne**
