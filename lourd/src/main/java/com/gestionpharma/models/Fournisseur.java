package com.gestionpharma.models;

public class Fournisseur {
    private int id;
    private String nom;
    private String adresse;
    private String telephone;
    private String email;
    private String siret;
    private int pharmacieId;
    
    public Fournisseur() {
        // Constructeur par défaut
    }
    
    public Fournisseur(int id, String nom, String adresse, String telephone, String email, String siret, int pharmacieId) {
        this.id = id;
        this.nom = nom;
        this.adresse = adresse;
        this.telephone = telephone;
        this.email = email;
        this.siret = siret;
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
    
    public String getAdresse() {
        return adresse;
    }
    
    public void setAdresse(String adresse) {
        this.adresse = adresse;
    }
    
    public String getTelephone() {
        return telephone;
    }
    
    public void setTelephone(String telephone) {
        this.telephone = telephone;
    }
    
    public String getEmail() {
        return email;
    }
    
    public void setEmail(String email) {
        this.email = email;
    }
    
    public String getSiret() {
        return siret;
    }
    
    public void setSiret(String siret) {
        this.siret = siret;
    }
    
    public int getPharmacieId() {
        return pharmacieId;
    }
    
    public void setPharmacieId(int pharmacieId) {
        this.pharmacieId = pharmacieId;
    }
    
    @Override
    public String toString() {
        return nom;
    }
}
