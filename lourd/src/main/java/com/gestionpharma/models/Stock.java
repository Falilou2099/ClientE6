package com.gestionpharma.models;

import java.time.LocalDate;
import java.time.LocalDateTime;

public class Stock {
    private int id;
    private int produitId;
    private String produitNom;
    private int pharmacieId;
    private int quantite;
    private int seuilMinimum;
    private String statut;
    private LocalDate dateExpiration;
    private LocalDateTime dernierMouvement;
    
    public Stock() {
        // Constructeur par défaut
    }
    
    public Stock(int id, int produitId, String produitNom, int pharmacieId, int quantite, 
                int seuilMinimum, String statut, LocalDate dateExpiration, LocalDateTime dernierMouvement) {
        this.id = id;
        this.produitId = produitId;
        this.produitNom = produitNom;
        this.pharmacieId = pharmacieId;
        this.quantite = quantite;
        this.seuilMinimum = seuilMinimum;
        this.statut = statut;
        this.dateExpiration = dateExpiration;
        this.dernierMouvement = dernierMouvement;
    }
    
    // Getters et Setters
    public int getId() {
        return id;
    }
    
    public void setId(int id) {
        this.id = id;
    }
    
    public int getProduitId() {
        return produitId;
    }
    
    public void setProduitId(int produitId) {
        this.produitId = produitId;
    }
    
    public String getProduitNom() {
        return produitNom;
    }
    
    public void setProduitNom(String produitNom) {
        this.produitNom = produitNom;
    }
    
    public int getPharmacieId() {
        return pharmacieId;
    }
    
    public void setPharmacieId(int pharmacieId) {
        this.pharmacieId = pharmacieId;
    }
    
    public int getQuantite() {
        return quantite;
    }
    
    public void setQuantite(int quantite) {
        this.quantite = quantite;
    }
    
    public int getSeuilMinimum() {
        return seuilMinimum;
    }
    
    public void setSeuilMinimum(int seuilMinimum) {
        this.seuilMinimum = seuilMinimum;
    }
    
    public String getStatut() {
        return statut;
    }
    
    public void setStatut(String statut) {
        this.statut = statut;
    }
    
    public LocalDate getDateExpiration() {
        return dateExpiration;
    }
    
    public void setDateExpiration(LocalDate dateExpiration) {
        this.dateExpiration = dateExpiration;
    }
    
    public LocalDateTime getDernierMouvement() {
        return dernierMouvement;
    }
    
    public void setDernierMouvement(LocalDateTime dernierMouvement) {
        this.dernierMouvement = dernierMouvement;
    }
    
    @Override
    public String toString() {
        return produitNom + " - Quantité: " + quantite;
    }
}
