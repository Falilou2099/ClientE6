package com.gestionpharma;

import java.util.Date;

public class Produit {
    private int id;
    private String nom;
    private String description;
    private double prix;
    private double prixAchat;
    private int quantiteStock;
    private String categorie;
    private Date dateExpiration;

    // Constructeurs
    public Produit() {}

    public Produit(String nom, String description, double prix, int quantiteStock, String categorie) {
        this.nom = nom;
        this.description = description;
        this.prix = prix;
        this.quantiteStock = quantiteStock;
        this.categorie = categorie;
    }
    
    public Produit(String nom, String description, double prix, double prixAchat, int quantiteStock, String categorie, Date dateExpiration) {
        this.nom = nom;
        this.description = description;
        this.prix = prix;
        this.prixAchat = prixAchat;
        this.quantiteStock = quantiteStock;
        this.categorie = categorie;
        this.dateExpiration = dateExpiration;
    }

    // Getters et Setters
    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public String getNom() { return nom; }
    public void setNom(String nom) { this.nom = nom; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public double getPrix() { return prix; }
    public void setPrix(double prix) { this.prix = prix; }

    public int getQuantiteStock() { return quantiteStock; }
    public void setQuantiteStock(int quantiteStock) { this.quantiteStock = quantiteStock; }

    public String getCategorie() { return categorie; }
    public void setCategorie(String categorie) { this.categorie = categorie; }
    
    public double getPrixAchat() { return prixAchat; }
    public void setPrixAchat(double prixAchat) { this.prixAchat = prixAchat; }
    
    public Date getDateExpiration() { return dateExpiration; }
    public void setDateExpiration(Date dateExpiration) { this.dateExpiration = dateExpiration; }

    @Override
    public String toString() {
        return "Produit{" +
                "id=" + id +
                ", nom='" + nom + '\'' +
                ", description='" + description + '\'' +
                ", prix=" + prix +
                ", prixAchat=" + prixAchat +
                ", quantiteStock=" + quantiteStock +
                ", categorie='" + categorie + '\'' +
                ", dateExpiration=" + dateExpiration +
                '}';
    }
}
