package com.gestionpharma.models;

import java.time.LocalDate;

public class Produit {
    private int id;
    private String nom;
    private String description;
    private double prixAchat;
    private double prixVente;
    private String categorie;
    private int quantiteStock;
    private int seuilAlerte;
    private LocalDate dateExpiration;
    private LocalDate dateAjout;
    private int pharmacieId;
    
    public Produit() {
        // Constructeur par défaut
    }
    
    public Produit(int id, String nom, String description, double prixAchat, double prixVente, 
                  String categorie, int quantiteStock, int seuilAlerte, LocalDate dateExpiration, LocalDate dateAjout, int pharmacieId) {
        this.id = id;
        this.nom = nom;
        this.description = description;
        this.prixAchat = prixAchat;
        this.prixVente = prixVente;
        this.categorie = categorie;
        this.quantiteStock = quantiteStock;
        this.seuilAlerte = seuilAlerte;
        this.dateExpiration = dateExpiration;
        this.dateAjout = dateAjout;
        this.pharmacieId = pharmacieId;
    }
    
    // Getters et Setters
    public int getId() {
        return id;
    }
    
    public void setId(int id) {
        this.id = id;
    }
    
    public String getNom() {
        return nom;
    }
    
    public void setNom(String nom) {
        this.nom = nom;
    }
    
    public String getDescription() {
        return description;
    }
    
    public void setDescription(String description) {
        this.description = description;
    }
    
    public double getPrixAchat() {
        return prixAchat;
    }
    
    public void setPrixAchat(double prixAchat) {
        this.prixAchat = prixAchat;
    }
    
    public double getPrixVente() {
        return prixVente;
    }
    
    public void setPrixVente(double prixVente) {
        this.prixVente = prixVente;
    }
    
    public String getCategorie() {
        return categorie;
    }
    
    public void setCategorie(String categorie) {
        this.categorie = categorie;
    }
    
    public int getQuantiteStock() {
        return quantiteStock;
    }
    
    public void setQuantiteStock(int quantiteStock) {
        this.quantiteStock = quantiteStock;
    }
    
    public LocalDate getDateExpiration() {
        return dateExpiration;
    }
    
    public void setDateExpiration(LocalDate dateExpiration) {
        this.dateExpiration = dateExpiration;
    }
    
    public LocalDate getDateAjout() {
        return dateAjout;
    }
    
    public void setDateAjout(LocalDate dateAjout) {
        this.dateAjout = dateAjout;
    }
    
    public int getSeuilAlerte() {
        return seuilAlerte;
    }
    
    public void setSeuilAlerte(int seuilAlerte) {
        this.seuilAlerte = seuilAlerte;
    }
    
    /**
     * Alias pour getDateAjout() pour compatibilité avec les méthodes existantes
     * @return La date de création du produit
     */
    public LocalDate getDateCreation() {
        return dateAjout;
    }
    
    /**
     * Alias pour setDateAjout() pour compatibilité avec les méthodes existantes
     * @param dateCreation La date de création du produit
     */
    public void setDateCreation(LocalDate dateCreation) {
        this.dateAjout = dateCreation;
    }
    
    /**
     * Retourne l'ID de la pharmacie associée au produit
     * @return l'ID de la pharmacie
     */
    public int getPharmacieId() {
        return pharmacieId;
    }
    
    /**
     * Définit l'ID de la pharmacie associée au produit
     * @param pharmacieId l'ID de la pharmacie
     */
    public void setPharmacieId(int pharmacieId) {
        this.pharmacieId = pharmacieId;
    }
    
    @Override
    public String toString() {
        return nom;
    }
}
