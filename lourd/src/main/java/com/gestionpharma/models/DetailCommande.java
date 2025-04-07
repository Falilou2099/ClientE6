package com.gestionpharma.models;

/**
 * Classe représentant une ligne de détail d'une commande
 */
public class DetailCommande {
    private int id;
    private int commandeId;
    private int produitId;
    private Produit produit;
    private int quantite;
    private double prixUnitaire;
    
    /**
     * Constructeur par défaut
     */
    public DetailCommande() {
        // Constructeur par défaut
    }
    
    /**
     * Constructeur avec tous les champs
     * @param id ID du détail de commande
     * @param commandeId ID de la commande
     * @param produitId ID du produit
     * @param quantite Quantité commandée
     * @param prixUnitaire Prix unitaire du produit
     */
    public DetailCommande(int id, int commandeId, int produitId, int quantite, double prixUnitaire) {
        this.id = id;
        this.commandeId = commandeId;
        this.produitId = produitId;
        this.quantite = quantite;
        this.prixUnitaire = prixUnitaire;
    }
    
    /**
     * @return ID du détail de commande
     */
    public int getId() {
        return id;
    }
    
    /**
     * @param id ID du détail de commande
     */
    public void setId(int id) {
        this.id = id;
    }
    
    /**
     * @return ID de la commande
     */
    public int getCommandeId() {
        return commandeId;
    }
    
    /**
     * @param commandeId ID de la commande
     */
    public void setCommandeId(int commandeId) {
        this.commandeId = commandeId;
    }
    
    /**
     * @return ID du produit
     */
    public int getProduitId() {
        return produitId;
    }
    
    /**
     * @param produitId ID du produit
     */
    public void setProduitId(int produitId) {
        this.produitId = produitId;
    }
    
    /**
     * @return Produit associé
     */
    public Produit getProduit() {
        return produit;
    }
    
    /**
     * @param produit Produit associé
     */
    public void setProduit(Produit produit) {
        this.produit = produit;
        if (produit != null) {
            this.produitId = produit.getId();
        }
    }
    
    /**
     * @return Quantité commandée
     */
    public int getQuantite() {
        return quantite;
    }
    
    /**
     * @param quantite Quantité commandée
     */
    public void setQuantite(int quantite) {
        this.quantite = quantite;
    }
    
    /**
     * @return Prix unitaire du produit
     */
    public double getPrixUnitaire() {
        return prixUnitaire;
    }
    
    /**
     * @param prixUnitaire Prix unitaire du produit
     */
    public void setPrixUnitaire(double prixUnitaire) {
        this.prixUnitaire = prixUnitaire;
    }
    
    /**
     * Calcule le montant total de la ligne (quantité * prix unitaire)
     * @return Montant total de la ligne
     */
    public double getMontantTotal() {
        return quantite * prixUnitaire;
    }
    
    @Override
    public String toString() {
        String produitNom = (produit != null) ? produit.getNom() : "Produit inconnu";
        return produitNom + " x " + quantite + " = " + getMontantTotal() + " €";
    }
}
