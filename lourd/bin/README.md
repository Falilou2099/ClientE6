# Gestion des Produits Pharmaceutiques

## Prérequis
- Java 11 ou supérieur
- MySQL
- Maven

## Configuration de la base de données
1. Créez la base de données `clientlegerlourd`
2. Créez la table `produits` avec la structure suivante :

```sql
CREATE TABLE produits (
    id INT AUTO_INCREMENT PRIMARY KEY,
    nom VARCHAR(255) NOT NULL,
    description TEXT,
    prix DECIMAL(10, 2) NOT NULL,
    quantite_stock INT NOT NULL,
    categorie VARCHAR(100)
);
```

## Exécution de l'application
1. Importez le projet dans Eclipse
2. Assurez-vous d'avoir Maven installé
3. Mettez à jour les informations de connexion dans `DatabaseConnection.java`
4. Exécutez `GestionProduitsPharma.java`

## Fonctionnalités
- Ajouter un produit pharmaceutique
- Lister tous les produits
- Modifier un produit existant
- Supprimer un produit

## Dépendances
- MySQL Connector Java 8.0.27
