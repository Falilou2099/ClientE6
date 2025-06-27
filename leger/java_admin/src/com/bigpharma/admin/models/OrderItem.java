package com.bigpharma.admin.models;

import java.math.BigDecimal;

/**
 * Modèle représentant un article de commande dans le système
 * Cette classe correspond à la table 'lignes_commande' dans la base de données
 */
public class OrderItem {
    private Integer id;
    private Integer commandeId;
    private Integer produitId;
    private Integer quantite;
    private BigDecimal prixUnitaire;
    private String nomProduit;
    
    // Constructeur par défaut
    public OrderItem() {
    }
    
    // Constructeur complet
    public OrderItem(Integer id, Integer commandeId, Integer produitId, Integer quantite, 
                    BigDecimal prixUnitaire, String nomProduit) {
        this.id = id;
        this.commandeId = commandeId;
        this.produitId = produitId;
        this.quantite = quantite;
        this.prixUnitaire = prixUnitaire;
        this.nomProduit = nomProduit;
    }
    
    // Getters et Setters
    public Integer getId() {
        return id;
    }
    
    public void setId(Integer id) {
        this.id = id;
    }
    
    public Integer getCommandeId() {
        return commandeId;
    }
    
    public void setCommandeId(Integer commandeId) {
        this.commandeId = commandeId;
    }
    
    public Integer getProduitId() {
        return produitId;
    }
    
    public void setProduitId(Integer produitId) {
        this.produitId = produitId;
    }
    
    public Integer getQuantite() {
        return quantite;
    }
    
    public void setQuantite(Integer quantite) {
        this.quantite = quantite;
    }
    
    public BigDecimal getPrixUnitaire() {
        return prixUnitaire;
    }
    
    public void setPrixUnitaire(BigDecimal prixUnitaire) {
        this.prixUnitaire = prixUnitaire;
    }
    
    public String getNomProduit() {
        return nomProduit;
    }
    
    public void setNomProduit(String nomProduit) {
        this.nomProduit = nomProduit;
    }
    
    /**
     * Calcule le montant total de cette ligne de commande
     * @return Le montant total (prix unitaire * quantité)
     */
    public BigDecimal getTotal() {
        return prixUnitaire.multiply(new BigDecimal(quantite));
    }
    
    @Override
    public String toString() {
        return "OrderItem{" +
                "id=" + id +
                ", commandeId=" + commandeId +
                ", produitId=" + produitId +
                ", quantite=" + quantite +
                ", prixUnitaire=" + prixUnitaire +
                ", nomProduit='" + nomProduit + '\'' +
                '}';
    }
}
