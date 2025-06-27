# Guide de Synchronisation BigPharma Java ↔ PHP

## 🎯 Objectif
Résoudre le problème de synchronisation des données entre l'application Java et PHP pour le compte `tourefaliloumbacke12345@gmail.com`.

## 📋 Problème identifié
- L'application PHP affiche : **0 produits**, **0 stock limité**, **0 produits sur ordonnance**
- L'application Java et PHP n'ont pas les mêmes données pour ce compte
- Manque de cohérence entre les deux systèmes

## 🛠️ Solution implémentée

### 1. Outil de synchronisation graphique (`SynchronisationDonnees.java`)
- Interface Swing avec logs en temps réel
- Synchronisation automatique des données
- Vérification de la cohérence

### 2. Script SQL de synchronisation (`sync_database.sql`)
- Création des tables manquantes
- Insertion des données de référence
- Mise à jour de l'utilisateur cible

### 3. Script d'automatisation (`sync_data.bat`)
- Compilation automatique
- Lancement de l'outil

## 🚀 Procédure de synchronisation

### Étape 1: Préparation
1. Assurez-vous que MySQL est démarré
2. Vérifiez que la base `bigpharma` existe
3. Ouvrez un terminal dans le dossier du projet

### Étape 2: Exécution du script SQL (OBLIGATOIRE)
**Option A - Via MySQL en ligne de commande :**
```bash
mysql -u root -p bigpharma < sync_database.sql
```

**Option B - Via phpMyAdmin :**
1. Ouvrez phpMyAdmin
2. Sélectionnez la base `bigpharma`
3. Allez dans l'onglet "SQL"
4. Copiez-collez le contenu de `sync_database.sql`
5. Exécutez le script

### Étape 3: Lancement de l'outil de synchronisation
```bash
.\sync_data.bat
```

### Étape 4: Utilisation de l'interface graphique
1. Cliquez sur **"🔍 Tester Connexion DB"** pour vérifier la base
2. Cliquez sur **"🔄 Synchroniser Toutes les Données"**
3. Suivez les logs en temps réel
4. Attendez le message de confirmation

## 📊 Données synchronisées

### Utilisateur
- **Email :** `tourefaliloumbacke12345@gmail.com`
- **Nom :** Toure Falilou Mbacke
- **Pharmacie ID :** 1
- **Mot de passe :** `password` (hashé en SHA-256)

### Pharmacie
- **ID :** 1
- **Nom :** Pharmacie BigPharma
- **Adresse :** 123 Rue de la Santé, 75000 Paris

### Catégories (17 au total)
- Analgésiques, Anti-inflammatoires, Antibiotiques
- Antihistaminiques, Antispasmodiques, Cardiovasculaires
- Dermatologiques, Digestifs, Endocrinologiques
- Gynécologiques, Neurologiques, Ophtalmologiques
- ORL, Pneumologiques, Psychiatriques, Urologiques, Vitamines

### Fournisseurs (5 laboratoires)
- Laboratoires Sanofi
- Pfizer France
- Novartis Pharma
- Roche France
- Merck France

### Produits (20 médicaments)
- Doliprane 1000mg, Advil 400mg, Amoxicilline 500mg
- Cetirizine 10mg, Spasfon 80mg, Kardegic 75mg
- Biafine, Smecta, Levothyrox 50µg
- Gynéfam, Laroxyl 25mg, Maxidex
- Rhinofluimucil, Ventoline, Lexomil 6mg
- Monuril 3g, Supradyn, Efferalgan 500mg
- Nurofen 200mg, Augmentin 1g

## ✅ Vérification du succès

### Dans l'application PHP
Après synchronisation, vous devriez voir :
- **Produits Totaux :** 20 (au lieu de 0)
- **Stock Limité :** Plusieurs produits avec stock < seuil
- **Produits sur Ordonnance :** Médicaments nécessitant ordonnance

### Dans l'application Java
- Connexion réussie avec le compte
- Accès aux mêmes produits et fournisseurs
- Cohérence des données

## 🔧 Dépannage

### Erreur de connexion MySQL
```
❌ Erreur de connexion: Communications link failure
```
**Solution :** Vérifiez que MySQL est démarré et accessible sur localhost:3306

### Base de données introuvable
```
❌ Unknown database 'bigpharma'
```
**Solution :** Créez la base avec `CREATE DATABASE bigpharma;`

### Erreur de compilation Java
```
❌ Erreur de compilation !
```
**Solution :** Vérifiez que Java JDK est installé et dans le PATH

### Tables manquantes
```
⚠️ Table produits non trouvée
```
**Solution :** Exécutez d'abord le script SQL `sync_database.sql`

## 📝 Logs de vérification

L'outil affiche des logs détaillés :
```
[14:30:15] 🚀 Début de la synchronisation complète
[14:30:16] 📧 Recherche du compte: tourefaliloumbacke12345@gmail.com
[14:30:17] ✅ Utilisateur trouvé: Toure Falilou Mbacke (ID: 1)
[14:30:18] ✅ Pharmacie trouvée: Pharmacie BigPharma (ID: 1)
[14:30:19] ➕ Produit créé: Doliprane 1000mg
[14:30:20] 📊 Nombre total de produits pour la pharmacie 1: 20
[14:30:21] ✅ Synchronisation complète terminée avec succès !
```

## 🎉 Résultat attendu

Après la synchronisation, les deux applications (Java et PHP) auront :
- **Même utilisateur** avec accès complet
- **Mêmes produits** (20 médicaments)
- **Mêmes fournisseurs** (5 laboratoires)
- **Mêmes catégories** (17 catégories)
- **Cohérence parfaite** des données

Le compte `tourefaliloumbacke12345@gmail.com` aura maintenant accès aux mêmes données dans les deux applications !
