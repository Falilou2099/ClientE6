package com.gestionpharma;

import java.util.List;
import java.util.Scanner;

public class GestionProduitsPharma {
    private static ProduitDAO produitDAO = new ProduitDAO();
    private static Scanner scanner = new Scanner(System.in);

    public static void main(String[] args) {
        boolean continuer = true;
        while (continuer) {
            afficherMenu();
            int choix = scanner.nextInt();
            scanner.nextLine(); // Consommer la nouvelle ligne

            switch (choix) {
                case 1:
                    ajouterProduit();
                    break;
                case 2:
                    listerProduits();
                    break;
                case 3:
                    modifierProduit();
                    break;
                case 4:
                    supprimerProduit();
                    break;
                case 5:
                    continuer = false;
                    System.out.println("Au revoir!");
                    break;
                default:
                    System.out.println("Choix invalide. Réessayez.");
            }
        }
        scanner.close();
    }

    private static void afficherMenu() {
        System.out.println("\n--- Gestion des Produits Pharmaceutiques ---");
        System.out.println("1. Ajouter un produit");
        System.out.println("2. Lister tous les produits");
        System.out.println("3. Modifier un produit");
        System.out.println("4. Supprimer un produit");
        System.out.println("5. Quitter");
        System.out.print("Votre choix : ");
    }

    private static void ajouterProduit() {
        System.out.print("Nom du produit : ");
        String nom = scanner.nextLine();
        
        System.out.print("Description : ");
        String description = scanner.nextLine();
        
        System.out.print("Prix : ");
        double prix = scanner.nextDouble();
        
        System.out.print("Quantité en stock : ");
        int quantite = scanner.nextInt();
        scanner.nextLine(); // Consommer la nouvelle ligne
        
        System.out.print("Catégorie : ");
        String categorie = scanner.nextLine();

        Produit nouveauProduit = new Produit(nom, description, prix, quantite, categorie);
        produitDAO.creerProduit(nouveauProduit);
        System.out.println("Produit ajouté avec succès!");
    }

    private static void listerProduits() {
        List<Produit> produits = produitDAO.obtenirTousProduits();
        if (produits.isEmpty()) {
            System.out.println("Aucun produit trouvé.");
        } else {
            for (Produit produit : produits) {
                System.out.println(produit);
            }
        }
    }

    private static void modifierProduit() {
        System.out.print("ID du produit à modifier : ");
        int id = scanner.nextInt();
        scanner.nextLine(); // Consommer la nouvelle ligne

        Produit produit = produitDAO.obtenirProduitParId(id);
        if (produit == null) {
            System.out.println("Produit non trouvé.");
            return;
        }

        System.out.print("Nouveau nom (laisser vide pour garder l'actuel) : ");
        String nom = scanner.nextLine();
        if (!nom.isEmpty()) produit.setNom(nom);

        System.out.print("Nouvelle description (laisser vide pour garder l'actuelle) : ");
        String description = scanner.nextLine();
        if (!description.isEmpty()) produit.setDescription(description);

        System.out.print("Nouveau prix (-1 pour garder l'actuel) : ");
        double prix = scanner.nextDouble();
        if (prix >= 0) produit.setPrix(prix);

        System.out.print("Nouvelle quantité en stock (-1 pour garder l'actuelle) : ");
        int quantite = scanner.nextInt();
        if (quantite >= 0) produit.setQuantiteStock(quantite);
        scanner.nextLine(); // Consommer la nouvelle ligne

        System.out.print("Nouvelle catégorie (laisser vide pour garder l'actuelle) : ");
        String categorie = scanner.nextLine();
        if (!categorie.isEmpty()) produit.setCategorie(categorie);

        produitDAO.mettreAJourProduit(produit);
        System.out.println("Produit mis à jour avec succès!");
    }

    private static void supprimerProduit() {
        System.out.print("ID du produit à supprimer : ");
        int id = scanner.nextInt();
        scanner.nextLine(); // Consommer la nouvelle ligne

        produitDAO.supprimerProduit(id);
        System.out.println("Produit supprimé avec succès!");
    }
}
